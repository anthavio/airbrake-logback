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