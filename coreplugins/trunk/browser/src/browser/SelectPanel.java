package browser;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.SelectEvent;
import cytoscape.data.SelectEventListener;
import cytoscape.view.CytoscapeDesktop;
import filter.model.Filter;
import filter.model.FilterManager;

/**
 * Advanced Panel.<br>
 * This section catches all selection events and property changes
 * for Attribute browser.<br>
 * 
 * @author xmas
 * @author kono
 *
 */
public class SelectPanel extends JPanel implements PropertyChangeListener,
		ActionListener, SelectEventListener {
	public static final int NODES = 0;
	public static final int EDGES = 1;
	int graphObjectType;
	
	JComboBox networkBox;
	JComboBox filterBox;
	DataTableModel tableModel;
	DataTable table;
	Map titleIdMap;
	JCheckBox mirrorSelection;

	CyNetwork currentNetwork;

	/**
	 * Constructor.<br>
	 * Initialize GUI components.
	 * 
	 * @param tableModel table model used in the Attribute Browser.
	 * @param graphObjectType Graph object types - Node or Edge.
	 * 
	 */
	public SelectPanel(final DataTable table, final int graphObjectType) {

		this.table = table;
		this.tableModel = table.getDataTableModel();
		this.graphObjectType = graphObjectType;

		titleIdMap = new HashMap();
		networkBox = getNetworkBox();
		networkBox.setMaximumSize(new Dimension(15, (int) networkBox
				.getPreferredSize().getHeight()));

		// Filter is disabled for now...
		filterBox = new JComboBox(FilterManager.defaultManager()
				.getComboBoxModel());

		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(
				this);
		Cytoscape.getDesktop().getSwingPropertyChangeSupport()
				.addPropertyChangeListener(this);

		mirrorSelection = new JCheckBox();
		mirrorSelection.setSelected(true);

		setBorder(new TitledBorder("Object Selection"));
		add(new JLabel("Filter: "));
		add(filterBox);
		add(new JLabel("Network: "));
		add(networkBox);
		add(new JLabel("Mirror Network Selection"));
		add(mirrorSelection);

		filterBox.addActionListener(this);
		networkBox.addActionListener(this);
	}

	/**
	 * Catch the selection event.<br>
	 * This is only for nodes and edges.<br>
	 * 
	 */
	public void onSelectEvent(SelectEvent event) {
		
		if (mirrorSelection.isSelected()) {
			if (graphObjectType == NODES
					&& (event.getTargetType() == SelectEvent.SINGLE_NODE || event
							.getTargetType() == SelectEvent.NODE_SET)) {
				// node selection
				tableModel.setSelectedColor(JSortTable.SELECTED_NODE);
				tableModel.setSelectedColor(JSortTable.REV_SELECTED_NODE);
				
				tableModel.setTableDataObjects(new ArrayList(Cytoscape
						.getCurrentNetwork().getSelectedNodes()));
			} else if (graphObjectType == EDGES
					&& (event.getTargetType() == SelectEvent.SINGLE_EDGE || event
							.getTargetType() == SelectEvent.EDGE_SET)) {
				// edge selection
				tableModel.setSelectedColor(JSortTable.SELECTED_EDGE);
				tableModel.setSelectedColor(JSortTable.REV_SELECTED_EDGE);
				tableModel.setTableDataObjects(new ArrayList(Cytoscape
						.getCurrentNetwork().getSelectedEdges()));
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == filterBox) {
			Filter filter = (Filter) filterBox.getSelectedItem();
			System.out.println("Showing all that Pass Filter: " + filter);
			List list = new ArrayList(getGraphObjectCount());
			Iterator objs = getGraphObjectIterator();
			while (objs.hasNext()) {
				Object obj = objs.next();
				if (filter.passesFilter(obj))
					list.add(obj);
			}
			tableModel.setTableDataObjects(list);
		}

		if (e.getSource() == networkBox) {
			String network_id = (String) titleIdMap.get(networkBox
					.getSelectedItem());
			CyNetwork network = Cytoscape.getNetwork(network_id);
			System.out.println("Showing all that Pass Network: " + network);
			tableModel.setTableDataObjects(getGraphObjectList(network));
		}
	}

	private List getGraphObjectList(CyNetwork network) {

		Iterator it = null;
		List objList = new ArrayList();

		if (graphObjectType == NODES) {
			it = network.nodesIterator();
		} else if (graphObjectType == EDGES) {
			it = network.edgesIterator();
		} else {
			return null;
		}

		while (it.hasNext()) {
			objList.add(it.next());
		}
		return objList;
	}

	private Iterator getGraphObjectIterator() {
		if (graphObjectType == NODES)
			return Cytoscape.getRootGraph().nodesIterator();
		else
			return Cytoscape.getRootGraph().edgesIterator();
	}

	private int getGraphObjectCount() {
		if (graphObjectType == NODES)
			return Cytoscape.getRootGraph().getNodeCount();
		else
			return Cytoscape.getRootGraph().getEdgeCount();
	}

	/**
	 * Catch property change events here.
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent e) {
		
		

		if (e.getPropertyName().equals(Cytoscape.NETWORK_CREATED)
				|| e.getPropertyName().equals(Cytoscape.NETWORK_DESTROYED)) {
			updateNetworkBox();
			tableModel.setTableDataObjects(new ArrayList());

		} else if (e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_FOCUS
				|| e.getPropertyName().equals(Cytoscape.SESSION_LOADED)
				|| e.getPropertyName().equals(Cytoscape.ATTRIBUTES_CHANGED)
				|| e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
			

			if (currentNetwork != null) {
				currentNetwork.removeSelectEventListener(this);
			}

			// Change the target network
			currentNetwork = Cytoscape.getCurrentNetwork();
			if (currentNetwork != null) {
				currentNetwork.addSelectEventListener(this);
				if (graphObjectType == NODES) {
					tableModel.setTableDataObjects(new ArrayList(Cytoscape
							.getCurrentNetwork().getSelectedNodes()));
				} else if (graphObjectType == EDGES) {
					tableModel.setTableDataObjects(new ArrayList(Cytoscape
							.getCurrentNetwork().getSelectedEdges()));
				} else {
					// Network Attribute
					tableModel.setTableDataObjects(new ArrayList());
				}
			}
		}
	}

	protected void updateNetworkBox() {
		Iterator i = Cytoscape.getNetworkSet().iterator();
		Vector vector = new Vector();
		while (i.hasNext()) {
			CyNetwork net = (CyNetwork) i.next();
			titleIdMap.put(net.getTitle(), net.getIdentifier());
			vector.add(net.getTitle());
		}
		DefaultComboBoxModel model = new DefaultComboBoxModel(vector);
		networkBox.setModel(model);
	}

	protected JComboBox getNetworkBox() {
		Iterator i = Cytoscape.getNetworkSet().iterator();
		Vector vector = new Vector();
		while (i.hasNext()) {
			CyNetwork net = (CyNetwork) i.next();
			titleIdMap.put(net.getTitle(), net.getIdentifier());
			vector.add(net.getTitle());
		}
		DefaultComboBoxModel model = new DefaultComboBoxModel(vector);
		return new JComboBox(model);
	}

}
