/*
  Copyright (c) 2006, 2007, 2008 The Cytoscape Consortium (www.cytoscape.org)

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

package chemViz.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Frame;
import java.awt.Image;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import giny.model.GraphObject;
import giny.view.EdgeView;
import giny.view.NodeView;

import cytoscape.Cytoscape;
import cytoscape.CyEdge;
import cytoscape.CyNode;
import cytoscape.data.CyAttributes;

import chemViz.model.Compound;
import chemViz.model.Compound.AttriType;

public class CompoundPopup extends JDialog implements ComponentListener {
	
	private List<Compound> compoundList;
	private Map<Component, Compound> imageMap;
	private String labelAttribute;
	private static final int LABEL_HEIGHT = 20;

	public CompoundPopup(List<Compound> compoundList, List<GraphObject> objectList, String labelAttribute) {
		super(Cytoscape.getDesktop());

		this.compoundList = compoundList;
		this.imageMap = new HashMap();
		this.labelAttribute = labelAttribute;

		if (objectList != null && objectList.size() > 0) 
			setTitle(getObjectTitle(objectList));
		else
			setTitle("2D Structures");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBackground(Color.BLACK);

		addImages(400);
		pack();
		setVisible(true);
	}

	private String getObjectTitle(List<GraphObject> objectList) {
		GraphObject go = objectList.get(0);
		if (go instanceof CyNode) {
			if (objectList.size() == 1) {
				return("2D Structures for Node "+((CyNode)go).getIdentifier());
			} else {
				return("2D Structures for Selected Nodes");
			}
		} else  {
			if (objectList.size() == 1) {
				return("2D Structures for Edge "+((CyEdge)go).getIdentifier());
			} else {
				return("2D Structures for Selected Edges");
			}
		}
	}

	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {
		JLabel labelComponent = (JLabel)e.getComponent();
		// Get our new width
		int width = labelComponent.getWidth();
		int height = labelComponent.getHeight();
		String label = labelComponent.getText();
		if (label != null && label.length() > 0)
			height = height - LABEL_HEIGHT;
		// Is it in our map?
		if (imageMap.containsKey(labelComponent)) {
			Image img = imageMap.get(labelComponent).getImage(width,height, Color.WHITE);
			if (img != null)
				labelComponent.setIcon(new ImageIcon(img));
		}
	}


	// TODO: Add labels on image squares
	private void addImages(int width) {
		CyAttributes attributes = null;

		// How many images do we have?
		int structureCount = compoundList.size();
		int nCols = (int)Math.sqrt((double)structureCount);
		GridLayout layout = new GridLayout(nCols, structureCount/nCols, 1, 1);
		setLayout(layout);

		// Get the right attributes
		if (labelAttribute != null && labelAttribute.startsWith("node.")) {
			attributes = Cytoscape.getNodeAttributes();
			labelAttribute = labelAttribute.substring(5);
		} else if (labelAttribute != null && labelAttribute.startsWith("edge.")) {
			attributes = Cytoscape.getEdgeAttributes();
			labelAttribute = labelAttribute.substring(5);
		} else
			attributes = null;

		for (Compound compound: compoundList) {
			// Get the image
			Image img = compound.getImage(width/nCols, width/nCols-LABEL_HEIGHT, Color.WHITE);
			JLabel label;
			if (labelAttribute == null) {
				label = new JLabel(new ImageIcon(img));
			} else {
				String textLabel = labelAttribute;
				if (attributes != null) {
					textLabel = attributes.getAttribute(compound.getSource().getIdentifier(),labelAttribute).toString();
					if (textLabel == null)
						textLabel = compound.getSource().getIdentifier();
				}
				label = new JLabel(textLabel.toString(), new ImageIcon(img), JLabel.CENTER);
				label.setVerticalTextPosition(JLabel.BOTTOM);
				label.setHorizontalTextPosition(JLabel.CENTER);
			}
			label.setBackground(Color.WHITE);
			label.setOpaque(true);
			label.setBorder(BorderFactory.createEtchedBorder());
			label.addComponentListener(this);
			imageMap.put(label, compound);
			add (label);
		}
	}
}
