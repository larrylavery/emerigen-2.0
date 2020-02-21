package com.emerigen.infrastructure.learning;

import java.util.ArrayList;
import java.util.List;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class NotPredictingState implements PatternRecognizerState {

	private PatternRecognizer patternRecognizer;
	private PredictionService predictionService;
	private SensorEvent previousSensorEvent;

	public NotPredictingState(PatternRecognizer patternTRecognizer) {
		this.patternRecognizer = patternTRecognizer;
		this.predictionService = patternRecognizer.getPredictionService();
	}

	@Override
	public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {
		List<Prediction> currentPredictions = new ArrayList<Prediction>();

		// Predictions for the specified sensor event?
		currentPredictions = predictionService.getPredictionsForSensorEvent(sensorEvent);
		if (!currentPredictions.isEmpty()) {

			// Yes, change to predeicting state
			patternRecognizer.setSate(new PredictingState(patternRecognizer));
		} else {

			// No predictions found, create a new one
			if (previousSensorEvent != null)
				predictionService.createPredictionFromSensorEvents(previousSensorEvent,
						sensorEvent);
		} // end-else no new predictions

		// Save previous event and current predictions
		previousSensorEvent = sensorEvent;
		predictionService.setCurrentPredictions(currentPredictions);
		return currentPredictions;
	}

}
