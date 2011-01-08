/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.webservice;

import java.net.URI;
import java.net.URISyntaxException;



/**
 * Abstract class for all web service clients.
 * All clients MUST extend this class.

 * @param <S>  Stub object type.  This is service dependent.
 */
public abstract class AbstractWebServiceClient<S> implements WebServiceClient<S> {

	// Service locaiton
	protected final URI serviceURI;
	
	// Endpoint
	protected final S clientStub;

	// Display Name for this client.
	private final String displayName;
	private final String description;


	public AbstractWebServiceClient(final String uri, final String displayName, final String description,
	                            final S endpoint) {
		
		// Create URI
		try {
			this.serviceURI = new URI(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("URI string is invalid.");
		}
		
		this.displayName = displayName;
		this.description = description;
		this.clientStub = endpoint;
	}


	@Override public String getDisplayName() {
		return displayName;
	}


	@Override public String getDescription() {
		return description;
	}


	@Override
	public URI getServiceLocation() {
		return this.serviceURI;
	}

	@Override
	public S getEndpoint() {
		return this.clientStub;
	}
}
