package com.emerigen.infrastructure.learning;

import java.util.List;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class PredictingState implements PatternRecognizerState {

	private PatternRecognizer patternRecognizer;
	private PredictionService predictionService;
	private SensorEvent previousSensorEvent;

	public PredictingState(PatternRecognizer patternTRecognizer) {
		this.patternRecognizer = patternTRecognizer;
		this.predictionService = patternRecognizer.getPredictionService();
	}

	@Override
	public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {
		List<Prediction> currentPredictions = predictionService.getPredictionsForSensorEvent(sensorEvent);

		// Current event matches one of my predictions
		if (predictionService.getCurrentPredictions().contains(sensorEvent)) {
			currentPredictions = predictionService.getPredictionsForSensorEvent(sensorEvent);

			// Increment count of successful predictions for my sensor
			predictionService.incrementPredictionsForSensor();

			// Stay in predicting state and return next predictions
		} else {
			// Event does not match my predictions, switch states
			patternRecognizer.setSate(new NotPredictingState(patternRecognizer));
		}
		previousSensorEvent = sensorEvent;
		predictionService.setCurrentPredictions(currentPredictions);
		return currentPredictions;
	}

}
