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
package structureViz.ui;

// System imports
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.Position;
import javax.swing.WindowConstants.*;
import javax.swing.border.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.event.*;

// Cytoscape imports
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.view.CyNetworkView;
import giny.view.NodeView;

// StructureViz imports
import structureViz.model.Structure;
import structureViz.model.AlignmentTableModel;
import structureViz.actions.CyChimera;
import structureViz.actions.Chimera;
import structureViz.actions.Align;

public class AlignStructuresDialog extends JDialog implements ActionListener {
	// Instance variables
	Chimera chimeraObject;
	List structures;
	boolean status;
	Structure referenceStruct;

	// Dialog components
	private JLabel titleLabel;
	private JTable resultsTable;
	private JPanel buttonBox;
	private JPanel checkBoxes;
	private JButton alignButton;
	private	JCheckBox showSequence;
	private JCheckBox assignResults;

	// Models
	private AlignmentTableModel tableModel;

	public AlignStructuresDialog (Frame parent, Chimera object, List structures) {
		super(parent, false);
		chimeraObject = object;
		this.structures = structures;
		initComponents();
		status = false;
	}

	public void setReferenceStruct(Structure ref) {
		this.referenceStruct = ref;
		// update the table model
		tableModel.setReferenceStruct(this.referenceStruct.name());
	}

	public void setAlignEnabled(boolean value) {
		if (alignButton != null) alignButton.setEnabled(value);
	}

	private void initComponents() {
		this.setTitle("Cytoscape/Chimera Structure Alignment Dialog");

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Create a panel for the main content
		JPanel dataPanel = new JPanel();
		BoxLayout layout = new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS);
		dataPanel.setLayout(layout);

		// Create the menu for the reference structure
		JPanel refBox = new JPanel();
		JComboBox refStruct = new JComboBox(structureList(structures));
		refStruct.addActionListener(new setRefStruct());
		refBox.add(refStruct);

		Border refBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder titleBorder = BorderFactory.createTitledBorder(refBorder, "Reference Structure");
		titleBorder.setTitlePosition(TitledBorder.LEFT);
		titleBorder.setTitlePosition(TitledBorder.TOP);

		refBox.setBorder(titleBorder);
		dataPanel.add(refBox);

		// Create the results table
		tableModel = new AlignmentTableModel(chimeraObject, structures, this);

		JTable results = new JTable(tableModel);
		ListSelectionModel lsm = results.getSelectionModel();
		lsm.addListSelectionListener(tableModel);

		JScrollPane scrollPane = new JScrollPane(results);
		results.setPreferredScrollableViewportSize(new Dimension(500, 70));
		// lots more goes here
		dataPanel.add(scrollPane);

		// Create the checkbox
		JPanel checkBoxes = new JPanel(new GridLayout(2, 1));
		showSequence = new JCheckBox("Show sequence panel for each alignment");
		checkBoxes.add(showSequence);
		assignResults = new JCheckBox("Assign results to Cytoscape edge attributes");
		checkBoxes.add(assignResults);
		checkBoxes.setBorder(new CompoundBorder(
									BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
									new EmptyBorder(10,10,10,10)));
		dataPanel.add(checkBoxes);

		// Create the button box
		JPanel buttonBox = new JPanel();
		JButton doneButton = new JButton("Done");
		doneButton.setActionCommand("done");
		doneButton.addActionListener(this);

		alignButton = new JButton("Align");
		alignButton.setActionCommand("align");
		alignButton.setEnabled(false);
		alignButton.addActionListener(this);
		buttonBox.add(doneButton);
		buttonBox.add(alignButton);
		buttonBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		dataPanel.add(buttonBox);
		setContentPane(dataPanel);
	}
	
	private Object[] structureList(List structures) {
		Object[] structureList = new Object[structures.size()+1];
		Iterator iter = structures.iterator();
		int index = 0;
		structureList[index++] = new String("      ---------");
		while (iter.hasNext()) {
			Structure st = (Structure)iter.next();
			structureList[index++] = st;
		}
		return structureList;
	}

	public void actionPerformed(ActionEvent e) {
		if ("done".equals(e.getActionCommand())) {
			setVisible(false);
		}
		else if ("align".equals(e.getActionCommand())) {
			Align alignment = new Align(chimeraObject);

			if (showSequence.isSelected()) {
				alignment.setShowSequence(true);
			}

			if (assignResults.isSelected()) {
				alignment.setCreateEdges(true);
			}

			if (chimeraObject.getModel(referenceStruct.name()) == null) {
				chimeraObject.open(referenceStruct);
			}

			List matchList = tableModel.getSelectedStructures();
			Iterator modelIter = matchList.iterator();
			while (modelIter.hasNext()) {
				Structure structure = (Structure)modelIter.next();
				if (chimeraObject.getModel(structure.name()) == null) {
					chimeraObject.open(structure);
				}
			}
			chimeraObject.modelChanged();

			// Align them
			alignment.align(referenceStruct, matchList);

			// Display the results
			modelIter = matchList.iterator();
			while (modelIter.hasNext()) {
				Structure structure = (Structure)modelIter.next();
				float[] results = alignment.getResults(structure.name());
				tableModel.setResults(structure.name(), results);
			}
			tableModel.updateTable();
		}
	}

	private class setRefStruct implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox)e.getSource();
			Object sel = cb.getSelectedItem();
			if (sel.getClass() == Structure.class) {
				Structure referenceStruct = (Structure)sel;
				setReferenceStruct(referenceStruct);
			} else {
				setReferenceStruct(null);
			}
		}
	}
}

