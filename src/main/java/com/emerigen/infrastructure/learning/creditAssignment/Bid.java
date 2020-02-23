package com.emerigen.infrastructure.learning.creditassignment;

public class Bid {

	private PredictionConsumer predictionConsumer;

	private Double bid;

	public Bid(PredictionConsumer bidder, double bidAmount) {
		if (bidder == null)
			throw new IllegalArgumentException("Bidder must not be null");
		if (bidAmount < 0.0)
			throw new IllegalArgumentException("Bid amount must be positive");
		this.bid = bid;
		this.predictionConsumer = bidder;
	}

	/**
	 * @return the predictionConsumer
	 */
	public PredictionConsumer getPredictionConsumer() {
		return predictionConsumer;
	}

	/**
	 * @param predictionConsumer the predictionConsumer to set
	 */
	public void setPredictionConsumer(PredictionConsumer predictionConsumer) {
		this.predictionConsumer = predictionConsumer;
	}

	/**
	 * @return the bid
	 */
	public double getBid() {
		return bid;
	}

	/**
	 * @param bid the bid to set
	 */
	public void setBid(double bid) {
		this.bid = bid;
	}

	@Override
	public String toString() {
		return "Bid [predictionConsumer=" + predictionConsumer + ", bid=" + bid + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bid);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((predictionConsumer == null) ? 0 : predictionConsumer.hashCode());
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
		Bid other = (Bid) obj;
		if (Double.doubleToLongBits(bid) != Double.doubleToLongBits(other.bid))
			return false;
		if (predictionConsumer == null) {
			if (other.predictionConsumer != null)
				return false;
		} else if (!predictionConsumer.equals(other.predictionConsumer))
			return false;
		return true;
	}

	public int compareTo(Bid other) {
		return bid.compareTo(other.getBid());
	}

}
