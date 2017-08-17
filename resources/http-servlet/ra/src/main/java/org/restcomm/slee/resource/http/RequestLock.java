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

import net.java.slee.resource.http.events.HttpServletRequestEvent;

public class RequestLock {

	private ConcurrentHashMap<HttpServletRequestEvent, Object> map = new ConcurrentHashMap<HttpServletRequestEvent, Object>();

	public Object getLock(HttpServletRequestEvent event) {
		Object obj = map.get(event);
		if (obj == null) {
			obj = createLock(event);
		}
		return obj;
	}

	private Object createLock(HttpServletRequestEvent event) {
		Object object = new Object();
		Object anotherObject = map.putIfAbsent(event, object);
		if (anotherObject == null) {
			return object;
		}
		else {
			return anotherObject;
		}
	}

	public Object removeLock(HttpServletRequestEvent event) {
		return map.remove(event);
	}

}
