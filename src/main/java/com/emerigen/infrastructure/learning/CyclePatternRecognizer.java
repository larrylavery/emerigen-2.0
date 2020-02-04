/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.util.ArrayList;
import java.util.List;

import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * See cuperclass documentation
 * 
 * @author Larry
 *
 */
public abstract class CyclePatternRecognizer {

	public CyclePatternRecognizer() {
	}

	public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {
		List<Prediction> predictions = new ArrayList<Prediction>();
		// TODO return cycle-based predictions based on the current event
		return predictions;
	}

	public List<Prediction> getPredictionsForSensorEvent(SensorEvent sensorEvent) {
		List<Prediction> predictions = new ArrayList<Prediction>();
		// TODO return cycle-based predictions based on the given event

		return predictions;
	}

}
