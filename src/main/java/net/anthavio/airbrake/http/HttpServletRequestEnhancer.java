/**
 * Copyright (C) 2014 Anthavio (http://dev.anthavio.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.anthavio.airbrake.http;

import io.airbrake.javabrake.Notice;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by m.vanek on 30/06/2017.
 */
public class HttpServletRequestEnhancer implements RequestEnhancer<HttpServletRequest> {

    private static final ThreadLocal<HttpServletRequest> tlRequest = new ThreadLocal<HttpServletRequest>();

    private final ServletContext servletContext;

    public HttpServletRequestEnhancer(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        tlRequest.set(request);
    }

    @Override
    public void endRequest(HttpServletRequest request) {
        tlRequest.remove();
    }

    @Override
    public void enhance(Notice notice) {
        doContext(notice, servletContext);
        HttpServletRequest request = tlRequest.get();
        if (request != null) {
            doRequest(notice, request);
            doParameters(notice, request);
            HttpSession session = request.getSession(false);
            if (session != null) {
                doSession(notice, session);
            }
        }
    }

    protected void doRequest(Notice notice, HttpServletRequest request) {
        StringBuffer urlBuffer = request.getRequestURL();
        String query = request.getQueryString();
        if (query != null && query.length() != 0) {
            urlBuffer.append('?').append(query);
        }

        // https://airbrake.io/docs/api/#create-notice-v3

        notice.url = urlBuffer.toString();
        notice.setContext("component", request.getContextPath());
        //notice.setContext("url",urlBuffer.toString());
        request.getRemoteUser();

        notice.setContext("remoteAddr", request.getLocalAddr());
        notice.setContext("userAddr", request.getRemoteAddr());

    }

    protected void doParameters(Notice notice, HttpServletRequest request) {

        Enumeration pnames = request.getParameterNames();
        while (pnames.hasMoreElements()) {
            String pname = (String) pnames.nextElement();
            String[] pvalues = request.getParameterValues(pname);
            if (pvalues.length == 1) {
                notice.setParam(pname, pvalues[0]);
            } else {
                notice.setParam(pname, join(pvalues, ","));
            }
        }
    }

    protected void doSession(Notice notice, HttpSession session) {
        Enumeration snames = session.getAttributeNames();
        while (snames.hasMoreElements()) {
            String sname = (String) snames.nextElement();
            Object svalue = session.getAttribute(sname);
            notice.setSession(sname, svalue);
        }
        notice.setSession("JSESSIONID", session.getId());
    }

    protected void doContext(Notice notice, ServletContext servletContext) {
        notice.setContext("rootDirectory", servletContext.getRealPath("/"));
    }

    public static String join(String[] values, String delim) {
        if (values != null) {
            if (values.length == 1) {
                return values[0];
            } else {
                StringBuilder sb = new StringBuilder();
                for (String value : values) {
                    sb.append(value).append(delim);
                }
                return sb.deleteCharAt(sb.length() - 1).toString();
            }
        }
        return null;
    }

}
