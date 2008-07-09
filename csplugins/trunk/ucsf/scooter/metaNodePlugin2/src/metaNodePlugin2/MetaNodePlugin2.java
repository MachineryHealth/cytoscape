/* vim: set ts=2: */
/**
 * Copyright (c) 2006 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package metaNodePlugin2;

// System imports
import javax.swing.JOptionPane;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.lang.reflect.Method;

// giny imports
import giny.view.NodeView;
import ding.view.*;

// Cytoscape imports
import cytoscape.Cytoscape;
import cytoscape.CyEdge;
import cytoscape.CyNode;
import cytoscape.CyNetwork;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.plugin.PluginInfo;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CyNodeView;
import cytoscape.data.CyAttributes;
import cytoscape.util.CytoscapeAction;

import cytoscape.groups.CyGroup;
import cytoscape.groups.CyGroupManager;
import cytoscape.groups.CyGroupViewer;

// our imports
import metaNodePlugin2.model.MetaNode;

/**
 * The MetaNodePlugin2 class provides the primary interface to the
 * Cytoscape plugin mechanism.  This class also implements the 
 * CyGroupViewer for the metaNode viewer.
 */
public class MetaNodePlugin2 extends CytoscapePlugin 
                             implements CyGroupViewer, 
                                        NodeContextMenuListener,
                                        PropertyChangeListener {

	public static final String viewerName = "metaNode";
	public static final double VERSION = 1.2;
	public enum Command {
		NONE("none"),
		COLLAPSE("collapse"),
		EXPAND("expand"),
		NEW("new"),
		REMOVE("remove"),
		ADD("add"),
		DELETE("delete"),
		EXPANDALL("expandAll"),
		COLLAPSEALL("collapseAll"),
		EXPANDNEW("expandNew");

		private String name;
		private Command(String s) { name = s; }
		public String toString() { return name; }
	}

	// Controlling variables
	public static boolean multipleEdges = false;
	public static boolean recursive = true;

	// State values
	public static final int EXPANDED = 1;
	public static final int COLLAPSED = 2;

	private static CyGroupViewer groupViewer = null;

	private static boolean registeredWithGroupPanel = false;

	private Method updateMethod = null;
	private CyGroupViewer namedSelectionViewer = null;

	protected int descendents = 0;

	/**
	 * The main constructor
	 */
	public MetaNodePlugin2() {
		// Listen for network changes (so we can add our context menu)
		try {
			// Add ourselves to the network view created change list
			Cytoscape.getDesktop().getSwingPropertyChangeSupport()
			          .addPropertyChangeListener( CytoscapeDesktop.NETWORK_VIEW_CREATED, this);
			// Add our context menu
			Cytoscape.getCurrentNetworkView().addNodeContextMenuListener(this);
		} catch (ClassCastException e) {
			System.out.println(e.getMessage());
		}

		// Create our main plugin menu
		JMenu menu = new JMenu("MetaNode Operations");
		menu.addMenuListener(new MetanodeMenuListener(null));

		JMenu pluginMenu = Cytoscape.getDesktop().getCyMenus().getMenuBar()
																.getMenu("Plugins");
		pluginMenu.add(menu);

		// Register with CyGroup
		CyGroupManager.registerGroupViewer(this);
		this.groupViewer = this; // this makes it easier to get at from inner classes

		// See if we can get any help from the group panel.  We'll try this again
		// later if we fail now.
		registerWithGroupPanel();

		System.out.println("metaNodePlugin2 "+VERSION+" initialized");
	}

	// These are required by the CyGroupViewer interface

	/**
	 * Return the name of our viewer
	 *
	 * @return viewer name
	 */
	public String getViewerName() { return viewerName; }

	/**
	 * This is called when a new group has been created that
	 * we care about.  If we weren't building our menu each
	 * time, this would be used to update the list of groups
	 * we present to the user.
	 *
	 * @param group the CyGroup that was just created
	 */
	public void groupCreated(CyGroup group) { 
		if (MetaNode.getMetaNode(group) == null) {
			MetaNode newNode = new MetaNode(group);
		}
		// Update the attributes of the group node
		registerWithGroupPanel();
	}

	/**
	 * This is called when a new group has been created that
	 * we care about.  This version of the groupCreated
	 * method is called by XGMML and provides the CyNetworkView
	 * that is in the process of being created.
	 *
	 * @param group the CyGroup that was just created
	 * @param view the CyNetworkView that is being created
	 */
	public void groupCreated(CyGroup group, CyNetworkView myview) { 
		if (MetaNode.getMetaNode(group) == null) {
			MetaNode newNode = new MetaNode(group);

			// We need to be a little tricky if we are restoring a collapsed
			// metaNode from XGMML.  We essentially need to "recollapse" it,
			// but we need to save the old hints
			if (group.getState() == COLLAPSED) {
				// We are, we need to "fix up" the network
				newNode.recollapse(recursive, multipleEdges, myview);
			} else {
				CyNetwork network = myview.getNetwork();
				network.hideNode(group.getGroupNode());
			}
		}
		registerWithGroupPanel();
	}

	/**
	 * This is called when a group we care about is about to 
	 * be deleted.  If we weren't building our menu each
	 * time, this would be used to update the list of groups
	 * we present to the user.
	 *
	 * @param group the CyGroup that will be deleted
	 */
	public void groupWillBeRemoved(CyGroup group) { 
		MetaNode mn = MetaNode.getMetaNode(group);
		// Expand the group
		if (group.getState() == COLLAPSED) {
			mn.expand(true, null, true);
		}
		// Get rid of the MetaNode
		MetaNode.removeMetaNode(mn);
	}

	/**
	 * This is called when a group we care about has been
	 * changed (usually node added or deleted).
	 *
	 * @param group the CyGroup that has changed
	 * @param node the CyNode that caused the change
	 * @param change the change that occured
	 */
	public void groupChanged(CyGroup group, CyNode node, ChangeType change) { 
		MetaNode mn = MetaNode.getMetaNode(group);
		if (mn == null) return;

		if (change == ChangeType.NODE_ADDED)
			mn.nodeAdded(node);
		else if (change == ChangeType.NODE_REMOVED)
			mn.nodeRemoved(node);
		else if (change == ChangeType.STATE_CHANGED)
			if (group.getState() == COLLAPSED) {
				mn.collapse(recursive, multipleEdges, true, Cytoscape.getCurrentNetworkView());
			} else {
				mn.expand(recursive, Cytoscape.getCurrentNetworkView(), true);
			}
	}

	// PropertyChange support

	/**
	 * Implements propertyChange
	 *
	 * @param e the property change event
	 */
	public void propertyChange (PropertyChangeEvent e) {
		if (e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_CREATED) {
			((CyNetworkView)e.getNewValue()).addNodeContextMenuListener(this);
		}
	}

	/**
	 * Implements addNodeContextMenuItems
	 *
	 * @param nodeView the views to add this to
	 * @param menu the menu to add
	 */
	public void addNodeContextMenuItems (NodeView nodeView, JPopupMenu menu) {
		if (menu == null) {
			menu = new JPopupMenu();
		}
		menu.add(getNodePopupMenu(nodeView));
	}

	/**
	 * return the context menu to popup for a node.  The menu depends not
	 * only on the context of the node it's over, but also the number of
	 * other selected items, etc.
	 *
	 */
	private JMenu getNodePopupMenu(NodeView nodeView) {
		JMenu menu = new JMenu("Metanode operations");
		menu.addMenuListener(new MetanodeMenuListener(nodeView));
		return menu;
	}

	private void registerWithGroupPanel() {
		if (registeredWithGroupPanel) {
			try {
				updateMethod.invoke(namedSelectionViewer);
			} catch (Exception e) {
				System.err.println(e);
			}
			return;
		}

		namedSelectionViewer = CyGroupManager.getGroupViewer("namedSelection");
		if (namedSelectionViewer == null)
			return;

		if (namedSelectionViewer.getClass().getName().equals("namedSelection.NamedSelection")) {
			// Get the addViewerToGroupPanel method

			try {
				updateMethod = namedSelectionViewer.getClass().getMethod("updateGroupPanel");
				Method regMethod = namedSelectionViewer.getClass().getMethod("addViewerToGroupPanel", CyGroupViewer.class);
				regMethod.invoke(namedSelectionViewer, (CyGroupViewer)this);
				registeredWithGroupPanel = true;
			} catch (Exception e) {
				System.err.println(e);
				return;
			}
			// Invoke it
		}
	}

	/**
	 * The MetanodeMenuListener provides the interface to the metanode
	 * Node context menu and the plugin menu.
	 */
	public class MetanodeMenuListener implements MenuListener {
		private MetanodeCommandListener staticHandle;
		private NodeView overNode = null;
		private CyNode contextNode = null;

		/**
		 * Create the metaNode menu listener
		 *
		 * @param nv the Cytoscape NodeView the mouse was over
		 */
		MetanodeMenuListener(NodeView nv) {
			this.staticHandle = new MetanodeCommandListener(Command.NONE,null,null);
			this.overNode = nv;
			if (nv != null)
				this.contextNode = (CyNode)overNode.getNode();
		}

	  public void menuCanceled (MenuEvent e) {};
		public void menuDeselected (MenuEvent e) {};

		/**
		 * Process the selected menu
		 *
		 * @param e the MenuEvent for the selected menu
		 */
		public void menuSelected (MenuEvent e)
		{
			JMenu m = (JMenu)e.getSource();
			// Clear the menu
			Component[] subMenus = m.getMenuComponents();
			for (int i = 0; i < subMenus.length; i++) { m.remove(subMenus[i]); }

			CyNetwork network = Cytoscape.getCurrentNetwork();
			Set currentNodes = network.getSelectedNodes();
			List<CyGroup>groupList = CyGroupManager.getGroupList(groupViewer);
			List<CyGroup>nodeGroups = null;

			if (contextNode != null) {
				nodeGroups = contextNode.getGroups();
			}

			// Add our menu items
			{
			  JMenuItem item = new JMenuItem("Create new metanode");
				MetanodeCommandListener l = new MetanodeCommandListener(Command.NEW, null,null);
				item.addActionListener(l);
				if (currentNodes.size() > 0) {
					item.setEnabled(true);
				} else {
					item.setEnabled(false);
				}
				m.add(item);
			}

			{
				addMenuItem(m, Command.EXPAND, groupList, contextNode, "Expand metanode");
				addMenuItem(m, Command.COLLAPSE, groupList, contextNode, "Collapse metanode");
				addMenuItem(m, Command.EXPANDNEW, groupList, contextNode, "Expand metanode");
				addMenuItem(m, Command.REMOVE, groupList, contextNode, "Remove metanode");
				addMenuItem(m, Command.ADD, groupList, contextNode, "Add node to metanode");
				addMenuItem(m, Command.DELETE, groupList, contextNode, "Remove node from metanode");
				addMenuItem(m, Command.EXPANDALL, groupList, null, "Expand all metanodes");
				addMenuItem(m, Command.COLLAPSEALL, groupList, null, "Collapse all metanodes");
			}
		}

		/**
		 * Create the appropriate menu item
		 *
		 * @param menu the JMenu to add our JMenuItems to
		 * @param command the command we will be executing
		 * @param groupList the list of CyGroups to add
		 * @param contextNode the CyNode this menu refers to
		 * @param label the label for this menu item
		 */
		private void addMenuItem(JMenu menu, Command command, List<CyGroup>groupList,
		                            CyNode contextNode, String label) {

			if (groupList == null || groupList.size() == 0) {
				if (contextNode == null) {
			  	JMenuItem item = new JMenuItem(label);
					item.setEnabled(false);
					menu.add(item);
				}
			} else if (contextNode == null) {
				if (command == Command.EXPANDALL || command == Command.COLLAPSEALL) {
					addSubMenu(menu, label, command, null, null);
				} else if (command == Command.EXPANDNEW) {
			  	JMenu item = new JMenu(label+"(s) into new network");
					if (addGroupMenu(item, command, groupList, contextNode))
						menu.add(item);
				} else if (command != Command.ADD && command != Command.DELETE) {
			  	JMenu item = new JMenu(label);
					if (addGroupMenu(item, command, groupList, contextNode))
						menu.add(item);
				} else if (command == Command.ADD) {
			  	JMenu item = new JMenu("Add node(s) to metanode");
					if (addGroupMenu(item, command, groupList, null))
						menu.add(item);
				} else if (command == Command.DELETE) {
			  	JMenu item = new JMenu("Remove node(s) from metanode");
					if (addGroupMenu(item, command, groupList, null))
						menu.add(item);
				}
			} else if (contextNode.isaGroup() && command == Command.EXPAND) {
				// Get the groups this group is a member of
				CyGroup group = CyGroupManager.findGroup(contextNode.getIdentifier());
				if (group.getState() == COLLAPSED) {
					addSubMenu(menu, label+" "+group.getGroupName(), 
					           command, group, contextNode);
				}
			} else if (CyGroupManager.isaGroup(contextNode) && command == Command.EXPANDNEW) {
				CyGroup group = CyGroupManager.findGroup(contextNode.getIdentifier());
				if (group.getState() == COLLAPSED) {
					addSubMenu(menu, label+" "+group.getGroupName()+" into new network", 
					           command, group, contextNode);
				}
			} else if (command == Command.COLLAPSE) {
				List<CyGroup>nodeGroups = contextNode.getGroups();
				if (nodeGroups != null && nodeGroups.size() > 0) {
					if (nodeGroups.size() == 1) {
						CyGroup group = nodeGroups.get(0);
						addSubMenu(menu, label+" "+group.getGroupName(), 
						           command, group, contextNode);
					} else {
						JMenu item = new JMenu(label);
						if (addGroupMenu(item, command, nodeGroups, contextNode))
							menu.add(item);
					}
				}
			} else if (command == Command.ADD) {
				if (groupList.size() == 1 && 
				    !groupList.get(0).getGroupName().equals(contextNode.getIdentifier())) {
					CyGroup group = groupList.get(0);
					List<CyGroup>nodeGroups = contextNode.getGroups();
					if (nodeGroups == null || !nodeGroups.contains(group)) {
						addSubMenu(menu, label+" "+group.getGroupName(), 
					 	          command, group, contextNode);
					}
				} else {
					JMenu item = new JMenu(label);
					if (addGroupMenu(item, command, groupList, contextNode))
						menu.add(item);
				}
			} else if (command == Command.DELETE) {
				List<CyGroup>nodeGroups = contextNode.getGroups();
				if (nodeGroups != null && nodeGroups.size() > 0) {
					if (nodeGroups.size() == 1)  {
						CyGroup group = nodeGroups.get(0);
						addSubMenu(menu, label+" "+group.getGroupName(), 
						           command, group, contextNode);
					} else {
						JMenu item = new JMenu(label);
						if (addGroupMenu(item, command, nodeGroups, contextNode))
							menu.add(item);
					}
				}
			}
		}

		/**
		 * Add all groups to a menu, as appropriate.
		 *
		 * @param menu the JMenu to add our JMenuItems to
		 * @param command the command we will be executing
		 * @param groupList the list of CyGroups to add
		 * @param node the CyNode this menu refers to
		 * @return true if the menu should be added
		 */
		private boolean addGroupMenu(JMenu menu, Command command, List<CyGroup>groupList,
		                             CyNode node) {
			List<CyGroup>nodeGroups = null;
			boolean foundItem = false;
			if (groupList == null) return false;

			if (command == Command.ADD && node != null) {
				nodeGroups = node.getGroups();
			} 
			// List current named selections
			for (CyGroup group: groupList) {
				CyNode groupNode = group.getGroupNode();
				List<CyGroup> parents = groupNode.getGroups();
				if (group.getViewer() != null && group.getViewer().equals(groupViewer.getViewerName())) {
					// Only present reasonable choices to the user
					if ((command == Command.COLLAPSE && group.getState() == COLLAPSED) ||
					    (command == Command.EXPAND && group.getState() == EXPANDED)) 
						continue;
					// If command is expand and we're a child of a group that isn't
					// yet expanded, don't give this as an option
					if ((command == Command.EXPAND) && (parents != null) && (parents.size() > 0)) {
						boolean parentCollapsed = false;
						for (CyGroup parent: parents) {
							if (groupList.contains(parent) && (parent.getState() == COLLAPSED)) {
								parentCollapsed = true;
								break;
							}
						}
						if (parentCollapsed) continue;
					}

					if (command == Command.ADD) {
						MetaNode metaNode = null;
						// Are we already in this group?
						if ((nodeGroups != null) && (nodeGroups.contains(group)))
							continue;
						// Are we this group?
						if (((metaNode = MetaNode.getMetaNode(node)) != null) && 
						    (metaNode.getCyGroup() == group)) {
							continue;
						}
					}

					foundItem = true;
					addSubMenu(menu, group.getGroupName(), command, group, node);
				}
			}
			return foundItem;
		}

		/**
		 * Add a submenu item to an existing menu
		 *
		 * @param menu the JMenu to add the new submenu to
		 * @param label the label for this menu
		 * @param command the comment this menu refers to
		 * @param group the group node
		 * @param node the node
		 */
		private void addSubMenu(JMenu menu, String label, Command command, CyGroup group, CyNode node) {
			JMenuItem item = new JMenuItem(label);
			MetanodeCommandListener l = new MetanodeCommandListener(command, group, node);
			item.addActionListener(l);
		  menu.add(item);
		}
	}
	
  /**
   * This class gets attached to the menu item.
   */
  private class MetanodeCommandListener implements ActionListener {
  	private static final long serialVersionUID = 1;
		private Command command;
		private CyGroup group = null; // The group we care about
		private CyNode node = null; // The node this is refering to

		/**
		 * The main constructor for the command listener.
		 *
		 * @param command the command to execute
		 * @param group the group to apply the command to
		 * @param node the node to apply the command to
		 */
		MetanodeCommandListener(Command command, CyGroup group, CyNode node) {
			this.command = command;
			this.group = group;
			this.node = node;
		}

    /**
     * This method is called when the user selects the menu item.
     */
    public void actionPerformed(ActionEvent ae) {

			registerWithGroupPanel();

			String label = ae.getActionCommand();
			if (command == Command.COLLAPSE) {
				collapse();
			} else if (command == Command.EXPAND) {
				expand();
			} else if (command == Command.EXPANDNEW) {
				createNetworkFromGroup();
			} else if (command == Command.NEW) {
				newGroup();
			} else if (command == Command.REMOVE) {
				removeGroup();
			} else if (command == Command.ADD) {
				addToGroup(node);
			} else if (command == Command.DELETE) {
				removeFromGroup(node);
			} else if (command == Command.EXPANDALL) {
				expandAll();
			} else if (command == Command.COLLAPSEALL) {
				collapseAll();
			}
		}

		/**
		 * Create a new group.  
		 */
		private void newGroup() {
			CyNetwork network = Cytoscape.getCurrentNetwork();
			List<CyNode> currentNodes = new ArrayList(network.getSelectedNodes());
			List<CyGroup> groupList = CyGroupManager.getGroupList();
			String groupName = JOptionPane.showInputDialog("Please enter a name for this metanode");
			if (groupName == null) return;
			CyGroup group = CyGroupManager.createGroup(groupName, currentNodes, viewerName);
			if (group == null) {
				// Oops -- already have a group named groupName!
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(), 
					"There is already a group named "+groupName,"GroupError",
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			MetaNode newNode = new MetaNode(group);
			groupCreated(group);
			newNode.collapse(recursive, multipleEdges, true, null);
		}

		/**
		 * Remove a group.
		 */
		private void removeGroup() {
			// We need to make sure the group is expanded, first
			expand();
			CyGroupManager.removeGroup(group);
		}

		/**
		 * Add a node to a group
		 *
		 * @param node the node to add to this group
		 */
		private void addToGroup(CyNode node) {
			if (node != null) {
				node.addToGroup(group);  // NOTE: this will trigger a groupChanged callback
			} else {
				// Get the currently selected nodes and add
				// them one-by-one
				CyNetwork network = Cytoscape.getCurrentNetwork();
				List<CyNode> currentNodes = new ArrayList(network.getSelectedNodes());
				for (CyNode selNode: currentNodes) {
					selNode.addToGroup(group);
				}
			}
		}

		/**
		 * Remove a node from a group
		 *
		 * @param node the node to remove from this group
		 */
		private void removeFromGroup(CyNode node) {
			if (node != null) {
				if (group.contains(node))
					node.removeFromGroup(group);  // NOTE: this will trigger a groupChanged callback
			} else {
				// Get the currently selected nodes and add
				// them one-by-one
				CyNetwork network = Cytoscape.getCurrentNetwork();
				List<CyNode> currentNodes = new ArrayList(network.getSelectedNodes());
				for (CyNode selNode: currentNodes) {
					if (group.contains(selNode))
						selNode.removeFromGroup(group);
				}
			}
		}

		/**
		 * Perform the action associated with a select menu selection
		 */
		private void collapse() {
			MetaNode mNode = MetaNode.getMetaNode(group);
			mNode.collapse(recursive, multipleEdges, true, Cytoscape.getCurrentNetworkView());
		}

		/**
		 * Perform the action associated with an unselect menu selection
		 */
		private void expand() {
			MetaNode mNode = MetaNode.getMetaNode(group);
			mNode.expand(recursive, Cytoscape.getCurrentNetworkView(), true);
		}

		/**
 		 * Create a new network from the currently collapsed group
 		 */
		private void createNetworkFromGroup() {
			MetaNode mNode = MetaNode.getMetaNode(group);
			mNode.createNetworkFromGroup();
		}

		/**
 		 * Expand all metanodes
 		 */
		private void expandAll() {
			MetaNode.expandAll();
		}

		/**
 		 * Collapse all metanodes
 		 */
		private void collapseAll() {
			MetaNode.collapseAll();
		}
	}
}
