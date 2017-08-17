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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * Takes incoming Http requests and routes them to the JSLEE HttpServlet RA
 *
 * @author Ivelin Ivanov
 * @author martins
 *
 */
public class HttpServletRaServlet extends HttpServlet {

    private static final int HTTP_ERROR_CODE_SERVICE_UNAVAILABLE = 503;
    private static final Logger LOGGER = Logger.getLogger(HttpServletRaServlet.class.getName());
    private static final long serialVersionUID = 7542822118420702996L;

    private String servletName = null;

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) {
        if (servletName == null) {
            servletName = getServletConfig().getServletName();
        }
        HttpServletResourceEntryPoint resourceEntryPoint = HttpServletResourceEntryPointManager.getResourceEntryPoint(servletName);
        if (resourceEntryPoint == null) {
            replyNotAvailableError(servletName, response);
        } else {
            resourceEntryPoint.onRequest(request, response);
        }
    }

    public static void replyNotAvailableError(String name, HttpServletResponse response) {
        try {
            response.sendError(HTTP_ERROR_CODE_SERVICE_UNAVAILABLE, "JSLEE Http Servlet RA Entity with name " + name + " is not available");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to send 503 response to inform client that RA is not available", e);
        }
    }
}
