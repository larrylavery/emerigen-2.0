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

	/**
	 * Called whenever a new sensor event is generated that matches its sensor type.
	 * It will learn new events by attempting to aggregate consecutive data point
	 * duration times. The end result of processing many new sensor events is a
	 * cycle list populated with cycle nodes that have the longest durations.
	 * 
	 * For example, in the case of a GPS sensor, the nodes represent the GPS
	 * locations that have the longest visition durations; effectively the route
	 * prediction nodes for the specified cycle duration (ie daily, weekly, monthly,
	 * ...)
	 * 
	 * The logic is not specific to one type of data point or cycle and should work
	 * just as well for most sensors (one exception is Accelerometer) and all cycle
	 * durations.
	 * 
	 * @param sensorEvent
	 * @return
	 */
	@Override
	public List<Prediction> onSensorChanged(SensorEvent currentSensorEvent) {

		// Validate parms
		if (currentSensorEvent == null)
			throw new IllegalArgumentException("currentSensorEvent must not be null");
		if (!(cycle.getSensorType() == currentSensorEvent.getSensorType()))
			throw new IllegalArgumentException("given sensor type (" + currentSensorEvent.getSensorType()
					+ "), does not match cycle sensor type (" + cycle.getSensorType() + ")");
		if (!(cycle.getSensorLocation() == currentSensorEvent.getSensorLocation()))
			throw new IllegalArgumentException(
					"given sensor location (" + currentSensorEvent.getSensorLocation()
							+ "), does not match cycle sensor location (" + cycle.getSensorLocation() + ")");
		List<Prediction> predictions = new ArrayList<Prediction>();

		// Required elapse time has passed since last event?
		if (currentSensorEvent.getSensor().minimumDelayBetweenReadingsIsSatisfied(previousSensorEvent,
				currentSensorEvent)) {

			// Data has significantly changed?
			if (currentSensorEvent.getSensor().significantChangeHasOccurred(previousSensorEvent,
					currentSensorEvent)) {

				if (cycle.eventIsOutOfOrder(currentSensorEvent))
					throw new IllegalArgumentException(
							"specified sensorEvent is out of order (it's timestamp occurs in the past).");

				// Roll over n cycles if the event timestamp is past our current end time
				cycle.adjustCycleStartTimeToClosestEnclosingCycle(currentSensorEvent);

				// Add sensor event to beginning of empty cycle
				if (cycle.isEmpty()) {
					cycle.addSensorEvent(currentSensorEvent);
					return predictions;

				} else {

					// Locate the right position to insert the new cycle node
					for (int i = 0; i < cycle.getNodeList().size(); i++) {

						// Prior and new sensor event are equal? merge them
						if (equalsExistingSensorEvent(currentSensorEvent)) {
							predictions = cycle.mergeAndReplacePreviousEvent(currentSensorEvent);
							return predictions;

							// Have we passed the most recent previous cycle node?
						} else if (cycle
								.previousSensorEventOccuredAfterCurrentSensorEvent(currentSensorEvent)) {
							if (locateLastPreviousNodeWithTimestampAfterCurrentSensorEvent(
									currentSensorEvent)) {
								predictions = cycle.insertBeforePreviousNode(currentSensorEvent);
							} else {
								predictions = cycle.addToBeginningOfCycle(currentSensorEvent);
							}
							return predictions;
						}
					} // end for more cycle nodes

				} // end else the cycle is not empty
				/**
				 * Non-empty list, no equals found, no prior closest found, add to the "end".
				 */
				cycle.addSensorEvent(currentSensorEvent);

			} // end data has changed significantly
		} // end minimum delay has occurred

		// Save and return any current predictions
		predictionService.setCurrentPredictions(predictions);
		return predictions;
	}

	/**
	 * 
	 * Locate the first node that is equal to the given node, ore return false
	 * 
	 * @param currentSensorEvent
	 * @return
	 */
	private boolean equalsExistingSensorEvent(SensorEvent currentSensorEvent) {
		int startIndex = 0;
		while (startIndex < cycle.nodeList.size()) {

			// current node equals given event
			if (cycle.nodeList.get(startIndex).getSensorEvent().equals(currentSensorEvent)) {
				cycle.setPreviousCycleNodeIndex(startIndex);
				return true;
			} else {

				// Compare the next event
				startIndex++;
			}
		}
		/**
		 * No sensor Event found
		 */
		return false;
	}

	/**
	 * Locate the first prior node in the cycle with a timestamp less than the
	 * current event. At the end of this method the previous Cycle index will be
	 * pointing to the position to insert the current node.
	 * 
	 * @param currentSensorEvent
	 */
	private boolean locateLastPreviousNodeWithTimestampAfterCurrentSensorEvent(
			SensorEvent currentSensorEvent) {
		while (cycle.getPreviousCycleNodeIndex() >= 0) {

			// previous node time greater than current, iterate to its previous
			if (cycle.previousSensorEventOccuredAfterCurrentSensorEvent(currentSensorEvent)) {
				cycle.setPreviousCycleNodeIndex(cycle.getPreviousCycleNodeIndex() - 1);
			} else {
				/**
				 * We have located the 1st previous node whose timestamp is less than the
				 * current event. Insert the new event after this node.
				 */
				cycle.incrementPreviousNodeIndex();
				return true;
			}
		}
		/**
		 * All cycle nodes have timestamps greater than the current, insert the new node
		 * first in the cycle
		 */
		return false;
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
