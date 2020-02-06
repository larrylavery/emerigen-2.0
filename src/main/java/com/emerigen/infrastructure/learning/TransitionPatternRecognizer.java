/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * 
 * See PatternRecognizer super class for documentation.
 * 
 * @author Larry
 *
 */
public class TransitionPatternRecognizer extends PatternRecognizer {

	private SensorEvent sensorEvent;

	private SensorEvent previousSensorEvent = null;
	private static final Logger logger = Logger.getLogger(TransitionPatternRecognizer.class);

	public TransitionPatternRecognizer() {
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

		List<Prediction> predictions = new ArrayList<Prediction>();

		if (isNewEvent(currentSensorEvent)) {
			logger.info("sensorEvent IS new");

			// Create new Transition from previous event, unless no previous event
			if (null != previousSensorEvent) {
				logger.info("Creating new transition from sensorEvent: " + previousSensorEvent
						+ ", to sensorEvent: " + currentSensorEvent);
				KnowledgeRepository.getInstance().newTransition(previousSensorEvent,
						currentSensorEvent);
			}

			// Not predicting now
			predictions = new ArrayList<Prediction>();

		} else if (eventHasPredictions(currentSensorEvent)
				|| predictions.contains(currentSensorEvent)) {
			logger.info("sensorEvent has predictions");

			// Save the predicted sensor events
			List<SensorEvent> predictionEvents = KnowledgeRepository.getInstance()
					.getPredictionsForSensorEvent(currentSensorEvent);

			// Convert them to Predictions and calculate their probability
			predictions = predictionEvents.stream()
					.map(sensorEvent -> new TransitionPrediction(sensorEvent))
					.collect(Collectors.toList());

			// Calculate the probability for each
			double probability = 1.0 / predictions.size();
			predictions.forEach(prediction -> prediction.setProbability(probability));
			logger.info("Predictions from current sensorEvent: " + predictions);
		}

		// Update previous event and save any new predictions
		previousSensorEvent = currentSensorEvent;
		setCurrentPredictions(predictions);
		return predictions;

	}

	@Override
	public List<Prediction> getPredictionsForSensorEvent(SensorEvent sensorEvent) {
		List<SensorEvent> predictionEvents = KnowledgeRepository.getInstance()
				.getPredictionsForSensorEvent(sensorEvent);
		List<Prediction> predictions;

		// Convert them to Predictions and calculate their probability
		int predictionsSize = predictionEvents.size();
		predictions = predictionEvents.stream().map(event -> new TransitionPrediction(event))
				.collect(Collectors.toList());

		// Calculate the probability for each
		double probability = 1.0 / predictions.size();
		predictions.forEach(prediction -> prediction.setProbability(probability));
		return predictions;
	}

	/**
	 * 
	 * @param sensorEvent
	 * @return true if predictions exist for the given event
	 */
	protected boolean eventHasPredictions(SensorEvent sensorEvent) {

		// Retrieve the count of predictions for this sensor event
		int predictionCount = KnowledgeRepository.getInstance()
				.getPredictionCountForSensorTypeAndLocation(sensorEvent.getSensorType(),
						sensorEvent.getSensorLocation());
		return predictionCount > 0;
	}

	/**
	 * TODO the sensor event has just been logged, so this should return false???
	 * 
	 * @param sensorEvent
	 * @return true if the sensorEvent is not in the repository
	 */
	private boolean isNewEvent(SensorEvent sensorEvent) {

		SensorEvent event = KnowledgeRepository.getInstance().getSensorEvent(sensorEvent.getKey());
		return null == event;
	}

}
