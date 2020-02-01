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
public class YearlyCycle extends Cycle {

	final private long secondsPerYear = 31556952L;
	final private long milliSecondsPerSecond = 1000;
	private long cycleStartTimeMillis;
	private long cycleDurationMillis;

	public YearlyCycle() {
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	public YearlyCycle(List<SensorEvent> sensorEvents) {
		super(sensorEvents);
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	/**
	 * Calculate the start time of my cycle as Jan 1, 12am of the current year
	 */
	@Override
	public long calculateCycleStartTimeMillis() {
//		Calendar cal = GregorianCalendar.getInstance();
//		cal.set(Calendar.DAY_OF_MONTH, 1);// I might have the wrong Calendar constant...
//		cal.set(Calendar.MONTH, 0);// -1 as month is zero-based
//		cal.set(Calendar.YEAR, 2020);
//		cal.set(Calendar.HOUR, 0);
//		cal.set(Calendar.MINUTE, 0);
//		cal.set(Calendar.SECOND, 0);
//		long time = cal.getTimeInMillis();

		ZoneId zoneId = ZoneId.systemDefault();

		// Get the first day of this year
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentYear = today.with(TemporalAdjusters.firstDayOfYear());

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentYear.atStartOfDay(zoneId);
		return firtDayStartTime.getSecond() * milliSecondsPerSecond;
	}

	/**
	 * Caculate duration of 1 year expressed as milliseconds
	 */
	@Override
	public long calculateCycleDurationMillis() {
		return secondsPerYear * milliSecondsPerSecond;
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
