
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

package org.cytoscape.event;


import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 */
public class AbstractCyPayloadEventTest {

	private static class TestEvent<T,P> extends AbstractCyPayloadEvent<T,P> {
		TestEvent(T src, Class<?> c, Collection<P> payload) {
			super(src,c,payload);
		}
	}

	@Test
	public void testOnlyAnsynchronous() {
		List<String> l = new ArrayList<String>();
		l.add("homer");
		l.add("marge");
		Object source = new Object(); 
		TestEvent<Object,String> e = new TestEvent<Object,String>(source,Object.class,l);
		assertFalse( e.synchronousOnly() );	
	}

	@Test
	public void testGetPayload() {
		List<String> l = new ArrayList<String>();
		l.add("homer");
		l.add("marge");
		Object source = new Object(); 
		TestEvent<Object,String> e = new TestEvent<Object,String>(source,Object.class,l);
		Collection<String> payload = e.getPayloadCollection();
		assertEquals(2,payload.size());
		assertTrue(payload.contains("homer"));
		assertTrue(payload.contains("marge"));
		assertFalse(payload.contains("bart"));
	}

	@Test(expected=NullPointerException.class)
	public void testGetPayloadNull() {
		Object source = new Object(); 
		TestEvent<Object,String> e = new TestEvent<Object,String>(source,Object.class,null);
	}

	@Test
	public void testGetEmptyPayload() {
		List<String> l = new ArrayList<String>();
		Object source = new Object(); 
		TestEvent<Object,String> e = new TestEvent<Object,String>(source,Object.class,l);
		Collection<String> payload = e.getPayloadCollection();
		assertEquals(0,payload.size());
		assertFalse(payload.contains("bart"));
	}
}
