
package org.cytoscape.session.events;

import org.cytoscape.event.CyListener;

/**
 * 
 */
public interface NetworkViewDestroyedListener extends CyListener {
	public void handleEvent(NetworkViewDestroyedEvent e);
}
