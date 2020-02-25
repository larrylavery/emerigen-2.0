package com.emerigen.infrastructure.learning.creditassignment;

public class Bid {

	private PredictionConsumer predictionConsumer;

	private Double amount;

	public Bid(PredictionConsumer bidder, double amount) {
		if (bidder == null)
			throw new IllegalArgumentException("Bidder must not be null");
		if (amount < 0.0)
			throw new IllegalArgumentException("Bid amount must be positive");
		this.amount = amount;
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
		if (predictionConsumer == null)
			throw new IllegalArgumentException("predictionConsumer must not be null");
		this.predictionConsumer = predictionConsumer;
	}

	/**
	 * @param bid the bid to set
	 */
	public void setAmount(double amount) {
		if (amount < 0.0)
			throw new IllegalArgumentException("Bid amount must be positive");
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "Bid [predictionConsumer=" + predictionConsumer + ", amount=" + amount
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
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
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (predictionConsumer == null) {
			if (other.predictionConsumer != null)
				return false;
		} else if (!predictionConsumer.equals(other.predictionConsumer))
			return false;
		return true;
	}

	public int compareTo(Bid other) {
		return amount.compareTo(other.getAmount());
	}

	public double getAmount() {
		return amount;
	}

}
