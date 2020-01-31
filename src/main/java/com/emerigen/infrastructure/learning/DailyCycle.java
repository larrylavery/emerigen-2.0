/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.emerigen.infrastructure.sensor.SensorEvent;

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

	public DailyCycle() {
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	public DailyCycle(List<SensorEvent> sensorEvents) {
		super(sensorEvents);
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	/**
	 * Calculate the start time of my cycle as 12am
	 */
	@Override
	public long calculateCycleStartTimeMillis() {
		ZoneId zoneId = ZoneId.of("US/Central");
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
	public long getCycleStartTimeMillis() {
		return cycleStartTimeMillis;
	}

	/**
	 * @return the cycleDurationMillis
	 */
	public long getCycleDurationMillis() {
		return cycleDurationMillis;
	}

}
