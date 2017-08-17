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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the active resource entry points. 
 * @author martins
 *
 */
public class HttpServletResourceEntryPointManager {

	/**
	 * 
	 */
	private static final ConcurrentHashMap<String, HttpServletResourceEntryPoint> map = new ConcurrentHashMap<String, HttpServletResourceEntryPoint>(1);
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static HttpServletResourceEntryPoint getResourceEntryPoint(String name) {
		return map.get(name);
	}
	
	/**
	 * 
	 * @param name
	 */
	public static void removeResourceEntryPoint(String name) {
		map.remove(name);
	}
	
	/**
	 * 
	 * @param name
	 * @param resourceEntryPoint
	 */
	public static void putResourceEntryPoint(String name, HttpServletResourceEntryPoint resourceEntryPoint) {
		map.put(name,resourceEntryPoint);
	}
}
