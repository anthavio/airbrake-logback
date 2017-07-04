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
