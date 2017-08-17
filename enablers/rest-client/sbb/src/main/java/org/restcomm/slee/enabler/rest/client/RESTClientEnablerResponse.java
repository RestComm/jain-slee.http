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

import org.apache.http.HttpResponse;

/**
 * The response related with the execution of a {@link RESTClientEnablerRequest}
 * .
 * 
 * @author martins
 * 
 */
public class RESTClientEnablerResponse {

	private final RESTClientEnablerRequest request;
	private final HttpResponse response;
	private final Exception exception;

	RESTClientEnablerResponse(RESTClientEnablerRequest request,
			HttpResponse response, Exception exception) {
		this.request = request;
		this.response = response;
		this.exception = exception;
	}

	/**
	 * Retrieves the executed request.
	 * 
	 * @return
	 */
	public RESTClientEnablerRequest getRequest() {
		return request;
	}

	/**
	 * Retrieves the response obtained from HTTP client, if any.
	 * 
	 * @return
	 */
	public HttpResponse getHttpResponse() {
		return response;
	}

	/**
	 * Retrieves the exception thrown when executing the request, if any.
	 * 
	 * @return
	 */
	public Exception getExecutionException() {
		return exception;
	}

}
