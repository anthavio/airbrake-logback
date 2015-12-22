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

import java.util.List;

import net.anthavio.airbrake.AirbrakeLogbackAppender.Notify;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import airbrake.AirbrakeNotice;
import airbrake.AirbrakeNotifier;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.status.Status;

/**
 * 
 * @author martin.vanek
 *
 */
public class AirbrakeLogbackAppenderTest {

    AirbrakeNotifier notifier;

    AirbrakeLogbackAppender appender;

    Logger logger;

    ArgumentCaptor<AirbrakeNotice> captor;

    public AirbrakeLogbackAppenderTest() {
        captor = ArgumentCaptor.forClass(AirbrakeNotice.class);
        notifier = Mockito.mock(AirbrakeNotifier.class);
        Mockito.when(notifier.notify(captor.capture())).thenReturn(0);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        appender = new AirbrakeLogbackAppender(notifier);
        appender.setApiKey("whatever");
        appender.setEnv("test");
        appender.setContext(context);
        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel("ERROR");
        appender.addFilter(filter);
        appender.start();

        logger = (Logger) LoggerFactory.getLogger(getClass());
        logger.addAppender(appender);
        logger.setAdditive(false);

    }

    @Before
    public void before() {
        appender.setNotify(Notify.EXCEPTIONS);
        Mockito.reset(notifier);
        //Mockito.reset(captor);
    }

    @Test
    public void testWithException() {
        //When
        IllegalArgumentException exception = new IllegalArgumentException("This is exception message");
        logger.error("This is error message", exception);

        //Then
        Mockito.verify(notifier).notify(captor.capture());
        AirbrakeNotice notice = captor.getValue();

        Assertions.assertThat(notice.errorClass()).isEqualTo(exception.getClass().getName());
        Assertions.assertThat(notice.errorMessage()).isEqualTo(exception.getMessage());

        String topTraceLine = notice.backtrace().iterator().next();
        Assertions.assertThat(topTraceLine).startsWith("at net.anthavio.airbrake.AirbrakeLogbackAppenderTest.testWithException");

        Assertions.assertThat(notice.env()).isEqualTo(appender.getEnv());
        Assertions.assertThat(notice.apiKey()).isEqualTo("whatever");

        Assertions.assertThat(notice.component()).isNull();
        Assertions.assertThat(notice.projectRoot()).isNull();
    }

    @Test
    public void testSimpleMessage() {
        // Given - configure message sending
        appender.setNotify(Notify.ALL);
        // When
        logger.error("This is error message");
        // Then
        Mockito.verify(notifier).notify(captor.capture());
        AirbrakeNotice notice = captor.getValue();

        Assertions.assertThat(notice.errorClass()).isNull();
        Assertions.assertThat(notice.errorMessage()).isEqualTo("This is error message");
        String topTraceLine = notice.backtrace().iterator().next();
        Assertions.assertThat(topTraceLine).startsWith("at net.anthavio.airbrake.AirbrakeLogbackAppenderTest.testSimpleMessage");

    }

    @Test
    public void testNotifyExeptionsOnly() {
        // Given
        appender.setNotify(Notify.EXCEPTIONS);
        // When
        logger.error("This is error message");
        // Then
        Mockito.verifyZeroInteractions(notifier);

        // When
        IllegalArgumentException exception = new IllegalArgumentException("This is exception message");
        logger.error("This is error message", exception);
        // Then
        Mockito.verify(notifier).notify(captor.capture());
        AirbrakeNotice notice = captor.getValue();

        Assertions.assertThat(notice.errorClass()).isEqualTo(exception.getClass().getName());
        Assertions.assertThat(notice.errorMessage()).isEqualTo(exception.getMessage());
    }

    @Test
    public void testNotifyOff() {
        // Given
        appender.setNotify(Notify.OFF);
        // When
        logger.error("This is error message");
        // Then
        Mockito.verifyZeroInteractions(notifier);

        // When
        logger.error("This is error message", new IllegalArgumentException("This is exception message"));
        // Then
        Mockito.verifyZeroInteractions(notifier);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConfiguration() {
        // When
        AirbrakeLogbackAppender appender = new AirbrakeLogbackAppender();
        appender.setContext(new LoggerContext());
        appender.start();
        List<Status> statusList = appender.getStatusManager().getCopyOfStatusList();
        Assertions.assertThat(statusList).hasSize(2); // 

        appender.stop();

        // When with protocol
        appender.setUrl("https://www.example.org");

        // When without protocol
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Wrong url: www.example.org");

        appender.setUrl("www.example.org");
    }

    @Test
    public void testEnableDisable() {
        // When
        appender.setEnabled(false);
        logger.error("This is error message");
        logger.error("This is error message", new NullPointerException("Test test test"));
        // Then
        Mockito.verifyZeroInteractions(notifier);

        // When
        appender.setEnabled(true);
        logger.error("This is error message", new NullPointerException("Test test test"));
        // Then
        Mockito.verify(notifier).notify(captor.capture());
    }

}
