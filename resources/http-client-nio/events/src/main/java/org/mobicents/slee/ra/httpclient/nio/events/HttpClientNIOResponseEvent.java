/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2012, TeleStax and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for
 * a full listing of individual contributors.
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

package org.mobicents.slee.ra.httpclient.nio.events;

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
