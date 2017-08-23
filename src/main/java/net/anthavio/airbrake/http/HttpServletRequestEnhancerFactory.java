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

/**
 * Created by m.vanek on 30/06/2017.
 */
public class HttpServletRequestEnhancerFactory implements RequestEnhancerFactory {

    private static final boolean IS_SERVLET_API;

    static {
        boolean found = false;
        try {
            Class.forName("javax.servlet.http.HttpServletRequest");
            found = true;
        } catch (ClassNotFoundException cnfx) {
        }
        IS_SERVLET_API = found;
    }

    public static boolean isServletApi() {
        return IS_SERVLET_API;
    }

    private static HttpServletRequestEnhancer enhancer; // single instance of enhancer

    public static HttpServletRequestEnhancer init(ServletContext servletContext) {
        enhancer = new HttpServletRequestEnhancer(servletContext);
        return enhancer;
    }

    @Override
    public HttpServletRequestEnhancer get() {
        return enhancer;
    }

}
