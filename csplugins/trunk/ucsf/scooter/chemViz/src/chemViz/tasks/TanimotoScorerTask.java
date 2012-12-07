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

package chemViz.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import giny.model.GraphObject;
import giny.view.EdgeView;
import giny.view.NodeView;

import cytoscape.Cytoscape;
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.data.CyAttributes;
import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualStyle;

import chemViz.model.Compound;
import chemViz.model.Compound.AttriType;
import chemViz.similarity.CDKTanimotoScore;
import chemViz.ui.ChemInfoSettingsDialog;

/**
 * The TanimotoScorerTask fetches all of the compounds defined by the
 * object passed in its constructor and then calculates the tanimoto distances
 * between each of them, storing the results in attributes or by creating edges
 * in a new network.
 */
public class TanimotoScorerTask extends AbstractCompoundTask {
	List<GraphObject> selection;
	ChemInfoSettingsDialog settingsDialog;
	TaskMonitor monitor;
	boolean createNewNetwork = false;
	boolean canceled = false;
	static private CyLogger logger = CyLogger.getLogger(TanimotoScorerTask.class);

	/**
 	 * Creates the task.
 	 *
 	 * @param selection the graph objects that we're comparing
 	 * @param dialog the settings dialog, which we use to pull the attribute names that contain the compound descriptors
 	 * @param newNetwork if 'true' create a new network
 	 */
	public TanimotoScorerTask(Collection<GraphObject> selection, ChemInfoSettingsDialog dialog, boolean newNetwork) {
		this.selection = new ArrayList(selection);
		this.settingsDialog = dialog;
		this.createNewNetwork = newNetwork;
	}

	public String getTitle() {
		return "Creating Scores Table";
	}

	/**
 	 * Runs the task -- this will get all of the compounds, and compute the tanimoto values
 	 */
	public void run() {
		// Set up
		CyAttributes attributes = Cytoscape.getNodeAttributes();
		CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
		CyNetwork origNetwork = Cytoscape.getCurrentNetwork();
		CyNetworkView origNetworkView = Cytoscape.getCurrentNetworkView();
		CyNetworkView newNetworkView = null;
		CyNetwork newNet = null;
		VisualStyle vs = null;
		double tcCutoff = 0.25;
		if (settingsDialog != null)
			tcCutoff = settingsDialog.getTcCutoff();

		objectCount = 0;
		totalObjects = selection.size();
		List<CyEdge> edgeList = Collections.synchronizedList(new ArrayList<CyEdge>());

		updateMonitor();

		List<CalculateTanimotoTask> taskList = new ArrayList<CalculateTanimotoTask>();

		for (int index1 = 0; index1 < totalObjects; index1++) {
			CyNode node1 = (CyNode)selection.get(index1);
			if (canceled) break;
			setStatus("Calculating tanimoto coefficients for "+node1.getIdentifier());

			for (int index2 = 0; index2 < index1; index2++) {
				if (canceled) break;
				CyNode node2 = (CyNode)selection.get(index2);

				if (node2 == node1)
					continue;

				taskList.add(new CalculateTanimotoTask(origNetwork, node1, node2, tcCutoff));
			}
		}

		int nThreads = Runtime.getRuntime().availableProcessors()-1;
		int maxThreads = settingsDialog.getMaxThreads();
		if (maxThreads > 0)
			nThreads = maxThreads;

		ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);

		try {
			List<Future<CyEdge>> futures = threadPool.invokeAll(taskList);
			// System.out.println("invokeAll completes");
			for (Future<CyEdge> future: futures) {
				CyEdge edge = future.get();
				if (edge != null)
					edgeList.add(edge);
			}
		} catch (Exception e) {
			logger.warning("Thread execution exception: "+e);
		}

