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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

/**
 * 
 * @author martins
 *
 */
public class HttpServletRaSessionListener implements HttpSessionListener {

	private static final Logger log = Logger
			.getLogger(HttpServletRaSessionListener.class.getName());

	/**
	 * even if multiple listeners are instanciated only one becomes active
	 */
	private static AtomicBoolean masterListener = new AtomicBoolean(true);
	
	private boolean active;
	
	public HttpServletRaSessionListener() {
		if (masterListener.compareAndSet(true, false)) {
			active = true;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		if (active) {
			if (log.isDebugEnabled()) 
				log.debug("sessionCreated sessionId = "
						+ httpSessionEvent.getSession().getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		if (active) {
			if (log.isDebugEnabled())
				log.debug("sessionDestroyed called for sessionId = "
						+ httpSessionEvent.getSession().getId());

			HttpSession session = httpSessionEvent.getSession();
			HttpSessionWrapper wrapper = new HttpSessionWrapper(session);
			String name = wrapper.getResourceEntryPoint();
			if (name != null) {
				HttpServletResourceEntryPoint resourceEntryPoint = HttpServletResourceEntryPointManager.getResourceEntryPoint(name);
				if (resourceEntryPoint != null) {
					resourceEntryPoint.onSessionTerminated(wrapper);
				}
			}
		}
	}

}
