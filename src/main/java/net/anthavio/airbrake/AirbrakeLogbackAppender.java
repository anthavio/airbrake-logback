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

import java.util.LinkedList;

import airbrake.AirbrakeNotice;
import airbrake.AirbrakeNotifier;
import airbrake.Backtrace;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;

/**
 * 
 * @author martin.vanek
 *
 */
public class AirbrakeLogbackAppender extends AppenderBase<ILoggingEvent> {

	public static enum Notify {
		ALL, EXCEPTIONS, OFF;
	}

	private final AirbrakeNotifier airbrakeNotifier;

	private String apiKey;

	private String env;

	private Notify notify = Notify.ALL;

	private Backtrace backtraceBuilder = new Backtrace(new LinkedList<String>());

	public AirbrakeLogbackAppender() {
		airbrakeNotifier = new AirbrakeNotifier();
	}

	protected AirbrakeLogbackAppender(AirbrakeNotifier airbrakeNotifier) {
		this.airbrakeNotifier = airbrakeNotifier;
	}

	public void setApiKey(final String apiKey) {
		this.apiKey = apiKey;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(final String env) {
		this.env = env;
	}

	public Backtrace getBacktraceBuilder() {
		return backtraceBuilder;
	}

	public void setBacktraceBuilder(Backtrace backtraceBuilder) {
		this.backtraceBuilder = backtraceBuilder;
	}

	public void setUrl(final String url) {
		if (url == null || !url.startsWith("http")) {
			throw new IllegalArgumentException("Wrong url: " + url);
		}
		airbrakeNotifier.setUrl(url);
	}

	public Notify getNotify() {
		return notify;
	}

	public void setNotify(Notify notify) {
		if (notify == null) {
			throw new IllegalArgumentException("Null Notify");
		}
		this.notify = notify;
	}

	public void setEnabled(boolean enabled) {
		if (enabled) {
			notify = Notify.ALL;
		} else {
			notify = Notify.OFF;
		}
	}

	@Override
	protected void append(final ILoggingEvent event) {
		if (notify == Notify.OFF) {
			return;
		}

		IThrowableProxy proxy;
		if ((proxy = event.getThrowableProxy()) != null) {
			Throwable throwable = ((ThrowableProxy) proxy).getThrowable();
			AirbrakeNotice notice = new AirbrakeNoticeBuilderUsingFilteredSystemProperties(apiKey, backtraceBuilder,
					throwable, env).newNotice();
			airbrakeNotifier.notify(notice);

		} else if (notify == Notify.ALL) {
			StackTraceElement[] stackTrace = event.getCallerData();
			AirbrakeNotice notice = new AirbrakeNoticeBuilderUsingFilteredSystemProperties(apiKey,
					event.getFormattedMessage(), stackTrace[0], env).newNotice();
			airbrakeNotifier.notify(notice);
		}
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public void start() {
		if (apiKey == null || apiKey.isEmpty()) {
			addError("API key not set for the appender named [" + name + "].");
		}
		if (env == null || env.isEmpty()) {
			addError("Environment not set for the appender named [" + name + "].");
		}
		super.start();
	}

}
