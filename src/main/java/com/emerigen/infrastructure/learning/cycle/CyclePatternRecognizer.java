/**
 * 
 */
package com.emerigen.infrastructure.learning.cycle;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.learning.PatternRecognizer;
import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.infrastructure.utils.Utils;

/**
 * See cuperclass documentation
 * 
 * @author Larry
 *
 */
public class CyclePatternRecognizer extends PatternRecognizer {

	/**
	 * @IDEA - enable cycle data point fuzziness using equality based on std
	 *       deviation. As long as values are within this standard deviation from
	 *       each other they will be considered to be equal. This allows for
	 *       fuzziness of the data points associated with the nodes of a cycle;
	 *       effectively enabling predictions when data points vary somewhat, but
	 *       not in principle (all too often occurs dealing with life). For example,
	 *       with GPS daily routes, multiple things may influence route node
	 *       visitations (and visitation durations) including traffic, working late,
	 *       detours, stopping for gas, stopping by the grocery store on the way
	 *       home, going to lunch at different places, ...
	 */

	private Cycle cycle;
	private Sensor sensor;
	private SensorEvent previousSensorEvent = null;
	private PredictionService predictionService;

	private double allowablePercentDifferenceForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.percent.difference.for.equality"));

	private static final Logger logger = Logger.getLogger(CyclePatternRecognizer.class);

	public CyclePatternRecognizer(Cycle cycle, Sensor sensor,
			PredictionService predictionService) {
		super(predictionService);
		if (cycle == null)
			throw new IllegalArgumentException("cycle must not be null");
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		if (predictionService == null)
			throw new IllegalArgumentException("predictionService must not be null");

		this.cycle = cycle;
		this.sensor = sensor;
		this.predictionService = predictionService;

	}

	private void adjustCycleStartTimeToClosestEnclosingCycle(SensorEvent sensorEvent) {
		if ((sensorEvent.getTimestamp() - cycle.getCycleStartTimeNano()) > cycle
				.getCycleDurationTimeNano()) {

			// Calculate closest enclosing cycle
			long cyclesToSkip = (sensorEvent.getTimestamp()
					- cycle.getCycleStartTimeNano()) / cycle.getCycleDurationTimeNano();

			cycle.setCycleStartTimeNano(cycle.getCycleStartTimeNano()
					+ (cyclesToSkip * cycle.getCycleDurationTimeNano()));
			logger.info(
					"Incoming event was past our current cycle duration so the new cycleStartTime ("
							+ cycle.getCycleStartTimeNano()
							+ "), sensor event timestamp (" + sensorEvent.getTimestamp()
							+ ")");
		}
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

		if (!(sensor.getSensorType() == currentSensorEvent.getSensorType()))
			throw new IllegalArgumentException(
					"given sensor type (" + currentSensorEvent.getSensorType()
							+ "), does not match my sensor type ("
							+ sensor.getSensorType() + ")");
		if (!(sensor.getSensorLocation() == currentSensorEvent.getSensorLocation()))
			throw new IllegalArgumentException(
					"given sensor location (" + currentSensorEvent.getSensorLocation()
							+ "), does not match cycle sensor location ("
							+ sensor.getSensorLocation() + ")");
		List<Prediction> predictions = new ArrayList<Prediction>();

		// return predictions;

		// Required elapse time has passed since last event?
		if (currentSensorEvent.getSensor().minimumDelayBetweenReadingsIsSatisfied(
				previousSensorEvent, currentSensorEvent)) {

			// Data has significantly changed?
			if (currentSensorEvent.getSensor().significantChangeHasOccurred(
					previousSensorEvent, currentSensorEvent)) {

				// Roll over n cycles if the event timestamp is past our current end time
				adjustCycleStartTimeToClosestEnclosingCycle(currentSensorEvent);

				if (previousSensorEvent == null) {
					previousSensorEvent = currentSensorEvent;
					predictions = predictionService
							.getPredictionsForSensorEvent(currentSensorEvent);
					predictionService.setCurrentPredictions(predictions);
					return predictions;
				}

				if (currentEventEqualsPreviousEvent(currentSensorEvent)) {

					// Sensor events equal? Merge, discard current, return predictions
					predictions = mergeAndReplacePreviousEvent(currentSensorEvent);
					predictionService.setCurrentPredictions(predictions);
					return predictions;
				} else if (currentEventIsGreaterThanPreviousEvent(currentSensorEvent)) {

					// Previous event occurs after current event? create new Transition
					predictionService.createPredictionFromSensorEvents(
							previousSensorEvent, currentSensorEvent);
					predictions = predictionService
							.getPredictionsForSensorEvent(currentSensorEvent);
					predictionService.setCurrentPredictions(predictions);
					previousSensorEvent = currentSensorEvent;

					return predictions;
				} else if (currentEventIsLessThanPreviousEvent(currentSensorEvent)) {

					// Current event prior to previous event? create backward Transition
					predictionService.createPredictionFromSensorEvents(currentSensorEvent,
							previousSensorEvent);
					predictions = predictionService
							.getPredictionsForSensorEvent(currentSensorEvent);
					predictionService.setCurrentPredictions(predictions);
					return predictions;
				}

			} // end data has changed significantly
		} // end minimum delay has occurred

		// Save and return any current predictions
		predictionService.setCurrentPredictions(predictions);
		return predictions;
	}

