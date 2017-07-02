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

import net.anthavio.airbrake.AirbrakeNoticeBuilderUsingFilteredSystemProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by m.vanek on 30/06/2017.
 */
public class HttpServletRequestEnhancer implements RequestEnhancer<HttpServletRequest> {

    private static final ThreadLocal<HttpServletRequest> tlRequest = new ThreadLocal<HttpServletRequest>();

    @Override
    public void setRequest(HttpServletRequest request) {
        tlRequest.set(request);
    }

    @Override
    public void endRequest(HttpServletRequest request) {
        tlRequest.remove();
    }

    @Override
    public void enhance(AirbrakeNoticeBuilderUsingFilteredSystemProperties builder) {
        HttpServletRequest request = tlRequest.get();
        if (request != null) {
            doRequest(builder, request);
            doParameters(builder, request);
            HttpSession session = request.getSession(false);
            if (session != null) {
                doSession(session, builder);
            }
        }
    }

    protected void doRequest(AirbrakeNoticeBuilderUsingFilteredSystemProperties builder, HttpServletRequest request) {
        StringBuffer urlBuffer = request.getRequestURL();
        String query = request.getQueryString();
        if (query != null && query.length() != 0) {
            urlBuffer.append('?').append(query);
        }
        builder.setRequest(urlBuffer.toString(), request.getContextPath());

    }

    protected void doParameters(AirbrakeNoticeBuilderUsingFilteredSystemProperties builder, HttpServletRequest request) {
        Map<String, Object> paramap = new HashMap<>();
        Enumeration pnames = request.getParameterNames();
        while (pnames.hasMoreElements()) {
            String pname = (String) pnames.nextElement();
            String[] pvalues = request.getParameterValues(pname);
            if (pvalues.length == 1) {
                paramap.put(pname, pvalues[0]);
            } else {
                paramap.put(pname, String.join(",", pvalues));
            }
        }
        builder.request(paramap);
    }

    protected void doSession(HttpSession session, AirbrakeNoticeBuilderUsingFilteredSystemProperties builder) {
        Map<String, Object> sessionMap = new HashMap<String, Object>();
        Enumeration snames = session.getAttributeNames();
        while (snames.hasMoreElements()) {
            String sname = (String) snames.nextElement();
            Object svalue = session.getAttribute(sname);
            sessionMap.put(sname, svalue);
        }
        builder.session(sessionMap);
    }

}
