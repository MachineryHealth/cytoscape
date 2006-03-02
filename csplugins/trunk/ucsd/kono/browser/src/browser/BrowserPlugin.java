package browser;

import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.plugin.CytoscapePlugin;

/**
 * @author kono
 * 
 * This Plugin is an attribute browser with smaller GUI. All functions are based
 * on Rowan's code.
 */
public class BrowserPlugin extends CytoscapePlugin {

	// Constructor
	public BrowserPlugin() {

		initialize();
		
		// Turn On Data menu
		Cytoscape.getDesktop().getCyMenus().getMenuBar().getMenu("Data")
				.setEnabled(true);

		JCheckBoxMenuItem switchBrowserView = new JCheckBoxMenuItem(
				new DisplayNetworkTreeAction());

		JCheckBoxMenuItem switchNetworkTreeView = new JCheckBoxMenuItem(
				new DisplayAttributeBrowserAction());

		JCheckBoxMenuItem switchAdvancedView = new JCheckBoxMenuItem(
				new DisplayAdvancedWindowAction());

		switchBrowserView.setSelected(true);
		switchNetworkTreeView.setSelected(true);

		switchNetworkTreeView.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F5, 0));

		Cytoscape.getDesktop().getCyMenus().getMenuBar().getMenu("View").add(
				switchBrowserView, 0);
		Cytoscape.getDesktop().getCyMenus().getMenuBar().getMenu("View").add(
				switchNetworkTreeView, 0);
		Cytoscape.getDesktop().getCyMenus().getMenuBar().getMenu("View").add(
				switchAdvancedView, 0);

	}

	// Call 2 tables, node and edges.
	// The DataTable class actually creates all CytoPanels.
	// For this version of browser, it creates Cytopanel3 only.
	// Filter functions are implemented as an Advanced Window.
	protected void initialize() {
		
		DataTable table_nodes = new DataTable(Cytoscape.getNodeAttributes(),
				DataTable.NODES);
		DataTable table_edges = new DataTable(Cytoscape.getEdgeAttributes(),
				DataTable.EDGES);
	
	}

}