package nct.networkblast.graph;

import junit.framework.*;
import java.io.*;
import java.util.logging.Level;

import nct.networkblast.NetworkBlast;

// A JUnit test class for InteractionGraph.java
public class InteractionGraphTest extends TestCase {
    InteractionGraph g;
    InteractionGraph h;

    protected void setUp() {
	try {
	    NetworkBlast.setUpLogging(Level.INFO);
	    g = new InteractionGraph("examples/test.input.sif");
	    h = new InteractionGraph("examples/testNet.input.sif");
	} catch (IOException e) {
	    System.out.println("Can't find test.input");
	    e.printStackTrace();
	    System.exit(0);
	}
    }
    public void testConstruction() {

    	assertTrue("g is null when it shouldn't be", g!=null);

	assertTrue("a is not a node when it should be", g.isNode("a"));
	assertTrue("b is not a node when it should be", g.isNode("b"));
	assertTrue("c is not a node when it should be", g.isNode("c"));
	assertTrue("d is not a node when it should be", g.isNode("d"));
	assertTrue("e is not a node when it should be", g.isNode("e"));

	assertTrue("e-d is not an edge when it should be", g.isEdge("e","d"));
	assertTrue("d-e is not an edge when it should be", g.isEdge("d","e"));
	assertTrue("a-b is not an edge when it should be", g.isEdge("a","b"));
	assertTrue("c-d is not an edge when it should be", g.isEdge("c","d"));

	assertTrue("expected 14 nodes, got: " + h.numberOfNodes(), h.numberOfNodes() == 14);
    }

    public void testgetEdgeWeight() {
	assertTrue("expected edge weight of -1.0, got: " + g.getEdgeWeight("a", "e"),
	           g.getEdgeWeight("a", "e") == -1.0); // edge doesn't exist
	assertTrue("expected edge weight of 0.5, got: " + g.getEdgeWeight("a", "b"),
	           g.getEdgeWeight("a", "b") == .5);
	assertTrue("expected edge weight of 0.5, got: " + g.getEdgeWeight("c", "b"),
	           g.getEdgeWeight("c", "b") == .5);
	assertTrue("expected edge weight of -1.0, got: " + g.getEdgeWeight("a", "c"),
	           g.getEdgeWeight("a", "c") == -1.0); // though connected, edge doesn't exist 
    }

    public void testgetDistance() {
	assertTrue("expected distance 0, got: " + g.getDistance("a", "a"),
	           g.getDistance("a", "a") == 0); 
	assertTrue("expected distance 1, got: " + g.getDistance("a", "b"),
	           g.getDistance("a", "b") == 1); 
	assertTrue("expected distance 2, got: " + g.getDistance("a", "c"),
	           g.getDistance("a", "c") == 2); 
	assertTrue("expected distance 3, got: " + g.getDistance("a", "d"),
	           g.getDistance("a", "d") == 3); 
	assertTrue("expected distance 3, got: " + g.getDistance("a", "e"),
	           g.getDistance("a", "e") == 3); 
    }
}
