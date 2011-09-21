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
package structureViz.actions;

// System imports
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// giny imports
import giny.model.Edge;
import giny.model.GraphObject;
import giny.model.Node;
import giny.view.EdgeView;
import giny.view.NodeView;

// Cytoscape imports
import cytoscape.*;
import cytoscape.CytoscapeInit;
import cytoscape.logger.CyLogger;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.CyNetworkView;
import cytoscape.data.CyAttributes;
import cytoscape.util.CytoscapeAction;

// structureViz imports
import structureViz.StructureViz;
import structureViz.actions.Chimera;
import structureViz.model.Structure;
import structureViz.model.Sequence;
import structureViz.model.ChimeraChain;
import structureViz.model.ChimeraModel;
import structureViz.model.ChimeraResidue;


/**
 * The CyChimera class provides some of the logic to interface between
 * Cytoscape and Chimera objects and (user) interfaces.
 *
 * @author scooter
 */
public class CyChimera {
	public static final String[] structureKeys = {"Structure","pdb","pdbFileName","PDB ID","structure","biopax.xref.PDB",null};
	public static final String[] chemStructKeys = {"Smiles","smiles","SMILES",null};
	public static final String[] residueKeys = {"FunctionalResidues","ResidueList",null};
	public static final String[] sequenceKeys = {"sequence",null};
	private static Map<String, Structure> pdbStructureMap = null;
	private static Map<String, Structure> smilesStructureMap = null;
	private static CyAttributes cyAttributes;
	private static CyLogger logger = null;
	private static boolean enableNodeSelection = true;

	static List<GraphObject> selectedList = null;

  static { 
		String structureAttribute = getProperty("structureAttribute");
		if (structureAttribute != null) {
			structureKeys[structureKeys.length-1] = structureAttribute;
		}
		String chemStructAttribute = getProperty("chemStructAttribute");
		if (chemStructAttribute != null) {
			chemStructKeys[chemStructKeys.length-1] = chemStructAttribute;
		}
		String residueAttribute = getProperty("residueAttribute");
		if (residueAttribute != null) {
			residueKeys[residueKeys.length-1] = residueAttribute;
		}
		logger = CyLogger.getLogger(StructureViz.class);
		pdbStructureMap =  new HashMap<String, Structure>();
		smilesStructureMap =  new HashMap<String, Structure>();
	}

	/**
	 * Get the list of structures associated with this node
	 *
	 * @param nodeView the NodeView of the node we're looking at
	 * @param overNodeOnly only look for structures in the node we're over
	 * @return a list of Structures associated with this node
	 */
	public static List<Structure>getSelectedStructures(NodeView nodeView, 
	                                                   boolean overNodeOnly) {
		List<Structure>structureList = new ArrayList<Structure>();
    //get the network object; this contains the graph
    CyNetwork network = Cytoscape.getCurrentNetwork();
    //get the network view object
    CyNetworkView view = Cytoscape.getCurrentNetworkView();
    //get the list of node attributes
    cyAttributes = Cytoscape.getNodeAttributes();
    //can't continue if any of these is null
    if (network == null || view == null || cyAttributes == null) {
			return structureList;
		}

		List<NodeView> selectedNodes = null;
		if (!overNodeOnly || nodeView == null)
			selectedNodes = view.getSelectedNodes();

    if (selectedNodes == null || selectedNodes.size() == 0) {
			if (nodeView == null) {
				return structureList;
			}
			selectedNodes = new ArrayList();
			selectedNodes.add(nodeView);
    }
    //iterate over every node view
		for (NodeView nView: selectedNodes) {
      //first get the corresponding node in the network
      CyNode node = (CyNode)nView.getNode();

			getStructures(node, structureList, null);

		}
		return structureList;
	}


	/**
	 * Return the list of structures associated with a
	 * Cytoscape node as a String.  
	 *
	 * @param node CyNode to use to look for PDB structure attribute
	 * @return a comma-separated String of PDB structures
	 */
	public static String getStructureName(CyNode node) {
    String nodeID = node.getIdentifier();
		return getStructureName(nodeID);
	}

