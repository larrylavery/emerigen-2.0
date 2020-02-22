package com.emerigen.knowledge;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class Transition {

	private long timestamp = System.nanoTime();

	private String firstSensorEventKey = null;
	private SensorEvent predictedSensorEvent = null;
	private int sensorType;
	private int sensorLocation;

	public static final long defaultDataPointDurationNano = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("cycle.default.data.point.duration.nano"));

	/**
	 * This value represents a Transition's strength. It is used during the "credit
	 * assignment" support and for other other reinforcement mechanisms.
	 */
	private double cashOnHand;

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
	 * The likelyhood that this "rule" will be successfull.
	 */
	private double probability;

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
		this.firstSensorEventKey = firstSensorEvent.getKey();
		this.predictedSensorEvent = predictedSensorEvent;
		this.dataPointDurationNano = defaultDataPointDurationNano;
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
		long temp;
		temp = Double.doubleToLongBits(cashOnHand);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ (int) (dataPointDurationNano ^ (dataPointDurationNano >>> 32));
		result = prime * result
				+ ((firstSensorEventKey == null) ? 0 : firstSensorEventKey.hashCode());
		result = prime * result
				+ ((predictedSensorEvent == null) ? 0 : predictedSensorEvent.hashCode());
		temp = Double.doubleToLongBits(probability);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + sensorLocation;
		result = prime * result + sensorType;
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
		if (Double.doubleToLongBits(cashOnHand) != Double
				.doubleToLongBits(other.cashOnHand))
			return false;
		if (dataPointDurationNano != other.dataPointDurationNano)
			return false;
		if (firstSensorEventKey == null) {
			if (other.firstSensorEventKey != null)
				return false;
		} else if (!firstSensorEventKey.equals(other.firstSensorEventKey))
			return false;
		if (predictedSensorEvent == null) {
			if (other.predictedSensorEvent != null)
				return false;
		} else if (!predictedSensorEvent.equals(other.predictedSensorEvent))
			return false;
		if (Double.doubleToLongBits(probability) != Double
				.doubleToLongBits(other.probability))
			return false;
		if (sensorLocation != other.sensorLocation)
			return false;
		if (sensorType != other.sensorType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Transition [timestamp=" + timestamp + ", firstSensorEventKey="
				+ firstSensorEventKey + ", predictedSensorEvent=" + predictedSensorEvent
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

	/**
	 * @return the firstSensorEventKey
	 */
	public String getFirstSensorEventKey() {
		return firstSensorEventKey;
	}

	/**
	 * @param firstSensorEventKey the firstSensorEventKey to set
	 */
	public void setFirstSensorEventKey(String firstSensorEventKey) {
		this.firstSensorEventKey = firstSensorEventKey;
	}

	/**
	 * @return the cashOnHand
	 */
	public double getCashOnHand() {
		return cashOnHand;
	}

	/**
	 * @param cashOnHand the cashOnHand to set
	 */
	public void setCashOnHand(int cashOnHand) {
		this.cashOnHand = cashOnHand;
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

	/**
	 * @return the probability
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * @param probability the probability to set
	 */
	public void setProbability(double probability) {
		this.probability = probability;
	}

	/**
	 * @return the defaultdatapointdurationnano
	 */
	public static long getDefaultdatapointdurationnano() {
		return defaultDataPointDurationNano;
	}

	/**
	 * @param cashOnHand the cashOnHand to set
	 */
	public void setCashOnHand(double cashOnHand) {
		this.cashOnHand = cashOnHand;
	}
}
