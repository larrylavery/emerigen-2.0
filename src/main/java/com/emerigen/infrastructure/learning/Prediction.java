package com.emerigen.infrastructure.learning;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * This class represents a potential next sensor event based on another sensor
 * event. It is returned by all pattern recognizers (i.e. cycle based and
 * transition-based) when a new sensor event is emitted/consumed. Pattern
 * recognizers may also be queried for predictions using any sensor event; not
 * just the current event.
 * 
 * This is the base class that defines all predictions in the system. Currently
 * there are CycleNode-based and transition-based predictions.
 * 
 * This class includes the probability that the next event will be the specific
 * event contained in this prediction. This allows the app to choose those with
 * a higher probability when making the final likely predictions.
 * 
 * For Transition-based pattern recognizers, this class simply contains the
 * predicted next sensor event with a probability based on the whole set of
 * current predictions (i.e. more predictions in the list lower the probability
 * of each prediction).
 * 
 * For cycle-based events, the probability is based on more advanced
 * calculations such as the expected duration at the predicted data point
 * (longer = higher), how many times this prediction was successful, how many
 * previous cycle nodes have already been predicted by a cycle leading up to
 * this prediction (i.e. more consecutive predictions = higher), recency of
 * successful prediction, the global/whole cycle prediction accuracy and the
 * successful prediction frequency (expressed by number of successful
 * predictions in a given time period or number of predictions).
 * 
 * TODO How to give feedback to each pattern recognizer about the accuracy of
 * their most recent prediction
 * 
 * @author Larry
 *
 */
public class Prediction {

	private static final Logger logger = Logger.getLogger(Prediction.class);
	private SensorEvent sensorEvent;
	protected double probability = 1.0;
	private long lastSuccessfulPredictionTimestamp = System.currentTimeMillis() * 1000000;
	private long numberOfPredictionAttempts = 1;
	private long numberOfSuccessfulPredictions = 0;

	public Prediction() {
	}

	public Prediction(SensorEvent sensorEvent, long lastSuccessfulPredictionTimestamp,
			long numberOfPredictionAttempts, long numberOfSuccessfulPredictions) {
		if (sensorEvent == null)
			throw new IllegalArgumentException("sensorEvent must not be null");
		if (lastSuccessfulPredictionTimestamp <= 0)
			throw new IllegalArgumentException(
					"lastSuccessfulPredictionTimestamp must be positive");
		if (numberOfPredictionAttempts <= 0)
			throw new IllegalArgumentException(
					"numberOfPredictionAttempts must be positive");
		if (numberOfSuccessfulPredictions <= 0)
			throw new IllegalArgumentException(
					"numberOfSuccessfulPredictions must be positive");
		this.sensorEvent = sensorEvent;
		this.lastSuccessfulPredictionTimestamp = lastSuccessfulPredictionTimestamp;
		this.numberOfPredictionAttempts = numberOfPredictionAttempts;
		this.numberOfSuccessfulPredictions = numberOfSuccessfulPredictions;
	}

	public Prediction(SensorEvent sensorEvent) {
		if (sensorEvent == null)
			throw new IllegalArgumentException("sensorEvent must not be null");
		this.sensorEvent = sensorEvent;
	}

	/**
	 * @return the sensorEvent
	 */
	public SensorEvent getSensorEvent() {
		return sensorEvent;
	}

	public void setSensorEvent(SensorEvent sensorEvent) {
		if (sensorEvent == null)
			throw new IllegalArgumentException("sensorEvent must not be null");
		this.sensorEvent = sensorEvent;
	}

	public void setProbability(double probability) {
		if (probability < 0.0)
			throw new IllegalArgumentException("probability must be zero or more");
		this.probability = probability;
	}

	/**
	 * Calulate the probabilty for this prediction based on available information.
	 * Multiple factors are used.
	 * 
	 * TODO implement after transition metadata finalized.
	 * 
	 * @return the probability
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Calulate the probabilty for this prediction based on the number of
	 * predictions returned from either a onSensorChanged() or
	 * getPredictionsForEvent(SensorEvent) request. Currently this type of
	 * probability is determined by using the average probability calculated by
	 * dividing 1.0 by the the total number of predictions.
	 * 
	 * @return the probability
	 */
	public void setProbability(int predictionCount) {
		if (predictionCount <= 0)
			throw new IllegalArgumentException(
					"predictionCount must be greater than zero");
		this.probability = 1.0 / predictionCount;
	}

}
