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


/**
 * A base implementation of CyEvent that can be used by events.
 */
public abstract class AbstractCyEvent<T> implements CyEvent<T> {

	private final T source;
	private final Class<?> listenerClass; 

	/** 
	 * Constructor.
	 * @param source The source object that fires the event. May NOT be null.
	 * @param listenerClass The Class that defines the listener interface. May NOT be null.
	 */
	public AbstractCyEvent(final T source, Class<?> listenerClass) {
		if ( source == null )
			throw new NullPointerException("event source is null");

		if ( listenerClass == null )
			throw new NullPointerException("listener class is null");

		this.source = source;
		this.listenerClass = listenerClass;
	}

	/**
	 * The object that fired the event. 
	 * @return The object that fired the event.
	 */
	public T getSource() {
		return source;
	}

	/**
	 * The Class of the listener that is expected to handle this event. 
	 * @return The Class of the listener that is expected to handle this event. 
	 */
	public Class<?> getListenerClass() {
		return listenerClass;
	}
}
