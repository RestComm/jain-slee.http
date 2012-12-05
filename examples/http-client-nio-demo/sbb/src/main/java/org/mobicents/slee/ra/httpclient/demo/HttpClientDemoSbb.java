package org.mobicents.slee.ra.httpclient.demo;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.mobicents.slee.ra.httpclient.events.HttpClientResponseEvent;
import org.mobicents.slee.ra.httpclient.ratype.HttpClientActivityContextInterfaceFactory;
import org.mobicents.slee.ra.httpclient.ratype.HttpClientRequestActivity;
import org.mobicents.slee.ra.httpclient.ratype.HttpClientResourceAdaptorSbbInterface;

/**
 * 
 * @author martins
 * 
 */
public abstract class HttpClientDemoSbb implements javax.slee.Sbb {

	private Tracer tracer;
	private SbbContext sbbContext;
	private HttpClientActivityContextInterfaceFactory httpClientACIF;
	private HttpClientResourceAdaptorSbbInterface httpClientRA;

	public void setSbbContext(SbbContext context) {

		this.sbbContext = context;
		this.tracer = sbbContext.getTracer(HttpClientDemoSbb.class
				.getSimpleName());
		try {
			Context ctx = (Context) new InitialContext()
					.lookup("java:comp/env");
			httpClientACIF = (HttpClientActivityContextInterfaceFactory) ctx
					.lookup("slee/resources/http-client/acifactory");
			httpClientRA = (HttpClientResourceAdaptorSbbInterface) ctx
					.lookup("slee/resources/http-client/sbbinterface");
		} catch (NamingException ne) {
			tracer.severe("Could not set SBB context:", ne);
		}
	}

	// Event handler methods

	public void onStartServiceEvent(
			javax.slee.serviceactivity.ServiceStartedEvent event,
			ActivityContextInterface aci) {
		tracer.info("Retreiving www.telestax.com...");
		try {
			HttpClientRequestActivity activity = httpClientRA.execute(
					new HttpGet("http://www.telestax.com"), null,
					System.currentTimeMillis());
			httpClientACIF.getActivityContextInterface(activity).attach(
					sbbContext.getSbbLocalObject());
		} catch (Exception e) {
			tracer.severe("failed to retrieve webpage", e);
		}
	}

	public void onHttpClientResponseEvent(HttpClientResponseEvent event,
			ActivityContextInterface aci) {
		HttpResponse response = event.getResponse();
		tracer.info("Receieved response in "
				+ (System.currentTimeMillis() - ((Long) event
						.getApplicationData())) + "ms, status: "
				+ response.getStatusLine().getStatusCode());
		try {
			tracer.info("Response content length = "
					+ EntityUtils.toString(response.getEntity()).length());
		} catch (Exception e) {
			tracer.severe("Failed reading response body", e);
		}
	}

	// Unused SBB lifecycle methods

	public void sbbActivate() {
	}

	public void sbbCreate() throws CreateException {
	}

	public void sbbExceptionThrown(Exception arg0, Object arg1,
			ActivityContextInterface arg2) {
	}

	public void sbbLoad() {
	}

	public void sbbPassivate() {
	}

	public void sbbPostCreate() throws CreateException {
	}

	public void sbbRemove() {
	}

	public void sbbRolledBack(RolledBackContext arg0) {
	}

	public void sbbStore() {
	}

	public void unsetSbbContext() {
	}

}
