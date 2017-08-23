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
package net.anthavio.airbrake;

import java.util.Arrays;

import io.airbrake.javabrake.*;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import net.anthavio.airbrake.http.HttpServletRequestEnhancerFactory;
import net.anthavio.airbrake.http.RequestEnhancer;
import net.anthavio.airbrake.http.RequestEnhancerFactory;

/**
 * @author martin.vanek
 * <p>
 * javabrake is v3 only and has url path hardcoded
 * String.format("%s/api/v3/projects/%d/notices", host, this.projectId);
 * so and only hostname can be changed
 * <p>
 * v3 API
 * https://airbrake.io/docs/api/#create-notice-v3
 * v2 API
 * https://airbrake.io/docs/legacy-xml-api/
 */
public class AirbrakeLogbackAppender extends AppenderBase<ILoggingEvent> {

    public enum Notify {
        EVERYTHING, EXCEPTIONS
    }

    public enum SendMode {
        SYNC, ASYNC
    }

    private String projectKey;

    private Integer projectId;

    private String requestEnhancerFactory;

    private Notify notify = Notify.EXCEPTIONS; // default compatible with airbrake-java

    private SendMode sendMode = SendMode.ASYNC;

    private boolean enabled = true;

    private RequestEnhancerFactory requestEnhancerFactoryInstance;

    private Notifier notifier;

    public AirbrakeLogbackAppender() {
        // for logback
    }

    /**
     * For testing...
     */
    protected AirbrakeLogbackAppender(Notifier notifier) {
        this.notifier = notifier;
    }

    public String getRequestEnhancerFactory() {
        return requestEnhancerFactory;
    }

    public void setRequestEnhancerFactory(String requestEnhancerFactory) {
        this.requestEnhancerFactory = requestEnhancerFactory;
    }

    /**
     * https://api.airbrake.io or http://errbit.yourdomain.com
     */
    public void setHost(final String host) {
        //TODO this should do addError instead of throwing exception
        if (host == null || !host.startsWith("http")) {
            throw new IllegalArgumentException("Wrong url: " + host);
        }
        notifier.setHost(host);
    }

    public Notify getNotify() {
        return notify;
    }

    public void setNotify(Notify notify) {
        this.notify = notify;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public SendMode getSendMode() {
        return sendMode;
    }

    public void setSendMode(SendMode sendMode) {
        this.sendMode = sendMode;
    }

    @Override
    protected void append(final ILoggingEvent event) {
        if (!enabled) {
            return;
        }

        IThrowableProxy proxy;
        if ((proxy = event.getThrowableProxy()) != null) {
            // Exception notifications are always sent
            Throwable throwable = ((ThrowableProxy) proxy).getThrowable();

            Notice notice = new Notice(throwable);
            notice.setContext("error_message",event.getFormattedMessage());
            // FIXME Allowed values: debug, info, notice, warning, error, critical, alert, emergency, invalid.
            notice.setContext("severity",event.getLevel());
            enhanceAndSend(notice);


        } else if (notify == Notify.EVERYTHING) {
            // Send other notifications then Exception only when Notify.EVERYTHING is set
            StackTraceElement stackTraceElement = event.getCallerData()[0];
            NoticeStackRecord noticeStackRecord = new NoticeStackRecord(stackTraceElement);
            NoticeError error = new NoticeError(event.getLoggerName(), event.getFormattedMessage(), Arrays.asList(noticeStackRecord));
            enhanceAndSend(new Notice(Arrays.asList(error)));
        }
    }

    private void enhanceAndSend(Notice notice) {
        if (requestEnhancerFactoryInstance != null) {
            RequestEnhancer enhancer = requestEnhancerFactoryInstance.get();
            if (enhancer != null) {
                enhancer.enhance(notice);
            }
        }
        if (sendMode == SendMode.ASYNC)
            notifier.send(notice);
        else
            notifier.sendSync(notice);
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void start() {
        if (notifier == null) {
            if (projectId == null) {
                // Have to throw otherwise NullPointerException will be thrown from new Notifier...
                throw new IllegalArgumentException("PROJECT_ID not set for the appender named [" + name + "].");
            }
            if (projectKey == null || projectKey.isEmpty()) {
                addError("PROJECT_KEY not set for the appender named [" + name + "].");
            }
            notifier = new Notifier(projectId, projectKey);
        }

        if (requestEnhancerFactory != null) {
            if (!requestEnhancerFactory.isEmpty()) { // Set factory to "" to disable it even in HttpServlet environment
                try {
                    requestEnhancerFactoryInstance = (RequestEnhancerFactory) Class.forName(requestEnhancerFactory).newInstance();
                } catch (Exception x) {
                    throw new IllegalStateException("Cannot create " + requestEnhancerFactory, x);
                }

            }
        } else if (HttpServletRequestEnhancerFactory.isServletApi()) {
            // This is surely be executed BEFORE AirbrakeServletRequestFilter initialization and RequestEnhancerFactory will return null
            requestEnhancerFactoryInstance = new HttpServletRequestEnhancerFactory();
        }
        super.start();
    }

}
