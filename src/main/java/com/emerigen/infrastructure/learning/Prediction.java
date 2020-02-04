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
public abstract class Prediction {

	private static final Logger logger = Logger.getLogger(Prediction.class);
	private SensorEvent sensorEvent;
	protected double probability = 1.0;

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

	/**
	 * Calulate the probabilty for this prediction based on available information.
	 * Transtion-based event probabilities are determined by using the average
	 * probability calculated by dividing 1.0 by the the total number of
	 * predictions.
	 * 
	 * As stated in the class comments, cycle-based predictions are calculated using
	 * multiple "prediction factors".
	 * 
	 * @return the probability
	 */
	public abstract void setProbability(double probability);

	/**
	 * @return the probability
	 */
	public double getProbability() {
		return probability;
	}

}
