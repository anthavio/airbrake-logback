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
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by m.vanek on 02/07/2017.
 */
public class HttpServletRequestEnhancerTest {

    @Test
    public void testEnhance() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("paramName","paramValue");
        request.addParameter("pmulti", new String[] {"v1","v2"});
        request.setContextPath("/webcontext");
        request.setRequestURI("/webcontext/whatever?xxx=yyy");

        MockHttpSession session= new MockHttpSession();
        session.putValue("sessionName","sessionValue");
        request.setSession(session);

        HttpServletRequestEnhancer enhancer = new HttpServletRequestEnhancerFactory().get();
        enhancer.setRequest(request);

        StackTraceElement stElement = new StackTraceElement("declaringClass", "methodName",
                "fileName", 5);
        AirbrakeNoticeBuilderUsingFilteredSystemProperties builder = new AirbrakeNoticeBuilderUsingFilteredSystemProperties("akpiKey","error message", stElement,"environment");
        enhancer.enhance(builder);
        AirbrakeNotice notice = builder.newNotice();

        assertThat(notice.url()).isEqualTo("http://localhost:80/webcontext/whatever?xxx=yyy");
        assertThat(notice.component()).isEqualTo("/webcontext");

        Map<String, Object> requestMap = notice.request();
        assertThat(requestMap).containsEntry("paramName","paramValue");
        assertThat(requestMap).containsEntry("pmulti","v1,v2");

        Map<String, Object> sessionMap = notice.session();
        assertThat(sessionMap).containsEntry("sessionName","sessionValue");
    }
}
