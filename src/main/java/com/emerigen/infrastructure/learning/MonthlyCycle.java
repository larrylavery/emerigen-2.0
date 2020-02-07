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
	final private long nanosecondsPerMillisecond = 1000000;
	final private long milliSecondsPerSecond = 1000;

	public MonthlyCycle(int sensorType, int sensorLocation) {
		super(sensorType, sensorLocation, "Monthly");
	}

	/**
	 * Calculate the start time of my cycle as day 1, 12am of the current month
	 */
	@Override
	public long calculateCycleStartTimeNano() {
		ZoneId zoneId = ZoneId.systemDefault();

		// Get the first day of this month
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentMonth = today.with(TemporalAdjusters.firstDayOfMonth());

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentMonth.atStartOfDay(zoneId);
		return firtDayStartTime.toEpochSecond() * milliSecondsPerSecond * nanosecondsPerMillisecond;
	}

	/**
	 * Caculate duration of 1 month expressed as nanoseconds
	 */
	@Override
	public long calculateCycleDurationNano() {

		long secondsPerMonth = secondsPerYear / 12;
		return secondsPerMonth * milliSecondsPerSecond * nanosecondsPerMillisecond;
	}

}
