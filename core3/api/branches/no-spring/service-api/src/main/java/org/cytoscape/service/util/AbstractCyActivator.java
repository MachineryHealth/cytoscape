
package org.cytoscape.service.util;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import org.osgi.framework.BundleContext; 
import org.osgi.framework.BundleActivator; 
import org.osgi.framework.ServiceRegistration; 
import org.osgi.framework.ServiceReference; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.service.util.internal.CyServiceListener;

/**
 * A simple BundleActivator with convenience methods for registering
 * OSGi services and either getting references to single services or
 * registering interest in all services of a specified type.  
 *
 * Users should extend this class and at implement the start(BundleContext bc)
 * method.  
 * 
 */
public abstract class AbstractCyActivator implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(AbstractCyActivator.class);
	
	private final Map<Class,Map<Object,ServiceRegistration>> serviceRegistrations;
	private final List<CyServiceListener> serviceListeners;
	private final List<ServiceReference> gottenServices;

	/**
	 * Constructor.
	 */
	public AbstractCyActivator() {
		serviceRegistrations = new HashMap<Class,Map<Object,ServiceRegistration>>();
		serviceListeners = new ArrayList<CyServiceListener>();
		gottenServices = new ArrayList<ServiceReference>();
	}

	/**
	 * A default implementation of the BundleActivator.stop() method that cleans
	 * up any services registered, services gotten, or services being listened
	 * for as determined by calls to the utility methods provided by this class. If
	 * you register a service, get a service, or listen for services outside yourself
	 * using normal calls to the OSGi API, you will need to clean everything up 
	 * yourself!
	 */
	public void stop(BundleContext bc) {
		// unregister and clear all services registered 
		for ( Map<Object,ServiceRegistration> registrations : serviceRegistrations.values() )  {
			for ( ServiceRegistration reg : registrations.values() ) 
				reg.unregister();
			registrations.clear();
		}
		serviceRegistrations.clear();

		// unregister and clear all service listeners
		for ( CyServiceListener listener : serviceListeners )
			listener.close();
		serviceListeners.clear();

		// unget and clear all services
		for ( ServiceReference ref : gottenServices )
			bc.ungetService(ref);
		gottenServices.clear();
	}

	/**
	 * A method that attempts to get a service of the specified type. If an 
	 * appropriate service is not found, an exception will be thrown.
	 * @param bc The BundleContext used to find services.
	 * @param serviceClass The class defining the type of service desired.
	 * @return A reference to a service of type serviceClass.
	 * @throws RuntimeException If the requested service can't be found.
	 */
	protected <S> S getService(BundleContext bc, Class<S> serviceClass) {
		try {
			ServiceReference ref = bc.getServiceReference(serviceClass.getName());
			if ( ref == null ) 
				throw new NullPointerException("ServiceReference is null for: " + serviceClass.getName());

			gottenServices.add(ref);	
			return serviceClass.cast( bc.getService(ref) );

		} catch (Exception e) {
			throw new RuntimeException("Couldn't find service: " + serviceClass.getName(),e);
		}
	}

	/**
	 * A method that attempts to get a service of the specified type and that
	 * passes the specified filter. If an appropriate service is not found, an 
	 * exception will be thrown.
	 * @param bc The BundleContext used to find services.
	 * @param serviceClass The class defining the type of service desired.
	 * @param filter The string defining the filter the service must pass.  See OSGi's 
	 * service filtering syntax for more detail.
	 * @return A reference to a service of type serviceClass that passes the specified filter.
	 * @throws RuntimeException If the requested service can't be found.
	 */
	protected <S> S getService(BundleContext bc, Class<S> serviceClass, String filter) {
		try { 
			ServiceReference[] refs = bc.getServiceReferences(serviceClass.getName(),filter);
			if ( refs == null ) 
				throw new NullPointerException("ServiceReference is null for: " + serviceClass.getName() + " with filter: " + filter);

			gottenServices.add(refs[0]);	
			return serviceClass.cast( bc.getService(refs[0]) );
		} catch (Exception e) {
			throw new RuntimeException("Couldn't find service: " + serviceClass.getName() + " with filter: " + filter, e);
		}
	}

	/**
	 * A method that will cause the specified register/unregister methods on the listener
	 * object to be called any time that a service of the specified type is registered or
	 * unregistered. 
	 * @param bc The BundleContext used to find services.
	 * @param listener Your object listening for service registrations.
	 * @param registerMethodName The name of the method to be called when a service is registered.
	 * @param unregisterMethodName The name of the method to be called when a service is unregistered.
	 * @param serviceClass The class defining the type of service desired.
	 */
	protected void registerServiceListener(final BundleContext bc, final Object listener, final String registerMethodName, final String unregisterMethodName, final Class<?> serviceClass) {
		try {
			CyServiceListener serviceListener = new CyServiceListener(bc, listener, registerMethodName, unregisterMethodName, serviceClass);
			serviceListener.open();
			serviceListeners.add( serviceListener );
		} catch (Exception e) {
			throw new RuntimeException("Could not listen to services for object: " + listener + " with methods: " + registerMethodName + " and " + unregisterMethodName + " and service type: " + serviceClass, e); 
		}
	}

	/**
	 * A utility method that registers the specified service object as an OSGi service for
	 * all interfaces that the object implements.
	 * @param bc The BundleContext used to find services.
	 * @param service The object to be registered as one or more services.
	 * @param props The service properties to be registered with each service. 
	 */
	protected void registerAllServices(final BundleContext bc, final Object service, final Properties props) {
		for ( Class<?> c : service.getClass().getInterfaces() ) 
			registerService(bc, service, c, props);
	}

	/**
	 * A utility method that registers the specified service object as an OSGi service of
	 * the specified type.
	 * @param bc The BundleContext used to find services.
	 * @param service The object to be registered as one or more services.
	 * @param serviceClass The class defining the type of service to be registered.
	 * @param props The service properties to be registered with each service. 
	 */
	protected void registerService(final BundleContext bc, final Object service, final Class<?> serviceClass, final Properties props) {
		if ( service == null )
			throw new NullPointerException( "service object is null" );
		if ( serviceClass == null )
			throw new NullPointerException( "class is null" );
		if ( props == null )
			throw new NullPointerException( "props are null" );
		if ( bc == null )
			throw new IllegalStateException( "BundleContext is null" );

		logger.debug("attempting to register service: " + service.toString() + " of type " + serviceClass.getName());
		ServiceRegistration s = bc.registerService( serviceClass.getName(), service, props );

		if ( !serviceRegistrations.containsKey(serviceClass) )
			serviceRegistrations.put(serviceClass, new HashMap<Object,ServiceRegistration>() );

		serviceRegistrations.get(serviceClass).put(service,s);
	}
}
