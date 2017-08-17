/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.restcomm.slee.enabler.rest.client;

import javax.slee.ActivityContextInterface;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;

import net.java.client.slee.resource.http.HttpClientActivity;
import net.java.client.slee.resource.http.HttpClientActivityContextInterfaceFactory;
import net.java.client.slee.resource.http.HttpClientResourceAdaptorSbbInterface;
import net.java.client.slee.resource.http.event.ResponseEvent;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.mobicents.slee.SbbContextExt;

public abstract class RESTClientEnablerChildSbb implements javax.slee.Sbb,
		RESTClientEnablerChild {

	private SbbContextExt sbbContext;
	private static Tracer tracer;

	private HttpClientActivityContextInterfaceFactory httpClientActivityContextInterfaceFactory;
	private HttpClientResourceAdaptorSbbInterface httpProvider;

	@Override
	public void execute(RESTClientEnablerRequest request) throws Exception {

		// validate request
		if (request == null) {
			throw new NullPointerException("request is null");
		}
		if (request.getType() == null) {
			throw new NullPointerException("request type is null");
		}
		if (request.getUri() == null) {
			throw new NullPointerException("request uri is null");
		}

		// create http request
		HttpUriRequest httpRequest = null;
		switch (request.getType()) {
		case DELETE:
			httpRequest = new HttpDelete(request.getUri());
			break;
		case GET:
			httpRequest = new HttpGet(request.getUri());
			break;
		case POST:
			httpRequest = new HttpPost(request.getUri());
			break;
		case PUT:
			httpRequest = new HttpPut(request.getUri());
			break;
		default:
			throw new IllegalArgumentException("unknown request type");
		}

		// add headers, if any
		if (request.getHeaders() != null) {
			for (Header h : request.getHeaders()) {
				httpRequest.addHeader(h);
			}
		}

		// sign request using oauth, if needed
		if (request.getOAuthConsumer() != null) {
			request.getOAuthConsumer().sign(httpRequest);
		}

		// add content, if any
		if (request.getContent() != null) {
			if (!(httpRequest instanceof HttpEntityEnclosingRequest)) {
				throw new IllegalArgumentException(
						"request includes content but type does not allows content");
			}
			if (request.getContentType() == null) {
				throw new IllegalArgumentException(
						"request includes content but no content type");
			}
			BasicHttpEntity entity = new BasicHttpEntity();
			entity.setContent(request.getContent());
			if (request.getContentEncoding() != null)
				entity.setContentEncoding(request.getContentEncoding());
			entity.setContentType(request.getContentType());
			((HttpEntityEnclosingRequest) httpRequest).setEntity(entity);
		}

		// get http client activity and attach to related aci
		HttpClientActivity activity = httpProvider.createHttpClientActivity(
				true, null);
		ActivityContextInterface aci = httpClientActivityContextInterfaceFactory
				.getActivityContextInterface(activity);
		aci.attach(this.sbbContext.getSbbLocalObject());

		// execute request on the activity
		activity.execute(httpRequest, request);
	}

	public void onResponseEvent(ResponseEvent event,
			ActivityContextInterface aci) {

		// get local object
		RESTClientEnablerChildSbbLocalObject child = (RESTClientEnablerChildSbbLocalObject) this.sbbContext
				.getSbbLocalObject();

		// get parent
		RESTClientEnablerParent parent = (RESTClientEnablerParent) child
				.getParent();

		// build response
		RESTClientEnablerResponse response = new RESTClientEnablerResponse(
				(RESTClientEnablerRequest) event.getRequestApplicationData(),
				event.getHttpResponse(), event.getException());

		// pass it to parent
		parent.onResponse(child, response);
	}

	public void setSbbContext(SbbContext context) {
		sbbContext = (SbbContextExt) context;
		if (tracer == null) {
			tracer = this.sbbContext.getTracer("RESTClientEnabler");
		}
		this.httpClientActivityContextInterfaceFactory = (HttpClientActivityContextInterfaceFactory) sbbContext
				.getActivityContextInterfaceFactory(HttpClientActivityContextInterfaceFactory.RESOURCE_ADAPTOR_TYPE_ID);
		this.httpProvider = (HttpClientResourceAdaptorSbbInterface) sbbContext
				.getResourceAdaptorInterface(
						HttpClientResourceAdaptorSbbInterface.RESOURCE_ADAPTOR_TYPE_ID,
						"HttpClientResourceAdaptor");
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
		this.httpClientActivityContextInterfaceFactory = null;
		this.httpProvider = null;
	}

	public void sbbCreate() throws javax.slee.CreateException {
	}

	public void sbbPostCreate() throws javax.slee.CreateException {
	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbRemove() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbExceptionThrown(Exception exception, Object event,
			ActivityContextInterface activity) {
	}

	public void sbbRolledBack(RolledBackContext context) {
	}

}
