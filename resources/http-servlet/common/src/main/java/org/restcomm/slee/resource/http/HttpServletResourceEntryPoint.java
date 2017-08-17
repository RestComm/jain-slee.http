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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * Provides an entry point for a designated HttpServlet to the Resource Adaptor.
 * It bridges incoming Http requests via an HttpServlet to the RA.
 * 
 * 
 * @author Ivelin Ivanov
 * @author amit.bhayani
 * 
 */
public interface HttpServletResourceEntryPoint {
	
	public void onRequest(HttpServletRequest request, HttpServletResponse response);
	
	public void onSessionTerminated(HttpSessionWrapper httpSessionWrapper);

}
