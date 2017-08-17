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

package org.restcomm.slee.resource.http;

import javax.servlet.http.HttpSession;
import javax.slee.SLEEException;
import javax.slee.resource.ActivityAlreadyExistsException;
import javax.slee.resource.StartActivityException;

import net.java.slee.resource.http.HttpServletRaSbbInterface;
import net.java.slee.resource.http.HttpSessionActivity;

/**
 * 
 * Implementation class for HttpServletRaSbbInterface
 * 
 * @author Ivelin Ivanov
 * @author amit.bhayani
 * @author martins
 * 
 */
public class HttpServletRaSbbInterfaceImpl implements HttpServletRaSbbInterface {

	/**
	 * 
	 */
	private final HttpServletResourceAdaptor ra;
	
	/**
	 * 
	 * @param ra
	 */
	public HttpServletRaSbbInterfaceImpl(HttpServletResourceAdaptor ra) {
		this.ra = ra;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.java.slee.resource.http.HttpServletRaSbbInterface#getHttpSessionActivity(javax.servlet.http.HttpSession)
	 */
	public HttpSessionActivity getHttpSessionActivity(HttpSession httpSession)
			throws NullPointerException, IllegalArgumentException, IllegalStateException,
			ActivityAlreadyExistsException, StartActivityException,
			SLEEException {
		if (httpSession == null) {
			throw new NullPointerException("null http session");
		}
		if (!(httpSession instanceof HttpSessionWrapper)) {
			throw new IllegalArgumentException();
		}
		final HttpSessionWrapper httpSessionWrapper = (HttpSessionWrapper) httpSession;
		final HttpSessionActivityImpl activity = new HttpSessionActivityImpl(httpSessionWrapper);
		if (httpSessionWrapper.getResourceEntryPoint() == null) {
			ra.getSleeEndpoint().startActivitySuspended(activity,activity);
			httpSessionWrapper.setResourceEntryPoint(ra.getName());
		}
		return activity;
	}
}
