package nct.graph.basic;

import junit.framework.*;
import java.util.*;
import java.util.logging.Level;

import nct.networkblast.NetworkBlast;
import nct.graph.*;

// A JUnit test class for BasicDistanceGraph.java
public class BasicDistanceGraphTest extends TestCase {
    BasicDistanceGraph<String,Double> g;
    protected void setUp() {
        NetworkBlast.setUpLogging(Level.WARNING);
	g = new BasicDistanceGraph<String,Double>();
    }

    public void testgetDistance() {
	// creates the following network
	//  A -------- B         G
        //  |         /
        //  |        /
        //  C    D-E-
        //       |
        //       F
	//
   
   	assertTrue(g.addNode("A"));
   	assertTrue(g.addNode("B"));
   	assertTrue(g.addNode("C"));
   	assertTrue(g.addNode("D"));
   	assertTrue(g.addNode("E"));
   	assertTrue(g.addNode("F"));
	assertTrue(g.addNode("G"));

	assertTrue(g.addEdge("C", "A", 0.1));
	assertTrue(g.addEdge("B", "A", 0.2));
	assertTrue(g.addEdge("B", "E", 0.3));
	assertTrue(g.addEdge("D", "E", 0.4));
	assertTrue(g.addEdge("D", "F", 0.5));

	assertTrue(g.getDistance(null, "A") == -1.0);  // check nulls
	assertTrue(g.getDistance("G", null) == -1.0);
	assertTrue(g.getDistance(null, null) == -1.0);		   
	assertTrue(g.getDistance("X", "A") == -1.0); // check non existant
	assertTrue(g.getDistance("B", "Y") == -1.0); 
	assertTrue(g.getDistance("X", "Y") == -1.0); 
	assertTrue(g.getDistance("C", "C") == 0);  // check 0
	assertTrue(g.getDistance("C", "A") == 1);  // check 1
	assertTrue(g.getDistance("A", "C") == 1);  // check bidirectionality
	assertTrue(g.getDistance("C", "B") == 2);  // check 2
	assertTrue(g.getDistance("B", "C") == 2);  // check bidirectionality
	assertTrue(g.getDistance("C", "E") == 3);  // check 3
	assertTrue(g.getDistance("E", "C") == 3);  // check bidirectionality
	assertTrue(g.getDistance("C", "G") == 3);  // check unconnected (3)
	assertTrue(g.getDistance("G", "C") == 3);  // check bidirectionality
	assertTrue(g.getDistance("C", "D") == 3);  // check far (3)
	assertTrue(g.getDistance("D", "C") == 3);  // check bidirectionality
	assertTrue(g.getDistance("C", "F") == 3);  // check distant
	assertTrue(g.getDistance("F", "C") == 3);  // check distant bidir
    }
   
    public static Test suite() {
	return new TestSuite(BasicDistanceGraphTest.class);
    }

}
