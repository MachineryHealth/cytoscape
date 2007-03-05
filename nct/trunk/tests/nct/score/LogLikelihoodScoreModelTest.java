
//============================================================================
// 
//  file: LogLikelihoodScoreModelTest.java
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



package nct.score;

import junit.framework.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

import nct.graph.*;
import nct.networkblast.*;


public class LogLikelihoodScoreModelTest extends TestCase {
    InteractionGraph h, i;
    LogLikelihoodScoreModel<String> s;
    protected void setUp() {
        NetworkBlast.setUpLogging(Level.WARNING);
	try {
	    h = new InteractionGraph("examples/testScore.input.sif");
	    s = new LogLikelihoodScoreModel<String>(2.5, 0.8, 1e-10);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public void testscoreGraph() {
    	s.scoreEdge("a","b",h); // ensures scoreGraph is run
	GraphProbs probs = s.graphMap.get(h);
	assertNotNull(probs);
	System.out.println(probs.toString());
	assertTrue(probs.pTrue > 0.583 && probs.pTrue < 0.584); // == 0.58333333...
	assertTrue(probs.pObs > 0.333 && probs.pObs < 0.334); // == 1/3
	assertEquals(0.525, probs.pTrueGivenNotObs); 
	assertEquals((long)9.0, probs.exNumInt);
    }

    public void testscoreEdge() {

	assertEquals(Double.NEGATIVE_INFINITY, s.scoreEdge(null, null, h)); 
	assertEquals(Double.NEGATIVE_INFINITY, s.scoreEdge(null, null, null));
	assertEquals(Double.NEGATIVE_INFINITY, s.scoreEdge("a", "c", null));
	/*
	TODO
	assertEquals(0.0, s.scoreEdge("a", null, h));
	assertEquals(0.0, s.scoreEdge("a", "c", h));
	assertEquals(0.0, s.scoreEdge("a", "x", h)); // edge doesn't exist
	*/

	System.out.println("Score: a b " + s.scoreEdge("a", "b", h));
	System.out.println("Score: b c " + s.scoreEdge("b", "c", h));
	System.out.println("Score: c d " + s.scoreEdge("c", "d", h));
	System.out.println("Score: d e " + s.scoreEdge("d", "e", h));
	System.out.println("Score: e f " + s.scoreEdge("e", "f", h));
/*
	// Jason's style
	assertTrue(s.scoreEdge("a", "b", h) < -0.13485 && s.scoreEdge("a", "b", h) > -0.13486);
	assertTrue(s.scoreEdge("b", "c", h) > 0.02115 && s.scoreEdge("b", "c", h) < 0.02116);
	assertTrue(s.scoreEdge("c", "d", h) > 0.12736 && s.scoreEdge("c", "d", h) < 0.12737);
	assertTrue(s.scoreEdge("d", "e", h) > 0.20007 && s.scoreEdge("d", "e", h) < 0.20008);
	assertTrue(s.scoreEdge("e", "f", h) > 0.29142 && s.scoreEdge("e", "f", h) < 0.29143);
	*/
    }
    public static Test suite() { return new TestSuite( LogLikelihoodScoreModelTest.class ); }
}
	    
