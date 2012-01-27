/*
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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


package org.cytoscape.task;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import org.cytoscape.view.model.CyNetworkView;

import org.cytoscape.work.TaskIterator;

import java.util.Collection;

public class AbstractNetworkViewCollectionTaskFactoryTest {
	
	private class NetworkViewCollectionTaskFactory extends AbstractNetworkViewCollectionTaskFactory {
		public TaskIterator createTaskIterator(NetworkViewCollectionTaskContext context) {
			return null;
		}
	}

	NetworkViewCollectionTaskFactory factory; 

	@Before
	public void setUp() {
		factory = new NetworkViewCollectionTaskFactory();
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullSetNetworkViewCollection() throws Exception {
		NetworkViewCollectionTaskContext context = factory.createTaskContext();
		context.setNetworkViewCollection(null);
	}

	@Test
	public void testGoodSetNetworkViewCollection() throws Exception {
		NetworkViewCollectionTaskContext context = factory.createTaskContext();
		context.setNetworkViewCollection((Collection<CyNetworkView>)mock(Collection.class));
		assertNotNull( context.networkViews );
	}

	@Test
	public void testNotFinal() throws Exception {
		NetworkViewCollectionTaskContext context = factory.createTaskContext();
		context.setNetworkViewCollection((Collection<CyNetworkView>)mock(Collection.class));
		Collection<CyNetworkView> t1 = context.networkViews;
		context.setNetworkViewCollection((Collection<CyNetworkView>)mock(Collection.class));
		Collection<CyNetworkView> t2 = context.networkViews;
		assertFalse( (t1 == t2) );
	}
}
