package org.cytoscape.session.events;


import org.cytoscape.session.CyNetworkManager;
import org.cytoscape.model.CyNetwork;

import static org.junit.Assert.*;
import org.junit.Test;

import static org.mockito.Mockito.*;


public class NetworkDestroyedEventTest {
	@Test
	public final void testGetNetwork() {
		final CyNetworkManager networkManager = mock(CyNetworkManager.class);
		final NetworkDestroyedEvent event =
			new NetworkDestroyedEvent(networkManager);
		assertEquals("NetworkManager returned by getSource() is *not* the one passed into the constructor!",
			     networkManager, event.getSource());
	}
}