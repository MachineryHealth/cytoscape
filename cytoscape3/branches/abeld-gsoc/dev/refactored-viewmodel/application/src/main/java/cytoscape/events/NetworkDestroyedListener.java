
package cytoscape.events;

import org.cytoscape.event.CyListener;

/**
 * 
 */
public interface NetworkDestroyedListener extends CyListener {
	public void handleEvent(NetworkDestroyedEvent e);
}
