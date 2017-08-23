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
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by m.vanek on 02/07/2017.
 */
public class HttpServletRequestEnhancerTest {

    @Test
    public void testEnhance() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("paramName", "paramValue");
        request.addParameter("pmulti", new String[]{"v1", "v2"});
        request.setContextPath("/webcontext");
        request.setRequestURI("/webcontext/whatever?xxx=yyy");

        MockHttpSession session = new MockHttpSession();
        session.putValue("sessionName", "sessionValue");
        request.setSession(session);

        MockServletContext context = new MockServletContext();
        HttpServletRequestEnhancerFactory.init(context);

        HttpServletRequestEnhancer enhancer = new HttpServletRequestEnhancerFactory().get();
        enhancer.setRequest(request);

        Notice notice = new Notice(new RuntimeException("Just for fun"));
        enhancer.enhance(notice);


        assertThat(notice.url).isEqualTo("http://localhost:80/webcontext/whatever?xxx=yyy");
        assertThat(notice.context.get("component")).isEqualTo("/webcontext");

        Map<String, Object> requestMap = notice.params;
        assertThat(requestMap).containsEntry("paramName", "paramValue");
        assertThat(requestMap).containsEntry("pmulti", "v1,v2");

        Map<String, Object> sessionMap = notice.session;
        assertThat(sessionMap).containsEntry("sessionName", "sessionValue");
    }
}
