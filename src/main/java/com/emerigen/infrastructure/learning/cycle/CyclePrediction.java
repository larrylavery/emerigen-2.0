package com.emerigen.infrastructure.learning.cycle;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * See the Prediction class for full implementation details.
 * 
 * @author Larry
 *
 */
public class CyclePrediction extends Prediction {

	private double probability;

	private static final Logger logger = Logger.getLogger(CyclePrediction.class);

	public CyclePrediction(SensorEvent sensorEvent) {

		if (sensorEvent == null)
			throw new IllegalArgumentException("sensorEvent must not be null");
		setSensorEvent(sensorEvent);
	}

	/**
	 * See class comments for details of how this probability is calculated for a
	 * cycle-based pattern recognizer.
	 * 
	 * @return the probability
	 */
	@Override
	public double getProbability() {
		// TODO calculate cycle node prediction
		/**
		 * Calculate the probability from several metrics
		 */
		double probability = 0.0;

		return probability;

	}

	@Override
	public String toString() {
		return "CyclePrediction [probability=" + probability + "]";
	}

}
