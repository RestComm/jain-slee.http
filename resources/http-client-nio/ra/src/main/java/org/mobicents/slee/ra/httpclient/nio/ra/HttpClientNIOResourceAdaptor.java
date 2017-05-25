package org.mobicents.slee.ra.httpclient.nio.ra;

import org.apache.http.HttpResponse;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;
import org.mobicents.slee.ra.httpclient.nio.events.HttpClientNIOEventTypes;
import org.mobicents.slee.ra.httpclient.nio.events.HttpClientNIOResponseEvent;
import org.mobicents.slee.ra.httpclient.nio.ratype.HttpClientNIORequestActivity;
import org.mobicents.slee.ra.httpclient.nio.ratype.HttpClientNIOResourceAdaptorSbbInterface;

import javax.slee.Address;
import javax.slee.facilities.Tracer;
import javax.slee.resource.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author martins
 * 
 */
public class HttpClientNIOResourceAdaptor implements ResourceAdaptor {

    private static final int EVENT_FLAGS = EventFlags.REQUEST_EVENT_UNREFERENCED_CALLBACK;

    private static final String CFG_PROPERTY_HTTP_CLIENT_FACTORY = "HTTP_CLIENT_FACTORY";
    private static final String CFG_PROPERTY_MAX_CONNECTIONS_TOTAL = "MAX_CONNECTIONS_TOTAL";
    private static final String CFG_PROPERTY_DEFAULT_MAX_CONNECTIONS_PER_ROUTE = "DEFAULT_MAX_CONNECTIONS_PER_ROUTE";
    private static final String CFG_PROPERTY_DEFAULT_SOCKET_TIMEOUT = "DEFAULT_SOCKET_TIMEOUT";
    private static final String CFG_PROPERTY_DEFAULT_CONNECT_TIMEOUT = "DEFAULT_CONNECT_TIMEOUT";

    protected ResourceAdaptorContext resourceAdaptorContext;
    private ConcurrentHashMap<HttpClientNIORequestActivityHandle, HttpClientNIORequestActivity> activities;
    private HttpClientNIOResourceAdaptorSbbInterface sbbInterface;
    private Tracer tracer;
    protected HttpAsyncClient httpAsyncClient;
    protected volatile boolean isActive = false;

    // caching the only event this ra fires
    private FireableEventType fireableEventType;

    private int maxTotal;
    private int defaultMaxPerRoute;
    private int connectTimeout;
    private int socketTimeout;

    // configuration
    private HttpAsyncClientFactory httpClientFactory;

