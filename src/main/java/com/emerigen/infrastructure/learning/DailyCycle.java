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
	final private long nanoSecondsPerMillisecond = 1000000;
	private static final Logger logger = Logger.getLogger(DailyCycle.class);

	public DailyCycle(int sensorType, int sensorLocation) {
		super(sensorType, sensorLocation, "Daily");
	}

	/**
	 * Calculate the start time of my cycle as 12am today
	 */
	@Override
	public long calculateCycleStartTimeNano() {
		ZoneId zoneId = ZoneId.systemDefault();
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);
		long startTime = todayStart.toEpochSecond() * milliSecondsPerSecond
				* nanoSecondsPerMillisecond;
		logger.info("cycleStartTimeNano = " + startTime);
		return todayStart.toEpochSecond() * milliSecondsPerSecond * nanoSecondsPerMillisecond;
	}

	/**
	 * Caculate duration of 24 hours expressed as milliseconds
	 */
	@Override
	public long calculateCycleDurationNano() {
		return hoursPerDay * minutesPerHour * secondsPerMinute * milliSecondsPerSecond
				* nanoSecondsPerMillisecond;
	}

	@Override
	public String toString() {
		return "DailyCycle []";
	}

}
