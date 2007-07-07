/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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
package browser;

import cytoscape.Cytoscape;

import cytoscape.actions.ImportEdgeAttributesAction;
import cytoscape.actions.ImportExpressionMatrixAction;
import cytoscape.actions.ImportNodeAttributesAction;

import cytoscape.data.CyAttributes;
import cytoscape.data.CyAttributesUtils;

import cytoscape.dialogs.NetworkMetaDataDialog;

import cytoscape.util.swing.CheckBoxJList;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableColumnModelEvent;


/**
 * Define toolbar for Attribute Browser.
 *
 * @version 0.8
 * @since Cytoscape 2.3
 * @author kono
 *
 */
public class AttributeBrowserPanel extends JPanel implements PropertyChangeListener,
                                                             PopupMenuListener {
	private CyAttributes attributes;
	private DataTableModel tableModel;
	private int objectType;
	private AttributeModel model;
	private String attributeType = null;
	private Object[] selectedAttrNames = null;

	/*
	 * GUI components
	 */
	private JPopupMenu attributeSelectionPopupMenu = null;
	private JScrollPane jScrollPane = null;
	private JPopupMenu jPopupMenu1 = null;
	private JMenuItem jMenuItem = null;
	private JMenuItem jMenuItem1 = null;
	private JMenuItem jMenuItem2 = null;
	private JMenuItem jMenuItem3 = null;
	private JToolBar jToolBar = null;
	private JButton selectButton = null;
	private CheckBoxJList attributeList = null;
	private JList attrDeletionList = null;
	private JButton createNewAttributeButton = null;
	private JButton deleteAttributeButton = null;
	private JButton matrixButton = null;
	private JButton importButton = null;

	/**
	 * Constructor
	 *
	 */
	public AttributeBrowserPanel() {
		super();

		initialize(null);
	}

	/**
	 * Creates a new AttributeBrowserPanel object.
	 *
	 * @param data  DOCUMENT ME!
	 * @param a_model  DOCUMENT ME!
	 * @param l_model  DOCUMENT ME!
	 * @param graphObjectType  DOCUMENT ME!
	 */
	public AttributeBrowserPanel(final CyAttributes data, final AttributeModel a_model,
	                             final LabelModel l_model, final int graphObjectType) {
		this.attributes = data;
		this.objectType = graphObjectType;
		this.model = a_model;

		initialize(a_model);
	}

	/**
	 * This method initializes
	 *
	 * @return void
	 */
	private void initialize(final AttributeModel a_model) {
		this.setLayout(new BorderLayout());

		this.setPreferredSize(new java.awt.Dimension(210, 29));
		this.add(getJToolBar(), java.awt.BorderLayout.CENTER);

		getAttributeSelectionPopupMenu(a_model);
		getJPopupMenu1();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getSelectedAttribute() {
		return attributeList.getSelectedValue().toString();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getToBeDeletedAttribute() {
		return attrDeletionList.getSelectedValue().toString();
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param tableModel DOCUMENT ME!
	 */
	public void setTableModel(DataTableModel tableModel) {
		this.tableModel = tableModel;
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
			attributeSelectionPopupMenu.addPopupMenuListener(this);
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
			jScrollPane.setPreferredSize(new Dimension(300, 200));
			jScrollPane.setViewportView(getSelectedAttributeList(model));
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
			jToolBar.setMargin(new java.awt.Insets(0, 0, 0, 0));
			jToolBar.setPreferredSize(new java.awt.Dimension(200, 28));
			jToolBar.setFloatable(false);
			jToolBar.setOrientation(JToolBar.HORIZONTAL);

			final GroupLayout buttonBarLayout = new GroupLayout(jToolBar);
			jToolBar.setLayout(buttonBarLayout);

			// Layout information.
			if (objectType == DataTable.NODES) {
				buttonBarLayout.setHorizontalGroup(buttonBarLayout.createParallelGroup(GroupLayout.LEADING)
				                                                  .add(buttonBarLayout.createSequentialGroup()
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getSelectButton(),
				                                                                           GroupLayout.PREFERRED_SIZE,
				                                                                           GroupLayout.DEFAULT_SIZE,
				                                                                           GroupLayout.PREFERRED_SIZE)
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getNewButton())
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getDeleteButton())
				                                                                      .addPreferredGap(LayoutStyle.RELATED,
				                                                                                       150,
				                                                                                       Short.MAX_VALUE)
				                                                                      .add(getAttrModButton(),
				                                                                           GroupLayout.PREFERRED_SIZE,
				                                                                           28,
				                                                                           GroupLayout.PREFERRED_SIZE)
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getImportButton(),
				                                                                           GroupLayout.PREFERRED_SIZE,
				                                                                           28,
				                                                                           GroupLayout.PREFERRED_SIZE)
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getMatrixButton())
				                                                                      .addPreferredGap(LayoutStyle.RELATED)));
				buttonBarLayout.setVerticalGroup(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
				                                                     selectButton,
				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				                                                     27,
				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
				                                                     createNewAttributeButton,
				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				                                                     27, Short.MAX_VALUE)
				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
				                                                     deleteAttributeButton,
				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				                                                     27, Short.MAX_VALUE)
				                                                .add(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
				                                                                    .add(matrixButton,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				                                                                         27,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
				                                                                    .add(importButton,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				                                                                         27,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
				                                                                    .add(attrModButton,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				                                                                         27,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
			} else if (objectType == DataTable.NETWORK) {
				buttonBarLayout.setHorizontalGroup(buttonBarLayout.createParallelGroup(GroupLayout.LEADING)
				                                                  .add(buttonBarLayout.createSequentialGroup()
				                                                                      .add(getSelectButton(),
				                                                                           GroupLayout.PREFERRED_SIZE,
				                                                                           GroupLayout.DEFAULT_SIZE,
				                                                                           GroupLayout.PREFERRED_SIZE)
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getNewButton())
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getDeleteButton())
				                                                                      .addPreferredGap(LayoutStyle.RELATED,
				                                                                                       320,
				                                                                                       Short.MAX_VALUE)
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getAttrModButton(),
				                                                                           GroupLayout.PREFERRED_SIZE,
				                                                                           28,
				                                                                           GroupLayout.PREFERRED_SIZE)
				                                                                      .addPreferredGap(LayoutStyle.RELATED)));

				buttonBarLayout.setVerticalGroup(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
				                                                     selectButton,
				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				                                                     27,
				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
				                                                     createNewAttributeButton,
				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				                                                     27, Short.MAX_VALUE)
				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
				                                                     deleteAttributeButton,
				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				                                                     27, Short.MAX_VALUE)
				                                                .add(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
				                                                                    .add(attrModButton,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				                                                                         27,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
			} else {
				buttonBarLayout.setHorizontalGroup(buttonBarLayout.createParallelGroup(GroupLayout.LEADING)
				                                                  .add(buttonBarLayout.createSequentialGroup()
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getSelectButton(),
				                                                                           GroupLayout.PREFERRED_SIZE,
				                                                                           GroupLayout.DEFAULT_SIZE,
				                                                                           GroupLayout.PREFERRED_SIZE)
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getNewButton())
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getDeleteButton())
				                                                                      .addPreferredGap(LayoutStyle.RELATED,
				                                                                                       150,
				                                                                                       Short.MAX_VALUE)
				                                                                      .add(getAttrModButton(),
				                                                                           GroupLayout.PREFERRED_SIZE,
				                                                                           28,
				                                                                           GroupLayout.PREFERRED_SIZE)
				                                                                      .addPreferredGap(LayoutStyle.RELATED)
				                                                                      .add(getImportButton(),
				                                                                           GroupLayout.PREFERRED_SIZE,
				                                                                           28,
				                                                                           GroupLayout.PREFERRED_SIZE)
				                                                                      .addPreferredGap(LayoutStyle.RELATED)));
				buttonBarLayout.setVerticalGroup(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
				                                                     selectButton,
				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				                                                     27,
				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
				                                                     createNewAttributeButton,
				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				                                                     27, Short.MAX_VALUE)
				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
				                                                     deleteAttributeButton,
				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				                                                     27, Short.MAX_VALUE)
				                                                .add(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
				                                                                    .add(importButton,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				                                                                         27,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
				                                                                    .add(attrModButton,
				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				                                                                         27,
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
			selectButton.setBorder(null);
			selectButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			selectButton.setIcon(new javax.swing.ImageIcon(getClass()
			                                                   .getResource("images/stock_select-row.png")));
			selectButton.setToolTipText("Select Attributes");
			//selectButton.setComponentPopupMenu(attributeSelectionPopupMenu);
			selectButton.addMouseListener(new MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						attributeSelectionPopupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				});
		}

		return selectButton;
	}

	private JButton getImportButton() {
		if (importButton == null) {
			importButton = new JButton();
			importButton.setBorder(null);
			importButton.setIcon(new javax.swing.ImageIcon(getClass()
			                                                   .getResource("images/stock_open.png")));
			importButton.setToolTipText("Import attributes from file...");
			importButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
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

	private JButton getMatrixButton() {
		if (matrixButton == null) {
			matrixButton = new JButton();
			matrixButton.setBorder(null);
			matrixButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			matrixButton.setIcon(new javax.swing.ImageIcon(getClass()
			                                                   .getResource("images/microarray_24.png")));
			matrixButton.setToolTipText("Import Expression Matrix Data...");

			matrixButton.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						importMatrix();
					}
				});
		}

		return matrixButton;
	}

	private JButton attrModButton = null;
	private AttrSelectModPanel attrModPanel = null;
	private String tableObjectType = "";

	/**
	 *  DOCUMENT ME!
	 *
	 * @param pPanel DOCUMENT ME!
	 * @param pTableObjetType DOCUMENT ME!
	 */
	public void setAttrModPane(AttrSelectModPanel pPanel, String pTableObjetType) {
		attrModPanel = pPanel;
		tableObjectType = pTableObjetType;
	}

	private JButton getAttrModButton() {
		if (attrModButton == null) {
			attrModButton = new JButton();
			attrModButton.setBorder(null);
			attrModButton.setIcon(new javax.swing.ImageIcon(getClass()
			                                                    .getResource("images/stock_insert-columns.png")));
			attrModButton.setToolTipText("Attribute Modification");
			attrModButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

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

		attModDialog.setTitle(tableObjectType + " Attribute Modification");
		attModDialog.setModal(true);

		attModDialog.getContentPane().add(attrModPanel);
		attModDialog.pack();
		attModDialog.setVisible(true);
	} // showAttrModDialog()

	protected void editMetadata() {
		NetworkMetaDataDialog mdd = new NetworkMetaDataDialog(Cytoscape.getDesktop(), false,
		                                                      Cytoscape.getCurrentNetwork());
		mdd.setVisible(true);
	}

	protected void importAttributes() {
		if (objectType == DataTable.NODES) {
			ImportNodeAttributesAction nodeAction = new ImportNodeAttributesAction();
			nodeAction.actionPerformed(null);
		} else if (objectType == DataTable.EDGES) {
			ImportEdgeAttributesAction edgeAction = new ImportEdgeAttributesAction();
			edgeAction.actionPerformed(null);
		} else { // case for Network
			System.out.println("Network Attribute import not implemented yet");
		}
	}

	protected void importMatrix() {
		ImportExpressionMatrixAction matrixAction = new ImportExpressionMatrixAction();
		matrixAction.actionPerformed(null);
	}

	/**
	 * This method initializes jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteButton() {
		if (deleteAttributeButton == null) {
			deleteAttributeButton = new JButton();
			deleteAttributeButton.setBorder(null);
			deleteAttributeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			deleteAttributeButton.setIcon(new javax.swing.ImageIcon(getClass()
			                                                            .getResource("images/stock_delete.png")));
			deleteAttributeButton.setToolTipText("Delete Attributes...");

			// Create pop-up window for deletion
			deleteAttributeButton.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						final String[] attrArray = getAttributeArray();
						Arrays.sort(attrArray);

						final DeletionDialog dDialog = new DeletionDialog(Cytoscape.getDesktop(),
						                                                  true, attrArray,
						                                                  attributeType);

						dDialog.pack();
						dDialog.setLocationRelativeTo(jToolBar);
						dDialog.setVisible(true);
						model.sortAtttributes();
						propertyChange(null);
					}
				});
		}

		return deleteAttributeButton;
	}

	/**
	 * This method initializes jList1
	 *
	 * @return javax.swing.JList
	 */
	private JList getSelectedAttributeList(final AttributeModel a_model) {
		if (attributeList == null) {
			attributeList = new CheckBoxJList();
			attributeList.setModel(model);
			attributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			attributeList.addPropertyChangeListener(this);
			attributeList.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (SwingUtilities.isRightMouseButton(e)) {
							attributeSelectionPopupMenu.setVisible(false);
						}
					}
				});
		}

		return attributeList;
	}

	private String[] getAttributeArray() {
		final CyAttributes currentAttributes;

		if (objectType == DataTable.NODES) {
			attributeType = "Node";
			currentAttributes = Cytoscape.getNodeAttributes();
		} else if (objectType == DataTable.EDGES) {
			attributeType = "Edge";
			currentAttributes = Cytoscape.getEdgeAttributes();
		} else if (objectType == DataTable.NETWORK) {
			attributeType = "Network";
			currentAttributes = Cytoscape.getNetworkAttributes();
		} else {
			return new String[0];
		}

		return CyAttributesUtils.getVisibleAttributeNames(currentAttributes).toArray(new String[0]);
	}

	/**
	 * This method initializes jButton1
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getNewButton() {
		if (createNewAttributeButton == null) {
			createNewAttributeButton = new JButton();
			createNewAttributeButton.setBorder(null);

			createNewAttributeButton.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			createNewAttributeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			createNewAttributeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
			createNewAttributeButton.setToolTipText("Create New Attribute");
			createNewAttributeButton.setIcon(new javax.swing.ImageIcon(getClass()
			                                                               .getResource("images/stock_new.png")));
			createNewAttributeButton.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						jPopupMenu1.show(e.getComponent(), e.getX(), e.getY());
					}
				});
		}

		return createNewAttributeButton;
	}

	// Create a whole new attribute and set a default value.
	//
	private void createNewAttribute(final String type) {
		final String[] existingAttrs = CyAttributesUtils.getVisibleAttributeNames(attributes)
		                                                .toArray(new String[0]);
		boolean dupFlag = true;

		String name = null;

		while (dupFlag == true) {
			name = JOptionPane.showInputDialog(this, "Please enter new attribute name: ",
			                                   "Create New " + type + " Attribute",
			                                   JOptionPane.QUESTION_MESSAGE);

			if (existingAttrs.length == 0) {
				dupFlag = false;

				break;
			} else {
				for (int i = 0; i < existingAttrs.length; i++) {
					if (existingAttrs[i].equals(name) == false) {
						dupFlag = false;
					} else if (existingAttrs[i].equals(name)) {
						JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
						                              "Attribute " + name + " already exists.",
						                              "Error!", JOptionPane.ERROR_MESSAGE);
						dupFlag = true;

						break;
					}
				}
			}
		}

		if (name != null) {
			Object[] selectedVals = attributeList.getSelectedValues();

			final String testVal = "test";

			if (type.equals("String")) {
				attributes.setAttribute(testVal, name, new String());
			} else if (type.equals("Floating Point")) {
				attributes.setAttribute(testVal, name, new Double(0));
			} else if (type.equals("Integer")) {
				attributes.setAttribute(testVal, name, new Integer(0));
			} else if (type.equals("Boolean")) {
				attributes.setAttribute(testVal, name, new Boolean(false));
			} else {
				attributes.setAttribute(testVal, name, new String());
			}

			attributes.deleteAttribute(testVal, name);

			updateSelectedListItems(name, selectedVals);
		}
	}

	private void updateSelectedListItems(final Object newEntry, final Object[] selectedVals) {
		List<Object> selectedList = Arrays.asList(selectedVals);
		int[] selectedIndecies = new int[selectedVals.length + 1];

		Object listItem = null;
		int count = 0;

		for (int i = 0; i < attributeList.getModel().getSize(); i++) {
			listItem = attributeList.getModel().getElementAt(i);

			if (selectedList.contains(listItem) || listItem.equals(newEntry)) {
				selectedIndecies[count] = i;
				count++;
			}
		}

		attributeList.setSelectedIndices(selectedIndecies);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param evt DOCUMENT ME!
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ((evt == null) || evt.getPropertyName().equals(CheckBoxJList.LIST_UPDATED)) {
			selectedAttrNames = attributeList.getSelectedValues();
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void columnAdded(TableColumnModelEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void columnMarginChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void columnMoved(TableColumnModelEvent e) {
		// TODO Auto-generated method stub
		System.out.println("C moved!!!!!!!!!!!!! " + e.getSource());
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void columnRemoved(TableColumnModelEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void columnSelectionChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void popupMenuCanceled(PopupMenuEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// Update actual table
		try {
			tableModel.setTableDataAttributes(Arrays.asList(selectedAttrNames));
		} catch (Exception ex) {
			attributeList.clearSelection();
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// Do nothing
	}
}
