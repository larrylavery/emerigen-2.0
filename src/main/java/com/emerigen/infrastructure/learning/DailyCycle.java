/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.time.ZoneId;
import java.time.ZonedDateTime;

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

	private long cycleStartTimeMillis;
	private long cycleDurationMillis;

	public DailyCycle(int sensorType) {
		super(sensorType);
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	/**
	 * Calculate the start time of my cycle as 12am today
	 */
	@Override
	public long calculateCycleStartTimeMillis() {
		ZoneId zoneId = ZoneId.systemDefault();
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);
		return todayStart.getSecond() * milliSecondsPerSecond;
	}

	/**
	 * Caculate duration of 24 hours expressed as milliseconds
	 */
	@Override
	public long calculateCycleDurationMillis() {
		return hoursPerDay * minutesPerHour * secondsPerMinute * milliSecondsPerSecond;
	}

	/**
	 * @return the cycleStartTimeMillis
	 */
	@Override
	public long getCycleStartTimeMillis() {
		return cycleStartTimeMillis;
	}

	/**
	 * @return the cycleDurationMillis
	 */
	@Override
	public long getCycleDurationMillis() {
		return cycleDurationMillis;
	}

}