	private boolean currentEventEqualsPreviousEvent(SensorEvent currentSensorEvent) {
		boolean valuesEqual = Utils.equals(currentSensorEvent.hashCode(),
				previousSensorEvent.hashCode());
		return valuesEqual;
	}

	/**
	 * The previous and current events are statistically equal, merge the durations
	 * , accumulating the duration of the data point and discard the new event.
	 */
	List<Prediction> mergeAndReplacePreviousEvent(SensorEvent newSensorEvent) {
		List<Prediction> predictions = new ArrayList<Prediction>();

		long mergedDuration = previousSensorEvent.getDataPointDurationNano()
				+ newSensorEvent.getDataPointDurationNano();
		previousSensorEvent.setDataPointDurationNano(mergedDuration);
		logger.info("New sensor event merged with previous event, merged event: "
				+ previousSensorEvent.toString());

		predictions = predictionService.getPredictionsForSensorEvent(previousSensorEvent);
		return predictions;
	}

	private boolean currentEventIsGreaterThanPreviousEvent(
			SensorEvent currentSensorEvent) {
		long currentEventTimestampOffset = currentSensorEvent.getTimestamp()
				% cycle.getCycleDurationTimeNano();
		long previousEvcentTimestampOffset = previousSensorEvent.getTimestamp()
				% cycle.getCycleDurationTimeNano();
		return currentEventTimestampOffset > previousEvcentTimestampOffset;
	}

	private boolean currentEventIsLessThanPreviousEvent(SensorEvent currentSensorEvent) {
		long currentEventTimestampOffset = currentSensorEvent.getTimestamp()
				% cycle.getCycleDurationTimeNano();
		long previousEvcentTimestampOffset = previousSensorEvent.getTimestamp()
				% cycle.getCycleDurationTimeNano();
		return currentEventTimestampOffset < previousEvcentTimestampOffset;
	}

	@Override
	public String toString() {
		return "CyclePatternRecognizer [cycle=" + cycle + ", sensor=" + sensor
				+ ", previousSensorEvent=" + previousSensorEvent + ", predictionService="
				+ predictionService + ", allowablePercentDifferenceForEquality="
				+ allowablePercentDifferenceForEquality + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(allowablePercentDifferenceForEquality);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((cycle == null) ? 0 : cycle.hashCode());
		result = prime * result
				+ ((predictionService == null) ? 0 : predictionService.hashCode());
		result = prime * result
				+ ((previousSensorEvent == null) ? 0 : previousSensorEvent.hashCode());
		result = prime * result + ((sensor == null) ? 0 : sensor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CyclePatternRecognizer other = (CyclePatternRecognizer) obj;
		if (Double.doubleToLongBits(allowablePercentDifferenceForEquality) != Double
				.doubleToLongBits(other.allowablePercentDifferenceForEquality))
			return false;
		if (cycle == null) {
			if (other.cycle != null)
				return false;
		} else if (!cycle.equals(other.cycle))
			return false;
		if (predictionService == null) {
			if (other.predictionService != null)
				return false;
		} else if (!predictionService.equals(other.predictionService))
			return false;
		if (previousSensorEvent == null) {
			if (other.previousSensorEvent != null)
				return false;
		} else if (!previousSensorEvent.equals(other.previousSensorEvent))
			return false;
		if (sensor == null) {
			if (other.sensor != null)
				return false;
		} else if (!sensor.equals(other.sensor))
			return false;
		return true;
	}

	public long getCycleStartTimeNano() {
		return cycle.getCycleStartTimeNano();
	}

	public long getCycleDurationTimeNano() {
		return cycle.getCycleDurationTimeNano();
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
		cycle.setCycleType(cycle.getCycleType());
	}

	/**
	 * @return the sensor
	 */
	public Sensor getSensor() {
		return sensor;
	}

	/**
	 * @param sensor the sensor to set
	 */
	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	/**
	 * @return the previousSensorEvent
	 */
	public SensorEvent getPreviousSensorEvent() {
		return previousSensorEvent;
	}

	/**
	 * @param previousSensorEvent the previousSensorEvent to set
	 */
	public void setPreviousSensorEvent(SensorEvent previousSensorEvent) {
		this.previousSensorEvent = previousSensorEvent;
	}

	/**
	 * @return the predictionService
	 */
	@Override
	public PredictionService getPredictionService() {
		return predictionService;
	}

	/**
	 * @param predictionService the predictionService to set
	 */
	@Override
	public void setPredictionService(PredictionService predictionService) {
		this.predictionService = predictionService;
	}

	/**
	 * @return the allowablePercentDifferenceForEquality
	 */
	public double getAllowablePercentDifferenceForEquality() {
		return allowablePercentDifferenceForEquality;
	}

	/**
	 * @param allowablePercentDifferenceForEquality the
	 *                                              allowablePercentDifferenceForEquality
	 *                                              to set
	 */
	public void setAllowablePercentDifferenceForEquality(
			double allowablePercentDifferenceForEquality) {
		this.allowablePercentDifferenceForEquality = allowablePercentDifferenceForEquality;
	}

	/**
	 * @return the logger
	 */
	public static Logger getLogger() {
		return logger;
	}

	/**
	 * @param cycleDurationTimeNano the cycleDurationTimeNano to set
	 */
	public void setCycleDurationTimeNano(long cycleDurationTimeNano) {
		cycle.setCycleDurationTimeNano(cycleDurationTimeNano);
	}

}
