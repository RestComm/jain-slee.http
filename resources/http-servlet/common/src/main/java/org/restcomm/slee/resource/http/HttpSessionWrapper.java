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

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class HttpSessionWrapper implements HttpSession {

	private final HttpSession httpSession;
	
	private static final String RESOURCE_ENTRY_POINT_ATTR_NAME = "_ENTRY_POINT";

	public HttpSessionWrapper(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	/**
	 * 
	 * @return
	 */
	public String getResourceEntryPoint() {
		return (String) httpSession.getAttribute(RESOURCE_ENTRY_POINT_ATTR_NAME);
	}
	
	/**
	 * 
	 * @param resourceEntryPoint
	 */
	public void setResourceEntryPoint(String resourceEntryPoint) {
		httpSession.setAttribute(RESOURCE_ENTRY_POINT_ATTR_NAME,resourceEntryPoint);
	}
	
	public Object getAttribute(String arg0) {
		if (arg0.equals(RESOURCE_ENTRY_POINT_ATTR_NAME)) {
			throw new SecurityException("it is forbidden to use the attribute name "+arg0);
		}
		return httpSession.getAttribute(arg0);
	}

	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		return httpSession.getAttributeNames();
	}

	public long getCreationTime() {
		return httpSession.getCreationTime();
	}

	public String getId() {
		return httpSession.getId();
	}

	public long getLastAccessedTime() {
		return httpSession.getLastAccessedTime();
	}

	public int getMaxInactiveInterval() {
		return httpSession.getMaxInactiveInterval();
	}

	public ServletContext getServletContext() {
		return httpSession.getServletContext();
	}

	public HttpSessionContext getSessionContext() {
		return httpSession.getSessionContext();
	}

	public Object getValue(String arg0) {
		if (arg0.equals(RESOURCE_ENTRY_POINT_ATTR_NAME)) {
			throw new SecurityException("it is forbidden to use the attribute name "+arg0);
		}
		return httpSession.getValue(arg0);
	}

	public String[] getValueNames() {
		return httpSession.getValueNames();
	}

	public void invalidate() {
		httpSession.invalidate();
	}

	public boolean isNew() {
		return httpSession.isNew();
	}

	public void putValue(String arg0, Object arg1) {
		if (arg0.equals(RESOURCE_ENTRY_POINT_ATTR_NAME)) {
			throw new SecurityException("it is forbidden to use the attribute name "+arg0);
		}
		httpSession.putValue(arg0, arg1);
	}

	public void removeAttribute(String arg0) {
		if (arg0.equals(RESOURCE_ENTRY_POINT_ATTR_NAME)) {
			throw new SecurityException("it is forbidden to use the attribute name "+arg0);
		}
		httpSession.removeAttribute(arg0);
	}

	public void removeValue(String arg0) {
		if (arg0.equals(RESOURCE_ENTRY_POINT_ATTR_NAME)) {
			throw new SecurityException("it is forbidden to use the attribute name "+arg0);
		}
		httpSession.removeValue(arg0);
	}

	public void setAttribute(String arg0, Object arg1) {
		if (arg0.equals(RESOURCE_ENTRY_POINT_ATTR_NAME)) {
			throw new SecurityException("it is forbidden to use the attribute name "+arg0);
		}
		httpSession.setAttribute(arg0, arg1);
	}

	public void setMaxInactiveInterval(int arg0) {
		httpSession.setMaxInactiveInterval(arg0);		
	}

}
