
/*
 Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.model;

/**
 * An object that represents an edge within a network of nodes (vertices)
 * and edges.
 */
public interface CyEdge extends CyTableEntry {
	/**
	 * The Type enum is used by methods in {@link CyNetwork} to restrict
	 * the edges that match a query as follows:
	 * <ul>
	 * <li>UNDIRECTED: matches only undirected edges</li>
	 * <li>INCOMING: matches either undirected edges or directed edges that end with this node</li>
	 * <li>OUTGOING: matches either undirected edges or directed edges that start with this node</li>
	 * <li>DIRECTED: matches directed edges regardless of whether this node is the source or the target</li>
	 * <li>ANY: matches any edge</li>
	 * </ul>
	 */
	enum Type {
		UNDIRECTED,
		INCOMING,
		OUTGOING,
		DIRECTED,
		ANY;
	}


	/**
	 * An index of this edge within this network.  The index is guaranteed to
	 * be between 0 and the number of edges in the network.
	 * @return An index for this edge within this network.
	 */
	int getIndex();

	/**
	 * Returns the source node determining this edge. 
	 * For undirected networks this method will consistently 
	 * return the same node.
	 * @return The source node of this edge.
	 */
	CyNode getSource();

	/**
	 * Returns the target node determining this edge.
	 * For undirected networks this method will consistently 
	 * return the same node.
	 * @return The target node of this edge.
	 */
	CyNode getTarget();

	/**
	 * This will return true if the edge is directed and false if the node is undirected.
	 * @return Returns true if this edge is directed and false if the node is undirected.
	 */
	boolean isDirected();
}
