
package org.cytoscape.model.events;

import org.cytoscape.event.CyEventListener;

/**
 * Listener for RemovedEgeEvents.
 */
public interface RemovedEdgeListener extends CyEventListener<RemovedEdgeEvent> {
	public void handleEvent(RemovedEdgeEvent e);
}
