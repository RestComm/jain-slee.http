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

package net.java.slee.resource.http;

import javax.servlet.http.HttpSession;
import javax.slee.SLEEException;
import javax.slee.resource.ActivityAlreadyExistsException;
import javax.slee.resource.StartActivityException;

/**
 * 
 * Provides SBB with an interface
 * 
 * @author Ivelin Ivanov
 * @version 1.0
 * 
 */
public interface HttpServletRaSbbInterface {

	/**
	 * Retrieves an {@link HttpSessionActivity} for the specified
	 * {@link HttpSession}. If the activity does not exist a new one is created.
	 * 
	 * @param httpSession
	 * @return
	 * @throws NullPointerException
	 * @throws IllegalArgumentException if the http session instance class is not recognizable
	 * @throws IllegalStateException
	 * @throws ActivityAlreadyExistsException
	 * @throws StartActivityException
	 * @throws SLEEException
	 */
	public HttpSessionActivity getHttpSessionActivity(HttpSession httpSession)
			throws  NullPointerException, IllegalArgumentException, IllegalStateException,
			ActivityAlreadyExistsException, StartActivityException,
			SLEEException;

}
