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
public class CyclePatternRecognizer extends PatternRecognizer {
	private Cycle cycle;

	public CyclePatternRecognizer(Cycle cycle) {
		this.cycle = cycle;
	}

	@Override
	public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {
		List<Prediction> predictions = new ArrayList<Prediction>();

		// TODO return cycle-based predictions based on the current event
		setCurrentPredictions(predictions);
		return predictions;
	}

	@Override
	public List<Prediction> getPredictionsForSensorEvent(SensorEvent sensorEvent) {
		List<Prediction> predictions = new ArrayList<Prediction>();

		// TODO return cycle-based predictions based on the given event
		setCurrentPredictions(predictions);
		return predictions;
	}

	/**
	 * @return the cycle
	 */
	public Cycle getCycle() {
		return cycle;
	}

	/**
	 * @param cycle the cycle to set
	 */
	public void setCycle(Cycle cycle) {
		this.cycle = cycle;
	}

}
