/*
 Copyright (c) 2008, 2010, The Cytoscape Consortium (www.cytoscape.org)

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


import java.util.List;

import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 */
public class CyNetworkTest extends TestCase {
	private CyNetwork net;

        public void setUp() {
                net = TestCyNetworkFactory.getInstance();
        }

        public void tearDown() {
                net = null;
        }

	public void testAddNode() {
		final int NODE_COUNT = 100000;
		for (int i = 0; i < NODE_COUNT; ++i)
			net.addNode();
		assertEquals(net.getNodeCount(), NODE_COUNT);

		// Doesn't make sense for subnetworks
		//assertTrue("node index < num node", n.getIndex() < net.getNodeCount());
	}

/*
	public void testRemoveNode() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		// remove 1
		assertEquals("num nodes == 3", 3, net.getNodeCount());

		boolean rem1 = net.removeNode(n1);
		assertTrue("remove node 1 success", rem1);
		assertEquals("num nodes == 2", 2, net.getNodeCount());

		// create a dummy node and try removing that
		CyNode n4 = new DummyCyNode(10);
		boolean rem4 = net.removeNode(n4);
		assertFalse("remove dummy node 4 failure", rem4);
		assertEquals("num nodes == 2", 2, net.getNodeCount());

		// add another node
		CyNode n5 = net.addNode();
		assertEquals("num nodes == 3", 3, net.getNodeCount());

		// remove the rest of the nodes
		boolean rem5 = net.removeNode(n5);
		assertTrue("remove node 5 success", rem5);
		assertEquals("num nodes == 2", 2, net.getNodeCount());

		boolean rem3 = net.removeNode(n3);
		assertTrue("remove node 3 success", rem3);
		assertEquals("num nodes == 1", 1, net.getNodeCount());

		boolean rem2 = net.removeNode(n2);
		assertTrue("remove node 2 success", rem2);
		assertEquals("num nodes == 0", 0, net.getNodeCount());

		// try redundant remove
		rem2 = net.removeNode(n2);
		assertFalse("remove node 2 again fails", rem2);
		assertEquals("num nodes == 0", 0, net.getNodeCount());
	}

	public void testRemoveNodeWithEdges() {
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

		assertEquals("num nodes", 5, net.getNodeCount());
		assertEquals("num edges", 5, net.getNodeCount());

		boolean rem1 = net.removeNode(n1);
		assertTrue("successfully removed", rem1);
		assertEquals("num nodes", 4, net.getNodeCount());
		assertEquals("num edges", 3, net.getEdgeCount());

		boolean rem2 = net.removeNode(n2);
		assertTrue("successfully removed", rem2);
		assertEquals("num nodes", 3, net.getNodeCount());
		assertEquals("num edges", 0, net.getEdgeCount());
	}

	public void testAddEdge() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		// add a directed edge
		CyEdge e1 = net.addEdge(n1, n2, true);
		assertNotNull("edge is not null", e1);
		assertTrue("edge index >= 0", e1.getIndex() >= 0);
		assertTrue("edge index < num edge", e1.getIndex() < net.getEdgeCount());
		assertEquals("edge count", 1, net.getEdgeCount());

		// add an undirected edge
		CyEdge e2 = net.addEdge(n2, n3, false);
		assertNotNull("edge is not null", e2);
		assertTrue("edge index >= 0", e2.getIndex() >= 0);
		assertTrue("edge index < num edge", e2.getIndex() < net.getEdgeCount());
		assertEquals("edge count", 2, net.getEdgeCount());

		// try to add a bad edge
		CyNode n4 = new DummyCyNode(10);

		try {
			CyEdge e3 = net.addEdge(n2, n4, false);
			fail("successfully added an invalid edge");
		} catch (RuntimeException e) {
			assertEquals("edge count", 2, net.getEdgeCount());
		}

		// and again
		try {
			CyEdge e3 = net.addEdge(n4, n2, true);
			fail("successfully added an invalid edge");
		} catch (RuntimeException e) {
			assertEquals("edge count", 2, net.getEdgeCount());
		}

		// try to add a null edge
		try {
			CyEdge e3 = net.addEdge(n2, null, false);
			fail("successfully added a null edge");
		} catch (RuntimeException e) {
			assertEquals("edge count", 2, net.getEdgeCount());
		}

		// and again
		try {
			CyEdge e3 = net.addEdge(null, n2, true);
			fail("successfully added a null edge");
		} catch (RuntimeException e) {
			assertEquals("edge count", 2, net.getEdgeCount());
		}

		// add multiple edges
		CyEdge e3 = net.addEdge(n1, n2, true);
		assertNotNull("edge is not null", e3);
		assertEquals("edge count", 3, net.getEdgeCount());

		CyEdge e4 = net.addEdge(n1, n2, false);
		assertNotNull("edge is not null", e4);
		assertEquals("edge count", 4, net.getEdgeCount());

		// add self edges
		CyEdge e5 = net.addEdge(n1, n1, false);
		assertNotNull("edge is not null", e5);
		assertEquals("edge count", 5, net.getEdgeCount());

		CyEdge e6 = net.addEdge(n1, n1, true);
		assertNotNull("edge is not null", e6);
		assertEquals("edge count", 6, net.getEdgeCount());

		// add duplicate edges
		CyEdge e3two = net.addEdge(n1, n2, true);
		assertNotNull("edge is not null", e3two);
		assertEquals("edge count", 7, net.getEdgeCount());

		CyEdge e4two = net.addEdge(n1, n2, false);
		assertNotNull("edge is not null", e4two);
		assertEquals("edge count", 8, net.getEdgeCount());
	}

	public void testRemoveEdge() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, true);
		CyEdge e2 = net.addEdge(n2, n3, false);
		CyEdge e3 = net.addEdge(n1, n3, false);

		assertEquals("num edges == 3", 3, net.getEdgeCount());

		// basic remove
		boolean rem3 = net.removeEdge(e3);
		assertTrue("remove edge 3 success", rem3);
		assertEquals("num edges == 2", 2, net.getEdgeCount());

		// try to remove dummy edge
		CyEdge e4 = new DummyCyEdge(n1, n2, true, 10);
		boolean rem4 = net.removeEdge(e4);
		assertFalse("remove edge 4 failure", rem4);
		assertEquals("num edges == 2", 2, net.getEdgeCount());

		// try to remove null edge
		boolean remn = net.removeEdge(null);
		assertFalse("remove null edge failure", remn);
		assertEquals("num edges == 2", 2, net.getEdgeCount());

		// add undirected self edge
		CyEdge e5 = net.addEdge(n1, n1, false);
		assertEquals("num edges == 3", 3, net.getEdgeCount());

		// add directed self edge
		CyEdge e6 = net.addEdge(n1, n1, true);
		assertEquals("num edges == 4", 4, net.getEdgeCount());

		// remove self edge
		boolean rem6 = net.removeEdge(e6);
		assertTrue("remove edge 6 success", rem6);
		assertEquals("num edges == 3", 3, net.getEdgeCount());

		// remove the rest
		boolean rem5 = net.removeEdge(e5);
		assertTrue("remove edge 5 success", rem5);
		assertEquals("num edges == 2", 2, net.getEdgeCount());

		boolean rem2 = net.removeEdge(e2);
		assertTrue("remove edge 2 success", rem2);
		assertEquals("num edges == 1", 1, net.getEdgeCount());

		boolean rem1 = net.removeEdge(e1);
		assertTrue("remove edge 1 success", rem1);
		assertEquals("num edges == 0", 0, net.getEdgeCount());

		// try redundant remove
		rem1 = net.removeEdge(e1);
		assertFalse("remove edge 1 again fails", rem1);
		assertEquals("num edges == 0", 0, net.getEdgeCount());
	}

	public void testGetNodeCount() {
		assertEquals("num nodes == 0", 0, net.getNodeCount());

		CyNode n1 = net.addNode();
		assertEquals("num nodes == 1", 1, net.getNodeCount());

		CyNode n2 = net.addNode();
		assertEquals("num nodes == 2", 2, net.getNodeCount());

		CyNode n3 = net.addNode();
		assertEquals("num nodes == 3", 3, net.getNodeCount());

		boolean rem3 = net.removeNode(n3);
		assertTrue("successfully removed node 3", rem3);
		assertEquals("num nodes == 2", 2, net.getNodeCount());

		rem3 = net.removeNode(n3);
		assertFalse("unsuccessfully removed node 3 again", rem3);
		assertEquals("num nodes == 2", 2, net.getNodeCount());

		CyNode n4 = net.addNode();
		assertEquals("num nodes == 3", 3, net.getNodeCount());
	}

	public void testGetEdgeCount() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, true);
		CyEdge e2 = net.addEdge(n2, n3, false);
		CyEdge e3 = net.addEdge(n1, n3, false);

		assertEquals("num edges == 3", 3, net.getEdgeCount());

		// basic remove
		boolean rem3 = net.removeEdge(e3);
		assertTrue("remove edge 3 success", rem3);
		assertEquals("num edges == 2", 2, net.getEdgeCount());
	}

	public void testGetNodeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		// check list
		List<CyNode> l = net.getNodeList();
		assertEquals("list size", 3, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 2", l.contains(n2));
		assertTrue("contains node 3", l.contains(n3));

		// remove a node and check again
		boolean rem2 = net.removeNode(n2);
		l = net.getNodeList();
		assertEquals("list size", 2, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertFalse("contains node 2", l.contains(n2));
		assertTrue("contains node 3", l.contains(n3));

		// remove a dummy node and check again
		CyNode n4 = new DummyCyNode(10);
		boolean rem4 = net.removeNode(n4);
		l = net.getNodeList();
		assertEquals("list size", 2, l.size());
	}

	public void testGetEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, true);
		CyEdge e2 = net.addEdge(n2, n3, false);
		CyEdge e3 = net.addEdge(n1, n3, false);

		// check list
		List<CyEdge> l = net.getEdgeList();
		assertEquals("edge list size", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 2", l.contains(e2));
		assertTrue("contains edge 3", l.contains(e3));

		// add an edge and check again
		CyEdge e4 = net.addEdge(n1, n1, false);
		l = net.getEdgeList();
		assertEquals("edge list size", 4, l.size());
		assertTrue("contains edge 4", l.contains(e4));

		// remove an edge and check again
		boolean rem3 = net.removeEdge(e3);
		l = net.getEdgeList();
		assertEquals("edge list size", 3, l.size());
		assertFalse("contains edge 3", l.contains(e3));

		// remove a dummy edge and check again
		CyEdge e5 = new DummyCyEdge(n1, n2, true, 10);
		boolean rem5 = net.removeEdge(e5);
		l = net.getEdgeList();
		assertFalse("remove dummy edge 5 failure", rem5);
		assertEquals("edge list size", 3, l.size());
	}

	public void testIsNode() {
		CyNode n1 = net.addNode();
		CyNode n2 = new DummyCyNode(20);
		assertTrue("node 1 is good", net.containsNode(n1));
		assertFalse("node 2 is not", net.containsNode(n2));
	}

	public void testIsEdgeFromEdge() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, true);
		CyEdge e2 = new DummyCyEdge(n1, n2, true, 10);

		assertTrue("edge 1 is good", net.containsEdge(e1));
		assertFalse("edge 2 is not", net.containsEdge(e2));
	}

	public void testIsEdgeFromNodes() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = new DummyCyNode(699);

		CyEdge e1 = net.addEdge(n1, n2, true);

		assertTrue("edge 1 is good", net.containsEdge(n1, n2));
		assertFalse("not an edge", net.containsEdge(n3, n2));
		assertFalse("not an edge", net.containsEdge(n1, n1));
		assertFalse("not an edge", net.containsEdge(n2, n3));
		assertFalse("bad target node", net.containsEdge(n2, n4));
		assertFalse("bad source node", net.containsEdge(n4, n1));
	}

	public void testBasicGetNeighborList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);

		// one neighbor
		List<CyNode> l = net.getNeighborList(n1, CyEdge.Type.ANY);
		assertEquals("one neighbor", 1, l.size());
		assertTrue("contains node 2", l.contains(n2));

		// two neighbors
		l = net.getNeighborList(n2, CyEdge.Type.ANY);
		assertEquals("two neighbors", 2, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 3", l.contains(n3));
		assertFalse("contains node 4", l.contains(n4));

		// no neighbors
		l = net.getNeighborList(n4, CyEdge.Type.ANY);
		assertEquals("no neighbors", 0, l.size());

		// whoa!  what about self edges?
		// TODO
		CyEdge e3 = net.addEdge(n4, n4, false);
		l = net.getNeighborList(n4, CyEdge.Type.ANY);
		assertEquals("one neighbor?", 1, l.size());

		CyEdge e4 = net.addEdge(n4, n4, true);
		l = net.getNeighborList(n4, CyEdge.Type.ANY);
		assertEquals("two neighbors", 2, l.size());
	}

	public void testBadNodeNeighborList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = new DummyCyNode(699);

		CyEdge e1 = net.addEdge(n1, n2, false);

		try {
			List<CyNode> l = net.getNeighborList(n3, CyEdge.Type.ANY);
			assertNotNull(l);
			assertEquals(0,l.size());
		} catch (IllegalArgumentException iae) {
			fail("caught unexpected exception");
		}
	}

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

		List<CyNode> l = net.getNeighborList(n1, CyEdge.Type.UNDIRECTED);
		assertEquals("node 1 neighbors", 2, l.size());
		assertTrue("contains node 2", l.contains(n2));
		assertTrue("contains node 4", l.contains(n4));

		l = net.getNeighborList(n2, CyEdge.Type.UNDIRECTED);
		assertEquals("node 2 neighbors", 3, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 3", l.contains(n3));
		assertTrue("contains node 4", l.contains(n4));

		l = net.getNeighborList(n2, CyEdge.Type.ANY);
		assertEquals("node 2 neighbors", 3, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 3", l.contains(n3));
		assertTrue("contains node 4", l.contains(n4));

		l = net.getNeighborList(n2, CyEdge.Type.INCOMING);
		assertEquals("node 2 neighbors", 0, l.size());

		l = net.getNeighborList(n2, CyEdge.Type.OUTGOING);
		assertEquals("node 2 neighbors", 0, l.size());

		l = net.getNeighborList(n2, CyEdge.Type.DIRECTED);
		assertEquals("node 2 neighbors", 0, l.size());
	}

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

		List<CyNode> l = net.getNeighborList(n1, CyEdge.Type.DIRECTED);
		assertEquals("node 1 neighbors directed", 2, l.size());
		assertTrue("contains node 2", l.contains(n2));
		assertTrue("contains node 4", l.contains(n4));

		l = net.getNeighborList(n1, CyEdge.Type.INCOMING);
		assertEquals("node 1 neighbors incoming", 1, l.size());
		assertTrue("contains node 4", l.contains(n4));

		l = net.getNeighborList(n1, CyEdge.Type.OUTGOING);
		assertEquals("node 1 neighbors outgoing", 1, l.size());
		assertTrue("contains node 2", l.contains(n2));

		l = net.getNeighborList(n2, CyEdge.Type.UNDIRECTED);
		assertEquals("node 2 neighbors undirected", 1, l.size());
		assertTrue("contains node 5", l.contains(n5));

		l = net.getNeighborList(n2, CyEdge.Type.DIRECTED);
		assertEquals("node 2 neighbors directed", 3, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 3", l.contains(n3));
		assertTrue("contains node 4", l.contains(n4));

		l = net.getNeighborList(n2, CyEdge.Type.INCOMING);
		assertEquals("node 2 neighbors incoming", 2, l.size());
		assertTrue("contains node 1", l.contains(n1));
		assertTrue("contains node 4", l.contains(n4));

		l = net.getNeighborList(n2, CyEdge.Type.OUTGOING);
		assertEquals("node 2 neighbors outgoing", 1, l.size());
		assertTrue("contains node 3", l.contains(n3));
	}

	public void testBasicGetAdjacentEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();
		CyNode n4 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);

		// one edge
		List<CyEdge> l = net.getAdjacentEdgeList(n1, CyEdge.Type.ANY);
		assertEquals("one adjacent edge", 1, l.size());
		assertTrue("contains edge 1", l.contains(e1));

		// two edge
		l = net.getAdjacentEdgeList(n2, CyEdge.Type.ANY);
		assertEquals("two adjacent edges", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 2", l.contains(e2));

		// no adjacent edges
		l = net.getAdjacentEdgeList(n4, CyEdge.Type.ANY);
		assertEquals("no edges", 0, l.size());

		// whoa!  what about self edges?
		// TODO
		CyEdge e3 = net.addEdge(n4, n4, false);
		l = net.getAdjacentEdgeList(n4, CyEdge.Type.ANY);
		assertEquals("one adjacent edge?", 1, l.size());
	}

	public void testBadNodeAdjacentEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = new DummyCyNode(699);

		CyEdge e1 = net.addEdge(n1, n2, false);

		try {
			List<CyEdge> l = net.getAdjacentEdgeList(n3, CyEdge.Type.ANY);
			assertNotNull(l);
			assertEquals(0,l.size());
		} catch (Exception iae) {
			fail("caught unexpected exception");
		}
	}

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

		List<CyEdge> l = net.getAdjacentEdgeList(n1, CyEdge.Type.UNDIRECTED);
		assertEquals("node 1 adjacent edges", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 4", l.contains(e4));

		l = net.getAdjacentEdgeList(n2, CyEdge.Type.UNDIRECTED);
		assertEquals("node 2 adjacent edges", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 2", l.contains(e2));
		assertTrue("contains edge 3", l.contains(e3));

		l = net.getAdjacentEdgeList(n2, CyEdge.Type.ANY);
		assertEquals("node 2 adjacent edges", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 2", l.contains(e2));
		assertTrue("contains edge 3", l.contains(e3));

		l = net.getAdjacentEdgeList(n2, CyEdge.Type.INCOMING);
		assertEquals("node 2 adjacent edges", 0, l.size());

		l = net.getAdjacentEdgeList(n2, CyEdge.Type.OUTGOING);
		assertEquals("node 2 adjacent edges", 0, l.size());

		l = net.getAdjacentEdgeList(n2, CyEdge.Type.DIRECTED);
		assertEquals("node 2 adjacent edges", 0, l.size());
	}

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

		List<CyEdge> l = net.getAdjacentEdgeList(n1, CyEdge.Type.DIRECTED);
		assertEquals("node 1 adjacent edges directed", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 4", l.contains(e4));

		l = net.getAdjacentEdgeList(n1, CyEdge.Type.INCOMING);
		assertEquals("node 1 adjacent edges incoming", 1, l.size());
		assertTrue("contains edge 4", l.contains(e4));

		l = net.getAdjacentEdgeList(n1, CyEdge.Type.OUTGOING);
		assertEquals("node 1 adjacent edges outgoing", 1, l.size());
		assertTrue("contains edge 1", l.contains(e1));

		l = net.getAdjacentEdgeList(n2, CyEdge.Type.UNDIRECTED);
		assertEquals("node 2 adjacent edges undirected", 1, l.size());
		assertTrue("contains edge 5", l.contains(e5));

		l = net.getAdjacentEdgeList(n2, CyEdge.Type.DIRECTED);
		assertEquals("node 2 adjacent edges directed", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 2", l.contains(e2));
		assertTrue("contains edge 3", l.contains(e3));

		l = net.getAdjacentEdgeList(n2, CyEdge.Type.INCOMING);
		assertEquals("node 2 adjacent edges incoming", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 3", l.contains(e3));

		l = net.getAdjacentEdgeList(n2, CyEdge.Type.OUTGOING);
		assertEquals("node 2 adjacent edges outgoing", 1, l.size());
		assertTrue("contains edge 2", l.contains(e2));
	}

	public void testBasicGetConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);
		CyEdge e3 = net.addEdge(n1, n2, false);
		CyEdge e4 = net.addEdge(n1, n2, false);

		// between node 1 and 2
		List<CyEdge> l = net.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY);
		assertEquals("connecting edges", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 3", l.contains(e1));
		assertTrue("contains edge 4", l.contains(e4));

		// between node 2 and 3
		l = net.getConnectingEdgeList(n3, n2, CyEdge.Type.ANY);
		assertEquals("connecting edges", 1, l.size());
		assertTrue("contains edge 2", l.contains(e2));

		// between node 2 and 3 after adding an edge
		CyEdge e5 = net.addEdge(n3, n2, false);
		l = net.getConnectingEdgeList(n2, n3, CyEdge.Type.ANY);
		assertEquals("connecting edges", 2, l.size());
		assertTrue("contains edge 2", l.contains(e2));
		assertTrue("contains edge 5", l.contains(e5));

		// between node 2 and 3 after deleting an edge
		boolean rem5 = net.removeEdge(e5);
		assertTrue("removed successfully", rem5);
		l = net.getConnectingEdgeList(n2, n3, CyEdge.Type.ANY);
		assertEquals("connecting edges", 1, l.size());
		assertTrue("contains edge 2", l.contains(e2));
	}

	public void testBadTargetNodeConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = new DummyCyNode(699);

		CyEdge e1 = net.addEdge(n1, n2, false);

		try {
			List<CyEdge> l = net.getConnectingEdgeList(n2, n3, CyEdge.Type.ANY);
			assertNotNull(l);
			assertEquals(0,l.size());
		} catch (Exception iae) {
			fail("caught unexpected exception");
		}
	}

	public void testBadSourceNodeConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = new DummyCyNode(699);

		CyEdge e1 = net.addEdge(n1, n2, false);

		try {
			List<CyEdge> l = net.getConnectingEdgeList(n3, n1, CyEdge.Type.ANY);
			assertNotNull(l);
			assertEquals(0,l.size());
		} catch (Exception iae) {
			fail("caught unexpected exception");
		}
	}

	public void testUndirectedBasicGetConnectingEdgeList() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, false);
		CyEdge e2 = net.addEdge(n2, n3, false);
		CyEdge e3 = net.addEdge(n1, n2, false);
		CyEdge e4 = net.addEdge(n1, n2, true);

		// between node 1 and 2
		List<CyEdge> l = net.getConnectingEdgeList(n1, n2, CyEdge.Type.UNDIRECTED);
		assertEquals("connecting edges", 2, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 3", l.contains(e3));
	}

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
		List<CyEdge> l = net.getConnectingEdgeList(n1, n2, CyEdge.Type.DIRECTED);
		assertEquals("connecting edges", 3, l.size());
		assertTrue("contains edge 1", l.contains(e1));
		assertTrue("contains edge 3", l.contains(e3));
		assertTrue("contains edge 4", l.contains(e4));
	}

	public void testGetNode() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		assertEquals("nodes are equivalent", n1, net.getNode(n1.getIndex()));
		assertEquals("nodes are equivalent", n2, net.getNode(n2.getIndex()));
		assertEquals("nodes are equivalent", n3, net.getNode(n3.getIndex()));

		// test random index
		assertNull("node is null ", net.getNode(72));

		// test 0 index - the first node
		assertNotNull("node is NOT null ", net.getNode(0));

		// test -1 index
		assertNull("node is null ", net.getNode(-1));
	}

	public void testGetEdge() {
		CyNode n1 = net.addNode();
		CyNode n2 = net.addNode();
		CyNode n3 = net.addNode();

		CyEdge e1 = net.addEdge(n1, n2, true);
		CyEdge e2 = net.addEdge(n2, n3, true);

		assertEquals("edges are equivalent", e1, net.getEdge(e1.getIndex()));
		assertEquals("edges are equivalent", e2, net.getEdge(e2.getIndex()));

		// test random index
		assertNull("edge is null ", net.getEdge(72));

		// test 0 index - the first node
		assertNotNull("edge is NOT null ", net.getEdge(0));

		// test -1 index
		assertNull("edge is null ", net.getEdge(-1));
	}

	public void testGetCyRow() {
		// As long as the object is not null and is an instance of CyRow, we
		// should be satisfied.  Don't test any other properties of CyRow.
		// Leave that to the CyRow unit tests.
		assertNotNull("cyattrs exists", net.getCyRow("USER"));
		assertTrue("cyattrs is CyRow", net.getCyRow("USER") instanceof CyRow);
	}

	public void testGetCyRowNullNamespace() {
		try {
			net.getCyRow(null);
			fail("didn't throw a NullPointerException for null namespace");
		} catch (NullPointerException npe) {
			return;
		}

		fail("didn't catch what was thrown");
	}

	public void testCyRowBadNamespace() {
		try {
			net.getCyRow("homeradfasdf");
			fail("didn't throw a NullPointerException for null namespace");
		} catch (NullPointerException npe) {
			return;
		}

		fail("didn't catch what was thrown");
	}

	public void testDefaultAttributes() {
		assertEquals(String.class, net.getCyRow().getTable().getColumn("name").getType());
	}

	public void testGetDefaultNetworkTable() {
		assertNotNull(net.getDefaultNetworkTable());
		assertEquals(String.class, net.getDefaultNetworkTable().getColumn("name").getType());
	}

	public void testGetDefaultNodeTable() {
		assertNotNull(net.getDefaultNodeTable());
		assertEquals(String.class, net.getDefaultNodeTable().getColumn("name").getType());
	}

	public void testGetDefaultEdgeTable() {
		assertNotNull(net.getDefaultEdgeTable());
		assertEquals(String.class, net.getDefaultEdgeTable().getColumn("name").getType());
	}
*/
}
