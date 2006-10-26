package browser;

import giny.model.GraphObject;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.SwingPropertyChangeSupport;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.actions.ImportEdgeAttributesAction;
import cytoscape.actions.ImportExpressionMatrixAction;
import cytoscape.actions.ImportNodeAttributesAction;
import cytoscape.actions.MapOntologyAction;
import cytoscape.data.CyAttributes;
import cytoscape.dialogs.NetworkMetaDataDialog;

/**
 * Define toolbar for Attribute Browser.
 * 
 * @version 0.8
 * @since Cytoscape 2.3
 * @author kono
 * 
 */
public class AttributeBrowserPanel extends JPanel implements
		PropertyChangeListener, ListSelectionListener, ListDataListener,
		ActionListener {

	// Global Variables
	CyAttributes data;
	DataTableModel tableModel;

	// create new attribute
	JTextField newAttField;
	JButton newAttButton;
	JComboBox newAttType;

	// tags
	JList tagList;
	JButton addToTag;
	JButton removeFromTag;
	JTextField newTag;
	JButton newTagButton;

	protected SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(
			this);

	private JPopupMenu attributeSelectionPopupMenu = null;

	private JScrollPane jScrollPane = null;

	private JPopupMenu jPopupMenu1 = null;
	private JMenuItem jMenuItem = null;
	private JMenuItem jMenuItem1 = null;
	private JMenuItem jMenuItem2 = null;
	private JMenuItem jMenuItem3 = null;
	private JToolBar jToolBar = null;
	private JButton selectButton = null;

	private JList attributeList = null;
	private JList attrDeletionList = null;

	private JButton createNewButton = null;
	private JButton deleteButton = null;

	private JButton goButton = null;
	private JButton matrixButton = null;
	private JButton importButton = null;

	private int objectType;
	private AttributeModel model;

	private String attributeType = null;

	private JCheckBox networkAttrView = null;

	public AttributeBrowserPanel() {
		super();

		// TODO Auto-generated constructor stub
		initialize(null);
	}

	public AttributeBrowserPanel(CyAttributes data, AttributeModel a_model,
			LabelModel l_model, int got) {
		this.data = data;
		this.objectType = got;
		this.model = a_model;

		initialize(a_model);
	}

	/**
	 * This method initializes
	 * 
	 * @return void
	 */
	private void initialize(AttributeModel a_model) {

		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);

		this.setPreferredSize(new java.awt.Dimension(210, 46));
		this.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		this.add(getJToolBar(), java.awt.BorderLayout.CENTER);

		getAttributeSelectionPopupMenu(a_model);
		getJPopupMenu1();

		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(
				this);
	}

	public String getSelectedAttribute() {
		return attributeList.getSelectedValue().toString();
	}

	public String getToBeDeletedAttribute() {
		return attrDeletionList.getSelectedValue().toString();
	}

	public void setTableModel(DataTableModel tableModel) {
		this.tableModel = tableModel;
	}

	public void valueChanged(ListSelectionEvent e) {

		try {
			Object[] atts = attributeList.getSelectedValues();
			tableModel.setTableDataAttributes(Arrays.asList(atts));

		} catch (Exception ex) {
			attributeList.clearSelection();
		}
	}

	public void contentsChanged(ListDataEvent e) {

	}

	public void intervalAdded(ListDataEvent e) {
		// handleEvent(e);

	}

	public void intervalRemoved(ListDataEvent e) {
		// handleEvent(e);

	}

	public void propertyChange(PropertyChangeEvent e) {
		// updateLists();

		if ((e.getPropertyName().equals(Cytoscape.NETWORK_LOADED)
				|| e.getPropertyName().equals(Cytoscape.NETWORK_DESTROYED) || e
				.getPropertyName().equals(Cytoscape.DATASERVER_CHANGED))
				&& objectType == DataTable.NODES) {
			if (Cytoscape.getBioDataServer() == null) {
				goButton.setEnabled(false);
			} else if (Cytoscape.getBioDataServer().getAnnotationCount() == 0
					|| Cytoscape.getNetworkSet().size() == 0) {
				goButton.setEnabled(false);
			} else {
				goButton.setEnabled(true);
			}
		}

	}

	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * This method initializes jPopupMenu
	 * 
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getAttributeSelectionPopupMenu(AttributeModel a_model) {
		if (attributeSelectionPopupMenu == null) {
			attributeSelectionPopupMenu = new JPopupMenu();
			attributeSelectionPopupMenu.add(getJScrollPane(model));
		}
		return attributeSelectionPopupMenu;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane(AttributeModel a_model) {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new Dimension(220, 200));
			jScrollPane.setViewportView(getJList1(model));
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jPopupMenu1
	 * 
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getJPopupMenu1() {
		if (jPopupMenu1 == null) {
			jPopupMenu1 = new JPopupMenu();
			jPopupMenu1.add(getJMenuItem1());
			jPopupMenu1.add(getJMenuItem());
			jPopupMenu1.add(getJMenuItem2());
			jPopupMenu1.add(getJMenuItem3());
		}
		return jPopupMenu1;
	}

	/**
	 * This method initializes jMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem() {
		if (jMenuItem == null) {
			jMenuItem = new JMenuItem();
			jMenuItem.setText("String Attribute");
			jMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {

					// Auto-generated
					// Event stub
					// actionPerformed()
					createNewAttribute("String");

				}
			});
		}
		return jMenuItem;
	}

	/**
	 * This method initializes jMenuItem1
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem1() {
		if (jMenuItem1 == null) {
			jMenuItem1 = new JMenuItem();
			jMenuItem1.setText("Integer Attribute");
			jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// TODO Auto-generated Event stub actionPerformed()

					createNewAttribute("Integer");
				}
			});
		}
		return jMenuItem1;
	}

	/**
	 * This method initializes jMenuItem2
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem2() {
		if (jMenuItem2 == null) {
			jMenuItem2 = new JMenuItem();
			jMenuItem2.setText("Floating Point Attribute");
			jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {

					// Auto-generated
					// Event stub
					// actionPerformed()
					createNewAttribute("Floating Point");
				}
			});
		}
		return jMenuItem2;
	}

	/**
	 * This method initializes jMenuItem3
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getJMenuItem3() {
		if (jMenuItem3 == null) {
			jMenuItem3 = new JMenuItem();
			jMenuItem3.setText("Boolean Attribute");
			jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {

					// Auto-generated
					// Event stub
					// actionPerformed()
					createNewAttribute("Boolean");
				}
			});
		}
		return jMenuItem3;
	}

	/**
	 * This method initializes jToolBar
	 * 
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getJToolBar() {
		if (jToolBar == null) {

			jToolBar = new JToolBar();
			jToolBar.setPreferredSize(new java.awt.Dimension(200, 55));
			jToolBar.setFloatable(false);
			jToolBar.setOrientation(JToolBar.HORIZONTAL);

			GroupLayout buttonBarLayout = new GroupLayout(jToolBar);
			jToolBar.setLayout(buttonBarLayout);

			// Layout information.
			if (objectType == DataTable.NODES) {
				buttonBarLayout.setHorizontalGroup(buttonBarLayout
						.createParallelGroup(GroupLayout.LEADING).add(
								buttonBarLayout.createSequentialGroup().add(
										getSelectButton(),
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(getNewButton()).addPreferredGap(
												LayoutStyle.RELATED).add(
												getDeleteButton())
										.addPreferredGap(LayoutStyle.RELATED,
												350, Short.MAX_VALUE).add(
														getAttrModButton(),
														GroupLayout.PREFERRED_SIZE, 40,
														GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(LayoutStyle.RELATED).add(
												getImportButton(),
												GroupLayout.PREFERRED_SIZE, 40,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(getGOButton(),
												GroupLayout.PREFERRED_SIZE, 40,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(getMatrixButton())));
				buttonBarLayout
						.setVerticalGroup(buttonBarLayout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.LEADING)
								.add(
										org.jdesktop.layout.GroupLayout.CENTER,
										selectButton,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										37,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(
										org.jdesktop.layout.GroupLayout.CENTER,
										createNewButton,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										37, Short.MAX_VALUE)
								.add(
										org.jdesktop.layout.GroupLayout.CENTER,
										deleteButton,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										37, Short.MAX_VALUE)

								.add(
										buttonBarLayout
												.createParallelGroup(
														org.jdesktop.layout.GroupLayout.CENTER)
												.add(
														matrixButton,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
														36,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
												.add(
														importButton,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
														36,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
												.add(
														attrModButton,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
														36,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
												.add(
														goButton,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
														36,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

			} else if (objectType == DataTable.NETWORK) {
				buttonBarLayout.setHorizontalGroup(buttonBarLayout
						.createParallelGroup(GroupLayout.LEADING).add(
								buttonBarLayout.createSequentialGroup().add(
										getSelectButton(),
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(getNewButton()).addPreferredGap(
												LayoutStyle.RELATED).add(
												getDeleteButton())
										.addPreferredGap(LayoutStyle.RELATED,
												320, Short.MAX_VALUE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(getAttrModButton(),
												GroupLayout.PREFERRED_SIZE, 40,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(getImportButton(),
												GroupLayout.PREFERRED_SIZE, 40,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)));

				buttonBarLayout
						.setVerticalGroup(buttonBarLayout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.LEADING)
								.add(
										org.jdesktop.layout.GroupLayout.CENTER,
										selectButton,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										37,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(
										org.jdesktop.layout.GroupLayout.CENTER,
										createNewButton,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										37, Short.MAX_VALUE)
								.add(
										org.jdesktop.layout.GroupLayout.CENTER,
										deleteButton,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										37, Short.MAX_VALUE)

								.add(
										buttonBarLayout
												.createParallelGroup(
														org.jdesktop.layout.GroupLayout.CENTER)
												.add(
														attrModButton,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
														36,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
												.add(
														importButton,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
														36,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
			} else {
				// Layout for edges
				buttonBarLayout.setHorizontalGroup(buttonBarLayout
						.createParallelGroup(GroupLayout.LEADING).add(
								buttonBarLayout.createSequentialGroup().add(
										getSelectButton(),
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(getNewButton()).addPreferredGap(
												LayoutStyle.RELATED).add(
												getDeleteButton())
										.addPreferredGap(LayoutStyle.RELATED,
												350, Short.MAX_VALUE).add(
												getAttrModButton(),
												GroupLayout.PREFERRED_SIZE, 40,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(
												getImportButton(),
												GroupLayout.PREFERRED_SIZE, 40,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.RELATED)));
				buttonBarLayout
						.setVerticalGroup(buttonBarLayout
								.createParallelGroup(
										org.jdesktop.layout.GroupLayout.LEADING)
								.add(
										org.jdesktop.layout.GroupLayout.CENTER,
										selectButton,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
										37,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
								.add(
										org.jdesktop.layout.GroupLayout.CENTER,
										createNewButton,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										37, Short.MAX_VALUE)
								.add(
										org.jdesktop.layout.GroupLayout.CENTER,
										deleteButton,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										37, Short.MAX_VALUE)

								.add(
										buttonBarLayout
												.createParallelGroup(
														org.jdesktop.layout.GroupLayout.CENTER)
												.add(
														attrModButton,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
														36,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
												.add(
														importButton,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
														36,
														org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
			}

		}
		return jToolBar;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JButton();

			selectButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

			selectButton.setIcon(new javax.swing.ImageIcon(getClass()
					.getResource("images/stock_select-row.png")));
			selectButton.setToolTipText("Select Attributes");
			selectButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					// TODO Auto-generated Event stub mouseClicked()

					attributeSelectionPopupMenu.show(e.getComponent(),
							e.getX(), e.getY());
				}
			});
		}
		return selectButton;
	}

	private JButton getImportButton() {
		if (importButton == null) {
			importButton = new JButton();
			importButton.setIcon(new javax.swing.ImageIcon(getClass()
					.getResource("images/stock_open.png")));
			importButton.setToolTipText("Import attributes from file...");
			importButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
			importButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					// TODO Auto-generated Event stub mouseClicked()
					importAttributes();
					// jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			});
		}
		return importButton;

	}

	private JCheckBox getnetworkAttrView() {
		if (networkAttrView == null) {
			networkAttrView = new JCheckBox();
			networkAttrView.setText("All Networks");
			networkAttrView
					.setToolTipText("Switch the table format: single network or all");
			networkAttrView.setEnabled(false);
			networkAttrView.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					// changeNetworkAttrView();
				}
			});
		}
		return networkAttrView;

	}
	
	private JButton getGOButton() {
		if (goButton == null) {
			goButton = new JButton();
			goButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
					"images/stock_data-linked-table.png")));
			goButton.setToolTipText("Map Ontology...");
			goButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
			goButton.setMaximumSize(new java.awt.Dimension(40, 40));
			goButton.setMinimumSize(new java.awt.Dimension(40, 40));
			goButton.setEnabled(false);
			goButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					// TODO Auto-generated Event stub mouseClicked()
					if (goButton.isEnabled() == true) {
						mapOntology();
					}

					// jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			});
		}
		return goButton;
	}

	private JButton getMatrixButton() {
		if (matrixButton == null) {
			matrixButton = new JButton();
			matrixButton.setIcon(new javax.swing.ImageIcon(getClass()
					.getResource("images/microarray_24.png")));
			matrixButton.setToolTipText("Import Expression Matrix Data...");
			matrixButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

			matrixButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					// TODO Auto-generated Event stub mouseClicked()
					importMatrix();
					// jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			});
		}
		return matrixButton;

	}

	private JButton attrModButton= null;
	private AttrSelectModPanel attrModPanel = null;
	private String tableObjectType = "";
	
	public void setAttrModPane(AttrSelectModPanel pPanel, String pTableObjetType) {
		attrModPanel = pPanel;
		tableObjectType = pTableObjetType;
	}

	private JButton getAttrModButton() {
		if (attrModButton == null) {
			attrModButton = new JButton();
			attrModButton.setIcon(new javax.swing.ImageIcon(getClass()
					.getResource("images/stock_insert-columns.png")));
			attrModButton.setToolTipText("Attribute Modification");
			attrModButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

			attrModButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					showAttrModDialog();
				}
			});
		}
		return attrModButton;
	}
	
	private void showAttrModDialog() {

		final JDialog attModDialog = new JDialog();

		attModDialog.setTitle(tableObjectType + " Attritube Modification");
		attModDialog.setModal(true);
				
		attModDialog.getContentPane().add(attrModPanel);				
		attModDialog.pack();
		attModDialog.setVisible(true);
	} // showAttrModDialog()
	
	
	protected void editMetadata() {
		NetworkMetaDataDialog mdd = new NetworkMetaDataDialog(Cytoscape
				.getDesktop(), false, Cytoscape.getCurrentNetwork());
		mdd.setVisible(true);
	}

	protected void importAttributes() {

		Object cytoPanelObject = Cytoscape.getDesktop().getCytoPanel(
				SwingConstants.SOUTH).getSelectedComponent();
		TitledBorder tb = (TitledBorder) ((JPanel) cytoPanelObject).getBorder();
		attributeType = tb.getTitle();
		String[] titleArray = attributeType.split(" ");
		attributeType = titleArray[0];

		if (attributeType.equalsIgnoreCase("node")) {
			ImportNodeAttributesAction nodeAction = new ImportNodeAttributesAction();
			nodeAction.actionPerformed(null);
		} else if (attributeType.equalsIgnoreCase("Edge")){
			ImportEdgeAttributesAction edgeAction = new ImportEdgeAttributesAction();
			edgeAction.actionPerformed(null);
		}
		else { // case for Network
			System.out.println("Network Attribute import not implemented yet");
		}

	}

	protected void importMatrix() {
		ImportExpressionMatrixAction matrixAction = new ImportExpressionMatrixAction();
		matrixAction.actionPerformed(null);
	}

	protected void mapOntology() {
		MapOntologyAction moa = new MapOntologyAction();
		moa.actionPerformed(null);
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton();
			deleteButton.setIcon(new javax.swing.ImageIcon(getClass()
					.getResource("images/stock_delete.png")));
			deleteButton.setToolTipText("Delete Attributes...");
			// Create pop-up window for deletion
			deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					// TODO Auto-generated Event stub mouseClicked()

					Object cytoPanelObject = Cytoscape.getDesktop()
							.getCytoPanel(SwingConstants.SOUTH)
							.getSelectedComponent();
					TitledBorder tb = (TitledBorder) ((JPanel) cytoPanelObject)
							.getBorder();
					attributeType = tb.getTitle();
					String[] titleArray = attributeType.split(" ");
					attributeType = titleArray[0];

					DeletionDialog dDialog = new DeletionDialog(Cytoscape
							.getDesktop(), true,
							getAttributeArray(attributeType), attributeType);

					dDialog.pack();
					dDialog.setLocationRelativeTo(Cytoscape.getDesktop());
					dDialog.setVisible(true);
					model.sortAtttributes();
					valueChanged(null);
					// getAttributeSelectionPopupMenu(model);

				}
			});
		}
		return deleteButton;
	}

	/**
	 * This method initializes jList1
	 * 
	 * @return javax.swing.JList
	 */
	private JList getJList1(AttributeModel a_model) {
		if (attributeList == null) {
			attributeList = new JList(model);
			attributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			attributeList.addMouseListener(new MouseAdapter() {

				ArrayList indices = new ArrayList();
				public void mouseClicked(java.awt.event.MouseEvent e) {

					if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
						// Right click
						attributeSelectionPopupMenu.setVisible(false);
					} else {

						// Left click
						model.sortAtttributes();

						// Get current selected node
						Integer indexObj = new Integer(attributeList.locationToIndex(e.getPoint()));

						// is this selected? if so remove it.
						if (indices.contains(indexObj)) {
							indices.remove(indexObj);
						} else {
							indices.add(indexObj);
						}
						
						// copy to an int array
						int[] arr = new int[indices.size()];
						for (int i = 0; i < indices.size(); i++) {
							arr[i] = ((Integer) indices.get(i)).intValue();
						}
						// set selected indices
						attributeList.setSelectedIndices(arr);
					}

				}
			});
			attributeList.addListSelectionListener(this);
			attributeList
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		}
		return attributeList;
	}

	private String[] getAttributeArray(String type) {

		CyAttributes currentAttributes = null;
		if (type.equalsIgnoreCase("node")) {
			currentAttributes = Cytoscape.getNodeAttributes();
		} else {
			currentAttributes = Cytoscape.getEdgeAttributes();
		}

		String[] currentAttributeNames = currentAttributes.getAttributeNames();
		return currentAttributeNames;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getNewButton() {
		if (createNewButton == null) {
			createNewButton = new JButton();
			// createNewButton.setText("New");
			createNewButton.setFont(new java.awt.Font("Dialog",
					java.awt.Font.PLAIN, 12));
			createNewButton
					.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			createNewButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
			createNewButton.setToolTipText("Create New Attribute");
			// createNewButton.setSize(new java.awt.Dimension(100, 27));
			createNewButton.setIcon(new javax.swing.ImageIcon(getClass()
					.getResource("images/stock_new.png")));
			createNewButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					// TODO Auto-generated Event stub mouseClicked()
					jPopupMenu1.show(e.getComponent(), e.getX(), e.getY());
				}
			});
		}
		return createNewButton;
	}

	// Create a whole new attribute and set a default value.
	//
	private void createNewAttribute(String type) {

		String[] existingAttrs = data.getAttributeNames();
		boolean dupFlag = true;

		String name = null;

		while (dupFlag == true) {
			name = JOptionPane.showInputDialog(this,
					"Please enter new attribute name: ", "Create New " + type
							+ " Attribute", JOptionPane.QUESTION_MESSAGE);

			if (existingAttrs.length == 0) {
				dupFlag = false;
				break;
			} else {
				for (int i = 0; i < existingAttrs.length; i++) {
					System.out.println("Checking duplication: "
							+ existingAttrs[i]);
					if (existingAttrs[i].equals(name) == false) {
						dupFlag = false;
					} else if (existingAttrs[i].equals(name)) {
						JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Attribute " + name
								+ " already exists.", "Error!",
								JOptionPane.ERROR_MESSAGE);
						dupFlag = true;
						break;
					}
				}
			}

		}

		if (name != null) {
			Object objects = null;

			Iterator i;

			if (objectType == DataTable.NODES) {
				objects = Cytoscape.getCyNodesList();
				i = ((List) objects).iterator();
			} else if (objectType == DataTable.EDGES) {
				objects = Cytoscape.getCyEdgesList();
				i = ((List) objects).iterator();
			} else {
				objects = Cytoscape.getNetworkSet();
				i = ((Set) objects).iterator();
			}

			if (objectType == DataTable.NETWORK) {
				CyNetwork network = (CyNetwork) i.next();
				if (type.equals("String")) {
					data.setAttribute(network.getIdentifier(), name,
							new String());
				} else if (type.equals("Floating Point")) {
					data.setAttribute(network.getIdentifier(), name,
							new Double(0));
				} else if (type.equals("Integer")) {
					data.setAttribute(network.getIdentifier(), name,
							new Integer(0));
				} else if (type.equals("Boolean")) {
					data.setAttribute(network.getIdentifier(), name,
							new Boolean(false));
				} else {
					data.setAttribute(network.getIdentifier(), name,
							new String());
				}
				data.deleteAttribute(network.getIdentifier(), name);
			} else {
				GraphObject go = (GraphObject) i.next();
				if (type.equals("String")) {
					data.setAttribute(go.getIdentifier(), name, new String());
				} else if (type.equals("Floating Point")) {
					data.setAttribute(go.getIdentifier(), name, new Double(0));
				} else if (type.equals("Integer")) {
					data.setAttribute(go.getIdentifier(), name, new Integer(0));
				} else if (type.equals("Boolean")) {
					data.setAttribute(go.getIdentifier(), name, new Boolean(
							false));
				} else {
					data.setAttribute(go.getIdentifier(), name, new String());
				}
				// This is a hack --- Remove the attribute right after its
				// creation.
				data.deleteAttribute(go.getIdentifier(), name);
			}
			updateSelectedListItems(name);
			
		}
	}
	
	private void updateSelectedListItems(Object newEntry) {
		int[] selected = attributeList.getSelectedIndices();
		
		//debug
		
		model.setSelectedItem(newEntry);
		int[] newSelected = new int[selected.length+1];
		System.arraycopy(selected, 0, newSelected, 0, selected.length);
		newSelected[selected.length] = attributeList.getSelectedIndex();
		attributeList.setSelectedIndices(newSelected);
		
	}

}
