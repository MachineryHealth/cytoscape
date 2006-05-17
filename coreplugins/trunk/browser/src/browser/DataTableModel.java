package browser;

import giny.model.Edge;
import giny.model.GraphObject;
import giny.model.Node;
import giny.view.EdgeView;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.data.attr.MultiHashMapListener;

/**
 * @author kono Actual data manipulation is implemented here.
 */
public class DataTableModel extends DefaultTableModel implements
		SortTableModel, MultiHashMapListener {

	public static Color DEFAULT_NODE_COLOR = Color.YELLOW;
	public static Color DEFAULT_EDGE_COLOR = Color.RED;

	//	 Property for this browser.  One for each panel.
	private Properties props;
	
	private CyAttributes data;
	private List graphObjects;
	private List attributeNames;

	public static final String LS = System.getProperty("line.separator");

	private static final Boolean DEFAULT_FLAG = new Boolean(false);

	private int objectType = 0;

	// will be used by internal selection.
	private HashMap internalSelection = null;

	public DataTableModel() {
		initProperties();
	}

	public DataTableModel(int rows, int cols) {
		super(rows, cols);
		initProperties();
	}

	public DataTableModel(Object[][] data, Object[] names) {
		super(data, names);
		initProperties();
	}

	public DataTableModel(Object[] names, int rows) {
		super(names, rows);
		initProperties();
	}

	public DataTableModel(Vector names, int rows) {
		super(names, rows);
		initProperties();
	}

	public DataTableModel(Vector data, Vector names) {
		super(data, names);
		initProperties();
	}
	
	/*
	 * Initialize properties for Browser Plugin.
	 */
	private void initProperties() {
		props = new Properties();
		props.setProperty("colorSwitch", "off");
		props.setProperty("defaultNodeColor", this.DEFAULT_NODE_COLOR.toString());
		props.setProperty("defaultEdgeColor", this.DEFAULT_EDGE_COLOR.toString());
	}
	
	protected void setColorSwitch(boolean flag) {
		if(flag) {
			props.setProperty("colorSwitch", "on");
		} else {
			props.setProperty("colorSwitch", "off");
		}
	}
	
	protected boolean getColorSwitch() {
		if(props.getProperty("colorSwitch").equals("on")) {
			return true;
		}else {
			return false;
		}
	}

	// Accept CyAttributes and create table
	public void setTableData(CyAttributes data, List graph_objects,
			List attributeNames, int objectType) {
		this.data = data;
		this.graphObjects = graph_objects;
		this.attributeNames = attributeNames;
		this.objectType = objectType;
		data.getMultiHashMap().addDataListener(this);

		if (objectType == DataTable.NETWORK) {
			setNetworkTable();
		} else {
			setTable();
		}
	}

	public Map getSelectionArray() {
		return internalSelection;
	}

	public void setSelectionArray(String key, boolean flag) {
		internalSelection.put(key, new Boolean(flag));
	}

	public void resetSelectionFlags() {

		if (this.objectType != DataTable.NETWORK) {
			Iterator it = graphObjects.iterator();
			while (it.hasNext()) {
				GraphObject obj = (GraphObject) it.next();
				String id = obj.getIdentifier();

				internalSelection.put(id, DEFAULT_FLAG);
			}
		}
	}

	public List getObjects() {
		return graphObjects;
	}

	public void setTableData(List graph_objects, List attributes) {

		this.graphObjects = graph_objects;
		this.attributeNames = attributes;
		if (this.objectType != DataTable.NETWORK) {
			setTable();
		} else {
			setNetworkTable();
		}

	}

	public void setTableDataAttributes(List attributes) {
		this.attributeNames = attributes;
		if (this.objectType != DataTable.NETWORK) {
			setTable();
		} else {
			setNetworkTable();
		}
	}

	public void setTableDataObjects(List graph_objects) {
		this.graphObjects = graph_objects;
		if (this.objectType != DataTable.NETWORK) {
			setTable();
		} else {
			setNetworkTable();
		}
	}

	public void setObjectType(int ot) {
		objectType = ot;
	}

	protected void setNetworkTable() {

		if(Cytoscape.getCurrentNetwork() == null) {
			return;
		}
		
		String networkName = Cytoscape.getCurrentNetwork().getIdentifier();


		int att_length = attributeNames.size();
		// Attribute names will be the row id, and num. of column is always
		Object[][] data_vector = new Object[att_length][2];
		Object[] column_names = new Object[2];

		column_names[0] = "Network Attribute Name";
		column_names[1] = "Value";

		for (int i = 0; i < att_length; i++) {
			String attributeName = (String) attributeNames.get(i);
			byte type = data.getType(attributeName);

			data_vector[i][0] = attributeName;
			Object value = getAttributeValue(type, networkName, attributeName);
			data_vector[i][1] = value;
		}
		setDataVector(data_vector, column_names);
		
	}

	// Fill the cells in the table
	// *** need to add an argument to copy edge attribute name correctly.
	//
	protected void setTable() {

		internalSelection = new HashMap();
		Iterator it = graphObjects.iterator();
		while (it.hasNext()) {
			GraphObject obj = (GraphObject) it.next();
			String id = obj.getIdentifier();

			internalSelection.put(id, DEFAULT_FLAG);

			if (objectType == DataTable.NODES) {
				Node targetNode = Cytoscape.getCyNode(id);
				Cytoscape.getCurrentNetworkView().getNodeView(targetNode)
						.setSelectedPaint(DEFAULT_NODE_COLOR);
			} else {
				String[] edgeNameParts = id.split(" ");
				String interaction = edgeNameParts[1].substring(1,
						edgeNameParts[1].length() - 1);
				Node source = Cytoscape.getCyNode(edgeNameParts[0]);
				Node target = Cytoscape.getCyNode(edgeNameParts[2]);
				Edge targetEdge = Cytoscape.getCyEdge(source, target,
						Semantics.INTERACTION, interaction, false);
				if (targetEdge != null) {
					EdgeView edgeView = Cytoscape.getCurrentNetworkView()
							.getEdgeView(targetEdge);
					if (edgeView != null) {
						edgeView.setSelectedPaint(DEFAULT_EDGE_COLOR);
					}
				}

			}

		}

		int att_length = attributeNames.size() + 1;
		int go_length = graphObjects.size();

		Object[][] data_vector = new Object[go_length][att_length];
		Object[] column_names = new Object[att_length];

		// Set column names (attribute names)
		// System.out.println("Debug: testsection start.");
		column_names[0] = DataTable.ID;
		for (int j = 0; j < go_length; ++j) {
			GraphObject obj = (GraphObject) graphObjects.get(j);

			data_vector[j][0] = obj.getIdentifier();
		}

		// Set actual data
		for (int i1 = 0; i1 < attributeNames.size(); ++i1) {
			int i = i1 + 1;
			column_names[i] = attributeNames.get(i1);
			String attributeName = (String) attributeNames.get(i1);

			byte type = data.getType(attributeName);
			// System.out.println("Debug: col name = " + column_names[i] + "
			// TYPE is " + type );

			for (int j = 0; j < go_length; ++j) {
				GraphObject obj = (GraphObject) graphObjects.get(j);

				Object value = getAttributeValue(type, obj.getIdentifier(),
						attributeName);
				//				
				// ArrayList testlist = (ArrayList) value;
				// value = testlist.get(0);
				//				

				data_vector[j][i] = value;

				// System.out.println("Debug: value = " + value.toString() + ",
				// class is " + value.getClass().toString() );
			}
		}

		setDataVector(data_vector, column_names);

	}

	public Object getAttributeValue(byte type, String id, String att) {
		if (type == CyAttributes.TYPE_INTEGER)
			return data.getIntegerAttribute(id, att);
		else if (type == CyAttributes.TYPE_FLOATING)
			return data.getDoubleAttribute(id, att);
		else if (type == CyAttributes.TYPE_BOOLEAN)
			return data.getBooleanAttribute(id, att);
		else if (type == CyAttributes.TYPE_STRING)
			return data.getStringAttribute(id, att);
		else if (type == CyAttributes.TYPE_SIMPLE_LIST)
			return data.getAttributeList(id, att);
		else if (type == CyAttributes.TYPE_SIMPLE_MAP)
			return data.getAttributeMap(id, att);
		return null;
	}

	public List getGraphObjects() {
		return graphObjects;
	}

	public Class getObjectTypeAt(String colName) {

		byte type = data.getType(colName);

		if (type == CyAttributes.TYPE_INTEGER)
			return Integer.class;
		else if (type == CyAttributes.TYPE_FLOATING)
			return Double.class;
		else if (type == CyAttributes.TYPE_BOOLEAN)
			return Boolean.class;
		else if (type == CyAttributes.TYPE_STRING)
			return String.class;
		else if (type == CyAttributes.TYPE_SIMPLE_LIST)
			return List.class;
		else if (type == CyAttributes.TYPE_SIMPLE_MAP)
			return Map.class;
		return null;

	}

	public void attributeValueAssigned(java.lang.String objectKey,
			java.lang.String attributeName, java.lang.Object[] keyIntoValue,
			java.lang.Object oldAttributeValue,
			java.lang.Object newAttributeValue) {
		// System.out.println( "attributeValueAssigned" );
		// setTable();
	}

	public void attributeValueRemoved(java.lang.String objectKey,
			java.lang.String attributeName, java.lang.Object[] keyIntoValue,
			java.lang.Object attributeValue) {
		// setTable();
	}

	public void allAttributeValuesRemoved(java.lang.String objectKey,
			java.lang.String attributeName) {
		// setTable();
	}

	// //////////////////////////////////////
	// Implements JSortTable

	public boolean isSortable(int col) {
		return true;
	}

	public void sortColumn(int col, boolean ascending) {
		Collections.sort(getDataVector(), new ColumnComparator(col, ascending));
	}

	public boolean isCellEditable(int rowIndex, int colIndex) {
		// System.out.println("row = " + rowIndex + ", col = " + colIndex);

		Class objectType = null;
		Object selectedObj = this.getValueAt(rowIndex, colIndex);

		if (selectedObj == null && colIndex != 0) {
			return true;
		} else if (selectedObj != null) {
			objectType = this.getValueAt(rowIndex, colIndex).getClass();
		}

		if (objectType != null) {

			if (colIndex == 0) {
				return false;
			} else if (objectType == ArrayList.class) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Instead of using a listener, just overwrite this method to save time and
	 * write to the temp object
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		DataEditAction edit = null;
		
		if(this.objectType != DataTable.NETWORK) {
			edit = new DataEditAction(this, (String) getValueAt(
					rowIndex, 0), (String) attributeNames.get(columnIndex - 1),
					null, getValueAt(rowIndex, columnIndex), aValue, objectType);
		} else {
			edit = new DataEditAction(
											this,
											Cytoscape.getCurrentNetwork().getIdentifier(),
											(String)this.getValueAt(rowIndex, 0),
											null,
											getValueAt(rowIndex, columnIndex), 
											aValue,
											objectType
										);
		}
		
		cytoscape.Cytoscape.getDesktop().addEdit(edit);

	}

}
