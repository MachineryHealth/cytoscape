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
package structureViz;

// System imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import java.util.List;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

// giny imports
import giny.view.NodeView;
import ding.view.*;

// Cytoscape imports
import cytoscape.*;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.CyNetworkView;
import cytoscape.data.CyAttributes;
import cytoscape.util.CytoscapeAction;

// structureViz imports
import structureViz.ui.ModelNavigatorDialog;
import structureViz.ui.AlignStructuresDialog;
import structureViz.model.Structure;
import structureViz.model.ChimeraModel;
import structureViz.actions.CyChimera;
import structureViz.actions.Chimera;
import structureViz.actions.Align;

/**
 * The StructureViz class provides the primary interface to the
 * Cytoscape plugin mechanism
 */
public class StructureViz extends CytoscapePlugin 
  implements NodeContextMenuListener, PropertyChangeListener {

	public static final int NONE = 0;
	public static final int OPEN = 1;
	public static final int CLOSE = 2;
	public static final int ALIGN = 3;
	public static final int EXIT = 4;
	public static final int COMPARE = 5;
	public static final int SALIGN = 6;
	public static final String[] attributeKeys = {"Structure","pdb","pdbFileName"};

  /**
   * Create our action and add it to the plugins menu
   */
  public StructureViz() {
		try {
			// Set ourselves up to listen for new networks
			Cytoscape.getDesktop().getSwingPropertyChangeSupport()
				.addPropertyChangeListener( CytoscapeDesktop.NETWORK_VIEW_CREATED, this );
	
			// Add ourselves to the current network context menu
			((DGraphView)Cytoscape.getCurrentNetworkView()).addNodeContextMenuListener(this);
		} catch (ClassCastException e) {
			System.out.println(e.getMessage());
		}
	    
		JMenu menu = new JMenu("Sequence/Structure Tools");
		menu.addMenuListener(new StructureVizMenuListener(null));

		JMenu pluginMenu = Cytoscape.getDesktop().getCyMenus().getMenuBar()
																.getMenu("Plugins");
		pluginMenu.add(menu);

  }

	/**
	 * The StructureVizMenuListener provides the interface to the structure viz
	 * Node context menu and the plugin menu.
	 */
	public class StructureVizMenuListener implements MenuListener {
		private StructureVizCommandListener staticHandle;
		private NodeView overNode = null;

		/**
		 * Create the structureViz menu listener
		 *
		 * @param nv the Cytoscape NodeView the mouse was over
		 */
		StructureVizMenuListener(NodeView nv) {
			this.staticHandle = new StructureVizCommandListener(NONE,null);
			this.overNode = nv;
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

			// Add our menu items
			{
			  JMenu item = new JMenu("Open structure(s)");
				List structures =  CyChimera.getSelectedStructures(overNode);
				if (structures.size() == 0) {
					item.setEnabled(false);
				} else {
					if (structures.size() > 1)
						addSubMenu(item, "all", OPEN, structures);
					Iterator iter = structures.iterator();
					while (iter.hasNext()) {
						Structure structure = (Structure)iter.next();
						addSubMenu(item, structure.name(), OPEN, structure);
					}
				}
				m.add(item);
			}
			{
				JMenuItem item = new JMenuItem("Align structures");
				List structures = CyChimera.getSelectedStructures(overNode);
				StructureVizCommandListener l = new StructureVizCommandListener(ALIGN, structures);
				item.addActionListener(l);
				if (structures.size() < 2) {
					item.setEnabled(false);
				}
				m.add(item);
			}
			{
				if (staticHandle.getChimera() == null || !staticHandle.getChimera().isLaunched())  
				{
			  	JMenuItem item = new JMenuItem("Close structure(s)");
					item.setEnabled(false);
			  	m.add(item);
				} else {
			  	JMenu item = new JMenu("Close structure(s)");
					List<Structure>openStructures = staticHandle.getOpenStructs();
					addSubMenu(item, "all", CLOSE, openStructures);
					Iterator iter = openStructures.iterator();
					while (iter.hasNext()) {
						Structure structure = (Structure)iter.next();
						addSubMenu(item, structure.name(), CLOSE, structure);
					}
					m.add(item);
				}
			}
			{
				JMenuItem item = new JMenuItem("Exit Chimera");
				StructureVizCommandListener l = new StructureVizCommandListener(EXIT, null);
				item.addActionListener(l);
				if (l.getChimera() == null || !l.getChimera().isLaunched()) item.setEnabled(false);
				m.add(item);
			}
/*
			m.addSeparator();
			{
				JMenuItem item = new JMenuItem("Compare sequences");
				List sequences = CyChimera.getSelectedSequences(overNode);
				if (sequences.size() < 2) item.setEnabled(false);
				StructureVizCommandListener l = new StructureVizCommandListener(COMPARE, sequences);
				item.addActionListener(l);
				m.add(item);
			}
			{
				JMenuItem item = new JMenuItem("Align sequences");
				List sequences = CyChimera.getSelectedSequences(overNode);
				if (sequences.size() < 2) item.setEnabled(false);
				StructureVizCommandListener l = new StructureVizCommandListener(SALIGN, sequences);
				item.addActionListener(l);
				m.add(item);
			}
*/
		}


		/**
		 * Add a submenu item to an existing menu
		 *
		 * @param menu the JMenu to add the new submenu to
		 * @param label the label for the submenu
		 * @param command the command to execute when selected
		 * @param userData data associated with the menu
		 */
		private void addSubMenu(JMenu menu, String label, int command, Object userData) {
			StructureVizCommandListener l = new StructureVizCommandListener(command, userData);
			JMenuItem item = new JMenuItem(label);
			item.addActionListener(l);
		  menu.add(item);
		}
	}
	
  /**
   * This class gets attached to the menu item.
   */
  static class StructureVizCommandListener implements ActionListener {
  	private static final long serialVersionUID = 1;
		private static Chimera chimera = null;
		private static ModelNavigatorDialog mnDialog = null;
		private static AlignStructuresDialog alDialog = null;
		private int command;
		private Object userData = null; // Either a Structure or an ArrayList

		StructureVizCommandListener(int command, Object userData) {
			this.command = command;
			this.userData = userData;
		}

    /**
     * This method is called when the user selects the menu item.
     */
    public void actionPerformed(ActionEvent ae) {
			String label = ae.getActionCommand();
			if (command == OPEN) {
				openAction(label);
			} else if (command == EXIT) {
				exitAction();
			} else if (command == ALIGN) {
				alignAction(label);
			} else if (command == CLOSE) {
				closeAction(label);
			} else if (command == SALIGN) {
				seqAlignAction(label);
			} else if (command == COMPARE) {
				seqCompareAction(label);
			}
		}

		/**
		 * Return the Chimera object.
		 *
		 * @return Chimera object for this
		 */
		public Chimera getChimera() {
			return chimera;
		}

		/**
		 * Return the list of open structures
		 *
		 * @return a List of Structures
		 */
		public List<Structure>getOpenStructs() {
			List<Structure>st = new ArrayList<Structure>();
			if (chimera == null) return st;

			List modelList = chimera.getChimeraModels();
			if (modelList == null) return st;

			Iterator modelIter = modelList.iterator();
			while (modelIter.hasNext()) {
				Structure structure = ((ChimeraModel)modelIter.next()).getStructure();
				if (structure != null)
					st.add(structure);
			}
			return st;
		}

		/**
		 * Perform the action associated with an align menu selection
		 *
		 * @param label the Label associated with this command
		 */
		private void alignAction(String label) {
			// Launch Chimera (if necessary)
			boolean isLaunched = (chimera != null && chimera.isLaunched());
			if (!isLaunched) {
				chimera = launchChimera();
			} 

			if (mnDialog == null) {
				mnDialog = initDialog(chimera);
			} else {
				mnDialog.setVisible(true);
			}

			List structures = (List)userData;

			// Bring up the dialog
			alDialog = 
								new AlignStructuresDialog(Cytoscape.getDesktop(), chimera, structures);
			alDialog.pack();
			alDialog.setLocationRelativeTo(Cytoscape.getDesktop());
			alDialog.setVisible(true);
			chimera.setAlignDialog(alDialog);
		}

		/**
		 * Exit Chimera and the plugin
		 */
		private void exitAction() {
			if (mnDialog != null) {
				// get rid of the dialog
				mnDialog.setVisible(false);
				mnDialog.dispose();
				mnDialog = null;
				chimera.setDialog(mnDialog);
			}
			if (alDialog != null) {
				alDialog.setVisible(false);
				alDialog.dispose();
				alDialog = null;
				chimera.setAlignDialog(alDialog);
			}
			if (chimera != null) {
				chimera.exit();
				chimera = null;
			}
		}

		/**
		 * Close a Chimera molecule
		 */
		private void closeAction(String commandLabel) {
			List<Structure>structList;
			if (chimera != null) {
				if (commandLabel.compareTo("all") != 0) {
					structList = new ArrayList<Structure>();
					structList.add((Structure)userData);
				} else {
					structList = (ArrayList)userData;
				}
				ListIterator iter = structList.listIterator();
				while (iter.hasNext()) {
					Structure structure = (Structure)iter.next();
					chimera.close(structure);
					// Not open any more -- remove it
					iter.remove();
				}
			}
			if (mnDialog != null) mnDialog.modelChanged();
		}

		/**
		 * Open a pdb model in Chimera
		 */
		private void openAction(String commandLabel) {
			boolean isLaunched = (chimera != null && chimera.isLaunched());
			if (!isLaunched) {
				chimera = launchChimera();
			}

			ArrayList<Structure>structList = null;
			if (commandLabel.compareTo("all") == 0) {
				structList = (ArrayList)userData;
			} else {
				structList = new ArrayList<Structure>();
				structList.add((Structure)userData);
			}

      // Send initial commands
			Iterator iter = structList.iterator();
			while (iter.hasNext()) {
				Structure structure = (Structure) iter.next();
				chimera.open(structure);
			}

			if (mnDialog == null || !isLaunched) {
				// Finally, open up our navigator dialog
				mnDialog = initDialog(chimera);
    	} else {
				mnDialog.setVisible(true);
				mnDialog.modelChanged();
			}
		}

		/**
		 * Align two sequences and open the resulting alignment in Chimera
		 */
		private void seqAlignAction(String commandLabel) {
			List sequenceList = (List)userData;
			// Start a new thread
			// Call backend to calculate alignment
			// Open resulting alignment in Chimera
		}

		/**
		 * Compare two sequences and use the results to add or update
		 * Cytoscape attributes on edges connecting the :w
		 *
		 */
		private void seqCompareAction(String commandLabel) {
			List sequenceList = (List)userData;
			// Start a new thread
			// Iterate through all pairs
			// Call backend to calculate comparison
			// Store results back onto connecting edge
		}
  }

	/**
	 * Detect that a new network view has been created and add our
	 * node context menu listener to nodes within this network
	 */
  public void propertyChange(PropertyChangeEvent evt) {
    if ( evt.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_CREATED ){
      // Add menu to the context dialog
			((DGraphView)Cytoscape.getCurrentNetworkView())
				.addNodeContextMenuListener(this);
    }
  }

	/**
	 * Launch UCSF Chimera and return the Chimera object
	 *
	 * @return the Chimera object associated with this Chimera instance
	 */
	private static Chimera launchChimera() {
		Chimera chimera = null;
    // Launch Chimera
    try {
    	// Get a chimera instance
    	chimera = new Chimera(Cytoscape.getCurrentNetworkView());
      chimera.launch();
    } catch (java.io.IOException e) {
      // Put up error panel
      JOptionPane.showMessageDialog(Cytoscape.getCurrentNetworkView().getComponent(),
       	 			"Unable to launch Chimera", "Unable to launch Chimera",
       	   			JOptionPane.ERROR_MESSAGE);
    }
		return chimera;
	}

	/**
	 * Create the ModelNavigatorDialog and associate the Chimera object with it
	 *
	 * @param chimera the Chimera object to use for Chimera interaction
	 * @return the ModelNavigatorDialog that was created
	 */
	private static ModelNavigatorDialog initDialog(Chimera chimera) {
		ModelNavigatorDialog mnDialog = new ModelNavigatorDialog(Cytoscape.getDesktop(), chimera);
		mnDialog.pack();
		mnDialog.setLocationRelativeTo(Cytoscape.getDesktop());
		mnDialog.setVisible(true);
		chimera.setDialog(mnDialog);
		return mnDialog;
	}

	/**
	 * Implements addNodecontextMenuItems
	 * @param nodeView
	 * @param menu
	 */
	public void addNodeContextMenuItems (NodeView nodeView, JPopupMenu pmenu) {
		if (pmenu == null) {
			pmenu = new JPopupMenu();
		}
		JMenu menu = new JMenu("Structure Visualization");
		menu.addMenuListener(new StructureVizMenuListener(nodeView));
		pmenu.add(menu);
	}
}
