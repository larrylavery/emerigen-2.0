package com.emerigen.infrastructure.learning;

import org.apache.log4j.Logger;

/**
 * See the Prediction class for full implementation details.
 * 
 * @author Larry
 *
 */
public class CyclePrediction extends Prediction {

	private double probability;

	private CycleNode cycleNode;

	private static final Logger logger = Logger.getLogger(CyclePrediction.class);

	/**
	 * This prediction is based on a cycle node, which contains the given sensor
	 * event and associated probability parameters.
	 * 
	 * @param cycleNode the cycle node for which the predictions will be based.
	 */
	public CyclePrediction(CycleNode cycleNode) {
		super(cycleNode.getSensorEvent());

		if (cycleNode == null)
			throw new IllegalArgumentException("cycleNode must not be null");

		this.cycleNode = cycleNode;
	}

	/**
	 * See class comments for details of how this probability is calculated for a
	 * cycle-based pattern recognizer.
	 * 
	 * @return the probability
	 */
	@Override
	public double getProbability(int numberOfPredictions) {
		// TODO calculate cycle node prediction
		/**
		 * Calculate the probability from sevaral metrics
		 */
		double probability = 0.0;

		return probability;

	}

	/**
	 * @return the cycleNode
	 */
	public CycleNode getCycleNode() {
		return cycleNode;
	}

}