    // LIFECYCLE METHODS

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#setResourceAdaptorContext(javax.slee
     * .resource.ResourceAdaptorContext)
     */
    public void setResourceAdaptorContext(ResourceAdaptorContext arg0) {
        resourceAdaptorContext = arg0;
        tracer = resourceAdaptorContext.getTracer(HttpClientNIOResourceAdaptor.class.getSimpleName());
        try {
            fireableEventType = resourceAdaptorContext.getEventLookupFacility().getFireableEventType(
                    HttpClientNIOEventTypes.HTTP_CLIENT_NIO_RESPONSE_EVENT_TYPE_ID);
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        sbbInterface = new HttpClientNIOResourceAdaptorSbbInterfaceImpl(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.resource.ResourceAdaptor#raConfigure(javax.slee.resource.
     * ConfigProperties)
     */
    @SuppressWarnings("unchecked")
    public void raConfigure(ConfigProperties properties) {
        String httpClientFactoryClassName = (String) properties.getProperty(CFG_PROPERTY_HTTP_CLIENT_FACTORY)
                .getValue();
        if (!httpClientFactoryClassName.isEmpty()) {
            try {
                httpClientFactory = ((Class<? extends HttpAsyncClientFactory>) Class
                        .forName(httpClientFactoryClassName)).newInstance();
            } catch (Exception e) {
                tracer.severe("failed to load http client factory class", e);
            }
        } else {
            this.maxTotal = (Integer) properties.getProperty(CFG_PROPERTY_MAX_CONNECTIONS_TOTAL).getValue();
            this.defaultMaxPerRoute = (Integer) properties.getProperty(CFG_PROPERTY_DEFAULT_MAX_CONNECTIONS_PER_ROUTE)
                    .getValue();
            this.connectTimeout = (Integer) properties.getProperty(CFG_PROPERTY_DEFAULT_CONNECT_TIMEOUT).getValue();
            this.socketTimeout = (Integer) properties.getProperty(CFG_PROPERTY_DEFAULT_SOCKET_TIMEOUT).getValue();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.resource.ResourceAdaptor#raActive()
     */
    public void raActive() {
        activities = new ConcurrentHashMap<HttpClientNIORequestActivityHandle, HttpClientNIORequestActivity>();
        try {
            if (httpClientFactory != null) {
                httpAsyncClient = httpClientFactory.newHttpAsyncClient();
            } else {
                httpAsyncClient = buildHttpAsyncClient();
            }

            // Can't be sure whether factory produced closeable client or some specialization of AbstractHttpAsyncClient
            if (httpAsyncClient instanceof CloseableHttpAsyncClient) {
                ((CloseableHttpAsyncClient) httpAsyncClient).start();
            }

            isActive = true;
            if (tracer.isInfoEnabled()) {
                tracer.info(String.format("HttpClientNIOResourceAdaptor=%s entity activated.",
                        this.resourceAdaptorContext.getEntityName()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.resource.ResourceAdaptor#raStopping()
     */
    public void raStopping() {
        this.isActive = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.resource.ResourceAdaptor#raInactive()
     */
    public void raInactive() {
        this.isActive = false;
        activities.clear();
        activities = null;
        try {
            if (httpAsyncClient instanceof  CloseableHttpAsyncClient) {
                ((CloseableHttpAsyncClient) httpAsyncClient).close();
            }


        } catch (IOException e) {
            tracer.severe("Failed to complete http client shutdown", e);
        }
        this.httpAsyncClient = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.resource.ResourceAdaptor#raUnconfigure()
     */
    public void raUnconfigure() {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.resource.ResourceAdaptor#unsetResourceAdaptorContext()
     */
    public void unsetResourceAdaptorContext() {
        resourceAdaptorContext = null;
        tracer = null;
        sbbInterface = null;
    }

    // CONFIG MANAGENT

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#raVerifyConfiguration(javax.slee.
     * resource.ConfigProperties)
     */
    @SuppressWarnings("unchecked")
    public void raVerifyConfiguration(ConfigProperties properties) throws InvalidConfigurationException {
        String httpClientFactoryClassName = (String) properties.getProperty(CFG_PROPERTY_HTTP_CLIENT_FACTORY)
                .getValue();
        if (!httpClientFactoryClassName.isEmpty()) {
            try {
                Class<? extends HttpAsyncClientFactory> c = (Class<? extends HttpAsyncClientFactory>) Class
                        .forName(httpClientFactoryClassName);
                c.newInstance();
            } catch (Exception e) {
                tracer.severe("failed to load http client factory class", e);
                throw new InvalidConfigurationException("failed to load http client factory class", e);
            }
        } else {
            try {
                Integer j = (Integer) properties.getProperty(CFG_PROPERTY_MAX_CONNECTIONS_TOTAL).getValue();
                if (j < 1) {
                    throw new InvalidConfigurationException(CFG_PROPERTY_MAX_CONNECTIONS_TOTAL + " must be > 0");
                }

                j = (Integer) properties.getProperty(CFG_PROPERTY_DEFAULT_MAX_CONNECTIONS_PER_ROUTE).getValue();
                if (j < 1) {
                    throw new InvalidConfigurationException(CFG_PROPERTY_DEFAULT_MAX_CONNECTIONS_PER_ROUTE
                            + " must be > 0");
                }

                j  = (Integer) properties.getProperty(CFG_PROPERTY_DEFAULT_CONNECT_TIMEOUT).getValue();
                if (j < 1) {
                    throw new InvalidConfigurationException(CFG_PROPERTY_DEFAULT_CONNECT_TIMEOUT
                            + " must be > 0");
                }

                j = (Integer) properties.getProperty(CFG_PROPERTY_DEFAULT_SOCKET_TIMEOUT).getValue();
                if (j < 1) {
                    throw new InvalidConfigurationException(CFG_PROPERTY_DEFAULT_SOCKET_TIMEOUT
                            + " must be > 0");
                }
            } catch (InvalidConfigurationException e) {
                throw e;
            } catch (Exception e) {
                tracer.severe("failure in config validation", e);
                throw new InvalidConfigurationException(e.getMessage());
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#raConfigurationUpdate(javax.slee.
     * resource.ConfigProperties)
     */
    public void raConfigurationUpdate(ConfigProperties arg0) {
        // not supported
    }

    // EVENT FILTERING

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#serviceActive(javax.slee.resource
     * .ReceivableService)
     */
    public void serviceActive(ReceivableService arg0) {
        // no event filtering
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#serviceStopping(javax.slee.resource
     * .ReceivableService)
     */
    public void serviceStopping(ReceivableService arg0) {
        // no event filtering
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#serviceInactive(javax.slee.resource
     * .ReceivableService)
     */
    public void serviceInactive(ReceivableService arg0) {
        // no event filtering
    }

    // ACCESS INTERFACE

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#getResourceAdaptorInterface(java.
     * lang.String)
     */
    public Object getResourceAdaptorInterface(String arg0) {
        return sbbInterface;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.resource.ResourceAdaptor#getMarshaler()
     */
    public Marshaler getMarshaler() {
        return null;
    }

    // MANDATORY CALLBACKS

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#administrativeRemove(javax.slee.resource
     * .ActivityHandle)
     */
    public void administrativeRemove(ActivityHandle arg0) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.resource.ResourceAdaptor#getActivity(javax.slee.resource.
     * ActivityHandle)
     */
    public Object getActivity(ActivityHandle activityHandle) {
        return activities.get(activityHandle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#getActivityHandle(java.lang.Object)
     */
    public ActivityHandle getActivityHandle(Object arg0) {
        if (arg0 instanceof HttpClientNIORequestActivityImpl) {
            HttpClientNIORequestActivityHandle handle = new HttpClientNIORequestActivityHandle(
                    ((HttpClientNIORequestActivityImpl) arg0).getId());
            if (activities.containsKey(handle)) {
                return handle;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#queryLiveness(javax.slee.resource
     * .ActivityHandle)
     */
    public void queryLiveness(ActivityHandle arg0) {
        // if the activity is not in the map end it, its a leak
        if (!activities.contains(arg0)) {
            resourceAdaptorContext.getSleeEndpoint().endActivity(arg0);
        }
    }

    // OPTIONAL CALLBACKS

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#eventProcessingSuccessful(javax.slee
     * .resource.ActivityHandle, javax.slee.resource.FireableEventType,
     * java.lang.Object, javax.slee.Address,
     * javax.slee.resource.ReceivableService, int)
     */
    public void eventProcessingSuccessful(ActivityHandle arg0, FireableEventType arg1, Object arg2, Address arg3,
            ReceivableService arg4, int arg5) {
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
    public void eventProcessingFailed(ActivityHandle arg0, FireableEventType arg1, Object arg2, Address arg3,
            ReceivableService arg4, int arg5, FailureReason arg6) {
        // not used
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#eventUnreferenced(javax.slee.resource
     * .ActivityHandle, javax.slee.resource.FireableEventType, java.lang.Object,
     * javax.slee.Address, javax.slee.resource.ReceivableService, int)
     */
    public void eventUnreferenced(ActivityHandle arg0, FireableEventType arg1, Object arg2, Address arg3,
            ReceivableService arg4, int arg5) {
        if (tracer.isFineEnabled()) {
            tracer.fine(String.format("Event=%s unreferenced", arg2));
        }

        if (arg2 instanceof HttpClientNIOResponseEvent) {
            HttpClientNIOResponseEvent event = (HttpClientNIOResponseEvent) arg2;
            HttpResponse response = event.getResponse();

            // May be this event is carrying Exception and not actual Response
            // in which case
            // skip housekeeping
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    this.tracer.severe("Exception while housekeeping. Event unreferenced", e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#activityEnded(javax.slee.resource
     * .ActivityHandle)
     */
    public void activityEnded(ActivityHandle activityHandle) {
        if (tracer.isFineEnabled()) {
            tracer.fine("activityEnded( handle = " + activityHandle + ")");
        }
        activities.remove(activityHandle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.slee.resource.ResourceAdaptor#activityUnreferenced(javax.slee.resource
     * .ActivityHandle)
     */
    public void activityUnreferenced(ActivityHandle arg0) {
        // not used
    }

    // OWN METHODS

    /**
     * Retrieves the ra context
     * 
     * @return
     */
    public ResourceAdaptorContext getResourceAdaptorContext() {
        return resourceAdaptorContext;
    }

    /**
     * Maps the specified activity to the specified handle
     * 
     * @param activityHandle
     * @param activity
     */
    public void addActivity(HttpClientNIORequestActivityHandle activityHandle, HttpClientNIORequestActivity activity) {
        activities.put(activityHandle, activity);
    }

    /**
     * Ends the specified activity
     * 
     * @param activity
     */
    public void endActivity(HttpClientNIORequestActivityImpl activity) {

        final HttpClientNIORequestActivityHandle ah = new HttpClientNIORequestActivityHandle(activity.getId());

        if (activities.containsKey(ah)) {
            resourceAdaptorContext.getSleeEndpoint().endActivity(ah);
        }
    }

    /**
     * Receives an Event from the HTTP client and sends it to the SLEE.
     * 
     * @param event
     * @param activity
     */
    public void processResponseEvent(HttpClientNIOResponseEvent event, HttpClientNIORequestActivityImpl activity) {

        HttpClientNIORequestActivityHandle ah = new HttpClientNIORequestActivityHandle(activity.getId());

        if (tracer.isFineEnabled())
            tracer.fine("==== FIRING ResponseEvent EVENT TO LOCAL SLEE, Event: " + event + " ====");

        try {
            resourceAdaptorContext.getSleeEndpoint().fireEvent(ah, fireableEventType, event, null, null, EVENT_FLAGS);
        } catch (Throwable e) {
            tracer.severe(e.getMessage(), e);
        }
    }

    /**
     * Creates an instance of async http client.
     */
    private HttpAsyncClient buildHttpAsyncClient() throws IOReactorException {
        // Create I/O reactor configuration
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSoTimeout(socketTimeout)
                .build();

        // Create a custom I/O reactor
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

        // Create a connection manager with simple configuration.
        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(maxTotal);
        connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        return httpAsyncClient = HttpAsyncClients.custom()
                .useSystemProperties()
                .setConnectionManager(connManager)
                .build();
    }

}
