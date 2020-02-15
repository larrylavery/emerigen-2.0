/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * 
 * See PatternRecognizer super class for documentation.
 * 
 * @author Larry
 *
 */
public class TransitionPatternRecognizer extends PatternRecognizer {

	private Sensor sensor;

	private PredictionService predictionService;

	private List<Prediction> currentPredictions = new ArrayList<Prediction>();

	// Indicates whether we are currently predicting
	boolean predicting = false;

	private SensorEvent previousSensorEvent;

	private int accuratePredictionCount;
	private static final Logger logger = Logger.getLogger(TransitionPatternRecognizer.class);

	public TransitionPatternRecognizer(Sensor sensor, PredictionService predictionService) {
		super(predictionService);
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		if (predictionService == null)
			throw new IllegalArgumentException("predictionService must not be null");
		this.sensor = sensor;
		this.predictionService = predictionService;
	}

	/**
	 * Locate any predictions from the current sensor event, creating a transition
	 * if necessary.
	 * 
	 * @param previousSensorEvent
	 * @param currentSensorEvent
	 */
	@Override
	public List<Prediction> onSensorChanged(SensorEvent currentSensorEvent) {
//		IF delay has occurred THEN
//	      IF we are predicting THEN
//	         IF curr matches my predictions THEN
//	            get the next set of predictions
//	            stay in predicting state
//	         ELSE curr does not match my predictions
//	              change to "not predicting" state
//	              update prediction accuracy
//	         END-ELSE
//	      ELSE we are not currently predicting
//	         get predictions for curr event
//	         IF at least one prediction found THEN
//	            set state to predicting
//	         ELSE no new predictions found
//	             create new transition w prev & curr
//	         END-ELSE no predictions
//	       END-ELSE we are not predicting

//		List<Prediction> predictions = new ArrayList<Prediction>();

		// Required elapse time has passed since last event?
		if (currentSensorEvent.getSensor().minimumDelayBetweenReadingsIsSatisfied(previousSensorEvent,
				currentSensorEvent)) {

			// Data has significantly changed?
			if (currentSensorEvent.getSensor().significantChangeHasOccurred(previousSensorEvent,
					currentSensorEvent)) {

				if (predicting) {

					// Current event matches one of my predictions
					if (predictionService.getCurrentPredictions().contains(currentSensorEvent)) {
						currentPredictions = predictionService
								.getPredictionsForSensorEvent(currentSensorEvent);

						// Stay in prediction state
					} else {
						// Current event doesn't match one of my predictions
						predicting = false;
						predictionService.incrementPredictionsForSensor();
					}
				} else {
					// We are not currently predicting
					currentPredictions = predictionService.getPredictionsForSensorEvent(currentSensorEvent);
					if (!currentPredictions.isEmpty()) {
						predicting = true;
					} else {
						// No new predictions found, create new transition
						if (previousSensorEvent != null)
							predictionService.createPredictionFromSensorEvents(previousSensorEvent,
									currentSensorEvent);
					} // end-else no new predictions

				} // end-else we are not currently predicting

			}
		}
		previousSensorEvent = currentSensorEvent;
		predictionService.setCurrentPredictions(currentPredictions);
		return currentPredictions;
	}
//
//	public static List<Prediction> getPredictionsForSensorEvent(SensorEvent sensorEvent) {
//		List<SensorEvent> predictedEvents = KnowledgeRepository.getInstance()
//				.getPredictedSensorEventsForSensorEvent(sensorEvent);
//		List<Prediction> predictions = new ArrayList<Prediction>();
//
//		// Convert them to Predictions and calculate their probability
//		if (predictedEvents == null)
//			return null;
//		int predictionsSize = predictedEvents.size();
//		List<Prediction> newPredictions = predictedEvents.stream()
//				.map(event -> new TransitionPrediction(event))
//				.collect(Collectors.toList());
//		if (newPredictions != null)
//			predictions.addAll(newPredictions);
//
//		// Calculate the probability for each
//		double probability = 1.0 / predictions.size();
//		predictions.forEach(prediction -> prediction.setProbability(probability));
//		return predictions;
//	}

//	public static List<SensorEvent> getPredictedSensorEventsForSensorEvent(
//			SensorEvent sensorEvent) {
//		List<SensorEvent> predictedSensorEvents = new ArrayList<SensorEvent>();
//
//		String statement = "SELECT predictedSensorEvent FROM `transition` WHERE "
//				+ "meta().id = " + sensorEvent.getKey();
//
//		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
//				N1qlQuery.simple(statement));
//		List<String> predictedSensorEventKeys = new ArrayList<String>();
//		for (N1qlQueryRow row : result) {
//			logger.debug("Adding sensorEvent to predicted sensorEvents: " + row.value());
//			predictedSensorEvents.add(row.value());
//		}
//		return predictedSensorEvents;
//	}

	/**
	 * 
	 * @param sensorEvent
	 * @return true if predictions exist for the given event
	 */
	protected boolean eventHasPredictions(SensorEvent sensorEvent) {

		// Retrieve the count of predictions for this sensor event
		return predictionService.getPredictionCountForSensorTypeAndLocation(sensorEvent.getSensorType(),
				sensorEvent.getSensorLocation()) > 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sensor == null) ? 0 : sensor.hashCode());
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
		TransitionPatternRecognizer other = (TransitionPatternRecognizer) obj;
		if (sensor == null) {
			if (other.sensor != null)
				return false;
		} else if (!sensor.equals(other.sensor))
			return false;
		return true;
	}

}
