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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by m.vanek on 30/06/2017.
 */
public class AirbrakeServletRequestFilter implements Filter {

    private RequestEnhancer<HttpServletRequest> requestEnhancer;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String factoryClassName = filterConfig.getInitParameter("requestEnhancerFactory");
        if (factoryClassName != null) {
            RequestEnhancerFactory factory;
            try {
                factory = (RequestEnhancerFactory) Class.forName(factoryClassName).newInstance();
            } catch (Exception x) {
                throw new ServletException("Cannot create " + factoryClassName, x);
            }
            requestEnhancer = factory.get();
        } else {
            requestEnhancer = new HttpServletRequestEnhancerFactory().get();
        }
    }

    @Override
    public void destroy() {
        // nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            requestEnhancer.setRequest(httpRequest);
            try {
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                requestEnhancer.endRequest(httpRequest);
            }
        }
    }
}
