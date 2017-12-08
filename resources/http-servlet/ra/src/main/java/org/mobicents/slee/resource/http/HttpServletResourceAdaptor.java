/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.slee.resource.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.slee.Address;
import javax.slee.facilities.EventLookupFacility;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityAlreadyExistsException;
import javax.slee.resource.ActivityHandle;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.EventFlags;
import javax.slee.resource.FailureReason;
import javax.slee.resource.FireableEventType;
import javax.slee.resource.InvalidConfigurationException;
import javax.slee.resource.Marshaler;
import javax.slee.resource.ReceivableService;
import javax.slee.resource.ResourceAdaptor;
import javax.slee.resource.ResourceAdaptorContext;
import javax.slee.resource.SleeEndpoint;

import net.java.slee.resource.http.events.HttpServletRequestEvent;

import org.jboss.mx.util.MBeanServerLocator;
import org.mobicents.slee.resource.http.events.HttpServletRequestEventImpl;
import org.mobicents.slee.resource.http.heartbeat.HttpLoadBalancerHeartBeatingService;
import org.mobicents.slee.resource.http.heartbeat.HttpLoadBalancerHeartBeatingServiceImpl;

/**
 *
 * @author martins
 *
 */
public class HttpServletResourceAdaptor implements ResourceAdaptor, HttpServletResourceEntryPoint {

	private static final String NAME_CONFIG_PROPERTY = "name";
	private static final String HTTP_REQUEST_TIMEOUT = "HTTP_REQUEST_TIMEOUT";
	
	private ResourceAdaptorContext resourceAdaptorContext;
	private SleeEndpoint sleeEndpoint;
	private Tracer tracer;

	/**
	 * the EventLookupFacility is used to look up the event id of incoming
	 * events
	 */
	private EventLookupFacility eventLookup;

	private RequestLock requestLock;

	private HttpServletRaSbbInterfaceImpl httpRaSbbinterface;

	/**
	 * caches the eventIDs, avoiding lookup in container
	 */
	private EventIDCache eventIdCache;

	/**
	 * tells the RA if an event with a specified ID should be filtered or not
	 */
	private EventIDFilter eventIDFilter;

	/**
	 * the ra entity name, which matches the servlet name
	 */
	private String name;

	private Integer httpRequestTimeout;
	
	private Properties loadBalancerHeartBeatingServiceProperties;
	private HttpLoadBalancerHeartBeatingService loadBalancerHeartBeatingService;

	/**
	 *
	 */
	public HttpServletResourceAdaptor() {
	}

	/**
	 *
	 * @return
	 */
	public ResourceAdaptorContext getResourceAdaptorContext() {
		return resourceAdaptorContext;
	}

