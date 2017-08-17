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

package org.restcomm.client.slee.resource.http;

import javax.slee.resource.ActivityHandle;

/**
 * @author amit bhayani
 * @author martins
 *
 */
public class HttpClientActivityHandle implements ActivityHandle {
	
	private final String sessionId;
	
	/**
	 * 
	 * @param newSessionId
	 */
    public HttpClientActivityHandle(String newSessionId){
        this.sessionId = newSessionId;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
    	if (o != null && o.getClass() == this.getClass()) {
			return ((HttpClientActivityHandle)o).sessionId.equals(this.sessionId);
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
    	return sessionId.hashCode();
    }	

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "HttpClientSession ID: " + sessionId;
    }
}
