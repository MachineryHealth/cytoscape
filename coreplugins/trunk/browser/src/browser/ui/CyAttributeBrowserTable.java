/*
 * $Archive: SourceJammer$
 * $FileName: JSortTable.java$
 * $FileID: 3984$
 *
 * Last change:
 * $AuthorName: Timo Haberkern$
 * $Date: 2007-07-11 17:47:31 -0700 (水, 11 7 2007) $
 * $Comment: $
 *
 * $KeyWordsOff: $
 */

/*
 =====================================================================

 JSortTable.java

 Created by Claude Duguay
 Copyright (c) 2002

 =====================================================================
 */
package browser.ui;

import browser.AttributeBrowser;
import browser.DataObjectType;
import static browser.DataObjectType.EDGES;
import static browser.DataObjectType.NETWORK;
import static browser.DataObjectType.NODES;

import browser.DataTableModel;
import browser.SortTableModel;

import browser.util.HyperLinkOut;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;

import cytoscape.data.CyAttributes;
import cytoscape.data.SelectEvent;
import cytoscape.data.SelectEventListener;
import cytoscape.data.Semantics;

import cytoscape.dialogs.NetworkMetaDataDialog;

import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;
import cytoscape.util.OpenBrowser;

import cytoscape.util.swing.ColumnResizer;

import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;

import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.VisualMappingManager;

import giny.model.Edge;
import giny.model.GraphObject;
import giny.model.Node;

import giny.view.EdgeView;
import giny.view.NodeView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;


/**
 * Based on JSortTable and completely rewrote by kono
 * 
 */
