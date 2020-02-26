package com.emerigen.infrastructure.learning.creditassignment;

import com.emerigen.infrastructure.learning.Prediction;

public interface PredictionSupplier {

	/**
	 * Accept the winning bidder's payment and add it to my cashOnHand.
	 * 
	 * @param winningBid The amount of the winning bid
	 */
	public void acceptPaymentFromWinningBidder(double winningBid);

	public Prediction getPrediction();

}
