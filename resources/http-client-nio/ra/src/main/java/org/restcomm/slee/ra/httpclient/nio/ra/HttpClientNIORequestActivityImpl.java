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

package org.restcomm.slee.ra.httpclient.nio.ra;

import java.util.UUID;
import java.util.concurrent.Future;


import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.restcomm.slee.ra.httpclient.nio.ratype.HttpClientNIORequestActivity;

/**
 * 
 * @author martins
 *
 */
public class HttpClientNIORequestActivityImpl implements HttpClientNIORequestActivity {

	private final String id;
	private final HttpClientNIOResourceAdaptor ra;
	
	private Future<HttpResponse> future;
	
	public HttpClientNIORequestActivityImpl(HttpClientNIOResourceAdaptor ra) {
		this.ra = ra;
		this.id = UUID.randomUUID().toString();
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}
	
	public boolean isCancelled() {
		return future.isCancelled();
	}
	
	public boolean isDone() {
		return future.isDone();
	}
	
	void execute(HttpUriRequest request,
			HttpContext context, Object applicationData) {		
		future = ra.httpAsyncClient.execute(request, processHttpContext(context), getFutureCallback(applicationData));
	}

	void execute(HttpHost target, HttpRequest request,
			HttpContext context, Object applicationData) {		
		future = ra.httpAsyncClient.execute(target, request, processHttpContext(context), getFutureCallback(applicationData));
	}
	
	private HttpContext processHttpContext(HttpContext context) {
		// Maintains HttpSession on server side
		if (context == null) {
			context = new BasicHttpContext();

		}
		if (context.getAttribute(ClientContext.COOKIE_STORE) == null) {
			BasicCookieStore cookieStore = new BasicCookieStore();
			context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		}
		return context;
	}
	
	private FutureCallback<HttpResponse> getFutureCallback(final Object applicationData) {
		return new FutureCallback<HttpResponse>() {		
			public void failed(Exception exception) {
				final HttpClientNIOResponseEventImpl event = new HttpClientNIOResponseEventImpl(null, exception, applicationData);
				ra.processResponseEvent(event, HttpClientNIORequestActivityImpl.this);
				ra.endActivity(HttpClientNIORequestActivityImpl.this);				
			}			
			public void completed(HttpResponse response) {
				final HttpClientNIOResponseEventImpl event = new HttpClientNIOResponseEventImpl(response, null, applicationData);
				ra.processResponseEvent(event, HttpClientNIORequestActivityImpl.this);
				ra.endActivity(HttpClientNIORequestActivityImpl.this);				
			}			
			public void cancelled() {
				ra.endActivity(HttpClientNIORequestActivityImpl.this);
			}					
		};
	}
	
	protected String getId() {
		return id;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			return ((HttpClientNIORequestActivityImpl) obj).id
					.equals(this.id);
		} else {
			return false;
		}
	}

}