public class CyAttributeBrowserTable extends JTable implements MouseListener, ActionListener,
                                                               PropertyChangeListener,
                                                               SelectEventListener {
	// Local Signal
	/**
	 *
	 */
	public final static String RESTORE_COLUMN = "RESTORE_COLUMN";

	/**
	 *
	 */
	public static final int SELECTED_NODE = 1;

	/**
	 *
	 */
	public static final int REV_SELECTED_NODE = 2;

	/**
	 *
	 */
	public static final int SELECTED_EDGE = 3;

	/**
	 *
	 */
	public static final int REV_SELECTED_EDGE = 4;

	// Target network to watch selection
	CyNetwork currentNetwork;

	// Gloval calcs used for coloring
	private VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
	private GlobalAppearanceCalculator gac;
	protected int sortedColumnIndex = -1;
	protected boolean sortedColumnAscending = true;
	private Color selectedNodeColor;
	private Color selectedEdgeColor;
	private Color reverseSelectedNodeColor;
	private Color reverseSelectedEdgeColor;

	// For right-click menu
	private JPopupMenu rightClickPopupMenu;
	private JPopupMenu cellMenu;
	private JMenuItem copyMenuItem = null;
	private JMenu exportMenu = null;
	private JMenuItem exportCellsMenuItem = null;
	private JMenuItem exportTableMenuItem = null;
	private JMenuItem selectAllMenuItem = null;
	private JMenuItem newSelectionMenuItem = null;
	private JCheckBoxMenuItem coloringMenuItem = null;
	private Clipboard systemClipboard;
	private StringSelection stsel;
	private DataTableModel tableModel;
	private DataObjectType objectType;
	private Map<String, Map<String, String>> linkoutProps;
	private static final Font BORDER_FONT = new Font("Sans-serif", Font.BOLD, 12);

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public JPopupMenu getContextMenu() {
		if (rightClickPopupMenu == null) {
			rightClickPopupMenu = getPopupMenu();
		}

		return rightClickPopupMenu;
	}

	/**
	 *
	 */
	protected static final String LS = System.getProperty("line.separator");

	// this is the only one that's actually used
	/**
	 * Creates a new JSortTable object.
	 *
	 * @param model  DOCUMENT ME!
	 * @param objectType  DOCUMENT ME!
	 */
	public CyAttributeBrowserTable(DataTableModel model, DataObjectType objectType) {
		super(model);
		initSortHeader();

		this.tableModel = model;
		this.objectType = objectType;

		initialize();
	}

	// Initialize some attributes of this table
	private void initialize() {
		this.setSize(400, 200);
		this.setCellSelectionEnabled(true);
		this.getPopupMenu();

		setKeyStroke();
		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);

		setSelectedColor(SELECTED_NODE);
		setSelectedColor(REV_SELECTED_NODE);
		setSelectedColor(SELECTED_EDGE);
		setSelectedColor(REV_SELECTED_EDGE);

		this.setDefaultRenderer(Object.class, new BrowserTableCellRenderer(false, objectType));
		this.getColumnModel().addColumnModelListener(this);

	}

	private void setKeyStroke() {
		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		// Identifying the copy KeyStroke user can modify this
		// to copy on some other Key combination.
		this.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	protected void setSelectedColor(final int type) {
		gac = vmm.getVisualStyle().getGlobalAppearanceCalculator();

		switch (type) {
			case SELECTED_NODE:
				selectedNodeColor = gac.getDefaultNodeSelectionColor();

				break;

			case REV_SELECTED_NODE:
				reverseSelectedNodeColor = gac.getDefaultNodeReverseSelectionColor();

				break;

			case SELECTED_EDGE:
				selectedEdgeColor = gac.getDefaultEdgeSelectionColor();

				break;

			case REV_SELECTED_EDGE:
				reverseSelectedEdgeColor = gac.getDefaultEdgeReverseSelectionColor();

				break;

			default:
				break;
		}
	}

	protected Color getSelectedColor(final int type) {
		Color newColor;
		gac = vmm.getVisualStyle().getGlobalAppearanceCalculator();

		switch (type) {
			case SELECTED_NODE:
				newColor = gac.getDefaultNodeSelectionColor();

				break;

			case REV_SELECTED_NODE:
				newColor = gac.getDefaultNodeReverseSelectionColor();

				break;

			case SELECTED_EDGE:
				newColor = gac.getDefaultEdgeSelectionColor();

				break;

			case REV_SELECTED_EDGE:
				newColor = gac.getDefaultEdgeReverseSelectionColor();

				break;

			default:
				newColor = null;

				break;
		}

		return newColor;
	}

	protected Edge getEdge(final String edgeString) {
		String[] edgeNameParts = edgeString.split(" \\(");
		final Node source = Cytoscape.getCyNode(edgeNameParts[0]);
		edgeNameParts = edgeNameParts[1].split("\\) ");

		final String interaction = edgeNameParts[0];
		final Node target = Cytoscape.getCyNode(edgeNameParts[1]);

		return Cytoscape.getCyEdge(source, target, Semantics.INTERACTION, interaction, false);
	}

	private Map<String, GraphObject> paintNodesAndEdges(int idLocation) {
		final int[] rowsSelected = getSelectedRows();

		final Map<String, GraphObject> selectedMap = new HashMap<String, GraphObject>();
		final int selectedRowLength = rowsSelected.length;
		String selectedName = null;

		Node selectedNode;
		Edge selectedEdge;
		NodeView nv;
		EdgeView ev;
		final CyNetworkView netView = Cytoscape.getCurrentNetworkView();

		for (int idx = 0; idx < selectedRowLength; idx++) {
			selectedName = (String) getValueAt(rowsSelected[idx], idLocation);

			if (objectType == NODES) {
				// Change node color
				selectedNode = Cytoscape.getCyNode(selectedName);
				selectedMap.put(selectedName, selectedNode);

				if (netView != Cytoscape.getNullNetworkView()) {
					nv = netView.getNodeView(selectedNode);

					if (nv != null) {
						nv.setSelectedPaint(reverseSelectedNodeColor);
					}
				}
			} else if (objectType == EDGES) {
				selectedEdge = getEdge(selectedName);
				selectedMap.put(selectedName, selectedEdge);

				if (netView != Cytoscape.getNullNetworkView()) {
					ev = netView.getEdgeView(selectedEdge);

					if (ev != null) {
						ev.setSelectedPaint(reverseSelectedEdgeColor);
					}
				}
			}
		}

		return selectedMap;
	}

	private void resetObjectColor(int idLocation) {
		CyNetworkView view = Cytoscape.getCurrentNetworkView();

		if (view == Cytoscape.getNullNetworkView() || view == null)
			return;

		final int rowCount = dataModel.getRowCount();

		Node selectedNode;
		Object val;
		NodeView nv;
		EdgeView ev;
		Edge selectedEdge;

		for (int idx = 0; idx < rowCount; idx++) {
			val = dataModel.getValueAt(idx, idLocation);

			if (val == null)
				continue;

			if (objectType == NODES) {
				selectedNode = Cytoscape.getCyNode(val.toString());

				// Set to the original color
				if (selectedNode != null) {
					nv = view.getNodeView(selectedNode);

					if (nv != null) {
						nv.setSelectedPaint(selectedNodeColor);
					}
				}
			} else if (objectType == EDGES) {
				selectedEdge = this.getEdge(val.toString());

				if (selectedEdge != null) {
					ev = view.getEdgeView(selectedEdge);

					if (ev != null) {
						ev.setSelectedPaint(selectedEdgeColor);
					}
				}
			}
		}
	}

	/**
	 * This method initializes jPopupMenu1
	 *
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getPopupMenu() {
		if (rightClickPopupMenu == null) {
			rightClickPopupMenu = new JPopupMenu();

			copyMenuItem = new JMenuItem("Copy");
			newSelectionMenuItem = new JMenuItem("Select from table");
			exportMenu = new JMenu("Export...");
			exportCellsMenuItem = new JMenuItem("Selected Cells");
			exportTableMenuItem = new JMenuItem("Entire Table");
			selectAllMenuItem = new JMenuItem("Select All");

			coloringMenuItem = new JCheckBoxMenuItem("On/Off Coloring");

			// showAdvancedWindow = new JCheckBoxMenuItem("Show Advanced
			// Window");
			copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						System.out.println("Cells copied to clipboard.");
						copyToClipBoard();
					}
				});

			exportCellsMenuItem.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						export(false);
					}
				});

			exportTableMenuItem.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						export(true);
					}
				});

			selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						selectAll();
					}
				});

			newSelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						int idLocation = 0;
						final int columnCount = getColumnCount();

						// First, find the location of the ID column
						for (int idx = 0; idx < columnCount; idx++) {
							if (getColumnName(idx).equals(AttributeBrowser.ID)) {
								idLocation = idx;

								break;
							}
						}

						final Map<String, GraphObject> selectedMap = paintNodesAndEdges(idLocation);
						final CyNetwork curNet = Cytoscape.getCurrentNetwork();

						final List<GraphObject> nonSelectedObjects = new ArrayList<GraphObject>();

						GraphObject fromMap;

						if (objectType == NODES) {
							for (Object curNode : curNet.getSelectedNodes()) {
								fromMap = selectedMap.get(((Node) curNode).getIdentifier());

								if (fromMap == null) {
									nonSelectedObjects.add((GraphObject) curNode);
								}
							}

							resetObjectColor(idLocation);
							curNet.setSelectedNodeState(nonSelectedObjects, false);
						} else {
							for (Object curEdge : curNet.getSelectedEdges()) {
								fromMap = selectedMap.get(((Edge) curEdge).getIdentifier());

								if (fromMap == null) {
									nonSelectedObjects.add((GraphObject) curEdge);
								}
							}

							resetObjectColor(idLocation);
							curNet.setSelectedEdgeState(nonSelectedObjects, false);
						}

						if (Cytoscape.getCurrentNetworkView() != Cytoscape.getNullNetworkView()) {
							Cytoscape.getCurrentNetworkView().updateView();
						}
					}
				});

			coloringMenuItem.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						if (Cytoscape.getCurrentNetworkView() != Cytoscape.getNullNetworkView()) {
							if (coloringMenuItem.isSelected() == true) {
								System.out.println("color ON");
								setNewRenderer(true);
							} else {
								System.out.println("color OFF");
								setNewRenderer(false);
							}
						}
					}
				});

			exportMenu.add(exportCellsMenuItem);
			exportMenu.add(exportTableMenuItem);

			if (objectType != NETWORK) {
				rightClickPopupMenu.add(newSelectionMenuItem);
			}

			rightClickPopupMenu.add(copyMenuItem);
			rightClickPopupMenu.add(selectAllMenuItem);
			rightClickPopupMenu.add(exportMenu);

			if (objectType != NETWORK) {
				rightClickPopupMenu.addSeparator();
				rightClickPopupMenu.add(coloringMenuItem);
			}
		}

		return rightClickPopupMenu;
	}

	private void setNewRenderer(boolean colorSwitch) {
		this.setDefaultRenderer(Object.class, new BrowserTableCellRenderer(colorSwitch, objectType));
		this.repaint();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param all DOCUMENT ME!
	 */
	public void export(final boolean all) {
		// Do this in the GUI Event Dispatch thread...
		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					final String name;

					try {
						name = FileUtil.getFile("Export Table", FileUtil.SAVE,
						                        new CyFileFilter[] {  }).toString();
					} catch (Exception exp) {
						// this is because the selection was canceled
						return;
					}

					String export = exportTable(all);
					export = export.replace("[", "\"");
					export = export.replace("]", "\"");

					try {
						final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(name)));
						writer.write(export);
						writer.close();
						export = null;
					} catch (Exception ex) {
						System.out.println("Table Export Write error");
						ex.printStackTrace();
					}
				}
			});
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String exportTable() {
		return exportTable("\t", LS, false);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param all DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String exportTable(boolean all) {
		return exportTable("\t", LS, all);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param element_delim DOCUMENT ME!
	 * @param eol_delim DOCUMENT ME!
	 * @param all DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String exportTable(final String element_delim, final String eol_delim, boolean all) {
		if (all == true) {
			this.selectAll();
		}

		final int[] selectedCols = this.getSelectedColumns();
		final StringBuffer buf = new StringBuffer();

		for (int i = 0; i < selectedCols.length; i++) {
			buf.append(this.getColumnName(selectedCols[i]) + "\t");
		}

		buf.append(LS);

		return buf.toString() + copyToClipBoard();
	}

	private void getSelected() {
		int selectedCol = this.getSelectedColumn();
		this.getSelectedRows();
	}

	protected void initSortHeader() {
		JTableHeader header = getTableHeader();
		header.setDefaultRenderer(new SortHeaderRenderer());
		header.addMouseListener(this);

		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		//
		// Event handler. Define actions when mouse is clicked.
		//
		addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					final int column = getColumnModel().getColumnIndexAtX(e.getX());
					final int row = e.getY() / getRowHeight();
					final Object value = getValueAt(row, column);
					getSelected();

					// If action is right click, then show edit pop-up menu
					if (SwingUtilities.isRightMouseButton(e)) {
						if (value != null) {
							rightClickPopupMenu.remove(rightClickPopupMenu.getComponentCount() - 1);
							rightClickPopupMenu.add(new HyperLinkOut(value.toString(), linkoutProps));
							rightClickPopupMenu.show(e.getComponent(), e.getX(), e.getY());
						}
					} else if (SwingUtilities.isLeftMouseButton(e)
					           && (getSelectedRows().length != 0)) {
						showListContents(e);

						if ((row >= getRowCount()) || (row < 0) || (column >= getColumnCount())
						    || (column < 0))
							return;

						// Object cellValue = getValueAt(row, column);
						if ((value != null) && (value.getClass() == String.class)) {
							URL url = null;

							try {
								url = new URL((String) value);
							} catch (MalformedURLException e1) {
							}

							if (url != null) {
								cytoscape.util.OpenBrowser.openURL(url.toString());
							}
						}
					}
				} // mouseClicked

				public void mouseReleased(MouseEvent e) {
					// When the mouse is released, fire signal to pass the selected
					// objects in the table.
					// Get selected object names
					final int[] rowsSelected = getSelectedRows();

					if (rowsSelected.length == 0) {
						return;
					}

					final int columnCount = getColumnCount();
					int idLocation = 0;

					// First, find the location of the ID column
					for (int idx = 0; idx < columnCount; idx++) {
						if (getColumnName(idx).equals(AttributeBrowser.ID)) {
							idLocation = idx;

							break;
						}
					}

					// Initialize internal selection table
					((DataTableModel) dataModel).resetSelectionFlags();

					setSelectedColor(SELECTED_NODE);
					setSelectedColor(REV_SELECTED_NODE);
					setSelectedColor(SELECTED_EDGE);
					setSelectedColor(REV_SELECTED_EDGE);

					System.out.println("------------ Release called! -------------");
					resetObjectColor(idLocation);
					paintNodesAndEdges(idLocation);

					//					for (int idx = 0; idx < rowsSelected.length; idx++) {
					//						selectedName = (String) getValueAt(rowsSelected[idx], idLocation);
					//
					//						if (objectType == DataTable.NODES) {
					//							// Flip the internal flag
					//							((DataTableModel) dataModel).setSelectionArray(selectedName, true);
					//
					//							Node selectedNode = Cytoscape.getCyNode(selectedName);
					//
					//							if (Cytoscape.getCurrentNetworkView() != Cytoscape.getNullNetworkView()) {
					//								NodeView nv = Cytoscape.getCurrentNetworkView()
					//								                       .getNodeView(selectedNode);
					//
					//								if (nv != null) {
					//									nv.setSelectedPaint(reverseSelectedNodeColor);
					//								}
					//							}
					//						} else if (objectType == DataTable.EDGES) {
					//							
					//							Edge selectedEdge = getEdge(selectedName);
					//
					//							if (Cytoscape.getCurrentNetworkView() != Cytoscape.getNullNetworkView()) {
					//								EdgeView ev = Cytoscape.getCurrentNetworkView()
					//								                       .getEdgeView(selectedEdge);
					//
					//								if (ev != null) {
					//									ev.setSelectedPaint(reverseSelectedEdgeColor);
					//								}
					//							}
					//						} else {
					//							// For network, do nothing.
					//						}
					//					}
					if (Cytoscape.getCurrentNetworkView() != Cytoscape.getNullNetworkView()) {
						Cytoscape.getCurrentNetworkView().updateView();
					}
				}
			});
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getSortedColumnIndex() {
		return sortedColumnIndex;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean isSortedColumnAscending() {
		return sortedColumnAscending;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param event DOCUMENT ME!
	 */
	public void mouseReleased(MouseEvent event) {
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param event DOCUMENT ME!
	 */
	public void mousePressed(MouseEvent event) {
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param event DOCUMENT ME!
	 */
	public void mouseClicked(MouseEvent event) {
		int cursorType = getTableHeader().getCursor().getType();

		if ((event.getButton() == MouseEvent.BUTTON1) && (cursorType != Cursor.E_RESIZE_CURSOR)
		    && (cursorType != Cursor.W_RESIZE_CURSOR)) {
			final int index = getColumnModel().getColumnIndexAtX(event.getX());

			if (index >= 0) {
				final int modelIndex = getColumnModel().getColumn(index).getModelIndex();

				final SortTableModel model = (SortTableModel) getModel();

				if (model.isSortable(modelIndex)) {
					// toggle ascension, if already sorted
					if (sortedColumnIndex == index) {
						sortedColumnAscending = !sortedColumnAscending;
					}

					sortedColumnIndex = index;

					model.sortColumn(modelIndex, sortedColumnAscending);
				}
			}
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param event DOCUMENT ME!
	 */
	public void mouseEntered(MouseEvent event) {
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param event DOCUMENT ME!
	 */
	public void mouseExited(MouseEvent event) {
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param event DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		if (event.getActionCommand().compareTo("Copy") == 0) {
			System.out.println("Cells copied to clipboard.");
			copyToClipBoard();
		}
	}

	// Display elements in te list & map objects
	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void showListContents(MouseEvent e) {
		int column = 0;
		int row = 0;
		row = this.getSelectedRow();
		column = this.getSelectedColumn();

		DataTableModel model = (DataTableModel) tableModel;

		// List tester = new ArrayList();
		Class tester = model.getObjectTypeAt(this.getColumnName(column));

		Object value = model.getValueAt(row, column);

		if ((tester != null) && tester.equals(List.class)) {
			int idCol = 0;

			for (int i = 0; i < this.getColumnCount(); i++) {
				if (this.getColumnName(i).equals(AttributeBrowser.ID)) {
					idCol = i;

					break;
				}
			}

			String idField = (String) this.getValueAt(row, idCol);

			List contents = (List) model.getAttributeValue(CyAttributes.TYPE_SIMPLE_LIST, idField,
			                                               this.getColumnName(column));
			cellMenu = new JPopupMenu();

			Object[] listItems = contents.toArray();

			if (listItems.length != 0) {
				getCellContentView(CyAttributes.TYPE_SIMPLE_LIST, listItems, idField, e);
			}
		} else if ((value != null) && (value instanceof Map)
		           && model.getValueAt(row, 0).equals(AttributeBrowser.NETWORK_METADATA)) {
			NetworkMetaDataDialog mdd = new NetworkMetaDataDialog(Cytoscape.getDesktop(), false,
			                                                      Cytoscape.getCurrentNetwork());
			mdd.setLocationRelativeTo(Cytoscape.getDesktop());
			mdd.setVisible(true);
		} else if ((tester != null) && tester.equals(Map.class)) {
			int idCol = 0;

			for (int i = 0; i < this.getColumnCount(); i++) {
				if (this.getColumnName(i).equals(AttributeBrowser.ID)) {
					idCol = i;

					break;
				}
			}

			String idField = (String) this.getValueAt(row, idCol);

			Map<String, Object> contents = (Map) model.getAttributeValue(CyAttributes.TYPE_SIMPLE_MAP,
			                                                             idField,
			                                                             this.getColumnName(column));

			if ((contents != null) && (contents.size() != 0)) {
				Object[] listItems = new Object[contents.size()];
				Object[] keySet = contents.keySet().toArray();

				for (int i = 0; i < contents.keySet().size(); i++) {
					// System.out.println("Key = " + key + ", Val = " +
					// contents.get(key));
					listItems[i] = keySet[i] + " = " + contents.get(keySet[i]);
				}

				cellMenu = new JPopupMenu();
				getCellContentView(CyAttributes.TYPE_SIMPLE_MAP, listItems, idField, e);
			}
		}
	}

	private void getCellContentView(Byte type, final Object[] listItems, String idField,
	                                MouseEvent e) {
		JMenu curItem = null;

		String dispName;

		for (final Object item : listItems) {
			dispName = item.toString();

			if (dispName.length() > 60) {
				dispName = dispName.substring(0, 59) + " ...";
			}

			curItem = new JMenu(dispName);
			curItem.setBackground(Color.white);
			curItem.add(getPopupMenu());

			JMenuItem openURL = new JMenuItem("Open as hyper link...");

			openURL.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						System.out.println(item.toString());
						OpenBrowser.openURL(item.toString());
					}
				});
			curItem.add(openURL);

			JMenuItem copyAll = new JMenuItem("Copy all");
			copyAll.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						StringBuilder builder = new StringBuilder();

						for (Object oneEntry : listItems) {
							builder.append(oneEntry.toString() + "\t");
						}

						stsel = new StringSelection(builder.toString());
						systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						systemClipboard.setContents(stsel, stsel);
					}
				});
			curItem.add(copyAll);

			JMenuItem copy = new JMenuItem("Copy one entry");
			copy.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						stsel = new StringSelection(item.toString());
						systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						systemClipboard.setContents(stsel, stsel);
					}
				});
			curItem.add(copy);

			//curItem.add(new JMenuItem("Edit"));
			//			openURL.setEnabled(false);
			if (item.toString().startsWith("http://")) {
				curItem.getMenuComponent(1).setEnabled(true);
			} else
				curItem.getMenuComponent(1).setEnabled(false);

			curItem.getMenuComponent(2).setEnabled(true);

			if (type == CyAttributes.TYPE_SIMPLE_LIST) {
				curItem.add(new HyperLinkOut(item.toString(), linkoutProps));
			} else {
				curItem.add(new HyperLinkOut(item.toString().split("=")[1], linkoutProps));
			}

			cellMenu.add(curItem);
		}

		final Border popupBorder = BorderFactory.createTitledBorder(null, idField,
		                                                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
		                                                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
		                                                            BORDER_FONT, Color.BLUE);
		cellMenu.setBorder(popupBorder);
		cellMenu.setBackground(Color.WHITE);
		cellMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	private String copyToClipBoard() {
		final StringBuffer sbf = new StringBuffer();

		/*
		 * Check to ensure we have selected only a contiguous block of cells.
		 */
		final int numcols = this.getSelectedColumnCount();
		final int numrows = this.getSelectedRowCount();

		final int[] rowsselected = this.getSelectedRows();
		final int[] colsselected = this.getSelectedColumns();

		// Return if no cell is selected.
		if ((numcols == 0) && (numrows == 0)) {
			return null;
		}

		if (!((((numrows - 1) == (rowsselected[rowsselected.length - 1] - rowsselected[0]))
		      && (numrows == rowsselected.length))
		    && (((numcols - 1) == (colsselected[colsselected.length - 1] - colsselected[0]))
		       && (numcols == colsselected.length)))) {
			JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Invalid Copy Selection",
			                              "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);

			return null;
		}

		Object tempCell = null;

		for (int i = 0; i < numrows; i++) {
			for (int j = 0; j < numcols; j++) {
				tempCell = this.getValueAt(rowsselected[i], colsselected[j]);

				sbf.append(tempCell);

				if (j < (numcols - 1))
					sbf.append("\t");
			}

			sbf.append(LS);
		}

		stsel = new StringSelection(sbf.toString());
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		systemClipboard.setContents(stsel, stsel);

		return sbf.toString();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(RESTORE_COLUMN)) {
			ColumnResizer.adjustColumnPreferredWidths(this);
		}

		if (e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
			Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(this);
		}

		if (e.getPropertyName().equals(Cytoscape.NETWORK_CREATED)
		    || e.getPropertyName().equals(Cytoscape.NETWORK_DESTROYED)) {
			tableModel.setTableData();
		}

		if ((e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_FOCUS)
		    || e.getPropertyName().equals(Cytoscape.SESSION_LOADED)
		    || e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
			if (currentNetwork != null) {
				currentNetwork.removeSelectEventListener(this);
			}

			// Change the target network
			currentNetwork = Cytoscape.getCurrentNetwork();

			if (currentNetwork != null) {
				currentNetwork.addSelectEventListener(this);

				if (objectType == NODES) {
					tableModel.setTableData(new ArrayList(Cytoscape.getCurrentNetwork()
					                                                      .getSelectedNodes()), null);
				} else if (objectType == EDGES) {
					tableModel.setTableData(new ArrayList(Cytoscape.getCurrentNetwork()
					                                                      .getSelectedEdges()), null);
				} else {
					// Network Attribute
					tableModel.setTableData(null, null);
				}
			}

			setSelectedColor(SELECTED_NODE);
			setSelectedColor(REV_SELECTED_NODE);
			setSelectedColor(SELECTED_EDGE);
			setSelectedColor(REV_SELECTED_EDGE);
		}

		// If initialized, check linkout props.
		if (e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
			// First, load existing property
			Properties props = CytoscapeInit.getProperties();

			// Use reflection to get resource 
			Class linkout = null;

			try {
				linkout = Class.forName("linkout.LinkOut");
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();

				return;
			}

			final ClassLoader cl = linkout.getClassLoader();

			try {
				props.load(cl.getResource("linkout.props").openStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			linkoutProps = new HashMap<String, Map<String, String>>();

			final String nodeLink = "nodelinkouturl";

			String[] parts = null;

			for (Entry<Object, Object> entry : props.entrySet()) {
				Map<String, String> pair = null;

				if (entry.getKey().toString().startsWith(nodeLink)) {
					parts = entry.getKey().toString().split("\\.");

					if (parts.length == 3) {
						pair = linkoutProps.get(parts[1]);

						if (pair == null) {
							pair = new HashMap<String, String>();
							linkoutProps.put(parts[1], pair);
						}

						pair.put(parts[2], entry.getValue().toString());
					}
				}
			}
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param arg0 DOCUMENT ME!
	 */
	public void onSelectEvent(SelectEvent event) {

		if ((objectType == NODES)
		    && ((event.getTargetType() == SelectEvent.SINGLE_NODE)
		       || (event.getTargetType() == SelectEvent.NODE_SET))) {
			// node selection
			tableModel.setSelectedColor(CyAttributeBrowserTable.SELECTED_NODE);
			tableModel.setSelectedColor(CyAttributeBrowserTable.REV_SELECTED_NODE);

			tableModel.setTableData(new ArrayList<GraphObject>(Cytoscape.getCurrentNetwork()
			                                                                   .getSelectedNodes()), null);
		} else if ((objectType == EDGES)
		           && ((event.getTargetType() == SelectEvent.SINGLE_EDGE)
		              || (event.getTargetType() == SelectEvent.EDGE_SET))) {
			// edge selection
			tableModel.setSelectedColor(CyAttributeBrowserTable.SELECTED_EDGE);
			tableModel.setSelectedColor(CyAttributeBrowserTable.REV_SELECTED_EDGE);
			tableModel.setTableData(new ArrayList<GraphObject>(Cytoscape.getCurrentNetwork()
			                                                                   .getSelectedEdges()), null);

		}

		ColumnResizer.adjustColumnPreferredWidths(this);

		setSelectedColor(SELECTED_NODE);
		setSelectedColor(REV_SELECTED_NODE);
		setSelectedColor(SELECTED_EDGE);
		setSelectedColor(REV_SELECTED_EDGE);

		if (Cytoscape.getCurrentNetworkView() != Cytoscape.getNullNetworkView()) {
			Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void editingStopped(ChangeEvent e) {
		super.editingStopped(e);
		Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
	}
}


/**
 *
 * Cell renderer for preview table.<br>
 * Coloring function is added. This will sync node color and cell colors.<br>
 *
 * @version 0.6
 * @since Cytoscape 2.3
 *
 * @author kono
 *
 */
class BrowserTableCellRenderer extends JLabel implements TableCellRenderer {
	private static final String HTML_BEG = "<html><body topmargin=\"5\" leftmargin=\"0\" marginheight=\"5\" marginwidth=\"5\" "
	                                       + "bgcolor=\"#ffffff\" text=\"#595959\" link=\"#0000ff\" vlink=\"#800080\" alink=\"#ff0000\">";
	private static final String HTML_STYLE = "<div style=\"width: 200px; background-color: #ffffff; padding: 3px;\"> ";

	// Define fonts & colors for the cells
	private Font labelFont = new Font("Sans-serif", Font.BOLD, 12);
	private Font normalFont = new Font("Sans-serif", Font.PLAIN, 12);
	private final Color metadataBackground = new Color(255, 210, 255);
	private static final Color NON_EDITABLE_COLOR = new Color(235, 235, 235, 100);
	private static final Color SELECTED_CELL_COLOR = new Color(0, 100, 255, 40);
	private static final Color SELECTED_LABEL_COLOR = Color.black.brighter();
	private DataObjectType type = DataObjectType.NODES;
	private boolean coloring;

	/**
	 * Creates a new BrowserTableCellRenderer object.
	 *
	 * @param coloring  DOCUMENT ME!
	 * @param type  DOCUMENT ME!
	 */
	public BrowserTableCellRenderer(boolean coloring, DataObjectType type) {
		super();
		this.type = type;
		this.coloring = coloring;

		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param table DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param isSelected DOCUMENT ME!
	 * @param hasFocus DOCUMENT ME!
	 * @param row DOCUMENT ME!
	 * @param column DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	                                               boolean hasFocus, int row, int column) {
		final String colName = table.getColumnName(column);

		// First, set values
		setHorizontalAlignment(JLabel.LEFT);
		setText((value == null) ? "" : value.toString());

		if (value != null) {
			// Set HTML style tooltip
			setToolTipText(getFormattedToolTipText(colName, value));
		}

		// If selected, return
		if (isSelected) {
			setFont(labelFont);
			setForeground(SELECTED_LABEL_COLOR);
			setBackground(SELECTED_CELL_COLOR);

			return this;
		}

		// set default colorings
		setForeground(table.getForeground());
		setFont(normalFont);
		setBackground(table.getBackground());

		final CyAttributes data = type.getAssociatedAttribute();

		if (data == null)
			return this;

		// check for non-editable columns
		if (data.getUserEditable(colName) == false) {
			setBackground(NON_EDITABLE_COLOR);
		}

		// If ID, return default.
		if (colName.equals(AttributeBrowser.ID)) {
			setFont(labelFont);
			setBackground(NON_EDITABLE_COLOR);
		}

		// handle special NETWORK coloring
		if ((type == NETWORK) && (value != null)) {
			if (colName.equals("Network Attribute Name") && !value.equals("Network Metadata")) {
				setFont(labelFont);
				setBackground(NON_EDITABLE_COLOR);
			} else if (value.equals("Network Metadata")) {
				setBackground(metadataBackground);
				setFont(labelFont);
			}
		}

		// if we're not coloring the ID column we're done
		if ((coloring == false) || !colName.equals(AttributeBrowser.ID))
			return this;

		// handle colors for the the ID column
		CyNetworkView netview = Cytoscape.getCurrentNetworkView();

		if (type == NODES) {
			if (netview != Cytoscape.getNullNetworkView()) {
				NodeView nodeView = netview.getNodeView(Cytoscape.getCyNode((String) table
				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          .getValueAt(row,
				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      column)));

				if (nodeView != null) {
					Color nodeColor = (Color) nodeView.getUnselectedPaint();
					setBackground(nodeColor);
				}
			}
		} else if (type == EDGES) {
			if (netview != Cytoscape.getNullNetworkView()) {
				final String edgeName = (String) table.getValueAt(row, column);
				final EdgeView edgeView = netview.getEdgeView(((CyAttributeBrowserTable) table)
				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      .getEdge(edgeName));

				if (edgeView != null) {
					Color edgeColor = (Color) edgeView.getUnselectedPaint();
					setBackground(edgeColor);
				}
			}
		}

		return this;
	}

	/**
	 * Returns organized & readable tooltip text.
	 * @param value
	 * @return
	 */
	private String getFormattedToolTipText(final String colName, final Object value) {
		StringBuilder html = new StringBuilder();

		html.append(HTML_BEG + "<strong text=\"#4169E1\" >" + colName + "</strong><br><hr>"
		            + HTML_STYLE);

		if (value instanceof List == false && value instanceof Map == false ) {
			html.append(value.toString());
		} else {
			html.append("<ul leftmargin=\"0\">");

			for (Object item : (List<Object>) value) {
				html.append("<li type=\"square\">" + item.toString() + "</li>");
			}

			html.append("</ul>");
		}

		html.append("</div></body></html>");

		return html.toString();
	}
}
