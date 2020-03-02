package com.emerigen.infrastructure.tracing;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.infrastructure.utils.LeakyBucket;

/**
 * TODO IDEA - dynamically control logging levels based on frequency of
 * exceptions
 * 
 * This aspect coordinates the capturing of log information related to problems
 * occurring in an application. It attempts to provide a solution to problems
 * that occur while the logging level is too high to capture relevant
 * information. Two annotations (@HighEventFrequencyHandler
 * and @LowEventFrequencyHandler) are provided to implement the raising and
 * lowering of application logging levels. The method associated with the
 * frequency exceeded is responsible for increasing the verbosity of the logging
 * (setting it to DEBUG or INFO). The method associated with the frequency
 * coming back to normal levels is resonsible for descrasing the verbosity of
 * the logging (setting it back to WARN or ERROR).
 * 
 * Currently, only the frequency of Exceptions are monitored in all application
 * classes. The "handlers" are part of this aspect but can be in different
 * classes as deemed appropriate.
 * 
 * 
 * @author Larry
 *
 */
public aspect DynamicLoggingAspect {

	private final static int exceptionOccuranceInterval = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("frequency.exception.interval.seconds"));
	private final static int maxExceptionsPerInterval = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("frequency.max.exceptions.per.interval"));
	private final static int normalExceptionsPerInterval = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("frequency.normal.exceptions.per.interval"));
	private final static int minExceptionsAboveThreshold = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("frequency.minimum.exceptions.above.threshold"));
	private final static int minExceptionsBelowThreshold = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("frequency.minimum.exceptions.below.threshold"));
	private final static String loggingLevelWhenFrequencyExceeded = EmerigenProperties
			.getInstance().getValue("frequency.logging.level.when.threshold.exceeded");
	private final static String loggingLevelWhenFrequencyNormal = EmerigenProperties
			.getInstance().getValue("frequency.logging.level.when.below.threshold");

	private static Logger logger = Logger.getLogger(DynamicLoggingAspect.class);
	private static Level previousLoggingLevel;

	/**
	 * Define the interesting pointcuts and exception advice below
	 */
	pointcut classes(): !ignoredClassesAndMethods() && within(com.emerigen..*);

	pointcut ignoredClassesAndMethods():
		within(Trace) 
		|| execution(* Object.*(..)) 
		|| within(AbstractTrace);

	pointcut constructors() : 
		execution(*.new(..));

	pointcut methods(): !ignoredClassesAndMethods() &&
		execution(* com.emerigen..*.*(..));

	/**
	 * This captures all exceptions that occur in this application and feeds them to
	 * the leaky bucket for frequency monitoring. It executes the "fill()" method of
	 * leaky bucket.
	 * 
	 * @param ex
	 */
	after() throwing (Exception ex):classes() &&  !ignoredClassesAndMethods() 
		&& (constructors() || methods()) {
		//TODO uncomment for leakyBucket testing
//		leakyBucket.fill();
	}

	public DynamicLoggingAspect() {
	}

	/**
	 * This executes when the frequency of exceptions exceeds a configurable
	 * threshold. It increases the verbosity of the logs so that information is
	 * captured closer to the first failure
	 */
	private Runnable highFrequencyExceededRunnable = new Runnable() {
		String loggingLevel = loggingLevelWhenFrequencyExceeded;

		@Override
		public void run() {
			previousLoggingLevel = logger.getLevel();
			logger.info(
					"Frequency of exceptions exceeded. Increasing log verbosity to DEBUG");
			DynamicLoggingAspect.setLoggingLevel(loggingLevel);
		}
	};

	/**
	 * This executes when the frequency of exceptions returns to normal. This
	 * decreases the verbosity and improves the performance of logging by decreasing
	 * the log verbosity when the exception frequency has returned to normal.
	 */
	private Runnable normalFrequencyDetectedRunnable = new Runnable() {

		@Override
		public void run() {
			logger.info(
					"Frequency of exceptions returned to normal. Lowering log verbosity to previous level("
							+ previousLoggingLevel + ")");
			if (previousLoggingLevel != null) {
				DynamicLoggingAspect.setLoggingLevel(previousLoggingLevel.toString());
			}
		}
	};

	/**
	 * LeakyBucket measures frequencies (high and low) and invokes the appropriate
	 * Runnable when high frequency exceeded or the frequency returns to normal.
	 */
//	private LeakyBucket leakyBucket = new LeakyBucket(maxExceptionsPerInterval,
//			Duration.ofSeconds(exceptionOccuranceInterval), minExceptionsBelowThreshold,
//			minExceptionsAboveThreshold, highFrequencyExceededRunnable,
//			normalFrequencyDetectedRunnable);

	//TODO uncomment for testing LeakyBucket
	/**
	 * Dynamically set the logging level for all classes in this application
	 * 
	 * @param level is DEBUG | INFO | WARN | ERROR
	 * @return
	 */
	public static Logger setLoggingLevel(String strLevel) {
		if (strLevel == null || strLevel.isEmpty())
			throw new IllegalArgumentException("strLevel must not be null or empty");
		Level level;

		// Translate input to valid logging level, WARN is default
		if ("DEBUG".equalsIgnoreCase(strLevel))
			level = Level.DEBUG;
		else if ("INFO".equalsIgnoreCase(strLevel))
			level = Level.INFO;
		else if ("WARN".equalsIgnoreCase(strLevel))
			level = Level.WARN;
		else if ("ERROR".equalsIgnoreCase(strLevel))
			level = Level.ERROR;
		else
			level = Level.WARN;

		// Retrieve all currently active loggers
		Logger root = Logger.getRootLogger();
		Enumeration allLoggers = root.getLoggerRepository().getCurrentLoggers();

		// Set all of them to the requested logging level
		while (allLoggers.hasMoreElements()) {
			Category tmpLogger = (Category) allLoggers.nextElement();
			tmpLogger.setLevel(level);
		}
		return root;
	}

}
