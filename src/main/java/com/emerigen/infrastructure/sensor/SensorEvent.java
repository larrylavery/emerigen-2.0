package com.emerigen.infrastructure.sensor;

import java.util.Arrays;

import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonIgnore;
import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.infrastructure.utils.Utils;

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
	private long timestamp = System.currentTimeMillis() * 1000000;

	/**
	 * The length of time that the data point [measurement] (as measured by the
	 * sensor event) is valid. If the event is a GPS measurement, then the
	 * dataPointDurationNano is the length of time the GPS coordinates did not
	 * significantly change (ie how long the user stayed at [visited] this location.
	 * 
	 * For a heart rate sensor, it represents the lengh of time the heart rate
	 * stayed at the given heart rate (plus or minus the standard deviation of
	 * course.
	 */
	private long dataPointDurationNano;

	/**
	 * The length and meaning of these values is sensor specific. One in particular
	 * may be the confidence level of the sensor reading, where 0.0 means no
	 * confidence and 1.0 means absolute confidence.
	 */
	private float[] values = null;

	private long defaultDataPointDurationNano = Long.parseLong(EmerigenProperties
			.getInstance().getValue("cycle.default.data.point.duration.nano"));

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
		this.dataPointDurationNano = defaultDataPointDurationNano;
	}

	@JsonIgnore
	public String getKey() {
		return "" + sensor.getType() + sensor.getLocation() + getValuesHashCode();
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
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
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

	private long getValuesHashCode() {
		final int prime = 31;
		long result = 1;
		result = prime * result + Arrays.hashCode(values);
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
		if (!Utils.equals(getValuesHashCode(), other.getValuesHashCode()))
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

	/**
	 * @param sensor the sensor to set
	 */
	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	/**
	 * @param sensorType the sensorType to set
	 */
	public void setSensorType(int sensorType) {
		this.sensorType = sensorType;
	}

	/**
	 * @param sensorLocation the sensorLocation to set
	 */
	public void setSensorLocation(int sensorLocation) {
		this.sensorLocation = sensorLocation;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(float[] values) {
		this.values = values;
	}

	/**
	 * @return the dataPointDurationNano
	 */
	public long getDataPointDurationNano() {
		return dataPointDurationNano;
	}

	/**
	 * @param dataPointDurationNano the dataPointDurationNano to set
	 */
	public void setDataPointDurationNano(long dataPointDurationNano) {
		this.dataPointDurationNano = dataPointDurationNano;
	}
}
