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

import javax.slee.ActivityContextInterface;
import javax.slee.FactoryException;
import javax.slee.UnrecognizedActivityException;
import javax.slee.resource.ResourceAdaptorTypeID;

/**
 * 
 * @author amit bhayani
 * 
 */
public interface HttpClientActivityContextInterfaceFactory {

	public static final ResourceAdaptorTypeID RESOURCE_ADAPTOR_TYPE_ID = new ResourceAdaptorTypeID(
			"HttpClientResourceAdaptorType", "org.restcomm", "4.0");

	public ActivityContextInterface getActivityContextInterface(
			HttpClientActivity acivity) throws NullPointerException,
			UnrecognizedActivityException, FactoryException;

}