		if (createNewNetwork) {
			// Create a new network if we're supposed to
			/* System.out.println("Creating "+origNetwork.getTitle()+" copy");
			System.out.println("Have "+selection.size()+" nodes and "+edgeList.size()+" edges");
			System.out.println("Nodes: ");
			for (GraphObject node: selection)  System.out.println("    "+node);
			System.out.println("Edges: ");
			for (CyEdge edge: edgeList)  System.out.println("    "+edge);
			*/
			newNet = Cytoscape.createNetwork(selection, edgeList, origNetwork.getTitle()+" copy",
																 origNetwork, true); 
			newNetworkView = Cytoscape.getNetworkView(newNet.getIdentifier());
			vs = Cytoscape.getVisualMappingManager().getVisualStyle();

			for (GraphObject go: selection) {
				CyNode node = (CyNode)go;
				NodeView orig = origNetworkView.getNodeView(node);
				NodeView newv = newNetworkView.getNodeView(node);
				newv.setXPosition(orig.getXPosition());
				newv.setYPosition(orig.getYPosition());
			}

			// All done -- create and update the view
			newNetworkView.fitContent();
			newNetworkView.setVisualStyle(vs.getName());
		}

	}

	class CalculateTanimotoTask implements Callable <CyEdge> {
		CyNode node1;
		CyNode node2;
		CyNetwork origNetwork;
		double tcCutoff = 0.25;
		CyEdge newEdge = null;

		public CalculateTanimotoTask(CyNetwork network, CyNode node1, CyNode node2, double tcCutoff) {
			this.node1 = node1;
			this.node2 = node2;
			this.origNetwork = network;
			this.tcCutoff = tcCutoff;
		}

		public CyEdge call() {
			CyAttributes attributes = Cytoscape.getNodeAttributes();
			CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();

			List<Compound> cList1 = getCompounds(node1, attributes, 
																					 settingsDialog.getCompoundAttributes("node",AttriType.smiles),
																					 settingsDialog.getCompoundAttributes("node",AttriType.inchi), null);
			if (cList1 == null) return null;

			List<Compound> cList2 = getCompounds(node2, attributes, 
																					 settingsDialog.getCompoundAttributes("node",AttriType.smiles),
																					 settingsDialog.getCompoundAttributes("node",AttriType.inchi), null);
			if (cList2 == null) return null;

			int nScores = cList1.size()*cList2.size();
			double maxScore = -1;
			double minScore = 10000000;
			double averageScore = 0;
			for (Compound compound1: cList1) {
				if (compound1 == null) return null;
				for (Compound compound2: cList2) {
					if (canceled) break;
					if (compound2 == null) return null;

					CDKTanimotoScore scorer = new CDKTanimotoScore(compound1, compound2);
					double score = scorer.calculateSimilarity();
					averageScore = averageScore + score/nScores;
					if (score > maxScore) maxScore = score;
					if (score < minScore) minScore = score;
				}
			}

			// Create the edge if we're supposed to
			CyEdge edge = null;
			if (createNewNetwork) {
				if (averageScore <= tcCutoff)
					return null;
				// System.out.print("   Creating and edge between "+node1.getIdentifier()+" and "+node2.getIdentifier());
				edge = Cytoscape.getCyEdge(node1, node2, "interaction", "similarity", true, true);
				// System.out.println("...done");
			} else {
				// Otherwise, get the edges connecting these nodes (if any)
				int[] node_indices = {node1.getRootGraphIndex(), node2.getRootGraphIndex()};
				int[] edge_indices = origNetwork.getConnectingEdgeIndicesArray(node_indices);
				if (edge_indices == null || edge_indices.length == 0) return null;
				edge = (CyEdge)origNetwork.getEdge(edge_indices[0]);
			}

			if (nScores > 1) {
				edgeAttributes.setAttribute(edge.getIdentifier(), "AverageTanimotoSimilarity", Double.valueOf(averageScore));
				edgeAttributes.setAttribute(edge.getIdentifier(), "MaxTanimotoSimilarity", Double.valueOf(maxScore));
				edgeAttributes.setAttribute(edge.getIdentifier(), "MinTanimotoSimilarity", Double.valueOf(minScore));
			} else {
				edgeAttributes.setAttribute(edge.getIdentifier(), "TanimotoSimilarity", Double.valueOf(averageScore));
			}

			newEdge = edge;

			return edge;
		}

		CyEdge get() {
			return newEdge;
		}
	}
}
