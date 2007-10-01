/*
 * DeletionDialog.java
 *
 * Created on 2006/03/20, 10:46
 */
package browser;

import cytoscape.Cytoscape;

import cytoscape.data.CyAttributes;

import org.jdesktop.layout.GroupLayout;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.JDialog;


/**
 *
 * @author kono
 */
public class DeletionDialog extends JDialog {
	private String[] attributes = null;
	private String type;
	private DataTableModel model;

	/** Creates new form DeletionDialog */
	protected DeletionDialog(Frame parent, boolean modal, String[] attributes, String type, DataTableModel model) {
		super(parent, modal);

		this.type = type;
		this.attributes = attributes;
		this.model = model;
		
		String title = "Delete " + type + " Attributes";
		this.setTitle(title);
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		deletionPane = new javax.swing.JScrollPane();
		attributeList = new javax.swing.JList();
		deleteButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		descriptionLabel = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		attributeList.setModel(new AbstractListModel() {
				public int getSize() {
					return attributes.length;
				}

				public Object getElementAt(int i) {
					return attributes[i];
				}
			});
		deletionPane.setViewportView(attributeList);

		deleteButton.setText("Delete");
		deleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					deleteButtonActionPerformed(evt);
				}
			});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					cancelButtonActionPerformed(evt);
					;
				}
			});

		descriptionLabel.setText("Please select attributes to be deleted:");

		GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
		                                .add(layout.createSequentialGroup().addContainerGap()
		                                           .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
		                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
		                                                           layout.createSequentialGroup()
		                                                                 .add(deleteButton)
		                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
		                                                                 .add(cancelButton)
		                                                                 .addContainerGap())
		                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
		                                                           layout.createSequentialGroup()
		                                                                 .add(deletionPane,
		                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                                                      229, Short.MAX_VALUE)
		                                                                 .add(12, 12, 12))
		                                                      .add(layout.createSequentialGroup()
		                                                                 .add(descriptionLabel)
		                                                                 .addContainerGap(26,
		                                                                                  Short.MAX_VALUE)))));
		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
		                              .add(org.jdesktop.layout.GroupLayout.TRAILING,
		                                   layout.createSequentialGroup().addContainerGap()
		                                         .add(descriptionLabel).add(12, 12, 12)
		                                         .add(deletionPane,
		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
		                                              200, Short.MAX_VALUE)
		                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
		                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
		                                                    .add(deleteButton).add(cancelButton))
		                                         .addContainerGap()));
		pack();
	} // </editor-fold>

	private void deleteButtonActionPerformed(ActionEvent evt) {
		// TODO add your handling code here:
		CyAttributes attr = null;

		if (type.equalsIgnoreCase("node")) {
			attr = Cytoscape.getNodeAttributes();
		} else {
			attr = Cytoscape.getEdgeAttributes();
		}

		Object[] selected = attributeList.getSelectedValues();

		for (int i = 0; i < selected.length; i++) {
			attr.deleteAttribute(selected[i].toString());
		}
		model.setTable();
		this.dispose();
	}

	private void cancelButtonActionPerformed(ActionEvent evt) {
		// TODO add your handling code here:
		this.dispose();
	}

	// Variables declaration - do not modify
	private javax.swing.JList attributeList;
	private javax.swing.JButton cancelButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JScrollPane deletionPane;
	private javax.swing.JLabel descriptionLabel;

	// End of variables declaration
}
