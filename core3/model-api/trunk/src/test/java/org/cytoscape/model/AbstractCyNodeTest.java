
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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import java.lang.RuntimeException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * DOCUMENT ME!
  */
public abstract class AbstractCyNodeTest extends TestCase {
	protected CyNetwork net;


	/**
	 *  DOCUMENT ME!
	 */
	public void testGetIndex() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		assertTrue("index >= 0", n1.getIndex() >= 0);
		assertTrue("index >= 0", n2.getIndex() >= 0);
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testBasicGetNeighborList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);

		// one neighbor
		List<CyNode> l = n1.getNeighborList(CyEdge.Type.ANY);
		assertEquals("one neighbor", 1, l.size());
		assertTrue("contains node 2", l.contains(n2));

		// two neighbors
		l = n2.getNeighborList(CyEdge.Type.ANY);
		assertEquals("two neighbors", 2, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 3", l.contains(n3));
		assertFalse("contains node 4", l.contains(n4));

		// no neighbors
		l = n4.getNeighborList(CyEdge.Type.ANY);
		assertEquals("no neighbors", 0, l.size());

		// whoa!  what about self edges?
		// TODO
		CyEdge e3 = net.addEdge(n4, n4, false);
		l = n4.getNeighborList(CyEdge.Type.ANY);
		assertEquals("one neighbor?", 1, l.size());

		CyEdge e4 = net.addEdge(n4, n4, true);
		l = n4.getNeighborList(CyEdge.Type.ANY);
		assertEquals("two neighbors", 2, l.size());
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testUndirectedGetNeighborList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();
		CyNode n5 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);
		CyEdge e3 = net.addEdge(n4, n2, false);
		CyEdge e4 = net.addEdge(n4, n1, false);

		List<CyNode> l = n1.getNeighborList(CyEdge.Type.UNDIRECTED);
		assertEquals("node 1 neighbors", 2, l.size());
		assertTrue("contains node 2", l.contains(n2));
		assertTrue("contains node 4", l.contains(n4));

		l = n2.getNeighborList(CyEdge.Type.UNDIRECTED);
		assertEquals("node 2 neighbors", 3, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 3", l.contains(n3));
		assertTrue("contains node 4", l.contains(n4));

		l = n2.getNeighborList(CyEdge.Type.ANY);
		assertEquals("node 2 neighbors", 3, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 3", l.contains(n3));
		assertTrue("contains node 4", l.contains(n4));

		l = n2.getNeighborList(CyEdge.Type.INCOMING);
		assertEquals("node 2 neighbors", 0, l.size());

		l = n2.getNeighborList(CyEdge.Type.OUTGOING);
		assertEquals("node 2 neighbors", 0, l.size());

		l = n2.getNeighborList(CyEdge.Type.DIRECTED);
		assertEquals("node 2 neighbors", 0, l.size());
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testDirectedGetNeighborList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();
		CyNode n5 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, true);
		CyEdge e2 = net.addEdge(n2, n3, true);
		CyEdge e3 = net.addEdge(n4, n2, true);
		CyEdge e4 = net.addEdge(n4, n1, true);
		CyEdge e5 = net.addEdge(n5, n2, false);

		List<CyNode> l = n1.getNeighborList(CyEdge.Type.DIRECTED);
		assertEquals("node 1 neighbors directed", 2, l.size());
		assertTrue("contains node 2", l.contains(n2));
		assertTrue("contains node 4", l.contains(n4));

		l = n1.getNeighborList(CyEdge.Type.INCOMING);
		assertEquals("node 1 neighbors incoming", 1, l.size());
		assertTrue("contains node 4", l.contains(n4));

		l = n1.getNeighborList(CyEdge.Type.OUTGOING);
		assertEquals("node 1 neighbors outgoing", 1, l.size());
		assertTrue("contains node 2", l.contains(n2));

		l = n2.getNeighborList(CyEdge.Type.UNDIRECTED);
		assertEquals("node 2 neighbors undirected", 1, l.size());
		assertTrue("contains node 5", l.contains(n5));

		l = n2.getNeighborList(CyEdge.Type.DIRECTED);
		assertEquals("node 2 neighbors directed", 3, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 3", l.contains(n3));
		assertTrue("contains node 4", l.contains(n4));

		l = n2.getNeighborList(CyEdge.Type.INCOMING);
		assertEquals("node 2 neighbors incoming", 2, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 4", l.contains(n4));

		l = n2.getNeighborList(CyEdge.Type.OUTGOING);
		assertEquals("node 2 neighbors outgoing", 1, l.size());
		assertTrue("contains node 3", l.contains(n3));
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testBasicGetAdjacentEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);

		// one edge
		List<CyEdge> l = n1.getAdjacentEdgeList(CyEdge.Type.ANY);
		assertEquals("one adjacent edge", 1, l.size());
		assertTrue("contains edge 1", l.contains(e1));

		// two edge
		l = n2.getAdjacentEdgeList(CyEdge.Type.ANY);
		assertEquals("two adjacent edges", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 2", l.contains(e2));

		// no adjacent edges
		l = n4.getAdjacentEdgeList(CyEdge.Type.ANY);
		assertEquals("no edges", 0, l.size());

		// whoa!  what about self edges?
		// TODO
		CyEdge e3 = net.addEdge(n4, n4, false);
		l = n4.getAdjacentEdgeList(CyEdge.Type.ANY);
		assertEquals("one adjacent edge?", 1, l.size());
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testUndirectedGetAdjacentEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();
		CyNode n5 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);
		CyEdge e3 = net.addEdge(n4, n2, false);
		CyEdge e4 = net.addEdge(n4, n1, false);

		List<CyEdge> l = n1.getAdjacentEdgeList(CyEdge.Type.UNDIRECTED);
		assertEquals("node 1 adjacent edges", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 4", l.contains(e4));

		l = n2.getAdjacentEdgeList(CyEdge.Type.UNDIRECTED);
		assertEquals("node 2 adjacent edges", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 2", l.contains(e2));
		assertTrue("contains edge 3", l.contains(e3));

		l = n2.getAdjacentEdgeList(CyEdge.Type.ANY);
		assertEquals("node 2 adjacent edges", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 2", l.contains(e2));
		assertTrue("contains edge 3", l.contains(e3));

		l = n2.getAdjacentEdgeList(CyEdge.Type.INCOMING);
		assertEquals("node 2 adjacent edges", 0, l.size());

		l = n2.getAdjacentEdgeList(CyEdge.Type.OUTGOING);
		assertEquals("node 2 adjacent edges", 0, l.size());

		l = n2.getAdjacentEdgeList(CyEdge.Type.DIRECTED);
		assertEquals("node 2 adjacent edges", 0, l.size());
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testDirectedGetAdjacentEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();
		CyNode n5 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, true);
		CyEdge e2 = net.addEdge(n2, n3, true);
		CyEdge e3 = net.addEdge(n4, n2, true);
		CyEdge e4 = net.addEdge(n4, n1, true);
		CyEdge e5 = net.addEdge(n5, n2, false);

		List<CyEdge> l = n1.getAdjacentEdgeList(CyEdge.Type.DIRECTED);
		assertEquals("node 1 adjacent edges directed", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 4", l.contains(e4));

		l = n1.getAdjacentEdgeList(CyEdge.Type.INCOMING);
		assertEquals("node 1 adjacent edges incoming", 1, l.size());
		assertTrue("contains edge 4", l.contains(e4));

		l = n1.getAdjacentEdgeList(CyEdge.Type.OUTGOING);
		assertEquals("node 1 adjacent edges outgoing", 1, l.size());
		assertTrue("contains edge 1", l.contains(e1));

		l = n2.getAdjacentEdgeList(CyEdge.Type.UNDIRECTED);
		assertEquals("node 2 adjacent edges undirected", 1, l.size());
		assertTrue("contains edge 5", l.contains(e5));

		l = n2.getAdjacentEdgeList(CyEdge.Type.DIRECTED);
		assertEquals("node 2 adjacent edges directed", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 2", l.contains(e2));
		assertTrue("contains edge 3", l.contains(e3));

		l = n2.getAdjacentEdgeList(CyEdge.Type.INCOMING);
		assertEquals("node 2 adjacent edges incoming", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 3", l.contains(e3));

		l = n2.getAdjacentEdgeList(CyEdge.Type.OUTGOING);
		assertEquals("node 2 adjacent edges outgoing", 1, l.size());
		assertTrue("contains edge 2", l.contains(e2));
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testBasicGetConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);
		CyEdge e3 = net.addEdge(n1, n2, false);
		CyEdge e4 = net.addEdge(n1, n2, false);

		// between node 1 and 2
		List<CyEdge> l = n1.getConnectingEdgeList(n2, CyEdge.Type.ANY);
		assertEquals("connecting edges", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 3", l.contains(e1));
		assertTrue("contains edge 4", l.contains(e4));

		// between node 2 and 3
		l = n3.getConnectingEdgeList(n2, CyEdge.Type.ANY);
		assertEquals("connecting edges", 1, l.size());
		assertTrue("contains edge 2", l.contains(e2));

		// between node 2 and 3 after adding an edge
		CyEdge e5 = net.addEdge(n3, n2, false);
		l = n2.getConnectingEdgeList(n3, CyEdge.Type.ANY);
		assertEquals("connecting edges", 2, l.size());
		assertTrue("contains edge 2", l.contains(e2));
		assertTrue("contains edge 5", l.contains(e5));

		// between node 2 and 3 after deleting an edge
		boolean rem5 = net.removeEdge(e5);
		assertTrue("removed successfully", rem5);
		l = n2.getConnectingEdgeList(n3, CyEdge.Type.ANY);
		assertEquals("connecting edges", 1, l.size());
		assertTrue("contains edge 2", l.contains(e2));
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testUndirectedBasicGetConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);
		CyEdge e3 = net.addEdge(n1, n2, false);
		CyEdge e4 = net.addEdge(n1, n2, true);

		// between node 1 and 2
		List<CyEdge> l = n1.getConnectingEdgeList(n2, CyEdge.Type.UNDIRECTED);
		assertEquals("connecting edges", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 3", l.contains(e3));
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void testDirectedBasicGetConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, true);
		CyEdge e2 = net.addEdge(n2, n3, true);
		CyEdge e3 = net.addEdge(n1, n2, true);
		CyEdge e4 = net.addEdge(n2, n1, true);
		CyEdge e5 = net.addEdge(n2, n1, false);

		// between node 1 and 2
		List<CyEdge> l = n1.getConnectingEdgeList(n2, CyEdge.Type.DIRECTED);
		assertEquals("connecting edges", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 3", l.contains(e3));
		assertTrue("contains edge 4", l.contains(e4));
	}

	public void testDefaultAttributes() {
		CyNode n1 = net.addNode();
		assertEquals( String.class, n1.attrs().contains("name"));
		assertEquals( Boolean.class, n1.attrs().contains("selected"));
	}

	// by default a node should have a null nested network
	public void testInitGetNestedNetwork() {
		CyNode n1 = net.addNode();
		assertNull(n1.getNestedNetwork());
	}

	public void testSetNestedNetwork() {
		CyNode n1 = net.addNode();
		CyNetwork net2 = mock(CyNetwork.class);
		n1.setNestedNetwork( net2 );
		assertNotNull(n1.getNestedNetwork());
		assertEquals(net2, n1.getNestedNetwork());
	}

	// self nested networks are allowed
	public void testSetSelfNestedNetwork() {
		CyNode n1 = net.addNode();
		n1.setNestedNetwork( net );
		assertNotNull(n1.getNestedNetwork());
		assertEquals(net, n1.getNestedNetwork());
	}

	// null nested networks are allowed
	public void testSetNullNestedNetwork() {
		CyNode n1 = net.addNode();

		// put a real network here first 
		CyNetwork net2 = mock(CyNetwork.class);
		n1.setNestedNetwork( net2 );
		assertNotNull(n1.getNestedNetwork());
		assertEquals(net2, n1.getNestedNetwork());
	
		// now put a null network to verify that we've "unset" things
		n1.setNestedNetwork( null );
		assertNull(n1.getNestedNetwork());
	}
}
