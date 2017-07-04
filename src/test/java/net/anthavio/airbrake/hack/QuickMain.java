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


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by m.vanek on 29/06/2017.
 */
public class QuickMain {
    public static void main(String[]  args) {
        System.setProperty("logback.configurationFile","src/test/java/net/anthavio/airbrake/hack/logback-test.xml");
        Logger logger = LoggerFactory.getLogger("zxzx");
        logger.error("Brrrrrrr",new RuntimeException("Boom!"));
        logger.warn("Just a simple warn log message");


        //LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        //StatusPrinter.print(lc);
    }
}
