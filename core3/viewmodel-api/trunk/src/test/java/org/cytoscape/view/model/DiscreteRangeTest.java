package org.cytoscape.view.model;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;


public class DiscreteRangeTest {
	
	
	@Test
	public void testDiscreteRange() {
		
		final Set<String> rangeValues = new HashSet<String>();
		rangeValues.add("a");
		rangeValues.add("b");
		rangeValues.add("c");
		rangeValues.add("d");
		rangeValues.add("e");
		rangeValues.add("foo");
		
		final DiscreteRange<String> range1 = new DiscreteRange<String>(String.class, rangeValues);
		
		assertEquals(6, range1.values().size());
		assertTrue(range1.validate("a"));
		assertTrue(range1.validate("b"));
		assertTrue(range1.validate("c"));
		assertTrue(range1.validate("d"));
		assertTrue(range1.validate("e"));
		assertFalse(range1.validate("f"));
		assertTrue(range1.validate("foo"));
		
		range1.addRangeValue("f");
		assertEquals(7, range1.values().size());
		assertTrue(range1.validate("f"));
		
	}

}
