/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
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

	private List<Prediction> currentPredictions = new ArrayList<Prediction>();

	private SensorEvent previousSensorEvent;
	private static final Logger logger = Logger
			.getLogger(TransitionPatternRecognizer.class);

	public TransitionPatternRecognizer(Sensor sensor) {
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		this.sensor = sensor;
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
				logger.info(
						"Creating new transition from sensorEvent: " + previousSensorEvent
								+ ", to sensorEvent: " + currentSensorEvent);
				KnowledgeRepository.getInstance().newTransition(previousSensorEvent,
						currentSensorEvent);
			}

			// Not predicting now
			predictions = new ArrayList<Prediction>();

		} else {

			predictions = getPredictionsForSensorEvent(currentSensorEvent);
			if (predictions != null) {

				// Calculate the probability for each
				double probability = 1.0 / predictions.size();
				predictions.forEach(prediction -> prediction.setProbability(probability));
				logger.info("Predictions from current sensorEvent: " + predictions);
			}
		}

		// Update previous event and save any new predictions
		previousSensorEvent = currentSensorEvent;
		setCurrentPredictions(predictions);
		return predictions;

	}

	public static List<Prediction> getPredictionsForSensorEvent(SensorEvent sensorEvent) {
		List<SensorEvent> predictedEvents = KnowledgeRepository.getInstance()
				.getPredictedSensorEventsForSensorEvent(sensorEvent);
		List<Prediction> predictions;

		// Convert them to Predictions and calculate their probability
		if (predictedEvents == null)
			return null;
		int predictionsSize = predictedEvents.size();
		predictions = predictedEvents.stream()
				.map(event -> new TransitionPrediction(event))
				.collect(Collectors.toList());

		// Calculate the probability for each
		double probability = 1.0 / predictions.size();
		predictions.forEach(prediction -> prediction.setProbability(probability));
		return predictions;
	}

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

		SensorEvent event = KnowledgeRepository.getInstance()
				.getSensorEvent(sensorEvent.getKey());
		return null == event;
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
