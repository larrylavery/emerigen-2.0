/**
 * 
 */
package com.emerigen.infrastructure.learning.cycle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

import org.apache.log4j.Logger;

import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonIgnore;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.utils.EmerigenProperties;

/**
 * @author Larry
 *
 */
public class Cycle {

	/**
	 * @IDEA - enable cycle data point fuzzines using equality based on std
	 *       deviation. As long as values are within this standard deviation from
	 *       each other they will be considered to be equal. This allows for
	 *       fuzziness of the data points associated with the nodes of a cycle;
	 *       effectively enabling predictions when data points vary somewhat, but
	 *       not in principle (all too often occurs dealing with life). For example,
	 *       with GPS daily routes, multiple things may influence route node
	 *       visitations (and visitation durations) including traffic, working late,
	 *       detours, stopping for gas, stopping by the grocery store on the way
	 *       home, going to lunch at different places, ...
	 */
	private String cycleType;
	private String type = "cycle";
	private int sensorLocation;
	private int sensorType;
	/**
	 * This is the starting timestamp for the beginning of each cycle type. For
	 * example, Hourly cycles start at 0 minutes 0 seconds of the current hour,
	 * daily Cycles start at 12:00 am of the current day, weekly cycles atart at
	 * 12:00am Sunday morning of the current week, etc
	 * 
	 * This is an absolute value of nanoseconds since Jan 1, 1970. It is used to
	 * calculate offsets for data point timestamps in the sensor events. This field
	 * is rolled over to the next Cycle start time at the end of the duration of the
	 * present cycle (i.e. moved to the next 24 hours for DailyCycle, 7 days for a
	 * weekly cycle, etc.).
	 */
	private long cycleStartTimeNano;
	/**
	 * This is the duration for a cycle. Time zones and Daylight Savings times are
	 * taken into account. Generally, this will be 168 hours for a weekly cycle, 24
	 * hours for a daily cycle, etc.; all converted to nanoseconds.
	 */
	private long cycleDurationTimeNano;
	final private long secondsPerYear = 31556952L;
	final private int daysPerWeek = 7;
	final private long hoursPerDay = 24;
	final private long minutesPerHour = 60;
	final private long secondsPerMinute = 60;
	final private long milliSecondsPerSecond = 1000;
	final private long nanoSecondsPerMillisecond = 1000000;

	private double allowablePercentDifferenceForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.percent.difference.for.equality"));

	/**
	 * This variable refers to the last, more "permanent", sensor event. Its
	 * duration is adjusted when the data points destabilize indicating that we are
	 * on the move (for GPS of course)
	 */
	private SensorEvent permanentSensorEvent = null;

	/**
	 * This variable refers to the more "temporary" previous sensor event. It is
	 * used to accumulate durations when sensor event data points are destabilized
	 * (i.e. changing rapidly such as traveling from one GPS destination to
	 * another). When the changes have stabilized, then this becomes the more
	 * "permanent" sensor event.
	 */
	private SensorEvent temporarySensorEvent = null;

	private static final Logger logger = Logger.getLogger(Cycle.class);

	public Cycle(int sensorType, int sensorLocation, String cycleType) {
		if (sensorType <= 0)
			throw new IllegalArgumentException("sensor type must be positive");
		if (sensorLocation <= 0)
			throw new IllegalArgumentException("sensor location must be positive");
		if (cycleType == null || cycleType.isEmpty())
			throw new IllegalArgumentException("cycleType must not be null or empty");

		this.sensorType = sensorType;
		this.setCycleType(cycleType);
		this.sensorLocation = sensorLocation;

		// Set event references to a "dummy" event at the start of cycle
		this.permanentSensorEvent = new SensorEvent();
		this.permanentSensorEvent.setTimestamp(cycleStartTimeNano);
		this.temporarySensorEvent = permanentSensorEvent;
	}

	public Cycle() {
	}

	public Cycle(String cycleType) {
		setCycleType(cycleType);
	}

