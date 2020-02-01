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
public class HourlyCycle extends Cycle {

	final private long hoursPerDay = 24;
	final private long minutesPerHour = 60;
	final private long secondsPerMinute = 60;
	final private long milliSecondsPerSecond = 1000;

	private long cycleStartTimeMillis;
	private long cycleDurationMillis;

	public HourlyCycle() {
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	public HourlyCycle(List<SensorEvent> sensorEvents) {
		super(sensorEvents);
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	/**
	 * Calculate cycle start time as 0 minutes, 0 seconds of the current hour
	 */
	@Override
	public long calculateCycleStartTimeMillis() {
		ZoneId zoneId = ZoneId.systemDefault();
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);
		return todayStart.getSecond() * milliSecondsPerSecond;
	}

	/**
	 * Caculate duration of 1 hour expressed as milliseconds
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
