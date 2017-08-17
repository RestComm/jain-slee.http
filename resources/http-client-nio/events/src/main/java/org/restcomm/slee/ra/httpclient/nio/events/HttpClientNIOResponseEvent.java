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

package org.restcomm.slee.ra.httpclient.nio.events;

import org.apache.http.HttpResponse;

/**
 * The SLEE event providing the response to an HTTP Client NIO RA request.
 * 
 * @author martins
 * 
 */
public interface HttpClientNIOResponseEvent {

	/**
	 * The application data included in the HTTP Request.
	 * 
	 * @return
	 */
	Object getApplicationData();

	/**
	 * The response to the HTTP Request.
	 * 
	 * @return null if the request did not complete due to an exception.
	 */
	HttpResponse getResponse();

	/**
	 * The exception which occurred when sending the HTTP Request, if any.
	 * 
	 * @return null if the request did complete.
	 */
	Exception getException();

}
