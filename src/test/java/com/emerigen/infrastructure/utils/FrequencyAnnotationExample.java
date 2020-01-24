package com.emerigen.infrastructure.utils;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class FrequencyAnnotationExample {

	public static int highExceptionFrequencyHandlerCount;
	public static int lowExceptionFrequencyHandlerCount;
	public static int lowPredictionAccuracyHandlerCount;

	private static Logger logger = Logger.getLogger(FrequencyAnnotationExample.class);

	public FrequencyAnnotationExample() {
	}

	// Couchbase error (exceptions, error code, thrashing) frequency exceeded
//	@HighEventFrequencyHandler(timeUnit=TimeUnit.MILLISECONDS, highThreshold=3, 
//			minimumTimeOutsideThreshold=2, frequencyEventName="CouchbaseRepositoryException")

	// Exception frequency exceeded x per timeUnit. Raise logging level to DEBUG
	@HighEventFrequencyHandler(threshold = 3, minimumTimeAboveThreshold = 2, frequencyEventName = "Exception")
	public void highExceptionFrequencyHandler() {
		++highExceptionFrequencyHandlerCount;
		logger.info("in high freq handler, count: " + highExceptionFrequencyHandlerCount);
//
//		logger.warn("This warning starts the test");
//		logger.debug("This debug entry should NOT occur");
//		logger.warn("This warning should occur");
//		logger.debug("After logging level change: This debug entry should occur");
//		setLoggingLevel(Level.WARN);
//
//		logger.debug(" logging level change back to warning: This debug entry should NOT occur");
//		logger.warn("This warning should occur even after change back to warning");
	}
	@LowEventFrequencyHandler(threshold = 3, minimumTimeBelowThreshold = 2, frequencyEventName = "Exception")
	public void LowExceptionFrequencyHandler() {
		++lowExceptionFrequencyHandlerCount;
		logger.info("in low freq handler, count: " + lowExceptionFrequencyHandlerCount);

//
//		logger.warn("This warning starts the test");
//		logger.debug("This debug entry should NOT occur");
//		logger.warn("This warning should occur");
//		logger.debug("After logging level change: This debug entry should occur");
//		setLoggingLevel(Level.WARN);
//
//		logger.debug(" logging level change back to warning: This debug entry should NOT occur");
//		logger.warn("This warning should occur even after change back to warning");
	}

	public Logger setLoggingLevel(Level level) {

		Logger root = Logger.getRootLogger();
		Enumeration allLoggers = root.getLoggerRepository().getCurrentLoggers();

		while (allLoggers.hasMoreElements()) {
			Category tmpLogger = (Category) allLoggers.nextElement();
			tmpLogger.setLevel(level);
		}
		return root;
	}

	public static void main(String[] args) {
		FrequencyAnnotationExample exObj = new FrequencyAnnotationExample();

		exObj.highExceptionFrequencyHandler();
	}

	// Prediction accuracy lower than min specified
	//@LowEventFrequencyHandler(threshold = 2, minimumTimeBelowThreshold = 2, frequencyEventName = "PredictionAccuracyEvent")
	public void lowPredictionAccuracyHandler() {
		++lowPredictionAccuracyHandlerCount;
	}

	// Exception frequency below acceptable frequency. Raise logging level to
	// WARNING
	//@LowEventFrequencyHandler(threshold = 2, minimumTimeBelowThreshold = 2, frequencyEventName = "Exception")
	public void lowExceptionFrequencyHandler() {
		++lowExceptionFrequencyHandlerCount;
	}

	/**
	 * @return the highExceptionFrequencyHandlerCount
	 */
	public static int getHighExceptionFrequencyHandlerCount() {
		return highExceptionFrequencyHandlerCount;
	}

	/**
	 * @return the lowExceptionFrequencyHandlerCount
	 */
	public static int getLowExceptionFrequencyHandlerCount() {
		return lowExceptionFrequencyHandlerCount;
	}

	/**
	 * @return the lowPredictionAccuracyHandlerCount
	 */
	public static int getLowPredictionAccuracyHandlerCount() {
		return lowPredictionAccuracyHandlerCount;
	}

	public void printMsgs() {
		logger.debug(" DEBUG logging level message");
		logger.info(" INFO logging level message");
		logger.warn(" WARN logging level message");

	}
}
