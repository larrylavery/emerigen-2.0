/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

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
	final private long nanosecondsPerMilliSecond = 1000000;
	final private long milliSecondsPerSecond = 1000;

	public WeeklyCycle(int sensorType, int sensorLocation) {
		super(sensorType, sensorLocation, "Weekly");
	}

	public WeeklyCycle() {
		super("Weekly");
	}

	/**
	 * Calculate the start time of my cycle as Sunday 12am of this week
	 */
	@Override
	public long calculateCycleStartTimeNano() {
		ZoneId zoneId = ZoneId.systemDefault();

		// Get the first day of this week
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentWeek = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentWeek.atStartOfDay(zoneId);
		return firtDayStartTime.toEpochSecond() * milliSecondsPerSecond * nanosecondsPerMilliSecond;
	}

	/**
	 * Caculate duration of seven days expressed as nanoseconds
	 */
	@Override
	public long calculateCycleDurationNano() {
		return daysPerWeek * hoursPerDay * minutesPerHour * secondsPerMinute * milliSecondsPerSecond
				* nanosecondsPerMilliSecond;
	}

	@Override
	public String getCycleType() {
		// TODO Auto-generated method stub
		return null;
	}

}
