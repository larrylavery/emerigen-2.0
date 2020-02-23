/**
 * 
 */
package com.emerigen.infrastructure.learning.cycle;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * @author Larry
 * @param <T>
 *
 */
public class YearlyCycle extends Cycle {

	final private long secondsPerYear = 31556952L;
	final private long milliSecondsPerSecond = 1000;
	final private long nanosecondsPerMillisecond = 1000000;

	public YearlyCycle(int sensorType, int sensorLocation) {
		super(sensorType, sensorLocation, "Yearly");
	}

	public YearlyCycle() {
		super("Yearly");
	}

	/**
	 * Calculate the start time of my cycle as Jan 1, 12am of the current year
	 */
	@Override
	public long calculateCycleStartTimeNano() {

		ZoneId zoneId = ZoneId.systemDefault();

		// Get the first day of this year
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentYear = today.with(TemporalAdjusters.firstDayOfYear());

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentYear.atStartOfDay(zoneId);
		return firtDayStartTime.toEpochSecond() * milliSecondsPerSecond
				* nanosecondsPerMillisecond;
	}

	/**
	 * Caculate duration of 1 year expressed as nanoseconds
	 */
	@Override
	public long calculateCycleDurationNano() {
		return secondsPerYear * milliSecondsPerSecond * nanosecondsPerMillisecond;
	}

	@Override
	public String getCycleType() {
		// TODO Auto-generated method stub
		return null;
	}

}