	/**
	 * Return the list of structures associated with a
	 * Cytoscape node as a String.  
	 *
	 * @param node Node ID to use to look for PDB structure attribute
	 * @return a comma-separated String of PDB structures
	 */
	public static String getStructureName(String nodeID) {
		if (cyAttributes == null) cyAttributes = Cytoscape.getNodeAttributes();
		for (int key = 0; key < structureKeys.length; key++) {
			if (structureKeys[key] == null) continue;
     	if (cyAttributes.hasAttribute(nodeID, structureKeys[key])) {
       	// Add it to our list
				if (cyAttributes.getType(structureKeys[key]) == CyAttributes.TYPE_STRING) {
       		return cyAttributes.getStringAttribute(nodeID, structureKeys[key]);
				} else if (cyAttributes.getType(structureKeys[key]) == CyAttributes.TYPE_SIMPLE_LIST) {
					List<String>structList = cyAttributes.getListAttribute(nodeID, structureKeys[key]);
					String structures = "";
					for (String s: structList) {
						structures += s+",";
					}
					// Make sure to remove the training ","
					return structures.substring(0,structures.length()-1);
				}
			}
    }
		return null;
	}

	/**
	 * Return the list of 2D identifiers associated with a
	 * Cytoscape node as a String.  
	 *
	 * @param node CyNode to use to look for 2D structures attributes
	 * @return a comma-separated String of 2D structures
	 */
	public static String getChemStruct(CyNode node) {
    String nodeID = node.getIdentifier();
		for (int key = 0; key < chemStructKeys.length; key++) {
			if (chemStructKeys[key] == null) continue;
     	if (cyAttributes.hasAttribute(nodeID, chemStructKeys[key])) {
       	// Add it to our list
				if (cyAttributes.getType(chemStructKeys[key]) == CyAttributes.TYPE_STRING) {
       		return cyAttributes.getStringAttribute(nodeID, chemStructKeys[key]);
				} else if (cyAttributes.getType(chemStructKeys[key]) == CyAttributes.TYPE_SIMPLE_LIST) {
					List<String>structList = cyAttributes.getListAttribute(nodeID, chemStructKeys[key]);
					String structures = "";
					for (String s: structList) {
						structures += s+",";
					}
					// Make sure to remove the training ","
					return structures.substring(0,structures.length()-1);
				}
			}
    }
		return null;
	}


	/**
	 * Return the list of functional residues associated with a
	 * Cytoscape node as a String.  
	 *
	 * @param node CyNode to use to look for the functional residues attribute
	 * @return a comma-separated String of functional residue identifiers
	 */
	public static String getResidueList(CyNode node) {
		if (node == null) return null;
    String nodeID = node.getIdentifier();
		for (int key = 0; key < residueKeys.length; key++) {
			if (residueKeys[key] == null) continue;
     	if (cyAttributes.hasAttribute(nodeID, residueKeys[key])) {
				// Get the type
				if (cyAttributes.getType(residueKeys[key]) == CyAttributes.TYPE_STRING) {
       		// Add it to our list
       		return cyAttributes.getStringAttribute(nodeID, residueKeys[key]);
				} else if (cyAttributes.getType(residueKeys[key]) == CyAttributes.TYPE_SIMPLE_LIST) {
					List<String>residueList = cyAttributes.getListAttribute(nodeID, residueKeys[key]);
					String residues = "";
					for (String res: residueList) {
						residues = residues.concat(res+",");
					}
					// Make sure to remove the training ","
					return residues.substring(0, residues.length()-1);
				}
			}
    }
		return null;
	}

	/**
	 * Return the Structure object that corresponds to a model name.
	 *
	 * @param networkView the CyNetworkView that contains the nodes
	 * @param name the model name we're looking for
	 * @param create if true, create a structure if one doesn't exist
	 * @return the Structure object containing the name and referending the node
	 */
	public static Structure findStructureForModel(CyNetworkView networkView, 
	                                              String name, boolean create) {
		// Do we already know about this model?
		if (pdbStructureMap.containsKey(name))
			return pdbStructureMap.get(name);

		if (smilesStructureMap.containsKey(name))
			return smilesStructureMap.get(name);

		if (!create) return null;

		// Apparently not.  Iterate over all of our nodes and get the structures
		cyAttributes = Cytoscape.getNodeAttributes();
		for (CyNode node: ((List<CyNode>)networkView.getNetwork().nodesList())) {
			List<Structure> sList = getStructures(node, null, name);
			if (sList != null && sList.size() > 0)
				return sList.get(0);
		}
		
		// We don't know anything about this structure -- assume it's a PDB file loaded
		// into Chimera
		Structure s = Structure.getStructure(name, (CyNode)null,
				                                 Structure.StructureType.PDB_MODEL);
		pdbStructureMap.put(name, s);
		return s;
	}

