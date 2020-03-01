package com.emerigen.infrastructure.learning;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.learning.creditassignment.Bid;
import com.emerigen.infrastructure.learning.creditassignment.PredictionConsumer;
import com.emerigen.infrastructure.learning.creditassignment.PredictionSupplier;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class Transition implements PredictionConsumer, PredictionSupplier {

	private String type = "transition";
	private long timestamp = System.nanoTime();
	private long lastSuccessfulPredictionTimestamp;
	private long numberOfPredictionAttempts;
	private long numberOfSuccessfulPredictions;

	private String firstSensorEventKey = null;
	private SensorEvent predictedSensorEvent = null;

	public static final long defaultDataPointDurationNano = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("cycle.default.data.point.duration.nano"));

	public static final double defaultBidPercentage = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("prediction.consumer.default.bid.percent"));

	/**
	 * This value represents a Transition's strength. It is used during the "credit
	 * assignment" support and for P other reinforcement mechanisms.
	 */
	private double cashOnHand;

	/**
	 * The length of time that the data point [measurement] (as measured by the
	 * predicted sensor event) is valid. If the event is a GPS measurement, then the
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

	private final static Logger logger = Logger.getLogger(Transition.class);

	public static final double defaultCashOnHand = Double.parseDouble(
			EmerigenProperties.getInstance().getValue("prediction.default.cash.on.hand"));

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
		this.predictedSensorEvent = predictedSensorEvent;
		this.dataPointDurationNano = defaultDataPointDurationNano;
		this.lastSuccessfulPredictionTimestamp = System.currentTimeMillis() * 1000000;
		this.numberOfPredictionAttempts = 0;
		this.numberOfSuccessfulPredictions = 0;

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
		result = prime * result + (int) (lastSuccessfulPredictionTimestamp
				^ (lastSuccessfulPredictionTimestamp >>> 32));
		result = prime * result + (int) (numberOfPredictionAttempts
				^ (numberOfPredictionAttempts >>> 32));
		result = prime * result + (int) (numberOfSuccessfulPredictions
				^ (numberOfSuccessfulPredictions >>> 32));
		result = prime * result
				+ ((predictedSensorEvent == null) ? 0 : predictedSensorEvent.hashCode());
		temp = Double.doubleToLongBits(probability);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (lastSuccessfulPredictionTimestamp != other.lastSuccessfulPredictionTimestamp)
			return false;
		if (numberOfPredictionAttempts != other.numberOfPredictionAttempts)
			return false;
		if (numberOfSuccessfulPredictions != other.numberOfSuccessfulPredictions)
			return false;
		if (predictedSensorEvent == null) {
			if (other.predictedSensorEvent != null)
				return false;
		} else if (!predictedSensorEvent.equals(other.predictedSensorEvent))
			return false;
		if (Double.doubleToLongBits(probability) != Double
				.doubleToLongBits(other.probability))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Transition [type=" + type + ", timestamp=" + timestamp
				+ ", lastSuccessfulPredictionTimestamp="
				+ lastSuccessfulPredictionTimestamp + ", numberOfPredictionAttempts="
				+ numberOfPredictionAttempts + ", numberOfSuccessfulPredictions="
				+ numberOfSuccessfulPredictions + ", firstSensorEventKey="
				+ firstSensorEventKey + ", predictedSensorEvent=" + predictedSensorEvent
				+ ", cashOnHand=" + cashOnHand + ", dataPointDurationNano="
				+ dataPointDurationNano + ", probability=" + probability + "]";
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		if (timestamp < 0)
			throw new IllegalArgumentException("timestamp must not be negative");
		this.timestamp = timestamp;
	}

	/**
	 * @return the predictedSensorEvent
	 */
	@Override
	public Prediction getPrediction() {
		return new Prediction(predictedSensorEvent);
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
		if (predictedSensorEvent == null)
			throw new IllegalArgumentException("predictedSensorEvent must not be null");
		this.predictedSensorEvent = predictedSensorEvent;
	}

	/**
	 * @return the sensorType
	 */
	public int getSensorType() {
		return predictedSensorEvent.getSensor().getType();
	}

	/**
	 * @return the sensorLocation
	 */
	public int getSensorLocation() {
		return predictedSensorEvent.getSensor().getLocation();
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
		if (firstSensorEventKey == null || firstSensorEventKey.isEmpty())
			throw new IllegalArgumentException(
					"firstSensorEventKey must not be null or empty");
		this.firstSensorEventKey = firstSensorEventKey;
	}

	/**
	 * @return the cashOnHand
	 */
	@Override
	public double getCashOnHand() {
		return cashOnHand;
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
		if (dataPointDurationNano <= 0.0)
			throw new IllegalArgumentException("dataPointDurationNano must be positive");
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
		if (probability > 1.0 || probability < 0.0)
			throw new IllegalArgumentException("probability must be between 0.0 and 1.0");

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
	@Override
	public void setCashOnHand(double cashOnHand) {
		if (cashOnHand < 0.0)
			throw new IllegalArgumentException("cashOnHand must be positive");

		this.cashOnHand = cashOnHand;
	}

	@Override
	public void acceptPaymentFromWinningBidder(double winningBid) {
		if (winningBid < 0.0)
			throw new IllegalArgumentException("winningBid must be positive");
		cashOnHand = cashOnHand + winningBid;
	}

	@Override
	public Bid matchingPrediction(Prediction prediction) {
		if (prediction == null)
			throw new IllegalArgumentException("prediction must not be null");
		Bid bid = new Bid(this, cashOnHand * defaultBidPercentage);
		return bid;
	}

	@Override
	public Prediction makePayment(double winningBid) {
		if (winningBid < 0.0)
			throw new IllegalArgumentException("winningBid must be positive");
		cashOnHand = cashOnHand - winningBid;
		return new Prediction(predictedSensorEvent);
	}

	@Override
	public Sensor getSensor() {
		return predictedSensorEvent.getSensor();
	}

	/**
	 * @return the defaultbidpercentage
	 */
	public static double getDefaultbidpercentage() {
		return defaultBidPercentage;
	}

	/**
	 * @return the defaultcashonhand
	 */
	public static double getDefaultcashonhand() {
		return defaultCashOnHand;
	}

	/**
	 * @return the lastSuccessfulPredictionTimestamp
	 */
	public long getLastSuccessfulPredictionTimestamp() {
		return lastSuccessfulPredictionTimestamp;
	}

	/**
	 * @param lastSuccessfulPredictionTimestamp the
	 *                                          lastSuccessfulPredictionTimestamp to
	 *                                          set
	 */
	public void setLastSuccessfulPredictionTimestamp(
			long lastSuccessfulPredictionTimestamp) {
		if (lastSuccessfulPredictionTimestamp < 0)
			throw new IllegalArgumentException(
					"lastSuccessfulPredictionTimestamp must not be negative");

		this.lastSuccessfulPredictionTimestamp = lastSuccessfulPredictionTimestamp;
	}

	/**
	 * @return the numberOfPredictionAttempts
	 */
	public long getNumberOfPredictionAttempts() {
		return numberOfPredictionAttempts;
	}

	/**
	 * @param numberOfPredictionAttempts the numberOfPredictionAttempts to set
	 */
	public void setNumberOfPredictionAttempts(long numberOfPredictionAttempts) {
		if (numberOfPredictionAttempts < 0)
			throw new IllegalArgumentException(
					"numberOfPredictionAttempts must not be negative");
		this.numberOfPredictionAttempts = numberOfPredictionAttempts;
	}

	/**
	 * @return the numberOfSuccessfulPredictions
	 */
	public long getNumberOfSuccessfulPredictions() {
		return numberOfSuccessfulPredictions;
	}

	public double getPredictionAccuracy() {
		return (double) numberOfSuccessfulPredictions
				/ (double) numberOfPredictionAttempts;
	}

	/**
	 * @param numberOfSuccessfulPredictions the numberOfSuccessfulPredictions to set
	 */
	public void setNumberOfSuccessfulPredictions(long numberOfSuccessfulPredictions) {
		if (numberOfSuccessfulPredictions < 0)
			throw new IllegalArgumentException(
					"numberOfSuccessfulPredictions must not be negative");
		this.numberOfSuccessfulPredictions = numberOfSuccessfulPredictions;
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
}
