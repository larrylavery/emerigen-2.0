package com.emerigen.knowledge;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class Transition {

	private long timestamp = System.nanoTime();

	String firstPatternKey = null, predictedPatternKey = null;

	private static Logger logger = Logger.getLogger(Transition.class);

	public Transition() {
	}

	public Transition(final SensorEvent firstSensorEvent, final SensorEvent predictedSensorEvent) {

		firstPatternKey = "" + firstSensorEvent.getSensorType() + firstSensorEvent.getTimestamp();

		predictedPatternKey = "" + predictedSensorEvent.getSensorType()
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
		result = prime * result + ((firstPatternKey == null) ? 0 : firstPatternKey.hashCode());
		result = prime * result
				+ ((predictedPatternKey == null) ? 0 : predictedPatternKey.hashCode());
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
		if (firstPatternKey == null) {
			if (other.firstPatternKey != null)
				return false;
		} else if (!firstPatternKey.equals(other.firstPatternKey))
			return false;
		if (predictedPatternKey == null) {
			if (other.predictedPatternKey != null)
				return false;
		} else if (!predictedPatternKey.equals(other.predictedPatternKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Transition [timestamp=" + timestamp + ", firstPatternKey=" + firstPatternKey
				+ ", predictedPatternKey=" + predictedPatternKey + "]";
	}

	/**
	 * @return the firstPatternKey
	 */
	public String getFirstPatternKey() {
		return firstPatternKey;
	}

	/**
	 * @return the predictedPatternKey
	 */
	public String getPredictedPatternKey() {
		return predictedPatternKey;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @param firstPatternKey the firstPatternKey to set
	 */
	void setFirstPatternKey(String firstPatternKey) {
		this.firstPatternKey = firstPatternKey;
	}

	/**
	 * @param predictedPatternKey the predictedPatternKey to set
	 */
	void setPredictedPatternKey(String predictedPatternKey) {
		this.predictedPatternKey = predictedPatternKey;
	}
}
