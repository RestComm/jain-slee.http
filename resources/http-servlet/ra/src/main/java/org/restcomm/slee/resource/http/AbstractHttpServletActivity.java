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

import java.io.Serializable;

import javax.slee.resource.ActivityHandle;

/**
 * Base class for an activity (which is also the handle) of the {@link HttpServletResourceAdaptor}.
 * 
 * @author martins
 *
 */
public abstract class AbstractHttpServletActivity implements ActivityHandle, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final String id;
	
	public AbstractHttpServletActivity(String id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == this.getClass()) {
			return ((AbstractHttpServletActivity)obj).id.equals(this.id);
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return id;
	}
	
}