	/**
	 *
	 * @return
	 */
	public SleeEndpoint getSleeEndpoint() {
		return sleeEndpoint;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	// lifecycle methods

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#setResourceAdaptorContext(javax.slee.
	 * resource.ResourceAdaptorContext)
	 */
	public void setResourceAdaptorContext(ResourceAdaptorContext raContext) {
		this.resourceAdaptorContext = raContext;
		tracer = raContext.getTracer(HttpServletResourceAdaptor.class.getSimpleName());
		eventIdCache = new EventIDCache(raContext.getTracer(EventIDCache.class.getSimpleName()));
		eventIDFilter = new EventIDFilter();
		sleeEndpoint = raContext.getSleeEndpoint();
		eventLookup = raContext.getEventLookupFacility();
		requestLock = new RequestLock();
		httpRaSbbinterface = new HttpServletRaSbbInterfaceImpl(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#raConfigure(javax.slee.resource.
	 * ConfigProperties)
	 */
	public void raConfigure(ConfigProperties configProperties) {
		name = (String) configProperties.getProperty(NAME_CONFIG_PROPERTY).getValue();
		httpRequestTimeout = (Integer) configProperties.getProperty(HTTP_REQUEST_TIMEOUT).getValue();
		try {
			loadBalancerHeartBeatingServiceProperties = prepareHeartBeatingServiceProperties(configProperties);
		} catch (InvalidConfigurationException e) {
			tracer.severe("Invalid LB Heartbieating service configuration", e);
		}
	}

	/*
	 * /* (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#raActive()
	 */
	public void raActive() {
		// register in manager
		HttpServletResourceEntryPointManager.putResourceEntryPoint(name, this);
		if (loadBalancerHeartBeatingServiceProperties != null) {
			try {
				loadBalancerHeartBeatingService = initHeartBeatingService();
				loadBalancerHeartBeatingService.start();
			} catch (Exception e) {
				tracer.severe("An error occured while starting Load balancer heartbeating service: " + e.getMessage(),
						e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#raStopping()
	 */
	public void raStopping() {
	}

	/*
	 * /* (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#raInactive()
	 */
	public void raInactive() {
		// unregister from manager
		HttpServletResourceEntryPointManager.removeResourceEntryPoint(name);
		if (loadBalancerHeartBeatingService != null) {
			try {
				loadBalancerHeartBeatingService.stop();
				loadBalancerHeartBeatingService = null;
			} catch (Exception e) {
				tracer.severe("Error while stopping RAs LB heartbeating service "
						+ this.resourceAdaptorContext.getEntityName(), e);
			}
		}
	}

	/*
	 * /* (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#raUnconfigure()
	 */
	public void raUnconfigure() {
		name = null;
		httpRequestTimeout = null;
		loadBalancerHeartBeatingServiceProperties = null;
	}

	/*
	 * /* (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#unsetResourceAdaptorContext()
	 */
	public void unsetResourceAdaptorContext() {
		resourceAdaptorContext = null;
		tracer = null;
		eventIdCache = null;
		eventIDFilter = null;
		sleeEndpoint = null;
		eventLookup = null;
		requestLock = null;
		httpRaSbbinterface = null;
	}

	// config management methods

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#raVerifyConfiguration(javax.slee.
	 * resource.ConfigProperties)
	 */
	public void raVerifyConfiguration(ConfigProperties configProperties) throws InvalidConfigurationException {
		ConfigProperties.Property property = configProperties.getProperty(NAME_CONFIG_PROPERTY);
		if (property == null) {
			throw new InvalidConfigurationException("name property not found");
		}
		if (!property.getType().equals(String.class.getName())) {
			throw new InvalidConfigurationException("name property must be of type java.lang.String");
		}
		if (property.getValue() == null) {
			// don't think this can happen, but just to be sure
			throw new InvalidConfigurationException("name property must not have a null value");
		}

		try {
			checkAddressAndPortProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_PORT, configProperties);
			checkAddressAndPortProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_SSL_PORT, configProperties);
			checkBalancersProperty(configProperties);
			checkHeartBeatingServiceClassNameProperty(configProperties);
		} catch (Throwable e) {
			throw new InvalidConfigurationException(e.getMessage(), e);
		}
	}

	private void checkAddressAndPortProperty(String portPropertyName, ConfigProperties properties) throws IOException {
		ConfigProperties.Property property = properties.getProperty(portPropertyName);
		if (property != null) {
			Integer localHttpPort = (Integer) property.getValue();
			if (localHttpPort != null && localHttpPort != -1) {
				String localHttpAddress = (String) properties
						.getProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_ADDRESS).getValue();
				if (localHttpAddress != null && !localHttpAddress.isEmpty()) {
					InetSocketAddress sockAddress = new InetSocketAddress(localHttpAddress, localHttpPort);
					checkSocketAddress(sockAddress);
				}
			}
		}
	}

	private void checkSocketAddress(InetSocketAddress address) throws IOException {
		Socket s = new Socket();
		try {
			s.connect(address);
		} finally {
			s.close();
		}
	}

	private void checkBalancersProperty(ConfigProperties properties) {
		String balancers = (String) properties.getProperty(HttpLoadBalancerHeartBeatingService.BALANCERS).getValue();
		if (balancers != null && !balancers.isEmpty()) {
			String[] segments = balancers.split(HttpLoadBalancerHeartBeatingServiceImpl.BALANCERS_CHAR_SEPARATOR);
			for (String segment : segments) {
				String[] addressAndPort = segment
						.split(HttpLoadBalancerHeartBeatingServiceImpl.BALANCER_PORT_CHAR_SEPARATOR);
				Integer.parseInt(addressAndPort[1]);
			}
		}
	}

	private void checkHeartBeatingServiceClassNameProperty(ConfigProperties properties) throws ClassNotFoundException {
		String httpBalancerHeartBeatServiceClassName = (String) properties
				.getProperty(HttpLoadBalancerHeartBeatingService.LB_HB_SERVICE_CLASS_NAME).getValue();
		if (httpBalancerHeartBeatServiceClassName != null && !httpBalancerHeartBeatServiceClassName.isEmpty()) {
			Class.forName(httpBalancerHeartBeatServiceClassName);
		}
	}

	private HttpLoadBalancerHeartBeatingService initHeartBeatingService() throws Exception {
		String httpBalancerHeartBeatServiceClassName = (String) loadBalancerHeartBeatingServiceProperties
				.getProperty(HttpLoadBalancerHeartBeatingService.LB_HB_SERVICE_CLASS_NAME);
		HttpLoadBalancerHeartBeatingService service = (HttpLoadBalancerHeartBeatingService) Class
				.forName(httpBalancerHeartBeatServiceClassName).newInstance();
		MBeanServer mBeanServer = MBeanServerLocator.locateJBoss();
		String stackName = this.name;
		service.init(resourceAdaptorContext, mBeanServer, stackName, loadBalancerHeartBeatingServiceProperties);
		return service;
	}

	private Properties prepareHeartBeatingServiceProperties(ConfigProperties configProperties)
			throws InvalidConfigurationException {

		Properties properties = null;

		String localPort = prepareLocalHttpPortProperty(configProperties);
		String localSslPort = prepareLocalSslProperty(configProperties);
		String localAddress = prepareLocalHttpAddressProperty(configProperties);
		String balancers = prepareProperty(configProperties, HttpLoadBalancerHeartBeatingServiceImpl.BALANCERS);
		String heartbeatingInterval = prepareProperty(configProperties,
				HttpLoadBalancerHeartBeatingServiceImpl.HEARTBEAT_INTERVAL);
		String lbHearbeatingServiceClassName = prepareProperty(configProperties,
				HttpLoadBalancerHeartBeatingServiceImpl.LB_HB_SERVICE_CLASS_NAME);

		if (balancers != null && !balancers.isEmpty()) {

			if (localAddress == null || localPort == null || lbHearbeatingServiceClassName == null) {
				StringBuilder sb = new StringBuilder();

				sb.append("Missing one or more required properties.\n");
				sb.append("Required properties:\n");
				sb.append(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_ADDRESS).append("\n");
				sb.append(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_PORT).append("\n");
				sb.append(HttpLoadBalancerHeartBeatingServiceImpl.LB_HB_SERVICE_CLASS_NAME).append("\n");
				sb.append("Missing properties:\n");
				if (localAddress == null) {
					sb.append(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_ADDRESS).append("\n");
				}
				if (localPort == null) {
					sb.append(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_PORT).append("\n");
				}
				if (lbHearbeatingServiceClassName == null) {
					sb.append(HttpLoadBalancerHeartBeatingServiceImpl.LB_HB_SERVICE_CLASS_NAME).append("\n");
				}
				throw new InvalidConfigurationException(sb.toString());
			}
			properties = new Properties();
			properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.BALANCERS, balancers);
			properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_PORT, localPort);
			properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_ADDRESS, localAddress);
			properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.LB_HB_SERVICE_CLASS_NAME,
					lbHearbeatingServiceClassName);
			if (localSslPort != null) {
				properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_SSL_PORT, localSslPort);
			}
			if (heartbeatingInterval != null) {
				properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.HEARTBEAT_INTERVAL,
						heartbeatingInterval);
			}

		} else if (localAddress != null && localPort != null && lbHearbeatingServiceClassName != null) {
			balancers = "";
			properties = new Properties();
			properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.BALANCERS, balancers);
			properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_PORT, localPort);
			properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_ADDRESS, localAddress);
			properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.LB_HB_SERVICE_CLASS_NAME,
					lbHearbeatingServiceClassName);
			if (localSslPort != null) {
				properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_SSL_PORT, localSslPort);
			}
			if (heartbeatingInterval != null) {
				properties.setProperty(HttpLoadBalancerHeartBeatingServiceImpl.HEARTBEAT_INTERVAL,
						heartbeatingInterval);
			}
		}
		return properties;
	}

	private String prepareLocalHttpPortProperty(ConfigProperties configProperties) {
		ConfigProperties.Property localPortProperty = configProperties
				.getProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_PORT);
		String localHttpPort = null;
		if (localPortProperty == null || localPortProperty.getValue() == null) {
			localHttpPort = getJBossPort();
		} else {
			Integer intValue = (Integer) localPortProperty.getValue();
			if (intValue > 1) {
				localHttpPort = String.valueOf(localPortProperty.getValue());
			}
		}
		return localHttpPort;
	}

	private String prepareLocalSslProperty(ConfigProperties configProperties) {
		String propertyValue = null;
		ConfigProperties.Property configProperty = configProperties
				.getProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_SSL_PORT);
		if (configProperty != null && configProperty.getValue() != null) {
			Integer intValue = (Integer) configProperty.getValue();
			if (intValue > 1) {
				propertyValue = String.valueOf(intValue);
			}
		}
		return propertyValue;
	}

	private String prepareProperty(ConfigProperties configProperties, String propertyName) {
		String propertyValue = null;
		ConfigProperties.Property configProperty = configProperties.getProperty(propertyName);
		if (configProperty != null && configProperty.getValue() != null) {
			propertyValue = String.valueOf(configProperty.getValue());
		}
		return propertyValue;
	}

	private String prepareLocalHttpAddressProperty(ConfigProperties configProperties) {
		ConfigProperties.Property localAddressProperty = configProperties
				.getProperty(HttpLoadBalancerHeartBeatingServiceImpl.LOCAL_HTTP_ADDRESS);
		String localHttpAddress = null;
		if (localAddressProperty == null || localAddressProperty.getValue() == null
				|| ((String) localAddressProperty.getValue()).isEmpty()) {
			localHttpAddress = getJBossAddress();
		} else {
			localHttpAddress = (String) localAddressProperty.getValue();
		}
		return localHttpAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#raConfigurationUpdate(javax.slee.
	 * resource.ConfigProperties)
	 */
	public void raConfigurationUpdate(ConfigProperties configProperties) {
		throw new UnsupportedOperationException();
	}

	// event filtering methods

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#serviceActive(javax.slee.resource.
	 * ReceivableService)
	 */
	public void serviceActive(ReceivableService receivableService) {
		eventIDFilter.serviceActive(receivableService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#serviceStopping(javax.slee.resource.
	 * ReceivableService)
	 */
	public void serviceStopping(ReceivableService receivableService) {
		eventIDFilter.serviceStopping(receivableService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#serviceInactive(javax.slee.resource.
	 * ReceivableService)
	 */
	public void serviceInactive(ReceivableService receivableService) {
		eventIDFilter.serviceInactive(receivableService);
	}

	// mandatory callbacks

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#administrativeRemove(javax.slee.
	 * resource.ActivityHandle)
	 */
	public void administrativeRemove(ActivityHandle activityHandle) {
		// nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#getActivity(javax.slee.resource.
	 * ActivityHandle)
	 */
	public Object getActivity(ActivityHandle activityHandle) {
		return activityHandle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#getActivityHandle(java.lang.Object)
	 */
	public javax.slee.resource.ActivityHandle getActivityHandle(Object object) {
		return (ActivityHandle) object;
	}

	// optional callbacks

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#activityEnded(javax.slee.resource.
	 * ActivityHandle)
	 */
	public void activityEnded(ActivityHandle activityHandle) {
		// not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#activityUnreferenced(javax.slee.
	 * resource.ActivityHandle)
	 */
	public void activityUnreferenced(ActivityHandle activityHandle) {
		// not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#eventProcessingFailed(javax.slee.
	 * resource.ActivityHandle, javax.slee.resource.FireableEventType,
	 * java.lang.Object, javax.slee.Address,
	 * javax.slee.resource.ReceivableService, int,
	 * javax.slee.resource.FailureReason)
	 */
	public void eventProcessingFailed(ActivityHandle activityHandle, FireableEventType fireableEventType, Object object,
			Address address, ReceivableService receivableService, int integer, FailureReason failureReason) {
		// not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#eventProcessingSuccessful(javax.slee.
	 * resource.ActivityHandle, javax.slee.resource.FireableEventType,
	 * java.lang.Object, javax.slee.Address,
	 * javax.slee.resource.ReceivableService, int)
	 */
	public void eventProcessingSuccessful(ActivityHandle activityHandle, FireableEventType fireableEventType,
			Object object, Address address, ReceivableService receivableService, int integer) {
		// not used
	}

	/*
	 * /* (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#eventUnreferenced(javax.slee.resource
	 * .ActivityHandle, javax.slee.resource.FireableEventType, java.lang.Object,
	 * javax.slee.Address, javax.slee.resource.ReceivableService, int)
	 */
	public void eventUnreferenced(ActivityHandle arg0, FireableEventType fireableEventType, Object event,
			Address address, ReceivableService receivableService, int integer) {
		// release event thread
		releaseHttpRequest((HttpServletRequestEvent) event);
	}

	/**
	 * Allows control to be returned back to the servlet conainer, which
	 * delivered the http request. The container will mandatory close the
	 * response stream.
	 *
	 */
	private void releaseHttpRequest(HttpServletRequestEvent hreqEvent) {
		if (tracer.isFinestEnabled()) {
			tracer.finest("releaseHttpRequest() enter");
		}

		final Object lock = requestLock.removeLock(hreqEvent);
		if (lock != null) {
			synchronized (lock) {
				lock.notify();
			}
		}

		if (tracer.isFineEnabled()) {
			tracer.fine("released lock for http request " + hreqEvent.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#queryLiveness(javax.slee.resource.
	 * ActivityHandle)
	 */
	public void queryLiveness(javax.slee.resource.ActivityHandle activityHandle) {
		// end any idle activity, it should be a leak, this is true assuming
		// that jboss web session timeout is smaller than the container timeout
		// to invoke this method
		if (tracer.isInfoEnabled()) {
			tracer.info("Activity " + activityHandle + " is idle in the container, terminating.");
		}
		endActivity((AbstractHttpServletActivity) activityHandle);
	}

	// interface accessors

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.slee.resource.ResourceAdaptor#getResourceAdaptorInterface(java.lang
	 * .String)
	 */
	public Object getResourceAdaptorInterface(String arg0) {
		return httpRaSbbinterface;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.slee.resource.ResourceAdaptor#getMarshaler()
	 */
	public Marshaler getMarshaler() {
		return null;
	}

	// ra logic

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.slee.resource.http.HttpServletResourceEntryPoint#onRequest
	 * (javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	public void onRequest(HttpServletRequest request, HttpServletResponse response) {
		AbstractHttpServletActivity activity = null;

		final HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request);
		final HttpSessionWrapper sessionWrapper = (HttpSessionWrapper) requestWrapper.getSession(false);
		final HttpServletRequestEvent requestEvent = new HttpServletRequestEventImpl(requestWrapper, response, this);
		final FireableEventType eventType = eventIdCache.getEventType(eventLookup, requestEvent, sessionWrapper);

		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);

		if (eventIDFilter.filterEvent(eventType)) {
			if (tracer.isInfoEnabled()) {
				tracer.info("Request event filtered: " + requestEvent);
			}
			// dude, get out of here
			return;
		}

		boolean createActivity = true;
		if (sessionWrapper == null) {
			// create request activity
			activity = new HttpServletRequestActivityImpl();
		} else {
			activity = new HttpSessionActivityImpl(sessionWrapper);
			if (sessionWrapper.getResourceEntryPoint() != null) {
				createActivity = false;
			}
		}

		if (createActivity) {
			// we have a session but its not activity yet, add it
			try {
				if (sessionWrapper != null) {
					sessionWrapper.setResourceEntryPoint(this.name);
				}
				sleeEndpoint.startActivity(activity, activity);
			} catch (ActivityAlreadyExistsException e) {
				if (tracer.isFineEnabled()) {
					tracer.fine("Failed to add activity " + activity, e);
				}
				// proceed, may be due to fail over
			} catch (Throwable e) {
				tracer.severe("Failed to add activity " + activity, e);
				return;
			}
		}

		if (tracer.isFineEnabled()) {
			tracer.fine("Firing event " + requestEvent + " in activity " + activity);
		}

		final Object lock = requestLock.getLock(requestEvent);
		synchronized (lock) {
			try {
				sleeEndpoint.fireEvent(activity, eventType, requestEvent, null, null,
						EventFlags.REQUEST_EVENT_UNREFERENCED_CALLBACK);
				// block thread until event has been processed
				lock.wait(httpRequestTimeout);
				// the event was unreferenced or 15s timeout, if the activity is
				// the request then end it
				if (sessionWrapper == null) {
					endActivity(activity);
				}
			} catch (Throwable e) {
				tracer.severe("Failure while firing event " + requestEvent + " on activity " + activity, e);
			}
		}
	}

	private void endActivity(AbstractHttpServletActivity activity) {
		if (tracer.isInfoEnabled()) {
			tracer.fine("Ending activity " + activity);
		}
		try {
			sleeEndpoint.endActivity(activity);
		} catch (Throwable e) {
			tracer.severe("Failed to end activity " + activity, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.slee.resource.http.HttpServletResourceEntryPoint#
	 * onSessionTerminated(java.lang.String)
	 */
	public void onSessionTerminated(HttpSessionWrapper httpSessionWrapper) {
		endActivity(new HttpSessionActivityImpl(httpSessionWrapper));
	}

	private String getJBossAddress() {
		return System.getProperty("jboss.bind.address");
	}

	private String getJBossPort() {
		return System.getProperty("jboss.bind.port");
	}
}
