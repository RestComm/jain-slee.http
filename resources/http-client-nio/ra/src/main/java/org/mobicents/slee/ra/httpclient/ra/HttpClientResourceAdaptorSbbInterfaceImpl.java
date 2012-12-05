package org.mobicents.slee.ra.httpclient.ra;

import javax.slee.SLEEException;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityFlags;
import javax.slee.resource.StartActivityException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.mobicents.slee.ra.httpclient.ratype.HttpClientRequestActivity;
import org.mobicents.slee.ra.httpclient.ratype.HttpClientResourceAdaptorSbbInterface;

/**
 * 
 * @author martins
 * 
 */
public class HttpClientResourceAdaptorSbbInterfaceImpl implements
		HttpClientResourceAdaptorSbbInterface {

	private static final int ACTIVITY_FLAGS = ActivityFlags.REQUEST_ENDED_CALLBACK;

	private final Tracer tracer;

	private final HttpClientResourceAdaptor ra;

	public HttpClientResourceAdaptorSbbInterfaceImpl(
			HttpClientResourceAdaptor ra) {
		this.ra = ra;
		this.tracer = ra.getResourceAdaptorContext().getTracer(
				HttpClientResourceAdaptorSbbInterfaceImpl.class.getName());
	}

	public HttpParams getHttpClientParams() {
		if (!this.ra.isActive) {
			throw new IllegalStateException("ra is not in active state");
		}
		return ra.httpclient.getParams();
	}

	public HttpClientRequestActivity execute(HttpHost target,
			HttpRequest request, HttpContext context, Object applicationData)
			throws SLEEException, StartActivityException {
		final HttpClientRequestActivityImpl activity = createActivity();
		try {
			activity.execute(target, request, context, applicationData);
			return activity;
		} catch (RuntimeException e) {
			ra.endActivity(activity);
			throw e;
		}
	}

	public HttpClientRequestActivity execute(HttpUriRequest request,
			HttpContext context, Object applicationData) throws SLEEException,
			StartActivityException {
		final HttpClientRequestActivityImpl activity = createActivity();
		try {
			activity.execute(request, context, applicationData);
			return activity;
		} catch (RuntimeException e) {
			ra.endActivity(activity);
			throw e;
		}
	}

	private HttpClientRequestActivityImpl createActivity()
			throws SLEEException, StartActivityException {
		final HttpClientRequestActivityImpl activity = new HttpClientRequestActivityImpl(
				ra);
		final HttpClientRequestActivityHandle handle = new HttpClientRequestActivityHandle(
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