	/**
	 * Return list of Sequence objects for all of the selected
	 * nodes that have a sequence attribute.
	 *
	 * @param nodeView if not null, use this as the reference node and add it first
	 * @return a List of the Sequence objects for each selected node (plus nodeView);
	 */
	public static List getSelectedSequences(NodeView nodeView) {
		String sequenceAttribute = getProperty("sequenceAttribute");
		if (sequenceAttribute != null) {
			sequenceKeys[1] = sequenceAttribute;
		}
		List<Sequence>sequenceList = new ArrayList<Sequence>();
    //get the network object; this contains the graph
    CyNetwork network = Cytoscape.getCurrentNetwork();
    //get the network view object
    CyNetworkView view = Cytoscape.getCurrentNetworkView();
    //get the list of node attributes
    cyAttributes = Cytoscape.getNodeAttributes();
    //can't continue if any of these is null
    if (network == null || view == null || cyAttributes == null) {return sequenceList;}

		List<NodeView> selectedNodes = view.getSelectedNodes();

    if (selectedNodes.size() == 0) {
			if (nodeView == null) {
				return sequenceList;
			}
			selectedNodes = new ArrayList();
			selectedNodes.add(nodeView);
    }
    //iterate over every node view
		for (NodeView nView: selectedNodes) {
      //first get the corresponding node in the network
      CyNode node = (CyNode)nView.getNode();
			Sequence seq = getSequence(node);
			if (seq != null) sequenceList.add(seq);
    }
		return sequenceList;
	}

	/**
	 * Get the sequence attribute associated with a node (if any) and return
	 * a new Sequence object which contains that sequence.
	 *
	 * @param node the Cytoscape node to get the sequence attribute from
	 * @return the resulting Sequence object, or null if the node doesn't
	 * have a sequence attribute.
	 */
	public static Sequence getSequence(CyNode node) {
    String nodeID = node.getIdentifier();
		for (int key = 0; key < sequenceKeys.length; key++) {
			if (sequenceKeys[key] == null) continue;
     	if (cyAttributes.hasAttribute(nodeID, sequenceKeys[key])) {
       	// Add it to our list
       	String sequence = cyAttributes.getStringAttribute(nodeID, sequenceKeys[key]);
				String sequenceName = getStructureName(node);
				if (sequenceName == null) sequenceName = nodeID;
       	return new Sequence(sequenceName,sequence,node);
     	}
		}
		return null;
	}

	/**
	 * Select a group of nodes in Cytoscape.  If a node is already selected,
	 * change the color to indicate a second-level selection.
	 *
	 * @param networkView the CyNetworkView that contains the nodes to be selected
	 * @param modelsToSelect a HashMap or the models we want to select, 
	 * with ChimeraModels as the keys
	 * @param chimeraModels the list of ChimeraModels we currently have open
	 */
	public static void selectCytoscapeNodes(CyNetworkView networkView, 
	                                        Map<ChimeraModel,ChimeraModel> modelsToSelect,
																					List<ChimeraModel> chimeraModels) {
		CyNetwork network = networkView.getNetwork();

		if (selectedList == null) selectedList = new ArrayList<GraphObject>();

		enableNodeSelection = false;

		// We need to do this in two passes since some parts of a structure might be
		// selected and some might not.  Our selection model (unfortunately) only tells
		// us that something has changed, not what...
		List<GraphObject> graphObjectList = new ArrayList<GraphObject>();
		for (ChimeraModel model: chimeraModels) {
			if (model == null) continue;
			Structure st = model.getStructure();
			if (modelsToSelect.containsKey(model)) {
				// Deselect everything in this structure
				graphObjectList.addAll(st.getGraphObjectList());
			}
		}
		setSelectedState(networkView, graphObjectList, false);
		graphObjectList.clear();

		for (ChimeraModel model: chimeraModels) {
			if (model == null) continue;
			Structure st = model.getStructure();
			if (modelsToSelect.containsKey(model)) {
				// Select the appropriate things
				List<ChimeraResidue>residueList = model.getSelectedResidues();
				graphObjectList.addAll(st.getGraphObjectList(residueList));
			}
		}
		setSelectedState(networkView, graphObjectList, true);

/*
			CyNode node = model.getStructure().node();
			if (node == null) continue;
			NodeView nodeView = networkView.getNodeView(node);

			if (modelsToSelect.containsKey(model)) {
				// Get the current selection state
				if (!nodeView.isSelected()) {
					// Not selected, mark the fact that we're selecting it.
					selectedList.add(nodeView);
					nodeView.setSelected(true);
				} 
				nodeView.setSelectedPaint(java.awt.Color.GREEN);
			} else {
				// Did we select it?
				if (nodeView.isSelected() && selectedList.contains(nodeView)) {
					// Yes, deselect it
					nodeView.setSelected(false);
					selectedList.remove(nodeView);
				} else {
					// No, just change the color
					nodeView.setSelectedPaint(java.awt.Color.YELLOW);
				}
			}
		}
*/

		networkView.updateView();
		enableNodeSelection = true;
	}

