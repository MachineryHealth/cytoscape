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

package csplugins.enhanced.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.apache.lucene.store.RAMDirectory;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

public class IndexAndSearchTask implements Task {

	public static final String INDEX_FIELD = "Identifier";

	private CyNetwork network;

	private String query;

	private TaskMonitor taskMonitor;

	private boolean interrupted = false;

	/**
	 * Constructor.
	 * 
	 * @param network
	 *            Network to execute query on.
	 * @param query
	 *            Query string.
	 */
	IndexAndSearchTask(CyNetwork network, String query) {
		this.network = network;
		this.query = query;
	}

	/**
	 * Executes Task: IndexAndSearch
	 */
	public void run() {

		// Index the given network
		taskMonitor.setStatus("Indexing network");
		EnhancedSearchIndex indexHandler = new EnhancedSearchIndex(network);
		RAMDirectory idx = indexHandler.getIndex();

		if (interrupted) {
			return;
		}

		// Execute query
		taskMonitor.setStatus("Executing query");
		EnhancedSearchQuery queryHandler = new EnhancedSearchQuery(idx);
		queryHandler.executeQuery(query);

		if (interrupted) {
			return;
		}

		// Display results
		Cytoscape.getCurrentNetwork().unselectAllNodes();
		Cytoscape.getCurrentNetwork().unselectAllEdges();
		Cytoscape.getCurrentNetworkView().updateView();

		int hitCount = queryHandler.getHitCount();
		if (hitCount == 0) {
			System.out.println("No hits. ");
			return;
		}
		System.out.println("There are " + hitCount + " hits.");
		taskMonitor.setStatus("Displaying " + hitCount + " hits");
		ArrayList<String> hits = queryHandler.getHits();

		List<CyNode> nodeList = new ArrayList<CyNode>();
		List<CyEdge> edgeList = new ArrayList<CyEdge>();

		Iterator it = hits.iterator();
		int numCompleted = 0;
		while (it.hasNext() && !interrupted) {
			// Document doc = hits.doc(i);
			// String currID = doc.get(INDEX_FIELD);
			String currID = (String) it.next();
			CyNode currNode = Cytoscape.getCyNode(currID, false);
			if (currNode != null) {
				nodeList.add(currNode);
			} else {
				CyEdge currEdge = Cytoscape.getRootGraph().getEdge(currID);
				if (currEdge != null) {
					edgeList.add(currEdge);
				} else {
					System.out.println("Unknown identifier " + (currID));
				}
			}

			int percentCompleted = (numCompleted * 100 / hitCount);
			taskMonitor.setPercentCompleted(percentCompleted);

			numCompleted++;
		}

		// Refresh view to show selected nodes and edges
		network.setSelectedNodeState(nodeList, true);
		network.setSelectedEdgeState(edgeList, true);
		Cytoscape.getCurrentNetworkView().updateView();

	}

	/**
	 * DOCUMENT ME!
	 */
	public void halt() {
		this.interrupted = true;
	}

	/**
	 * Sets the TaskMonitor.
	 * 
	 * @param taskMonitor
	 *            TaskMonitor Object.
	 * @throws IllegalThreadStateException
	 *             Illegal Thread State.
	 */
	public void setTaskMonitor(TaskMonitor taskMonitor)
			throws IllegalThreadStateException {
		this.taskMonitor = taskMonitor;
	}

	/**
	 * Gets Title of Task.
	 * 
	 * @return Title of Task.
	 */
	public String getTitle() {
		return "Searching the network";
	}

}