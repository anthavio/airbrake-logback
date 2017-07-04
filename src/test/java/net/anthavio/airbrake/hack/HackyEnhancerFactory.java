package net.anthavio.airbrake.hack;

import net.anthavio.airbrake.AirbrakeNoticeBuilderUsingFilteredSystemProperties;
import net.anthavio.airbrake.http.RequestEnhancer;
import net.anthavio.airbrake.http.RequestEnhancerFactory;

/**
 * Just an example how to implement simple RequestEnhancerFactory and RequestEnhancer
 */
public class HackyEnhancerFactory implements RequestEnhancerFactory {

    @Override
    public RequestEnhancer get() {
        return new HackyEnhancer();
    }

    static class HackyEnhancer implements RequestEnhancer<Void> {

        @Override
        public void setRequest(Void request) {
            // nothing
        }

        @Override
        public void endRequest(Void request) {
            // nothing
        }

        @Override
        public void enhance(AirbrakeNoticeBuilderUsingFilteredSystemProperties builder) {
            builder.setRequest("http://localhost","");
        }
    }
}
