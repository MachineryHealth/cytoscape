
//============================================================================
// 
//  file: LocalBlastTest.java
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



package nct.service.homology;

import junit.framework.*;

import java.util.*;
import java.util.logging.Level;
import java.io.*;
import java.lang.*;

import nct.graph.*;
import nct.service.synonyms.*;
import nct.service.interactions.*;
import nct.xml.*;

import org.xml.sax.helpers.*;
import org.xml.sax.*;
import org.xml.*;


// A JUnit test class for LocalBlastTest.java
public class LocalBlastTest extends TestCase {

	boolean noExceptions;
	DIPSynonyms synonyms;
	LocalBlast smallNorm;
	LocalBlast largeNorm;
	String outLoc;

	BlastGraph<String,Double> b1;
	BlastGraph<String,Double> b2;

	protected void setUp() {
		try { 
		noExceptions = true;
		synonyms = new DIPSynonyms();

		XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler(synonyms);
		xr.parse(new InputSource(new FileReader("examples/Gallus_gallus.xin")));

		Properties props = System.getProperties();
		outLoc = props.getProperty("java.io.tmpdir") + props.getProperty("file.separator") + "LocalBlastTest." + System.currentTimeMillis() + ".xml";
		props.load( new FileInputStream("examples/blast.properties"));

		smallNorm = new LocalBlast(props,synonyms, outLoc, 1.0e-10); 
		largeNorm = new LocalBlast(props,synonyms, outLoc, 10.0); 

		b1 = new BlastGraph<String,Double>("Gallus_gallus.fa","examples");
		DIPInteractionNetwork dipin1 = new DIPInteractionNetwork("Gallus gallus");
		dipin1.updateGraph(b1);

		b2 = new BlastGraph<String,Double>("Gallus_gallus.fa","examples");
		DIPInteractionNetwork dipin2 = new DIPInteractionNetwork("Gallus gallus");
		dipin2.updateGraph(b2);

		XMLSaxEventDistributor<DIPInteractionNetwork> xml = new XMLSaxEventDistributor<DIPInteractionNetwork>();
		xml.addHandler(dipin1);
		xml.addHandler(dipin2);
		xml.parse("examples/Gallus_gallus.xin");

		} catch (Exception e) { e.printStackTrace(); noExceptions = false; }

	}

	public void testBlast() {
		// sanity check
		assertTrue( noExceptions );
		assertEquals("expect 8 nodes, got: " + b1.numberOfNodes(), 8, b1.numberOfNodes());
		assertEquals("expect 8 nodes, got: " + b2.numberOfNodes(), 8, b2.numberOfNodes());
		assertEquals("expect 3 edge, got: " + b1.numberOfEdges(), 3, b1.numberOfEdges());
		assertEquals("expect 3 edge, got: " + b2.numberOfEdges(), 3, b2.numberOfEdges());

		for ( Edge<String,Double> e : b1.getEdges() )
			System.out.println( "edge " + e.toString() ); 


		Map<String,Map<String,Double>> map = smallNorm.expectationValues(b1,b2);
		checkMap(map,false);
		map = largeNorm.expectationValues(b1,b2);
		checkMap(map,true);
	}

	private void checkMap(Map<String,Map<String,Double>> map,boolean nonHomologs) {
		System.out.println("map size: " + map.size());
		assertTrue( map.size() > 0 );
		int numChecks = 0;
		for ( String key : map.keySet() ) {
			System.out.println("key " + key);
			for ( String key2 : map.get(key).keySet() ) {
				System.out.println(key + " " + key2 + " " + map.get(key).get(key2));
				if ( key.equals(key2) ) {
					assertEquals(0.0, map.get(key).get(key2),0.00000001);
					numChecks++;
				}
				else if ( key.equals("BMRB_CHICK") && key2.equals("PIR:S33568") ||
				          key2.equals("BMRB_CHICK") && key.equals("PIR:S33568") ) {
					assertTrue( "expect ~1.2e-18: got " +  map.get(key).get(key2), 
					            map.get(key).get(key2) > 1e-19 && 
					            map.get(key).get(key2) < 1e-17 );
					numChecks++;
				}
				else if ( nonHomologs ) {
					if (key.equals("HSF3_CHICK") && key2.equals("ATNB_CHICK")) {
						assertTrue("expect ~6.6: got " +  map.get(key).get(key2),map.get(key).get(key2) > 6 );
						numChecks++;
					}
					if (key2.equals("PRGR_CHICK") && key.equals("A1A1_CHICK")) {
						assertTrue("expect ~1.8: got " +  map.get(key).get(key2),map.get(key).get(key2) > 1.5 );
						numChecks++;
					}
				}
			}
		}
		assertTrue( numChecks > 0 );
	}

	protected void tearDown() {
		File f = new File(outLoc);
		f.delete();
	}
   
	public static Test suite() {
		return new TestSuite(LocalBlastTest.class);
	}
}
