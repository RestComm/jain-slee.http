package org.restcomm.slee.ra.httpclient.nio.ra;

import javax.slee.SLEEException;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityFlags;
import javax.slee.resource.StartActivityException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.restcomm.slee.ra.httpclient.nio.ratype.HttpClientNIORequestActivity;
import org.restcomm.slee.ra.httpclient.nio.ratype.HttpClientNIOResourceAdaptorSbbInterface;

/**
 * 
 * @author martins
 * 
 */
public class HttpClientNIOResourceAdaptorSbbInterfaceImpl implements
		HttpClientNIOResourceAdaptorSbbInterface {

	private static final int ACTIVITY_FLAGS = ActivityFlags.REQUEST_ENDED_CALLBACK;

	private final Tracer tracer;

	private final HttpClientNIOResourceAdaptor ra;

	public HttpClientNIOResourceAdaptorSbbInterfaceImpl(
			HttpClientNIOResourceAdaptor ra) {
		this.ra = ra;
		this.tracer = ra.getResourceAdaptorContext().getTracer(
				HttpClientNIOResourceAdaptorSbbInterfaceImpl.class.getName());
	}

	public HttpClientNIORequestActivity execute(HttpHost target,
			HttpRequest request, HttpContext context, Object applicationData)
			throws SLEEException, StartActivityException {
		final HttpClientNIORequestActivityImpl activity = createActivity();
		try {
			activity.execute(target, request, context, applicationData);
			return activity;
		} catch (RuntimeException e) {
			ra.endActivity(activity);
			throw e;
		}
	}

	public HttpClientNIORequestActivity execute(HttpUriRequest request,
			HttpContext context, Object applicationData) throws SLEEException,
			StartActivityException {
		final HttpClientNIORequestActivityImpl activity = createActivity();
		try {
			activity.execute(request, context, applicationData);
			return activity;
		} catch (RuntimeException e) {
			ra.endActivity(activity);
			throw e;
		}
	}

	private HttpClientNIORequestActivityImpl createActivity()
			throws SLEEException, StartActivityException {
		final HttpClientNIORequestActivityImpl activity = new HttpClientNIORequestActivityImpl(
				ra);
		final HttpClientNIORequestActivityHandle handle = new HttpClientNIORequestActivityHandle(
				activity.getId());
		// this happens with a tx context
		this.ra.getResourceAdaptorContext().getSleeEndpoint()
				.startActivitySuspended(handle, activity, ACTIVITY_FLAGS);
		this.ra.addActivity(handle, activity);
		if (tracer.isFineEnabled()) {
			tracer.fine("Started activity " + activity.getId());
		}
		return activity;
	}

}
