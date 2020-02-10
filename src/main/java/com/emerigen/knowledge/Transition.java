package com.emerigen.knowledge;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class Transition {

	private long timestamp = System.nanoTime();

	String firstSensorEventKey = null, predictedSensorEventKey = null;

	private static Logger logger = Logger.getLogger(Transition.class);

	public Transition() {
	}

	public Transition(final SensorEvent firstSensorEvent, final SensorEvent predictedSensorEvent) {

		firstSensorEventKey = "" + firstSensorEvent.getSensorType() + firstSensorEvent.getTimestamp();

		predictedSensorEventKey = "" + predictedSensorEvent.getSensorType()
				+ predictedSensorEvent.getTimestamp();

		if (firstSensorEvent.getSensorType() != predictedSensorEvent.getSensorType()) {
			throw new IllegalArgumentException("Transition patterns must belong to the same sensor."
					+ " firstPattern sensorType: " + firstSensorEvent.getSensorType()
					+ ", predictedPattern sensorType: " + predictedSensorEvent.getSensorType());
		}
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
		result = prime * result + ((firstSensorEventKey == null) ? 0 : firstSensorEventKey.hashCode());
		result = prime * result
				+ ((predictedSensorEventKey == null) ? 0 : predictedSensorEventKey.hashCode());
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
		if (firstSensorEventKey == null) {
			if (other.firstSensorEventKey != null)
				return false;
		} else if (!firstSensorEventKey.equals(other.firstSensorEventKey))
			return false;
		if (predictedSensorEventKey == null) {
			if (other.predictedSensorEventKey != null)
				return false;
		} else if (!predictedSensorEventKey.equals(other.predictedSensorEventKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Transition [timestamp=" + timestamp + ", firstSensorEventKey=" + firstSensorEventKey
				+ ", predictedSensorEventKey=" + predictedSensorEventKey + "]";
	}

	/**
	 * @return the firstPatternKey
	 */
	public String getFirstPatternKey() {
		return getFirstSensorEventKey();
	}

	/**
	 * @return the firstSensorEventKey
	 */
	public String getFirstSensorEventKey() {
		return firstSensorEventKey;
	}

	/**
	 * @return the predictedSensorEventKey
	 */
	public String getPredictedSensorEventKey() {
		return predictedSensorEventKey;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @param firstSensorEventKey the firstSensorEventKey to set
	 */
	void setFirstPatternKey(String firstPatternKey) {
		this.firstSensorEventKey = firstPatternKey;
	}

	/**
	 * @param predictedSensorEventKey the predictedSensorEventKey to set
	 */
	void setPredictedPatternKey(String predictedPatternKey) {
		this.predictedSensorEventKey = predictedPatternKey;
	}
}
