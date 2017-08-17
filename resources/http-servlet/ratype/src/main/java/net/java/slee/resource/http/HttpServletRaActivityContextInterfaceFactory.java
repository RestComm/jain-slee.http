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

import javax.slee.ActivityContextInterface;
import javax.slee.FactoryException;
import javax.slee.UnrecognizedActivityException;

/**
 * 
 * Allows creation of ACI for an incoming HttpServletRequest
 * 
 * @author Ivelin Ivanov
 * @author amit.bhayani
 * @author martins
 * 
 */
public interface HttpServletRaActivityContextInterfaceFactory {

	/**
	 * Gets the ActivityContextInterface for {@link HttpSessionActivity}
	 * 
	 * @param activity
	 *            HttpSessionActivity
	 * @return ActivityContextInterface
	 * @throws NullPointerException
	 * @throws UnrecognizedActivityException
	 * @throws FactoryException
	 */
	public ActivityContextInterface getActivityContextInterface(
			HttpSessionActivity activity) throws NullPointerException,
			UnrecognizedActivityException, FactoryException;
	
	/**
	 * Gets the ActivityContextInterface for {@link HttpServletRequestActivity}
	 * 
	 * @param activity
	 *            HttpServletRequestActivity
	 * @return ActivityContextInterface
	 * @throws NullPointerException
	 * @throws UnrecognizedActivityException
	 * @throws FactoryException
	 */
	public ActivityContextInterface getActivityContextInterface(
			HttpServletRequestActivity activity) throws NullPointerException,
			UnrecognizedActivityException, FactoryException;

}
