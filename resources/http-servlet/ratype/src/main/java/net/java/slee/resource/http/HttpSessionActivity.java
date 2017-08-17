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

/**
 * HttpSessionActivity represents HttpSession created from incoming
 * javax.servlet.http.HttpServletRequest. The implementing ResourceAdaptor can
 * create a HttpSession calling getSession() on incoming request and assign the
 * session.getId() to SessionId of HttpSessionActivity. <br/> The SBB can
 * expicitly end the HttpSessionActivity by calling invalidate() on HttpSession
 * Object or when ever the HttpSession times out Web Container will invalidate
 * session. <br/>
 * 
 * @author Ivelin Ivanov
 * @author amit.bhayani
 * @version 1.0
 * 
 */
public interface HttpSessionActivity {

	/**
	 * Retrieves the Session's ID.
	 * 
	 * @return Session ID
	 */
	public String getSessionId();

	/**
	*  End HttpSession activity
	*/

	public void endActivity();
	

}
