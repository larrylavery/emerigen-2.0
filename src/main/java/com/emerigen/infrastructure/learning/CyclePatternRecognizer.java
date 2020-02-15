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
	private SensorEvent previousSensorEvent = null;
	private PredictionService predictionService;

	public CyclePatternRecognizer(Cycle cycle, PredictionService predictionService) {
		super(predictionService);
		if (cycle == null)
			throw new IllegalArgumentException("cycle must not be null");
		if (predictionService == null)
			throw new IllegalArgumentException("predictionService must not be null");
		this.cycle = cycle;
		this.predictionService = predictionService;
	}

	@Override
	public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {
		List<Prediction> predictions = new ArrayList<Prediction>();

		// Required elapse time has passed since last event?
		if (sensorEvent.getSensor().minimumDelayBetweenReadingsIsSatisfied(previousSensorEvent,
				sensorEvent)) {

			// Data has significantly changed?
			if (sensorEvent.getSensor().significantChangeHasOccurred(previousSensorEvent, sensorEvent)) {

			}
		}
		// TODO return cycle-based predictions based on the current event
		setCurrentPredictions(predictions);
		return predictions;
	}

	public static List<Prediction> getPredictionsForSensorEvent(SensorEvent sensorEvent) {
		List<Prediction> predictions = new ArrayList<Prediction>();

		// TODO return cycle-based predictions based on the given event
		// TODO setCurrentPredictions(predictions);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cycle == null) ? 0 : cycle.hashCode());
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
		CyclePatternRecognizer other = (CyclePatternRecognizer) obj;
		if (cycle == null) {
			if (other.cycle != null)
				return false;
		} else if (!cycle.equals(other.cycle))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CyclePatternRecognizer [cycle=" + cycle + "]";
	}

}
