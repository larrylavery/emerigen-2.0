/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * @author Larry
 * @param <T>
 *
 */
public class MonthlyCycle extends Cycle {

	final private long secondsPerYear = 31556952L;
	final private long milliSecondsPerSecond = 1000;

	public MonthlyCycle(int sensorType) {
		super(sensorType);
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
		return firtDayStartTime.toEpochSecond() * milliSecondsPerSecond;
	}

	/**
	 * Caculate duration of 1 month expressed as milliseconds
	 */
	@Override
	public long calculateCycleDurationMillis() {

		long secondsPerMonth = secondsPerYear / 12;
		return secondsPerMonth * milliSecondsPerSecond;
	}

}
