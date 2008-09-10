/*
 File: InteractionsReader.java

 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.io.read.internal;


import org.cytoscape.io.read.ReadUtils;
import org.cytoscape.io.read.CyNetworkReader;

import cytoscape.task.TaskMonitor;
import cytoscape.task.PercentUtil;

import org.cytoscape.model.network.CyEdge;
import org.cytoscape.model.network.CyNetwork;
import org.cytoscape.model.network.CyNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * Reader for graphs in the interactions file format. Given the filename,
 * provides the graph and attributes objects constructed from the file.
 */
public class InteractionsReader implements CyNetworkReader {

	private TaskMonitor taskMonitor;
	private List<Interaction> allInteractions; 
	private InputStream inputStream;
	private CyNetwork network;
	private PercentUtil percentUtil;

	// TODO figure out insertion of network
	public InteractionsReader(CyNetwork net) {
		allInteractions = new ArrayList<Interaction>();
		network = net;
	}

	public void setTaskMonitor(TaskMonitor monitor) {
		taskMonitor = monitor;
	}

	public void setInput(InputStream is) {
		if ( is == null )
			throw new NullPointerException("input stream is null");
		inputStream = is;
	}

	public List<CyNetwork> getReadNetworks() {
		List<CyNetwork> ret = new ArrayList<CyNetwork>(1);
		ret.add(network);
		return ret;
	}

	public String[] getExtensions() {
		return new String[]{"sif"};
	}

	public String getExtensionDescription() {
		return "SIF files";
	}

	public void read() throws IOException {

		String rawText = ReadUtils.getInputString(inputStream);

		String delimiter = " ";

		if (rawText.indexOf("\t") >= 0)
			delimiter = "\t";

		percentUtil = new PercentUtil(6);

		final String[] lines = rawText.split(System.getProperty("line.separator"));

		String newLine;
		Interaction newInteraction;

		for (int i = 0; i < lines.length; i++) {
			if (taskMonitor != null) {
				taskMonitor.setPercentCompleted( percentUtil.getGlobalPercent(1, i,lines.length) );
			}

			newLine = lines[i];

			if (newLine.length() <= 0) {
				continue;
			}

			newInteraction = new Interaction(newLine, delimiter);
			allInteractions.add(newInteraction);
		}

		createNetwork();
	} 

	private void createNetwork() {

		// figure out how many nodes and edges we need before we create the graph;
		// this improves performance for large graphs
		final Map<String,CyNode> nodeMap = new HashMap<String,CyNode>();
		int edgeCount = 0;
		int i = 0;

		// put all node names in the Set
		for ( Interaction interaction : allInteractions ) {

			nodeMap.put(interaction.getSource(),null); 

			for ( String target : interaction.getTargets() ) {
				nodeMap.put(target,null);
				edgeCount++;
			}

			if (taskMonitor != null) {
				taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(2, i++,
				                                                             allInteractions.size()));
			}
		}

		i = 0;

		for (String nodeName : nodeMap.keySet()) {
			if (taskMonitor != null) {
				taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(3, i++,
				                                                             nodeMap.size()));
			}

			CyNode node = network.addNode();
			node.getCyAttributes("USER").set("name",nodeName);
			nodeMap.put( nodeName, node );
		}

		// Now loop over the interactions again, this time creating edges between
		// all sources and each of their respective targets.
		String srcName;
		String interactionType;
		String edgeName;

		i = 0;
		for ( Interaction interaction : allInteractions ) {
			if (taskMonitor != null) {
				taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(4, i++,
				                                                             allInteractions.size()));
			}

			srcName = interaction.getSource();
			interactionType = interaction.getType();

			for ( String tgtName : interaction.getTargets() ) {
				CyEdge edge = network.addEdge(nodeMap.get(srcName), nodeMap.get(tgtName), true);
				edge.getCyAttributes("USER").set("name", srcName + " (" + interactionType + ") " + tgtName ); 
				edge.getCyAttributes("USER").set("interaction", interactionType);
			} 
		} 
	} 
} 
