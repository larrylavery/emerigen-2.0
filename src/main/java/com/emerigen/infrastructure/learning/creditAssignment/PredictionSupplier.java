package com.emerigen.infrastructure.learning.creditAssignment;

public interface PredictionSupplier {

	/**
	 * Accept the winning bidder's payment and add it to my cashOnHand.
	 * 
	 * @param winningBid The amount of the winning bid
	 */
	void acceptPaymentFromWinningBidder(double winningBid);

}
