package com.emerigen.infrastructure.learning;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class TransitionPrediction extends Prediction {

	private SensorEvent sensorEvent;

	private static final Logger logger = Logger.getLogger(TransitionPrediction.class);

	/**
	 * This prediction is based on a sensor event information only
	 * 
	 * @param sensorEvent the event for which predictions will be based.
	 */
	public TransitionPrediction(SensorEvent sensorEvent) {
		super(sensorEvent);
	}

	/**
	 * Calulate the probabilty for this prediction based on the number of
	 * predictions returned from either a onSensorChanged() or
	 * getPredictionsForEvent(SensorEvent) request. Sinse we are a transition-based
	 * prediction, the probability is determined by using the average probability
	 * calculated by dividing 1.0 by the the total number of predictions.
	 * 
	 * @return the probability
	 */
	@Override
	public double getProbability(int numberOfPredictions) {
		return 1.0 / numberOfPredictions;
	}

}
