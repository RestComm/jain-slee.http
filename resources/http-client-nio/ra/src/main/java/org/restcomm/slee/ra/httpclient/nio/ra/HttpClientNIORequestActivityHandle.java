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

package org.restcomm.slee.ra.httpclient.nio.ra;

import javax.slee.resource.ActivityHandle;

/**
 * 
 * @author martins
 *
 */
public class HttpClientNIORequestActivityHandle implements ActivityHandle {
	
	private final String id;
	
	/**
	 * 
	 * @param id
	 */
    public HttpClientNIORequestActivityHandle(String id){
        this.id = id;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
    	if (o != null && o.getClass() == this.getClass()) {
			return ((HttpClientNIORequestActivityHandle)o).id.equals(this.id);
		}
		else {
			return false;
		}
    }    
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
    	return id.hashCode();
    }	

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "HttpClientNIOActivityHandle:" + id;
    }
}