	/**
	 * Must be overriden by subclasses
	 * 
	 * @return the cycleStartTimeNano
	 */
	public long calculateCycleStartTimeNano() {
		return 0;
	}

	/**
	 * Must be overriden by subclasses
	 * 
	 * @return the cycleDurationTimeNano
	 */
	public long calculateCycleDurationNano() {
		return 0;
	}

	/**
	 * @return the key for this cycle
	 */
	@JsonIgnore
	public String getKey() {
		return "" + sensorType + sensorLocation + getCycleType();
	}

	@Override
	public String toString() {
		return "Cycle [cycleType=" + cycleType + ", type=" + type + ", sensorLocation="
				+ sensorLocation + ", sensorType=" + sensorType + ", cycleStartTimeNano="
				+ cycleStartTimeNano + ", cycleDurationTimeNano=" + cycleDurationTimeNano
				+ ", allowablePercentDifferenceForEquality="
				+ allowablePercentDifferenceForEquality + ", permanentSensorEvent="
				+ permanentSensorEvent + ", temporarySensorEvent=" + temporarySensorEvent
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (cycleDurationTimeNano ^ (cycleDurationTimeNano >>> 32));
		result = prime * result
				+ (int) (cycleStartTimeNano ^ (cycleStartTimeNano >>> 32));
		result = prime * result + ((cycleType == null) ? 0 : cycleType.hashCode());
		result = prime * result + sensorLocation;
		result = prime * result + sensorType;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cycle other = (Cycle) obj;
		if (cycleDurationTimeNano != other.cycleDurationTimeNano)
			return false;
		if (cycleStartTimeNano != other.cycleStartTimeNano)
			return false;
		if (cycleType == null) {
			if (other.cycleType != null)
				return false;
		} else if (!cycleType.equals(other.cycleType))
			return false;
		if (sensorLocation != other.sensorLocation)
			return false;
		if (sensorType != other.sensorType)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * @return the cycleType
	 */
	public String getCycleType() {
		return cycleType;
	}

	public void setAllowablePercentDifferenceForEquality(
			double allowablePercentDifferenceForEquality) {
		this.allowablePercentDifferenceForEquality = allowablePercentDifferenceForEquality;
	}

	public double getAllowablePercentDifferenceForEquality() {
		return allowablePercentDifferenceForEquality;
	}

	/**
	 * @return the sensorLocation
	 */
	public int getSensorLocation() {
		return sensorLocation;
	}

	/**
	 * @param sensorLocation the sensorLocation to set
	 */
	public void setSensorLocation(int sensorLocation) {
		this.sensorLocation = sensorLocation;
	}

	/**
	 * @return the sensorType
	 */
	public int getSensorType() {
		return sensorType;
	}

	/**
	 * @param sensorType the sensorType to set
	 */
	public void setSensorType(int sensorType) {
		this.sensorType = sensorType;
	}

	/**
	 * Once we have the cycle type, figure out the values for cycleStartTimeNano and
	 * cycleDurationNano
	 * 
	 * @param cycleType the cycleType to set
	 */
	public void setCycleType(String cycleType) {
		this.cycleType = cycleType;

		// Daily cycle?
		if ("Daily".equals(cycleType))
			setDailyCycleNumbers();

		// Weekly cycle?
		else if ("Weekly".equals(cycleType))
			setWeeklyCycleNumbers();

		// Monthly cycle?
		else if ("Monthly".equals(cycleType))
			setMonthlyCycleNumbers();

		// Yearly cycle?
		else if ("Yearly".equals(cycleType))
			setYearlyCycleNumbers();
	}

	private void setYearlyCycleNumbers() {
		/**
		 * Start time in nano seconds
		 */
		ZoneId zoneId = ZoneId.systemDefault();

		// Get the first day of this year
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentYear = today.with(TemporalAdjusters.firstDayOfYear());

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentYear.atStartOfDay(zoneId);
		setCycleStartTimeNano(firtDayStartTime.toEpochSecond() * milliSecondsPerSecond
				* nanoSecondsPerMillisecond);

		/**
		 * Duration in nano seconds
		 */
		setCycleDurationTimeNano(
				secondsPerYear * milliSecondsPerSecond * nanoSecondsPerMillisecond);

	}

	private void setMonthlyCycleNumbers() {
		/**
		 * Start time in nano seconds
		 */
		ZoneId zoneId = ZoneId.systemDefault();

		// Get the first day of this month
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentMonth = today
				.with(TemporalAdjusters.firstDayOfMonth());

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentMonth.atStartOfDay(zoneId);
		setCycleStartTimeNano(firtDayStartTime.toEpochSecond() * milliSecondsPerSecond
				* nanoSecondsPerMillisecond);
		/**
		 * Duration in nano seconds
		 */
		long secondsPerMonth = secondsPerYear / 12;
		setCycleDurationTimeNano(
				secondsPerMonth * milliSecondsPerSecond * nanoSecondsPerMillisecond);
	}

	/**
	 * Calculate the weekly start time and duration
	 */
	private void setWeeklyCycleNumbers() {
		/**
		 * Start time in nano seconds
		 */
		ZoneId zoneId = ZoneId.systemDefault();

		// Get the first day of this week
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentWeek = today
				.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentWeek.atStartOfDay(zoneId);
		setCycleStartTimeNano(firtDayStartTime.toEpochSecond() * milliSecondsPerSecond
				* nanoSecondsPerMillisecond);

		/**
		 * Duration in nano seconds
		 */
		setCycleDurationTimeNano(daysPerWeek * hoursPerDay * minutesPerHour
				* secondsPerMinute * milliSecondsPerSecond * nanoSecondsPerMillisecond);
	}

	/**
	 * Calculate the daily start time and duration
	 */
	private void setDailyCycleNumbers() {
		/**
		 * Start time in nano seconds
		 */
		ZoneId zoneId = ZoneId.systemDefault();
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		ZonedDateTime todayStart = now.toLocalDate().atStartOfDay(zoneId);
		long startTime = todayStart.toEpochSecond() * milliSecondsPerSecond
				* nanoSecondsPerMillisecond;
		logger.info("cycleStartTimeNano = " + startTime);
		setCycleStartTimeNano(todayStart.toEpochSecond() * milliSecondsPerSecond
				* nanoSecondsPerMillisecond);
		/**
		 * Duration in nano seconds
		 */
		setCycleDurationTimeNano(hoursPerDay * minutesPerHour * secondsPerMinute
				* milliSecondsPerSecond * nanoSecondsPerMillisecond);
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param cycleStartTimeNano the cycleStartTimeNano to set
	 */
	public void setCycleStartTimeNano(long cycleStartTimeNano) {
		this.cycleStartTimeNano = cycleStartTimeNano;
	}

	/**
	 * @return the cycleDurationTimeNano
	 */
	public long getCycleDurationTimeNano() {
		return cycleDurationTimeNano;
	}

	/**
	 * @param cycleDurationTimeNano the cycleDurationTimeNano to set
	 */
	public void setCycleDurationTimeNano(long cycleDurationTimeNano) {
		this.cycleDurationTimeNano = cycleDurationTimeNano;
	}

	/**
	 * @return the cycleStartTimeNano
	 */
	public long getCycleStartTimeNano() {
		return cycleStartTimeNano;
	}

	/**
	 * @return the permanentSensorEvent
	 */
	SensorEvent getPermanentSensorEvent() {
		return permanentSensorEvent;
	}

	/**
	 * @param permanentSensorEvent the permanentSensorEvent to set
	 */
	void setPermanentSensorEvent(SensorEvent permanentSensorEvent) {
		this.permanentSensorEvent = permanentSensorEvent;
	}

	/**
	 * @return the temporarySensorEvent
	 */
	SensorEvent getTemporarySensorEvent() {
		return temporarySensorEvent;
	}

	/**
	 * @param temporarySensorEvent the temporarySensorEvent to set
	 */
	void setTemporarySensorEvent(SensorEvent temporarySensorEvent) {
		this.temporarySensorEvent = temporarySensorEvent;
	}

}
