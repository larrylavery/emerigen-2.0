/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.log4j.Logger;

/**
 * @author Larry
 * @param <T>
 *
 */
public class DailyCycle extends Cycle {

	final private long hoursPerDay = 24;
	final private long minutesPerHour = 60;
	final private long secondsPerMinute = 60;
	final private long milliSecondsPerSecond = 1000;
	private static final Logger logger = Logger.getLogger(DailyCycle.class);

	public DailyCycle(int sensorType) {
		super(sensorType);
	}

	/**
	 * Calculate the start time of my cycle as 12am today
	 */
	@Override
	public long calculateCycleStartTimeMillis() {
		ZoneId zoneId = ZoneId.systemDefault();
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);
		long startTime = todayStart.toEpochSecond() * milliSecondsPerSecond;
		logger.info("cycleStartTimeMillis = " + startTime);
		return todayStart.toEpochSecond() * milliSecondsPerSecond;
	}

	/**
	 * Caculate duration of 24 hours expressed as milliseconds
	 */
	@Override
	public long calculateCycleDurationMillis() {
		return hoursPerDay * minutesPerHour * secondsPerMinute * milliSecondsPerSecond;
	}

	@Override
	public String toString() {
		return "DailyCycle []";
	}

}
