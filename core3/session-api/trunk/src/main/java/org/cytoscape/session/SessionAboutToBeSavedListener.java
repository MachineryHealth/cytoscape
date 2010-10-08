
package org.cytoscape.session;

import org.cytoscape.event.CyListener;

/**
 * Any object that needs to know that a CySession is about to be
 * created listen to this event.  Additionally, plugins can set
 * a list of files to be saved in the CySession using the
 * appropriate method in the SessionAboutToBeSavedEvent.
 */
public interface SessionAboutToBeSavedListener extends CyListener {
	
	/**
	 * @param e The event that the listener is listening for.
	 */
	public void handleEvent(SessionAboutToBeSavedEvent e);
}
