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

package org.restcomm.slee.ra.httpclient.nio.ratype;

/**
 * 
 * @author martins
 * 
 */
public interface HttpClientNIORequestActivity {

	/**
	 * Attempts to cancel execution of the request. This attempt will fail if
	 * the request has already completed, has already been cancelled, or could
	 * not be cancelled for some other reason. If successful, and this request
	 * has not started when <tt>cancel</tt> is called, this request should never
	 * run. If the request has already started, then the
	 * <tt>mayInterruptIfRunning</tt> parameter determines whether the execution
	 * of this request should be interrupted in an attempt to stop the request.
	 * 
	 * <p>
	 * After this method returns, subsequent calls to {@link #isDone} will
	 * always return <tt>true</tt>. Subsequent calls to {@link #isCancelled}
	 * will always return <tt>true</tt> if this method returned <tt>true</tt>.
	 * 
	 * @param mayInterruptIfRunning
	 *            <tt>true</tt> if the execution of this request should be
	 *            interrupted; otherwise, in-progress requests are allowed to
	 *            complete
	 * @return <tt>false</tt> if the request could not be cancelled, typically
	 *         because it has already completed normally; <tt>true</tt>
	 *         otherwise
	 */
	boolean cancel(boolean mayInterruptIfRunning);

	/**
	 * Returns <tt>true</tt> if this request was cancelled before it completed
	 * normally.
	 * 
	 * @return <tt>true</tt> if this request was cancelled before it completed
	 */
	boolean isCancelled();

	/**
	 * Returns <tt>true</tt> if this request completed.
	 * 
	 * Completion may be due to normal termination, an exception, or
	 * cancellation -- in all of these cases, this method will return
	 * <tt>true</tt>.
	 * 
	 * @return <tt>true</tt> if this request completed
	 */
	boolean isDone();

}
