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
import java.util.Map;

import org.slf4j.MDC;

import airbrake.AirbrakeNoticeBuilder;
import airbrake.Backtrace;
import airbrake.BacktraceLine;

/**
 * Original airbrake.AirbrakeNoticeBuilderUsingFilteredSystemProperties has hardcoded org.apache.log4j.MDC usage
 * 
 * This class does the same but with org.slf4j.MDC
 * 
 * @author martin.vanek
 *
 */
public class AirbrakeNoticeBuilderUsingFilteredSystemProperties extends AirbrakeNoticeBuilder {

	public AirbrakeNoticeBuilderUsingFilteredSystemProperties(final String apiKey, final String errorMessage,
			StackTraceElement element, final String env) {

		super(apiKey, errorMessage, env);

		if (element != null) {
			LinkedList<String> list = new LinkedList<String>();
			list.add(new BacktraceLine(element.getClassName(), element.getFileName(), element.getLineNumber(), element
					.getMethodName()).toString());
			backtrace(new Backtrace(list));
		}
		environment(System.getProperties());
		addMDCToSession();
		standardEnvironmentFilters();
		ec2EnvironmentFilters();
	}

	public AirbrakeNoticeBuilderUsingFilteredSystemProperties(final String apiKey, final Backtrace backtraceBuilder,
			final Throwable throwable, final String env) {
		super(apiKey, backtraceBuilder, throwable, env);

		environment(System.getProperties());
		addMDCToSession();
		standardEnvironmentFilters();
		ec2EnvironmentFilters();
	}

	private void addMDCToSession() {
		Map<String, String> map = MDC.getCopyOfContextMap();
		if (map != null) {
			addSessionKey(":key", Integer.toString(map.hashCode()));
			addSessionKey(":data", map);
		}
	}

	/**
	 * Airflow builder has protected visibility on building methods for some reason
	 */

	public void request(Map<String, Object> request) {
		super.request(request);
	}

	public void session(Map<String, Object> session) {
		super.session(session);
	}

	public void setRequest(String url, String component) { super.setRequest(url, component); }

}
