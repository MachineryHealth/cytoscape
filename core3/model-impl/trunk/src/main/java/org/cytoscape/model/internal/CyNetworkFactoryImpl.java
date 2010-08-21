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
package org.cytoscape.model.internal;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyTableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 */
public class CyNetworkFactoryImpl implements CyNetworkFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(CyNetworkFactoryImpl.class);
	
	private final CyEventHelper help;
	private final CyTableManager mgr;

	/**
	 * Creates a new CyNetworkFactoryImpl object.
	 *
	 * @param help An instance of CyEventHelper. 
	 */
	public CyNetworkFactoryImpl(final CyEventHelper help, final CyTableManager mgr) {
		if (help == null)
			throw new NullPointerException("CyEventHelper is null");

		if (mgr == null)
			throw new NullPointerException("CyTableManager is null");

		this.help = help;
		this.mgr = mgr;
	}

	/**
	 * {@inheritDoc}
	 */
	public CyNetwork getInstance() {
		//return new MGraph(help);
		ArrayGraph net = new ArrayGraph(help,mgr);
		logger.info("ArrayGraph created: ID = " +  net.getSUID());
		logger.info("ArrayGraph created: Base Graph ID = " +  net.getBaseNetwork().getSUID());
		return net.getBaseNetwork(); 
	}
}
