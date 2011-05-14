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
package org.cytoscape.event.internal;

import org.cytoscape.event.CyEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.CyMicroListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyEventHelperImpl implements CyEventHelper {

	private static final Logger logger = LoggerFactory.getLogger(CyEventHelperImpl.class);

	private final CyListenerAdapter normal;
	private final CyMicroListenerAdapter micro;

	public CyEventHelperImpl(final CyListenerAdapter normal, final CyMicroListenerAdapter micro) {
		this.normal = normal;
		this.micro = micro;
	}

	@Override public <E extends CyEvent<?>> void fireSynchronousEvent(final E event) {
		normal.fireSynchronousEvent(event);
	}


	@Override public <E extends CyEvent<?>> void fireAsynchronousEvent(final E event) {
		normal.fireAsynchronousEvent(event);
	}

	@Override public <M extends CyMicroListener> M getMicroListener(Class<M> c, Object source) {
		return micro.getMicroListener(c,source);
	}

	@Override public <M extends CyMicroListener> void addMicroListener(M m, Class<M> c, Object source) {
		micro.addMicroListener(m,c,source);
	}

	@Override public <M extends CyMicroListener> void removeMicroListener(M m, Class<M> c, Object source) {
		micro.removeMicroListener(m,c,source);
	}

	@Override public void silenceEventSource(Object eventSource) {
		if ( eventSource == null )
			return;
		logger.info("silencing event source: " + eventSource.toString());
		normal.silenceEventSource(eventSource);
		micro.silenceEventSource(eventSource);
	}

	@Override public void unsilenceEventSource(Object eventSource) {
		if ( eventSource == null )
			return;
		logger.info("unsilencing event source: " + eventSource.toString());
		normal.unsilenceEventSource(eventSource);
		micro.unsilenceEventSource(eventSource);
	}
}
