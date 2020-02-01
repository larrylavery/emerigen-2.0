/**
 * 
 */
package com.emerigen.infrastructure.learning;

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
public class MonthlyCycle extends Cycle {

	final private long secondsPerYear = 31556952L;
	final private long milliSecondsPerSecond = 1000;
	private long cycleStartTimeMillis;
	private long cycleDurationMillis;

	public MonthlyCycle() {
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	public MonthlyCycle(List<SensorEvent> sensorEvents) {
		super(sensorEvents);
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	/**
	 * Calculate the start time of my cycle as day 1, 12am of the current month
	 */
	@Override
	public long calculateCycleStartTimeMillis() {
		ZoneId zoneId = ZoneId.systemDefault();

		// Get the first day of this month
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentMonth = today.with(TemporalAdjusters.firstDayOfMonth());

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentMonth.atStartOfDay(zoneId);
		return firtDayStartTime.getSecond() * milliSecondsPerSecond;
	}

	/**
	 * Caculate duration of 1 month expressed as milliseconds
	 */
	@Override
	public long calculateCycleDurationMillis() {

		long secondsPerMonth = secondsPerYear / 12;
		return secondsPerMonth * milliSecondsPerSecond;
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
