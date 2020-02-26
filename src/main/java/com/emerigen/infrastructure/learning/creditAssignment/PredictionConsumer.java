package com.emerigen.infrastructure.learning.creditassignment;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.sensor.Sensor;

public interface PredictionConsumer {

	/**
	 * A current, supplied, prediction matches my firstSensorEvent, so return my bid
	 * for that prediction. My bid is usually a percentage of my cashOnHand.
	 * 
	 * @param prediction The prediction that matches my firstSensorEvent
	 * @return My bid for the matching prediction.
	 */
	public Bid matchingPrediction(Prediction prediction);

	/**
	 * Deduct the winning bid from my cashOnHand and return my prediction for the
	 * next iteration.
	 * 
	 * @param winningBid My previous bid that won and allows me to publish my
	 *                   prediction
	 * @return My prediction for the next cycle, which is added to the pool of
	 *         predictions for the next iteration.
	 */
	public Prediction makePayment(double winningBid);

	/**
	 * PredictionConsumers know which sensor they are consuming predictions from
	 * 
	 * @return
	 */
	public Sensor getSensor();

	public double getCashOnHand();

	public void setCashOnHand(double cashOnHand);

}
