/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * @author Larry
 * @param <T>
 *
 */
public class WeeklyCycle extends Cycle {

	final private int daysPerWeek = 7;
	final private int hoursPerDay = 24;
	final private long minutesPerHour = 60;
	final private long secondsPerMinute = 60;
	final private long milliSecondsPerSecond = 1000;
	private long cycleStartTimeMillis;
	private long cycleDurationMillis;

	public WeeklyCycle() {
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	public WeeklyCycle(List<SensorEvent> sensorEvents) {
		super(sensorEvents);
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	/**
	 * Calculate the start time of my cycle as Sunday 12am of this week
	 */
	@Override
	public long calculateCycleStartTimeMillis() {
		ZoneId zoneId = ZoneId.systemDefault();

		// Get the first day of this week
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentWeek = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentWeek.atStartOfDay(zoneId);
		return firtDayStartTime.getSecond() * milliSecondsPerSecond;
	}

	/**
	 * Caculate duration of sevsn days expressed as milliseconds
	 */
	@Override
	public long calculateCycleDurationMillis() {
		return daysPerWeek * hoursPerDay * minutesPerHour * secondsPerMinute
				* milliSecondsPerSecond;
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
