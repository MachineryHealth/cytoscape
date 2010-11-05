/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.view.vizmap.gui.internal;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.view.CySwingApplication;
import org.cytoscape.view.CytoPanelName;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedListener;
import org.cytoscape.view.vizmap.events.VisualStyleAddedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAddedListener;
import org.cytoscape.view.vizmap.gui.DefaultViewEditor;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedEvent;
import org.cytoscape.view.vizmap.gui.internal.theme.ColorManager;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;
import com.l2fprod.common.swing.plaf.blue.BlueishButtonUI;


/**
 * New VizMapper UI main panel. Refactored for Cytoscape 3.
 * 
 * This panel consists of 3 panels:
 * <ul>
 * <li>Global Control Panel
 * <li>Default editor panel
 * <li>Visual Mapping Browser
 * </ul>
 * 
 * @version 0.8
 * @since Cytoscape 2.5
 * @param <syncronized>
 */
public class VizMapperMainPanel extends AbstractVizMapperPanel implements
		VisualStyleAddedListener, VisualStyleAboutToBeRemovedListener, PopupMenuListener, NetworkViewAddedListener, NetworkAddedListener {

	private final static long serialVersionUID = 1202339867854959L;
	
	private static final Logger logger = LoggerFactory.getLogger(VizMapperMainPanel.class);
	
	private static final String TAB_TITLE = "VizMapper\u2122";

	private boolean ignore = false;
	private VisualStyle lastVS = null;
	private DefaultViewMouseListener defaultViewMouseListener;

	/**
	 * Create new instance of VizMapperMainPanel object. GUI layout is handled
	 * by abstract class.
	 * 
	 * @param desktop
	 * @param dab
	 * @param iconMgr
	 * @param colorMgr
	 * @param vmm
	 * @param menuMgr
	 * @param editorFactory
	 */
	public VizMapperMainPanel(
			final VisualStyleFactory vsFactory,
			final CySwingApplication desktop,
			DefaultViewEditor defViewEditor, IconManager iconMgr,
			ColorManager colorMgr, VisualMappingManager vmm,
			VizMapperMenuManager menuMgr, EditorManager editorFactory,
			final PropertySheetPanel propertySheetPanel,
			VizMapPropertySheetBuilder vizMapPropertySheetBuilder,
			EditorWindowManager editorWindowManager,
			CyApplicationManager applicationManager, CyEventHelper eventHelper,
			final SelectedVisualStyleManager manager) {

		super(vsFactory, desktop, defViewEditor, iconMgr, colorMgr, vmm, menuMgr,
				editorFactory, propertySheetPanel, vizMapPropertySheetBuilder,
				editorWindowManager,
				applicationManager, eventHelper, manager);

		// Initialize all components
		initPanel();

	}

	private void initPanel() {

		defaultViewMouseListener = new DefaultViewMouseListener(defViewEditor);

		addVisualStyleChangeAction();

		// By default, force to sort property by prop name.
		propertySheetPanel.setSorting(true);

		refreshUI();

		cytoscapeDesktop.getCytoPanel(CytoPanelName.WEST).add(TAB_TITLE, this);

		// Switch to the default style.
		switchVS(manager.getDefaultStyle());
	}

	

	private void addVisualStyleChangeAction() {
		visualStyleComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				switchVS((VisualStyle) visualStyleComboBox.getSelectedItem());
			}
		});
	}

	public void switchVS(VisualStyle style) {

		if (ignore)
			return;

		// If new VS name is the same, ignore.
		if (style == null || style.equals(lastVS))
			return;

		
		editorWindowManager.closeAllEditorWindows();
		
		logger.debug("######## Fireing new style: " + style);
		eventHelper.fireSynchronousEvent(new SelectedVisualStyleSwitchedEvent(this, lastVS, style));
		logger.debug("######## Event end:  new style: " + style);
		
		if (vizMapPropertySheetBuilder.getPropertyMap().containsKey(style)) {
			final List<Property> props = vizMapPropertySheetBuilder
					.getPropertyMap().get(style);

			final Map<String, Property> unused = new TreeMap<String, Property>();

			/*
			 * Remove currently shown property
			 */
			for (Property item : propertySheetPanel.getProperties())
				propertySheetPanel.removeProperty(item);

			/*
			 * Add properties to current property sheet.
			 */
			for (Property prop : props) {
				if (prop.getCategory().startsWith(CATEGORY_UNUSED) == false) {
					propertySheetPanel.addProperty(prop);
				} else {
					unused.put(prop.getDisplayName(), prop);
				}
			}

			final List<String> keys = new ArrayList<String>(unused.keySet());
			Collections.sort(keys);

			for (Object key : keys) {
				propertySheetPanel.addProperty(unused.get(key));
			}
		} else {
			logger.debug("######## Set prop table called.");
			vizMapPropertySheetBuilder.setPropertyTable(style);
			updateAttributeList();
		}

		// Apply style to the current network view.
		final CyNetworkView currentView = applicationManager
				.getCurrentNetworkView();

		if (currentView != null) {
			vmm.setVisualStyle((VisualStyle) visualStyleComboBox.getModel()
					.getSelectedItem(), currentView);
			style.apply(applicationManager.getCurrentNetworkView());
		}
		
		/*
		 * Draw default view
		 */
		Image defImg = defaultImageManager.get(style);

		if (defImg == null) {
			// Default image is not available in the buffer. Create a new one.
			// updateDefaultImage(style,
			// defViewEditor.
			// .getDefaultView(style).getView(),
			// defaultViewImagePanel.getSize());
			defImg = defaultImageManager.get(style);
		}

		// Set the default view to the panel.
		setDefaultViewImagePanel(defImg);

		// TODO: Fix lock state
		// // Sync. lock state
		// final boolean lockState = vmm.getVisualStyle()
		// .getNodeAppearanceCalculator().getNodeSizeLocked();
		// spcs.firePropertyChange("UPDATE_LOCK", null, lockState);

		propertySheetPanel.setSorting(true);

		this.eventHelper.fireSynchronousEvent(new SelectedVisualStyleSwitchedEvent(this,lastVS, style));
		lastVS = style;

	}

	public void refreshUI() {

		final List<VisualStyle> visualStyles = new ArrayList<VisualStyle>(vmm
				.getAllVisualStyles());
		// final VisualStyle selectedStyle = (VisualStyle)
		// vsComboBox.getModel().getSelectedItem();

		// Disable action listeners
		final ActionListener[] li = visualStyleComboBox.getActionListeners();

		for (int i = 0; i < li.length; i++)
			visualStyleComboBox.removeActionListener(li[i]);

		visualStyleComboBox.removeAllItems();

		Component defPanel;

		final Dimension panelSize = defaultViewImagePanel.getSize();
		CyNetworkView view;

		// TODO: make sortable!
		// Collections.sort(visualStyles);

		for (VisualStyle vs : visualStyles) {
			logger.info("Adding VS: " + vs.getTitle());
			vsComboBoxModel.addElement(vs);
			defPanel = defViewEditor.getDefaultView(vs);
			view = ((DefaultViewPanel) defPanel).getView();

			updateDefaultImage(vs, view, panelSize);
		}

		// Switch back to the original style.
		switchVS(manager.getDefaultStyle());

		// Sync check box and actual lock state
		spcs.firePropertyChange("UPDATE_LOCK", null, true);

		// switchNodeSizeLock(lockSize.isSelected());

		// Restore listeners
		for (int i = 0; i < li.length; i++)
			visualStyleComboBox.addActionListener(li[i]);
	}

	/**
	 * Create image of a default dummy network and save in a Map object.
	 * 
	 * @param vsName
	 * @param view
	 * @param size
	 */
	// TODO: this should be called by listeners.
	public void updateDefaultImage(VisualStyle vs, CyNetworkView view,
			Dimension size) {
		Image image = defaultImageManager.remove(vs);

		if (image != null) {
			image.flush();
			image = null;
		}

		// TODO: Add image nenerator method in presentation
		// defaultImageManager.put(vs, view.createImage((int) size.getWidth(),
		// (int) size.getHeight(), 0.9));
	}

	public void updateAttributeList() {
		// TODO: use new event listener to do this.
		 vizMapPropertySheetBuilder.setAttrComboBox();
//		 final Set mappingTypes = vmm.getCalculatorCatalog().getMappingNames();
//		
//		 // mappingTypeEditor.setAvailableValues(mappingTypes.toArray());
//		 spcs.firePropertyChange("UPDATE_AVAILABLE_VAL", "mappingTypeEditor",
//		 mappingTypes.toArray());
	}

	/*
	 * private Set<Object> loadKeys(final String attrName, final CyTable
	 * attrs, final MappingCalculator mapping, final int nOre) { if
	 * (attrName.equals("ID")) { return loadID(nOre); }
	 * 
	 * Map mapAttrs; mapAttrs = CyAttributesUtils.getAttribute(attrName, attrs);
	 * 
	 * if ((mapAttrs == null) || (mapAttrs.size() == 0)) return new
	 * TreeSet<Object>();
	 * 
	 * List acceptedClasses = Arrays.asList(mapping.getAcceptedDataClasses());
	 * Class mapAttrClass = CyAttributesUtils.getClass(attrName, attrs);
	 * 
	 * if ((mapAttrClass == null) || !(acceptedClasses.contains(mapAttrClass)))
	 * return new TreeSet<Object>(); // Return empty set.
	 * 
	 * return loadKeySet(mapAttrs); }
	 */

	/**
	 * Loads the Key Set. private Set<Object> loadKeySet(final Map mapAttrs) {
	 * final Set<Object> mappedKeys = new TreeSet<Object>();
	 * 
	 * final Iterator keyIter = mapAttrs.values().iterator();
	 * 
	 * Object o = null;
	 * 
	 * while (keyIter.hasNext()) { o = keyIter.next();
	 * 
	 * if (o instanceof List) { List list = (List) o;
	 * 
	 * for (int i = 0; i < list.size(); i++) { Object vo = list.get(i);
	 * 
	 * if (!mappedKeys.contains(vo)) mappedKeys.add(vo); } } else { if
	 * (!mappedKeys.contains(o)) mappedKeys.add(o); } }
	 * 
	 * return mappedKeys; }
	 */
	public void setDefaultViewImagePanel(final Image defImage) {
		if (defImage == null) {

			// return;
		}

		defaultViewImagePanel.removeAll();

		final JButton defaultImageButton = new JButton();
		defaultImageButton.setUI(new BlueishButtonUI());
		defaultImageButton.setCursor(Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR));

		if(defImage != null)
			defaultImageButton.setIcon(new ImageIcon(defImage));
		if(vsComboBoxModel.getSelectedItem() != null)
			defaultImageButton
				.setText(vsComboBoxModel.getSelectedItem().toString());
		defaultViewImagePanel.add(defaultImageButton, BorderLayout.CENTER);
		defaultImageButton.addMouseListener(defaultViewMouseListener);
		this.repaint();
	}

	public JPanel getDefaultPanel() {
		return defaultViewImagePanel;
	}

	/**
	 * On/Off listeners. This is for performance.
	 * 
	 * @param on
	 *            DOCUMENT ME!
	 */
	private void enableListeners(boolean on) {
		// if (on) {
		// vmm.addChangeListener(this);
		// syncStyleBox();
		// ignore = false;
		// } else {
		// vmm.removeChangeListener(this);
		// }
	}

	
	
	

	/**
	 * DOCUMENT ME!
	 * 
	 * @param arg0
	 *            DOCUMENT ME!
	 */
	public void popupMenuCanceled(PopupMenuEvent arg0) {
		// TODO: replace this to firePropertyChange
		// disableAllPopup();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	/**
	 * Check the selected VPT and enable/disable menu items.
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// disableAllPopup();

		final int selected = propertySheetPanel.getTable().getSelectedRow();

		if (0 > selected) {
			return;
		}

		final Item item = (Item) propertySheetPanel.getTable().getValueAt(
				selected, 0);
		final Property curProp = item.getProperty();

		if (curProp == null)
			return;

		VizMapperProperty prop = ((VizMapperProperty) curProp);

		if (prop.getHiddenObject() instanceof VisualProperty
				&& (prop.getDisplayName().contains("Mapping Type") == false)
				&& (prop.getValue() != null)
				&& (prop.getValue().toString().startsWith("Please select") == false)) {
			// Enble delete menu
			// delete.setEnabled(true);
			Property[] children = prop.getSubProperties();

			for (Property p : children) {
				if ((p.getDisplayName() != null)
						&& p.getDisplayName().contains("Mapping Type")) {
					if ((p.getValue() == null)
							|| (p.getValue().equals("Discrete Mapping") == false)) {
						return;
					}
				}
			}

			VisualProperty type = ((VisualProperty) prop.getHiddenObject());

			Class dataType = type.getType();

			// if (dataType == Color.class) {
			// rainbow1.setEnabled(true);
			// rainbow2.setEnabled(true);
			// randomize.setEnabled(true);
			// brighter.setEnabled(true);
			// darker.setEnabled(true);
			// } else if (dataType == Number.class) {
			// randomize.setEnabled(true);
			// series.setEnabled(true);
			// }
			//
			// if ((type == VisualProperty.NODE_WIDTH)
			// || (type == VisualProperty.NODE_HEIGHT)) {
			// fit.setEnabled(true);
			// }
		}

		return;
	}

	/**
	 * <p>
	 * If user selects ID as controlling attributes name, cretate list of IDs
	 * from actual list of nodes/edges.
	 * </p>
	 * 
	 * @return
	 */
	/*
	 * not used any more, but since code that did use it is present in this file
	 * (commented out) I'll leave it here -- abeld private Set<Object>
	 * loadID(final int nOre) { Set<Object> ids = new TreeSet<Object>();
	 * 
	 * List<? extends CyTableEntry> obj;
	 * 
	 * if (nOre == MappingCalculator.NODE_MAPPING) { obj =
	 * applicationManager.getCurrentNetworkView
	 * ().getGraphPerspective().getNodeList(); } else { obj =
	 * applicationManager.getCurrentNetworkView
	 * ().getGraphPerspective().getEdgeList(); }
	 * 
	 * for (CyTableEntry o : obj) { ids.add(o.attrs().get("name", String.class));
	 * }
	 * 
	 * return ids; }
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *            DOCUMENT ME!
	 */
	// public void stateChanged(ChangeEvent e) {
	// final VisualStyle selected = vsComboBox
	// .getSelectedItem();
	//		
	//
	// final GraphView curView = applicationManager.getCurrentNetworkView();
	//
	// if (ignore)
	// return;
	//
	// System.out.println("Got VMM Change event.  Cur VS in VMM: "
	// + vmm.getVisualStyle().getName());
	//
	// if ((selectedName == null) || (currentName == null)
	// || (curView == null))
	// return;
	//
	// // Update GUI based on CalcCatalog's state.
	// if (!findVSName(currentName)) {
	// syncStyleBox();
	// } else {
	// // Bug fix: 0001802: if VS already existed in combobox, select it
	// for (int i = 0; i < vsComboBox.getItemCount(); i++) {
	// if (vsComboBox.getItemAt(i).equals(currentName)) {
	// vsComboBox.setSelectedIndex(i);
	//
	// break;
	// }
	// }
	// }
	//
	// // kono: should be placed here.
	// // MLC 03/31/08 BEGIN:
	// // Make fure we update the lastVSName based on anything that changes the
	// // visual style:
	// lastVSName = currentName;
	//
	// // MLC 03/31/08 END.
	// }
	// private void syncStyleBox() {
	// String curStyleName = vmm.getVisualStyle().getName();
	//
	// String styleName;
	// List<String> namesInBox = new ArrayList<String>();
	// namesInBox.addAll(vmm.getCalculatorCatalog().getVisualStyleNames());
	//
	// for (int i = 0; i < vsComboBox.getItemCount(); i++) {
	// styleName = vsComboBox.getItemAt(i).toString();
	//
	// if (vmm.getCalculatorCatalog().getVisualStyle(styleName) == null) {
	// // No longer exists in the VMM. Remove.
	// vsComboBox.removeItem(styleName);
	// defaultImageManager.remove(styleName);
	// vizMapPropertySheetBuilder.getPropertyMap().remove(styleName);
	// }
	// }
	//
	// Collections.sort(namesInBox);
	//
	// // Reset combobox items.
	// vsComboBox.removeAllItems();
	//
	// for (String name : namesInBox)
	// vsComboBox.addItem(name);
	//
	// // Bug fix: 0001721:
	// // Note: Because vsNameComboBox.removeAllItems() will fire unwanted
	// // event,
	// // vmm.getVisualStyle().getName() will not be the same as curStyleName
	// if ((curStyleName == null) || curStyleName.trim().equals(""))
	// switchVS(vmm.getVisualStyle().getName());
	// else
	// switchVS(curStyleName);
	// }
	// return true iff 'match' is found as a name within the
	// vsNameComboBox.
	private boolean findVSName(String match) {
		for (int i = 0; i < visualStyleComboBox.getItemCount(); i++) {
			if (visualStyleComboBox.getItemAt(i).equals(match)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Object getSelectedItem() {
		final JTable table = propertySheetPanel.getTable();

		return table.getModel().getValueAt(table.getSelectedRow(), 0);
	}

	public DefaultViewEditor getDefaultViewEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	public Component getVisualMappingBrowser() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDefaultViewEditor(DefaultViewEditor defViewEditor) {
		// TODO Auto-generated method stub

	}

	public void setDefaultViewPanel(JPanel defViewPanel) {
		// TODO Auto-generated method stub

	}

	public void setVisualMappingBrowser(Component visualMappingBrowser) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Update GUI components when new Visual Style is created.
	 */
	@Override public void handleEvent(final VisualStyleAddedEvent e) {
		final VisualStyle newStyle = e.getVisualStyleAdded();
		if (newStyle == null)
			return;
		
		vsComboBoxModel.addElement(newStyle);
		
		// Set selected style
		setSelectedVisualStyle(newStyle);
		final CyNetworkView currentView = applicationManager.getCurrentNetworkView();
		
		if (currentView != null)
			vmm.setVisualStyle(newStyle, currentView);

		// Update default panel
		final Component defPanel = defViewEditor.getDefaultView(newStyle);
		final CyNetworkView view = (CyNetworkView) ((DefaultViewPanelImpl) defPanel).getView();
		final Dimension panelSize = getDefaultPanel().getSize();

		if (view != null) {
			logger.debug("Creating Default Image for new visual style "
					+ newStyle.getTitle());
			updateDefaultImage(newStyle, view, panelSize);
			setDefaultViewImagePanel(getDefaultImageManager().get(newStyle));
		}

		switchVS(newStyle);
	}
	
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		VisualStyle targetStyle = lastVS;
		
		if(targetStyle == null) {
			targetStyle = manager.getDefaultStyle();
		}
		vmm.setVisualStyle(targetStyle, e.getNetworkView());
		targetStyle.apply(e.getNetworkView());
		
	}


	@Override public void handleEvent(NetworkAddedEvent e) {
		
		logger.debug("!!!!!!!!!! Network added. Need to update prop sheet: " + e.getNetwork().getSUID());
		vizMapPropertySheetBuilder.setPropertyTable(this.lastVS);
	}

	
	/**
	 * Update panel when removed
	 */
	@Override
	public void handleEvent(VisualStyleAboutToBeRemovedEvent e) {

		// Update image
		getDefaultImageManager().remove(e.getVisualStyleToBeRemoved());
		vizMapPropertySheetBuilder.getPropertyMap().remove(e.getVisualStyleToBeRemoved());
		// Switch to the default style
		final VisualStyle defaultStyle = manager.getDefaultStyle();

		switchVS(defaultStyle);
		// Apply to the current view
		final CyNetworkView view = applicationManager.getCurrentNetworkView();
		if (view != null)
			vmm.setVisualStyle(defaultStyle, view);
	}
}
