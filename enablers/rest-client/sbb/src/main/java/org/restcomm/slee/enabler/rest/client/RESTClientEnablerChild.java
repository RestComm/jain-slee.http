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

/**
 * The REST Client Enabler, an SBB to be used in JAIN SLEE Child Relations,
 * which allows the asynchronous execution of REST requests.
 * 
 * @author baranowb
 * @author martins
 * 
 */
public interface RESTClientEnablerChild {

	/**
	 * Executes the specified request asynchronously.
	 * 
	 * @param request
	 * @throws Exception
	 */
	public void execute(RESTClientEnablerRequest request) throws Exception;

}
