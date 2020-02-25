package com.emerigen.infrastructure.learning.creditassignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.sensor.Sensor;

public class CreditAssignmentModerator {

	private PredictionService predictionService;
	private HashMap<Sensor, List<Prediction>> predictionPool = new HashMap<Sensor, List<Prediction>>();

	public CreditAssignmentModerator(PredictionService predictionService) {
		if (predictionService == null)
			throw new IllegalArgumentException("prediction service must not be null");
		this.predictionService = predictionService;
	}

	public void registerSensorForPredictionConsumers(Sensor sensor) {
		List<Prediction> currentPredictionsForSensor = predictionPool.get(sensor);
		predictionPool.put(sensor, currentPredictionsForSensor);
	}

	public void registerConsumerPredictionForSensor(Prediction consumerPrediction,
			Sensor sensor) {
		List<Prediction> currentPredictionsForSensor = predictionPool.get(sensor);
		if (currentPredictionsForSensor == null) {
			currentPredictionsForSensor = new ArrayList<Prediction>();
		}
		predictionPool.put(sensor, currentPredictionsForSensor);
	}

	void resetPredictionPoolForSensor(Sensor sensor) {
		List<Prediction> currentPredictionsForSensor = predictionPool.get(sensor);

		// sensor has predictions? initialize
		if (currentPredictionsForSensor != null
				&& !currentPredictionsForSensor.isEmpty()) {
			currentPredictionsForSensor = new ArrayList<>();
			predictionPool.put(sensor, currentPredictionsForSensor);
		}
	}

	List<Bid> locatePotentialConsumersForPrediction(Prediction currentPrediction) {

		// Retrieve Consumers where firstSensorEvent matches current prediction
		List<PredictionConsumer> potentialConsumers = predictionService
				.getPredictionConsumersForSensorEvent(currentPrediction.getSensorEvent());

		// Get bids from each consumer
		List<Bid> bidders = potentialConsumers.stream()
				.map(consumer -> consumer.matchingPrediction(currentPrediction))
				.collect(Collectors.toList());
		return bidders;
	}

	void selectAndNotifyWinningBidder(List<Bid> bidders) {
//		Optional<Integer> maxNumber = list.stream().max((i, j) -> i.compareTo(j));
//		System.out.println(maxNumber.get());

		// Determine the winning bid
		Optional<Bid> winningBid = bidders.stream()
				.max((bidx, bidy) -> bidx.compareTo(bidy));

		PredictionConsumer winningConsumer = winningBid.get().getPredictionConsumer();
		double winningBidAmount = winningBid.get().getAmount();
		Prediction newPrediction = winningConsumer.makePayment(winningBidAmount);

		// Clean my prediction pool for this sensor
		resetPredictionPoolForSensor(winningConsumer.getSensor());

		// Add winning bidder's prediction to the sensor-based prediction pool
		registerConsumerPredictionForSensor(newPrediction, winningConsumer.getSensor());

	}

}
