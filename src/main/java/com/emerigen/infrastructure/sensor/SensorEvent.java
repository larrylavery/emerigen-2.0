package com.emerigen.infrastructure.sensor;

import java.util.Arrays;

/**
 * This class represents an event from any hardware-based sensor. The major
 * difference in events involve the number of float entries and the meaning of
 * each entry. For example, a HeartRate sensor has the heartRate in the first,
 * and only, float member. An Accelerometer sensore has three values that
 * contaion the current x, y, and z coordinates of the device in question (my
 * phone).
 * 
 * Unlike the SensorEvent replaced, these event don't necessarily have
 * measurements about entities that live an Environment. These events regard
 * sensor measurments about me. For example, my heart rate, my phone
 * acceleration, my current steps, my brainwaves, etc. Even those such as
 * ambient temperature measurments are still about the terperature where "I" am.
 * 
 * These events will be logged in the NOSQL repository (currently Couchbase) as
 * immutable facts turned into "Patterns". This allows PatternRecognizer,
 * Transition, Pattern/SensorEvent, and Prediction.
 * 
 * Since these event streams are continuous and potentially infinite, it makes
 * sense to throttle their processing with MinDelayBetweenEvents to ensure
 * reception of a filtered number of actual events. It may make sense to
 * incorporate repository maximums with automatic rollover. Especially if the
 * most important history can be brought forward.
 * 
 * 
 * @author Larry
 *
 */
public class SensorEvent {

	/**
	 * The sensor that published this event
	 */
	private Sensor sensor = null;

	// The hash of all values

	private int sensorType = 0;
	private int sensorLocation = 0;

	/**
	 * In nanoseconds
	 */
	private String timestamp = String.valueOf(System.nanoTime());

	/**
	 * The length and meaning of these values is sensor specific. One in particular
	 * may be the confidence level of the sensor reading, where 0.0 means no
	 * confidence and 1.0 means absolute confidence.
	 */
	private float[] values = null;

	public SensorEvent() {
	}

	public SensorEvent(Sensor sensor, float[] values) {

		if (sensor == null)
			throw new IllegalArgumentException("Sensor must not be null");
		if (values == null || values.length == 0)
			throw new IllegalArgumentException("Sensor values must not be null or empty");

		this.sensor = sensor;
		this.sensorType = sensor.getType();
		this.sensorLocation = sensor.getLocation();
		this.values = values;
	}

	public String getKey() {
		return "" + sensor.getType() + sensor.getLocation() + getValuesHashCode()
				+ values.toString();
	}

	public float[] getValues() {
		return values;
	}

	public Sensor getSensor() {
		return sensor;
	}

	@Override
	public String toString() {
		return "SensorEvent [sensor=" + sensor + ", sensorType=" + sensorType
				+ ", sensorLocation=" + sensorLocation + ", timestamp=" + timestamp
				+ ", values=" + Arrays.toString(values) + "]";
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sensor == null) ? 0 : sensor.hashCode());
		result = prime * result + sensorLocation;
		result = prime * result + sensorType;
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	private int getValuesHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null ? 0 : Arrays.hashCode(values)));
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
		SensorEvent other = (SensorEvent) obj;
		if (sensor == null) {
			if (other.sensor != null)
				return false;
		} else if (!sensor.equals(other.sensor))
			return false;
		if (sensorLocation != other.sensorLocation)
			return false;
		if (sensorType != other.sensorType)
			return false;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	/**
	 * @return the sensorType
	 */
	public int getSensorType() {
		return sensor.getType();
	}

	/**
	 * @return the sensorLocation
	 */
	public int getSensorLocation() {
		return sensor.getLocation();
	}
}
