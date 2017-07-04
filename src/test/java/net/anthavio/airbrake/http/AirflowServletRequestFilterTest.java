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

import airbrake.AirbrakeNotice;
import net.anthavio.airbrake.AirbrakeNoticeBuilderUsingFilteredSystemProperties;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

public class AirflowServletRequestFilterTest {

    @Test
    public void testFactoryClassNotFound() {
        AirflowServletRequestFilter filter = new AirflowServletRequestFilter();
        FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(filterConfig.getInitParameter("requestEnhancerFactory")).thenReturn("wrong.ClassName");
        try {
            filter.init(filterConfig);
        } catch(ServletException sx) {
            assertThat(sx.getMessage()).isEqualTo("Cannot create wrong.ClassName");
            assertThat(sx.getRootCause()).isInstanceOf(ClassNotFoundException.class);
        }
    }

    @Test
    public void testDefaultFactory() throws ServletException, IOException {
        AirflowServletRequestFilter filter = new AirflowServletRequestFilter();
        FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(filterConfig.getInitParameter("requestEnhancerFactory")).thenReturn(null);
        filter.init(filterConfig);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("paramName","paramValue");
        request.addParameter("pmulti", new String[] {"v1","v2"});

        MockHttpSession session= new MockHttpSession();
        session.putValue("sessionName","sessionValue");
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
    }
}