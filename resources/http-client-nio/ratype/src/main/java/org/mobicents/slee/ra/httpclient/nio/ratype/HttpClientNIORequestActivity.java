/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2012, TeleStax and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for
 * a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.ra.httpclient.nio.ratype;

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
