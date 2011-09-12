
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
package org.cytoscape.service.util.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.lang.reflect.Method;
import java.lang.NoSuchMethodException;
import java.util.Properties;
import java.util.Dictionary;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CyServiceListener extends ServiceTracker {


	private final BundleContext bc;
	private final Object target;
	private final Method registerMethod;
	private final Method unregisterMethod;
	private final Class<?> serviceClass;
	private static final Logger logger = LoggerFactory.getLogger(CyServiceListener.class);
	
	public CyServiceListener(BundleContext bc, Object target, String registerMethodName, String unregisterMethodName, Class<?> serviceClass) throws NoSuchMethodException {
		super(bc, serviceClass.getName(), null);
		this.bc = bc;
		this.target = target;
		this.serviceClass = serviceClass;
		this.registerMethod = getMethod(registerMethodName);
		this.unregisterMethod = getMethod(unregisterMethodName);
	}

	/**
	 * Tries the different possible method declarations.
	 */
	private Method getMethod(String name) throws NoSuchMethodException {
		Method m; 
		try {
			m = target.getClass().getMethod(name, serviceClass, Dictionary.class);
		} catch (NoSuchMethodException e) {
			// Ignore exception and try different signature.
			// If we throw an exception here, we WANT it to 
			// propagate, because that signals an error.
			m = target.getClass().getMethod(name, serviceClass, Map.class);
		}
		return m;
	}

	/**
	 * Invokes the register method on the listener target class with the specified service.
	 */
	@Override
	public Object addingService(ServiceReference ref) {
		try {
			Object service = super.addingService(ref);
			registerMethod.invoke(target, serviceClass.cast(service), getProperties(ref));
			return service;
		} catch (Exception nse) {
			logger.warn("Failed to register service: ",nse);
		}
		return null;
	}

	/**
	 * Invokes the UNregister method on the listener target class with the specified service.
	 */
	@Override
	public void removedService(ServiceReference ref, Object service) {
		super.removedService(ref, service);
		try {
			unregisterMethod.invoke(target, serviceClass.cast(service), getProperties(ref));
		} catch (Exception nse) {
			logger.warn("Failed to unregister service: ",nse);
		}
	}

	/**
	 * Converts the service properties contained in a ServiceReference to a Properties object.
	 */
	private Properties getProperties(ServiceReference ref) {
		Properties props = new Properties();
		for ( String key : ref.getPropertyKeys() )
			props.setProperty(key,(String)(ref.getProperty(key)));
		return props;
	}
}
