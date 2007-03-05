
//============================================================================
// 
//  file: CompatibilityGraphTest.java
// 
//  Copyright (c) 2006, University of California San Diego 
// 
//  This program is free software; you can redistribute it and/or modify it 
//  under the terms of the GNU General Public License as published by the 
//  Free Software Foundation; either version 2 of the License, or (at your 
//  option) any later version.
//  
//  This program is distributed in the hope that it will be useful, but 
//  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
//  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
//  for more details.
//  
//  You should have received a copy of the GNU General Public License along 
//  with this program; if not, write to the Free Software Foundation, Inc., 
//  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
//============================================================================



package nct.networkblast;

import junit.framework.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

import nct.score.*;
import nct.graph.*;
import nct.service.homology.*;

public class CompatibilityGraphTest extends TestCase {
    CompatibilityGraph g;
    InteractionGraph h, i;
    HomologyGraph homologyGraph;
    protected void setUp() {
	try {
	    NetworkBlast.setUpLogging(Level.CONFIG);
	    i = new InteractionGraph("examples/junit.inputA.sif");
	    h = new InteractionGraph("examples/junit.inputB.sif");
	    List<SequenceGraph<String,Double>> inputSpecies = new ArrayList<SequenceGraph<String,Double>>();
	    inputSpecies.add(i);
	    inputSpecies.add(h);
	    SIFHomologyReader sr = new SIFHomologyReader("examples/junit.compat.sif");
	    homologyGraph = new HomologyGraph(sr, 1e-5, inputSpecies);
	    LogLikelihoodScoreModel<String> lls = new LogLikelihoodScoreModel<String>(2.5,0.8,1e-10);
	    //CompatibilityCalculator<CompatibilityNode<String,Double>> compatCalc = new AdditiveCompatibilityCalculator(0.01,lls,true);
	    //g = new CompatibilityGraph(homologyGraph, inputSpecies, compatCalc );
	    g = new CompatibilityGraph(homologyGraph, inputSpecies, lls );
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
    }
    public void testnumberOfNodes() {
	System.out.println("hom num nodes:  " + homologyGraph.numberOfNodes());
	System.out.println("hom num edges:  " + homologyGraph.numberOfEdges());
	System.out.println("g num nodes:  " + g.numberOfNodes());
	System.out.println("h num nodes:  " + h.numberOfNodes());
	System.out.println("i num nodes:  " + i.numberOfNodes());
	System.out.println("g : " + g.toString());

	assertNotNull("g is null when it shouldn't be",g);
	assertEquals(9, homologyGraph.numberOfNodes());
	assertEquals(3, g.numberOfNodes());
	assertEquals(5, h.numberOfNodes());
	assertEquals(4, i.numberOfNodes());
    }

    public void testnumberOfEdges() {
	System.out.println("hom num edges:  " + homologyGraph.numberOfEdges());
	System.out.println("g num edges:  " + g.numberOfEdges());
	System.out.println("h num edges:  " + h.numberOfEdges());
	System.out.println("i num edges:  " + i.numberOfEdges());

	Set<Edge<CompatibilityNode<String,Double>,Double>> edges = g.getEdges();
	for (Edge e : edges) {
		System.out.print("src node: " + e.getSourceNode()); 
		System.out.print("  target node: " + e.getTargetNode()); 
		System.out.println("  weight: " + e.getWeight()); 
	}

	assertEquals(2, g.numberOfEdges());
	assertEquals(5, h.numberOfEdges());
	assertEquals(3, i.numberOfEdges());

    }

    public void testgetEdgeWeight() {
    /*
	System.out.println("A1|B1 A1|B2: " + g.getEdgeWeight("A1|B1", "A1|B2") );
	System.out.println("A1|B1 A1|B3: " + g.getEdgeWeight("A1|B1", "A1|B3") );
	System.out.println("A1|B1 A2|B1: " + g.getEdgeWeight("A1|B1", "A2|B1") );
	*/
	
	//assertTrue(g.getEdgeWeight("a|a", "e|e") == -2.0);
	//assertTrue(g.getEdgeWeight("a|a", "b|b") == .25);
	//assertTrue(g.getEdgeWeight("a|a", "c|c") == .0625);
	//assertTrue(g.addEdge("e|e", "a|a", 0));
	//assertTrue(g.getEdgeWeight("e|e", "b|b") == 0); // best path no matter what the distance?
    }

    public void testgetEdgeDescription() {
	assertNotNull("g is null when it shouldn't be", g);
	assertNull("edge desc for two null edges is not null", g.getEdgeDescription(null, null));
	System.out.println("node set " + g.getNodes());
	//System.out.println("edge desc A3|B1 A1|B2: " +g.getEdgeDescription("A3|B1", "A1|B2"));
	//System.out.println("edge desc A2|B1 A1|B1: " +g.getEdgeDescription("A2|B1", "A1|B1"));
	//assertTrue("edge desc expected: 21 while we got: " + g.getEdgeDescription("A3|B1", "A1|B2"),
	 //           g.getEdgeDescription("A3|B1", "A1|B2").equals("21"));
	//assertTrue("edge desc expected: 10 while we got: " + g.getEdgeDescription("A2|B1", "A1|B1"),
	 //           g.getEdgeDescription("A2|B1", "A1|B1").equals("10"));
    }
    public static Test suite() { return new TestSuite(CompatibilityGraphTest.class ); }
}
