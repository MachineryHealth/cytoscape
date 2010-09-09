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
package org.cytoscape.view.model;

import java.util.Collection;
import java.util.Set;

/**
 * Should be implemented as a service. 'Renderer' is simply anything that
 * provides VisualProperties. With a 'VisualProperties as annotations' this
 * won't be needed.
 * 
 * @since Cytoscape 3.0
 * 
 */
public interface VisualLexicon {
	
	/**
	 * Returns the Set of VisualPropertys supported by this Renderer.
	 * 
	 * @return Set of all visual properties
	 * 
	 */
	public Set<VisualProperty<?>> getAllVisualProperties();
	
	
	/**
	 * Get collection of visual properties for a given object type (node/edge/network).
	 * 
	 * @param objectType
	 *            CyTableEntry type. i.e., NODE/EDGE/NETWORK.
	 * 
	 * @return Collection of visual properties for the type
	 */
	public Collection<VisualProperty<?>> getVisualProperties(final String objectType);
	
	
	/**
	 * Register new visual property to the lexicon.
	 * 
	 * @param prop
	 */
	public void addVisualProperty(final VisualProperty<?> prop);
	
}
