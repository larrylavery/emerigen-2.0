package com.emerigen.infrastructure.utils;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class LeakyBucket {

	private static int leakInterval = Integer
			.parseInt(EmerigenProperties.getInstance().getValue("leakybucket.leak.interval"));
	private static int leakDelayInterval = Integer
			.parseInt(EmerigenProperties.getInstance().getValue("leakybucket.leak.delay.interval"));
	private Timer timer;
	private static Logger logger = Logger.getLogger(LeakyBucket.class);

	private int EMPTY = 0;
	private int FULL;
	private long bucketCounter = 0;
	private long numOccurances, numOccurancesAtEmpty, numOccurancesAtFull;
	private Duration interval;
	private Runnable frequencyAboveThresholdHandler;
	private Runnable frequencyBelowThresholdHandler;

	/**
	 * This counter controls how long to wait before notifying the handler that the
	 * frequency has been exceeded (i.e. the bucket has been full) for at least this
	 * number of event occurances.
	 */
	long minOccurancesAtFull = 0;

	/**
	 * This counter controls how long to wait before notifying the handler that the
	 * frequency has been below the low threshold (i.e. the bucket has been empty)
	 * for at least this number of event occurances.
	 */
	long minOccurancesAtEmpty = 0;

	/**
	 * This CTOR sets the occurance frequency to the number of occurances per time
	 * interval. A frequency of 60 per minute would set as numOccurances = 60 and
	 * interval = 60 seconds (or 1 every second).
	 * 
	 * @param numOccurances is the number of occurances per interval
	 * @param interval      is the time required to fill the bucket
	 */
	public LeakyBucket(int numOccurances, Duration interval, long minOccurancesAtEmpty, long minOccurancesAtFull,
			Runnable frequencyAboveThresholdHandler, Runnable frequencyBelowThresholdHandler) {

		if (minOccurancesAtEmpty <= 0)
			throw new IllegalArgumentException("minOccurancesAtEmpty must be positive.");
		if (minOccurancesAtFull <= 0)
			throw new IllegalArgumentException("minOccurancesAtFull must be positive.");
		if (numOccurances <= 0)
			throw new IllegalArgumentException("numOccurances must be positive.");
		if (interval == null || interval.isZero())
			throw new IllegalArgumentException("interval must not be null and must be positive.");
		if (frequencyAboveThresholdHandler == null && frequencyBelowThresholdHandler == null)
			throw new IllegalArgumentException(
					"Either or both of 'exceded' and 'below' threshold handlers must be specified.");

		this.numOccurances = numOccurances;
		FULL = numOccurances;
		this.interval = interval;
		bucketCounter = numOccurances * interval.toSeconds();
		this.minOccurancesAtEmpty = minOccurancesAtEmpty;
		this.minOccurancesAtFull = minOccurancesAtFull;
		this.frequencyAboveThresholdHandler = frequencyAboveThresholdHandler;
		this.frequencyBelowThresholdHandler = frequencyBelowThresholdHandler;

		// Start the timer
		initializeTimer();
	}

	/**
	 * Create, and start, a timer to periodically "leak()" the bucket.
	 */

	private void initializeTimer() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				logger.info("In timer loop. About to invoke leak()");
				leak();
			}
		}, leakDelayInterval, interval.toMillis()/numOccurances);
	}

	private void invokeLowFrequencyHandler() {

		if (frequencyBelowThresholdHandler != null) {
			logger.info("Invoking low Frequency handler...");
			frequencyBelowThresholdHandler.run();
		}
	}

	private void invokeHighFrequencyHandler() {
		if (frequencyAboveThresholdHandler != null) {
			logger.info("Invoking high Frequency handler...");
			frequencyAboveThresholdHandler.run();
		}
	}

	/**
	 * Driven by a time set in the constructor based on the number of occurances
	 * during an interval.
	 * 
	 * @return true if this bucket's level was successfully reduced
	 */
	public boolean leak() {
		logger.info("Leaking...bucketCounter= " + bucketCounter);

		if (isEmpty()) {
			numOccurancesAtEmpty++;
			logger.info("Empty...numOccurancesAtEmpty: " + numOccurancesAtEmpty);
			if (numOccurancesAtEmpty >= minOccurancesAtEmpty) {

				// Invoke the low frequency exceeded handler
				logger.info("Empty, minOccurances greater...invoking low freq handler");
				invokeLowFrequencyHandler();
				numOccurancesAtEmpty = 0;
			}
			return false;
		}
		if (--bucketCounter == EMPTY) {
			numOccurancesAtEmpty = 0;
			return true;
		}
		bucketCounter--;
		return true;
	}

	public boolean fill() {
		logger.info("Filling...bucketCounter= " + bucketCounter);
		if (isFull()) {
			numOccurancesAtFull++;
			logger.info("Full...numOccurancesAtFull: " + numOccurancesAtFull);
			if (numOccurancesAtFull >= minOccurancesAtFull) {

				// Invoke the high frequency exceeded handler
				invokeHighFrequencyHandler();
				numOccurancesAtFull = 0;
			}
			return false;
		}
		if (++bucketCounter == FULL) {
			numOccurancesAtFull = 0;
			return true;
		}
		bucketCounter++;
		return true;
	}

	public boolean isFull() {
		return bucketCounter >= numOccurances;
	}

	public boolean isEmpty() {
		return bucketCounter <= 0;
	}

}
