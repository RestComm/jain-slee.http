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

package org.restcomm.slee.ra.httpclient.nio.ratype;

import javax.slee.SLEEException;
import javax.slee.TransactionRequiredLocalException;
import javax.slee.resource.ActivityAlreadyExistsException;
import javax.slee.resource.StartActivityException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

/**
 * The SBB interface exposed by Http Client RA Type.
 * 
 * @author martins
 * 
 */
public interface HttpClientNIOResourceAdaptorSbbInterface {

	/**
	 * 
	 * @param request
	 *            the request to execute.
	 * @param context
	 *            the optional context to use on the request execution.
	 * @param applicationData
	 *            a data object optionally provided by the application, to be
	 *            returned in the response event.
	 * @return
	 * @throws StartActivityException 
	 * @throws SLEEException 
	 * @throws IllegalStateException 
	 * @throws NullPointerException 
	 * @throws ActivityAlreadyExistsException 
	 * @throws TransactionRequiredLocalException 
	 */
	HttpClientNIORequestActivity execute(HttpUriRequest request,
			HttpContext context, Object applicationData) throws TransactionRequiredLocalException, ActivityAlreadyExistsException, NullPointerException, IllegalStateException, SLEEException, StartActivityException;

	/**
	 * 
	 * @param target
	 *            the target host for the request. Implementations may accept
	 *            <code>null</code> if they can still determine a route, for
	 *            example to a default target or by inspecting the request.
	 * @param request
	 *            the request to execute.
	 * @param context
	 *            the optional context to use on the request execution.
	 * @param applicationData
	 *            a data object optionally provided by the application, to be
	 *            returned in the response event.
	 * @return
	 * @throws StartActivityException 
	 * @throws SLEEException 
	 * @throws IllegalStateException 
	 * @throws NullPointerException 
	 * @throws ActivityAlreadyExistsException 
	 * @throws TransactionRequiredLocalException 
	 */
	HttpClientNIORequestActivity execute(HttpHost target, HttpRequest request,
			HttpContext context, Object applicationData) throws TransactionRequiredLocalException, ActivityAlreadyExistsException, NullPointerException, IllegalStateException, SLEEException, StartActivityException;

}
