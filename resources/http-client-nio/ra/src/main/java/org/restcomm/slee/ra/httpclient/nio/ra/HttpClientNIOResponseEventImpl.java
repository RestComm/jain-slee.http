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

import org.apache.http.HttpResponse;
import org.restcomm.slee.ra.httpclient.nio.events.HttpClientNIOResponseEvent;

/**
 * 
 * @author martins
 *
 */
public class HttpClientNIOResponseEventImpl implements HttpClientNIOResponseEvent {

	private final Object applicationData;
	private final HttpResponse response;
	private final Exception exception;

	public HttpClientNIOResponseEventImpl(HttpResponse response,
			Exception exception, Object applicationData) {
		this.response = response;
		this.exception = exception;
		this.applicationData = applicationData;
	}

	public Object getApplicationData() {
		return applicationData;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public Exception getException() {
		return exception;
	}

}
