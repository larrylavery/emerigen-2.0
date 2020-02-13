package com.emerigen.knowledge;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class Transition {

	private long timestamp = System.nanoTime();

	SensorEvent firstSensorEvent = null, predictedSensorEvent = null;
	int sensorType;
	int sensorLocation;

	private static Logger logger = Logger.getLogger(Transition.class);

	public Transition() {
	}

	public Transition(final SensorEvent firstSensorEvent,
			final SensorEvent predictedSensorEvent) {
		if (firstSensorEvent == null || predictedSensorEvent == null)
			throw new IllegalArgumentException(
					"firstSensorEvent or predictedSensorEvent must not be null");
		if (firstSensorEvent.getSensorType() != predictedSensorEvent.getSensorType()) {
			throw new IllegalArgumentException(
					"Transition patterns must belong to the same sensor."
							+ " firstPattern sensorType: "
							+ firstSensorEvent.getSensorType()
							+ ", predictedPattern sensorType: "
							+ predictedSensorEvent.getSensorType());
		}
		this.sensorType = firstSensorEvent.getSensorType();
		this.sensorLocation = firstSensorEvent.getSensorLocation();
		this.firstSensorEvent = firstSensorEvent;
		this.predictedSensorEvent = predictedSensorEvent;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((firstSensorEvent == null) ? 0 : firstSensorEvent.hashCode());
		result = prime * result
				+ ((predictedSensorEvent == null) ? 0 : predictedSensorEvent.hashCode());
		result = prime * result + sensorLocation;
		result = prime * result + sensorType;
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
		Transition other = (Transition) obj;
		if (firstSensorEvent == null) {
			if (other.firstSensorEvent != null)
				return false;
		} else if (!firstSensorEvent.equals(other.firstSensorEvent))
			return false;
		if (predictedSensorEvent == null) {
			if (other.predictedSensorEvent != null)
				return false;
		} else if (!predictedSensorEvent.equals(other.predictedSensorEvent))
			return false;
		if (sensorLocation != other.sensorLocation)
			return false;
		if (sensorType != other.sensorType)
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Transition [timestamp=" + timestamp + ", firstSensorEvent="
				+ firstSensorEvent + ", predictedSensorEvent=" + predictedSensorEvent
				+ ", sensorType=" + sensorType + ", sensorLocation=" + sensorLocation
				+ "]";
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the firstSensorEvent
	 */
	public SensorEvent getFirstSensorEvent() {
		return firstSensorEvent;
	}

	/**
	 * @param firstSensorEvent the firstSensorEvent to set
	 */
	public void setFirstSensorEvent(SensorEvent firstSensorEvent) {
		this.firstSensorEvent = firstSensorEvent;
	}

	/**
	 * @return the predictedSensorEvent
	 */
	public SensorEvent getPredictedSensorEvent() {
		return predictedSensorEvent;
	}

	/**
	 * @param predictedSensorEvent the predictedSensorEvent to set
	 */
	public void setPredictedSensorEvent(SensorEvent predictedSensorEvent) {
		this.predictedSensorEvent = predictedSensorEvent;
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

	public String getKey() {
		return firstSensorEvent.getKey() + predictedSensorEvent.getKey();
	}
}