	/**
	 * Get a structureViz property from Cytoscape.
	 *
	 * @param name the name of the property we want to get
	 * @return the property value, or null if it doesn't exist
	 */
	public static String getProperty(String name) {
		return CytoscapeInit.getProperties().getProperty("structureViz."+name);
	}

	/**
	 * Search for structure references in the residue list
	 *
	 * @param residueList the list of residues
	 * @return a concatenated list of structures encoded in the list
	 */
	public static String findStructures(String residueList) {
		if (residueList == null) return null;
		String[] residues = residueList.split(",");
		String structures = new String();
		Map<String,String> structureNameMap = new HashMap<String,String>();
		for (int i = 0; i < residues.length; i++) {
			String[] components = residues[i].split("#");
			if (components.length > 1) {
				structureNameMap.put(components[0],components[1]);
			}
		}
		if (structureNameMap.isEmpty())
			return null;

		String structure = null;
		for (String struct: structureNameMap.keySet()) {
			if (structure == null)
				structure = new String();
			else
				structure = structure.concat(",");
			structure = structure.concat(struct);
		}
		return structure;
	}

	public static boolean selectionEnabled() {
		return enableNodeSelection;
	}

	public static void setSelectionEnabled(boolean enable) {
		enableNodeSelection = enable;
	}

	public static List<Structure> getStructures(CyNode node, 
	                                             List<Structure> structureList, 
	                                             String matchName) {
		if (structureList == null) {
			structureList = new ArrayList<Structure>();
		}
		// Start by handling any chemical (2D) structures
		String chemStruct = getChemStruct(node);
		if (chemStruct != null && chemStruct.length() > 0) {
				String[] structList = chemStruct.split(",");
				for (int i = 0; i < structList.length; i++) {
					Structure s = Structure.getStructure(structList[i].trim(), node, 
					                                     Structure.StructureType.SMILES);
					smilesStructureMap.put(structList[i].trim(), s);
					structureList.add(s);
				}
		}

		// Now handle the PDB structures
		String structure = getStructureName(node);
		String residues = getResidueList(node);
		if (structure == null || structure.length() == 0) {
			structure = findStructures(residues);
			if (structure == null || structure.length() == 0)
				return structureList;
		}
		// Check to see if this node has a list of structures, first
		String[] sList = structure.split(",");
		if (sList != null && sList.length > 0) {
			// It does, so add them all
			for (int i = 0; i < sList.length; i++) {
				Structure s = Structure.getStructure(sList[i].trim(),node, 
				                                     Structure.StructureType.PDB_MODEL);
				pdbStructureMap.put(sList[i].trim(), s);
				// System.out.println("Setting residue list for "+s);
				s.setResidueList(node, residues);
				if (matchName != null && matchName.equals(sList[i].trim())) {
					structureList.add(s);
					return structureList;
				} else if (matchName == null)
					structureList.add(s);
			}
		} else if (structure != null) {
			Structure s = Structure.getStructure(structure,node, Structure.StructureType.PDB_MODEL);
			pdbStructureMap.put(structure, s);
			s.setResidueList(node, residues);
			if (matchName != null && matchName.equals(structure))
      	structureList.add(s);
		}
		return structureList;
	}

	private static void setSelectedState(CyNetworkView view, List<GraphObject> goList, 
	                                     boolean state) {
		for (GraphObject obj: goList) {
			if (obj instanceof Node) {
				// Handle secondary paint??
				NodeView nv = view.getNodeView((Node)obj);
				if (nv != null)
					nv.setSelected(state);
			} else if (obj instanceof Edge) {
				EdgeView ev = view.getEdgeView((Edge)obj);
				if (ev != null)
					ev.setSelected(state);
			}
			if (selectedList.contains(obj))
				selectedList.remove(obj);
		}
	}
}
