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

package net.java.client.slee.resource.http;

import javax.slee.resource.ResourceAdaptorTypeID;
import javax.slee.resource.StartActivityException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.protocol.HttpContext;

/**
 * Provides SBB with the interface to interact with Http Client Resource
 * Adaptor. HttpClientResourceAdaptorSbbInterface is wrapper over
 * {@link org.apache.http.client.HttpClient} and exposes most commonly used
 * methods of HttpClient
 * 
 * @author amit bhayani
 * @author martins
 * 
 */
public interface HttpClientResourceAdaptorSbbInterface {

	public static final ResourceAdaptorTypeID RESOURCE_ADAPTOR_TYPE_ID = HttpClientActivityContextInterfaceFactory.RESOURCE_ADAPTOR_TYPE_ID;

	/**
	 * Retrieves the client managed by the RA, allowing execution of synchronous
	 * requests and access the client parameters. Note that the returned client
	 * throws {@link SecurityException} if the application tries to access its
	 * {@link ClientConnectionManager}.
	 * 
	 * @return
	 */
	public HttpClient getHttpClient();

	/**
	 * <p>
	 * Creates instance of {@link HttpClientActivity} for service that wants to
	 * send Requests asynchronously
	 * </p>
	 * 
	 * @param endOnReceivingResponse
	 *            if true Activity ends automatically as soon as the
	 *            ResponseEvent is sent by ResourceAdaptor. If false the service
	 *            has to explicitly end activity
	 * @param context
	 *            an optional http context may be provided. If not provided a
	 *            basic context will be set.
	 * @return
	 * @throws StartActivityException
	 */
	public HttpClientActivity createHttpClientActivity(
			boolean endOnReceivingResponse, HttpContext context)
			throws StartActivityException;

}
