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

import io.airbrake.javabrake.Notice;
import io.airbrake.javabrake.NoticeError;
import io.airbrake.javabrake.NoticeStackRecord;
import io.airbrake.javabrake.Notifier;
import net.anthavio.airbrake.AirbrakeLogbackAppender.Notify;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.status.Status;

/**
 * @author martin.vanek
 */
public class AirbrakeLogbackAppenderTest {

    Notifier notifier;

    AirbrakeLogbackAppender appender;

    Logger logger;

    ArgumentCaptor<Notice> captor;

    @Before
    public void before() {
        captor = ArgumentCaptor.forClass(Notice.class);
        notifier = Mockito.mock(Notifier.class);
        Mockito.when(notifier.send(captor.capture())).thenReturn(null);
        Mockito.when(notifier.sendSync(captor.capture())).thenReturn(null);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        appender = new AirbrakeLogbackAppender(notifier);
        appender.setProjectId(12345);
        appender.setProjectKey("test-project-id");
        appender.setContext(context);
        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel("ERROR");
        appender.addFilter(filter);
        appender.start();

        logger = (Logger) LoggerFactory.getLogger(getClass());
        logger.addAppender(appender);
        logger.setAdditive(false);

        appender.setNotify(Notify.EXCEPTIONS);
        appender.setSendMode(AirbrakeLogbackAppender.SendMode.ASYNC);
        appender.setEnabled(true);
        Mockito.reset(notifier);
        //Mockito.reset(captor);
    }

    @Test
    public void testWithException() {
        //When
        NullPointerException cexception = new NullPointerException("I'm poor child with no pointer");
        IllegalArgumentException pexception = new IllegalArgumentException("This is exception message", cexception);
        logger.error("This is log error message", pexception);

        //Then
        Mockito.verify(notifier).send(captor.capture());
        Notice notice = captor.getValue();
        assertThat(notice.errors).hasSize(2);
        assertThat(notice.context.get("error_message")).isEqualTo("This is log error message");

        NoticeError perror = notice.errors.get(0);

        assertThat(perror.type).isEqualTo(pexception.getClass().getName());
        assertThat(perror.message).isEqualTo(pexception.getMessage());
        NoticeStackRecord stackRecord = perror.backtrace.iterator().next();

        assertThat(stackRecord.file).endsWith("AirbrakeLogbackAppenderTest.class");
        assertThat(stackRecord.function).isEqualTo("testWithException");
        assertThat(stackRecord.line).isNotEqualTo(0);

        NoticeError cerror = notice.errors.get(1);
        assertThat(cerror.type).isEqualTo(cexception.getClass().getName());
        assertThat(cerror.message).isEqualTo(cexception.getMessage());
        assertThat(cerror.backtrace).isNotEmpty();


        assertThat(notice.url).isNull();
        assertThat(notice.id).isNull();

        Assertions.assertThat(notice.context.get("component")).isNull();
    }

    @Test
    public void testSimpleMessage() {
        // Given - configure message sending
        appender.setNotify(Notify.EVERYTHING);
        // When
        logger.error("This is error message");
        // Then
        Mockito.verify(notifier).send(captor.capture());
        Notice notice = captor.getValue();
        NoticeError error = notice.errors.get(0);

        assertThat(error.type).isEqualTo("net.anthavio.airbrake.AirbrakeLogbackAppenderTest");
        assertThat(error.message).isEqualTo("This is error message");
        NoticeStackRecord topTraceLine = error.backtrace.iterator().next();
        assertThat(topTraceLine.file).endsWith("AirbrakeLogbackAppenderTest.class");
        assertThat(topTraceLine.function).isEqualTo("testSimpleMessage");
        assertThat(topTraceLine.line).isNotEqualTo(0);
    }

    @Test
    public void testNotifyExeptionsOnly() {
        // Given
        appender.setNotify(Notify.EXCEPTIONS);
        // When
        logger.error("This is log error message");
        // Then
        Mockito.verifyZeroInteractions(notifier);

        // When
        IllegalArgumentException exception = new IllegalArgumentException("This is exception message");
        logger.error("This is log error message", exception);
        // Then
        Mockito.verify(notifier).send(captor.capture());
        Notice notice = captor.getValue();
        assertThat(notice.context.get("error_message")).isEqualTo("This is log error message");

        NoticeError error = notice.errors.get(0);
        assertThat(error.type).isEqualTo(exception.getClass().getName());
        assertThat(error.message).isEqualTo(exception.getMessage());
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConfiguration() {
        // When
        AirbrakeLogbackAppender appender = new AirbrakeLogbackAppender();
        appender.setProjectId(54321);
        appender.setContext(new LoggerContext());
        appender.start();
        List<Status> statusList = appender.getStatusManager().getCopyOfStatusList();
        assertThat(statusList).hasSize(1); //
        assertThat(statusList.get(0).getMessage()).startsWith("PROJECT_KEY not set");

        appender.stop();

        // When with protocol
        appender.setHost("https://www.example.org");

        // When without protocol
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Wrong url: www.example.org");

        appender.setHost("www.example.org");
    }

    @Test
    public void testEnableDisable() {
        // When
        appender.setEnabled(false);

        logger.error("This is error message");
        Mockito.verifyZeroInteractions(notifier);

        logger.error("This is error message", new NullPointerException("Test test test"));
        // Then
        Mockito.verifyZeroInteractions(notifier);

        // When
        appender.setEnabled(true);
        logger.error("This is error message", new NullPointerException("Test test test"));
        // Then
        Mockito.verify(notifier).send(captor.capture());
    }

    @Test
    public void testAsyncSendMode() {
        appender.setSendMode(AirbrakeLogbackAppender.SendMode.SYNC);
        logger.error("something", new NullPointerException("something"));
        Mockito.verify(notifier).sendSync(captor.capture());
    }

}
