airbrake-logback
================
[![Build Status](https://vanek.ci.cloudbees.com/buildStatus/icon?job=airbrake-logback-snapshot)](https://vanek.ci.cloudbees.com/job/airbrake-logback-snapshot/)
[![Coverage Status](https://coveralls.io/repos/anthavio/airbrake-logback/badge.png)](https://coveralls.io/r/anthavio/airbrake-logback)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.anthavio/airbrake-logback/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.anthavio/airbrake-logback)


Logback Appender for Airbrake

Built on the top of the official [airbrake.io library](https://github.com/airbrake/airbrake-java) adding Logback Appender

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="false" scan="true" scanPeriod="30 seconds">

	<appender name="AIRBRAKE" class="net.anthavio.airbrake.AirbrakeLogbackAppender">
		<apiKey>YOUR_AIRBRAKE_API_KEY</apiKey>
		<env>test</env>
		<enabled>true</enabled>

		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>ERROR</level>
		</filter>
	</appender>

	<root>
		<level value="info" />
		<appender-ref ref="AIRBRAKE" />
	</root>
	
</configuration>
```

Additionaly to [airbrake.io library](https://github.com/airbrake/airbrake-java) functionality, airbrake-logback also can send simple one line error messages without stacktraces. Source code line, where error was logged, is still captured and sent to Airbrake.

Configure it setting &lt;notify&gt;ALL&lt;/notify&gt; in logback.xml Possible values are ALL, EXCEPTIONS, OFF
```
	<appender name="AIRBRAKE" class="net.anthavio.airbrake.AirbrakeLogbackAppender">
		<apiKey>YOUR_AIRBRAKE_API_KEY</apiKey>
		<env>test</env>
		<notify>ALL</notify>

		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>ERROR</level>
		</filter>
	</appender>
```
Java code
```
Logger logger = LoggerFactory.getLogger(getClass());
logger.error("I'm going to Airbrake! Exact line will be there too");
```
