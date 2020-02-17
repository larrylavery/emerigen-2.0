/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonIgnore;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.utils.CircularList;
import com.emerigen.infrastructure.utils.EmerigenProperties;

/**
 * @author Larry
 *
 */
public abstract class Cycle {

	/**
	 * This is the starting timestamp for the beginning of each cycle type. For
	 * example, Hourly cycles start at 0 minutes 0 seconds of the current hour,
	 * daily Cycles start at 12:00 am of the current day, weekly cycles atart at
	 * 12:00am Sunday morning of the current week, etc
	 * 
	 * This is an absolute value of nanoseconds since Jan 1, 1970. It is used to
	 * calculate offsets for data point timestamps in the sensor events. This field
	 * is rolled over to the next Cycle start time at the end of the duration of the
	 * present cycle (i.e. moved to the next 24 hours for DailyCycle, 7 days for a
	 * weekly cycle, etc.).
	 */
	protected long cycleStartTimeNano;

	/**
	 * This is the duration for a cycle. Time zones and Daylight Savings times are
	 * taken into account. Generally, this will be 168 hours for a weekly cycle, 24
	 * hours for a daily cycle, etc.; all converted to nanoseconds.
	 */
	protected long cycleDurationTimeNano;

	/**
	 * @IDEA - enable cycle data point fuzzines using equality based on std
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
	protected CircularList<CycleNode> nodeList;

	private int previousCycleNodeIndex = -1;
	protected String cycleType;
	private int sensorLocation;
	private int sensorType;

	private double allowableStandardDeviationForEquality = Double.parseDouble(
			EmerigenProperties.getInstance().getValue("cycle.allowable.std.deviation.for.equality"));

	private static final Logger logger = Logger.getLogger(Cycle.class);

	public Cycle(int sensorType, int sensorLocation, String cycleType) {
		if (sensorType <= 0)
			throw new IllegalArgumentException("sensor type must be positive");
		if (sensorLocation <= 0)
			throw new IllegalArgumentException("sensor location must be positive");
		if (cycleType == null || cycleType.isEmpty())
			throw new IllegalArgumentException("cycleType must not be null or empty");

		nodeList = new CircularList<>();
		this.sensorType = sensorType;
		this.cycleType = cycleType;
		this.sensorLocation = sensorLocation;
		this.cycleStartTimeNano = calculateCycleStartTimeNano();
		this.cycleDurationTimeNano = calculateCycleDurationNano();
	}

	public Cycle(String cycleType) {
		nodeList = new CircularList<>();
//		this.sensorType = Sensor.TYPE_GPS; //TODO what is the default sensor type?
		this.cycleType = cycleType;
		this.cycleStartTimeNano = calculateCycleStartTimeNano();
		this.cycleDurationTimeNano = calculateCycleDurationNano();
	}

	public void incrementPreviousNodeIndex() {
		previousCycleNodeIndex = (previousCycleNodeIndex + 1) % nodeList.size();
	}

	boolean previousSensorEventOccuredAfterCurrentSensorEvent(SensorEvent currentSensorEvent) {
		CycleNode newCycleNode = new CycleNode(this, currentSensorEvent);
		return nodeList.get(previousCycleNodeIndex).getStartTimeOffsetNano() > newCycleNode
				.getStartTimeOffsetNano();

	}

	boolean currentSensorEventEqualsPreviousSensorEvent(SensorEvent currentSensorEvent) {
		return currentSensorEvent.getSensor().equals(currentSensorEvent,
				nodeList.get(previousCycleNodeIndex).getSensorEvent());
	}

	List<Prediction> insertBeforePreviousNode(SensorEvent newSensorEvent) {
		List<Prediction> predictions = new ArrayList<>();
		CycleNode newCycleNode = new CycleNode(this, newSensorEvent);
		nodeList.add(previousCycleNodeIndex, newCycleNode);
		predictions.add(new CyclePrediction(newSensorEvent));
		incrementPreviousNodeIndex();
		return predictions;

	}

	/**
	 * This is the first cycle node for my cycle. Calulate data point duration and
	 * time offset from the cycle start time. Add the node to the cycle, and update
	 * previous node index.
	 * 
	 * TODO revisit offset calculation and verify with test
	 */
	private void addFirstCycleNode(CycleNode newCycleNode) {
		long timestamp = newCycleNode.getSensorEvent().getTimestamp();
		long duration = newCycleNode.getTimeOffset(timestamp);
		newCycleNode.setDataPointDurationNano(duration);
		nodeList.add(newCycleNode);
		previousCycleNodeIndex = nodeList.indexOf(newCycleNode);
		logger.info("New cycle list, adding first node: " + newCycleNode.toString());
	}

	/**
	 * The previous and current cycle nodes are statistically equal, merge the Node
	 * information, accumulating the duration of the data point and adjust the start
	 * time of the current data point to include the previous measurement start
	 * time. Then replace the previous node with the merged node.
	 */
	List<Prediction> mergeAndReplacePreviousEvent(SensorEvent newSensorEvent) {
		List<Prediction> predictions = new ArrayList<Prediction>();
		CycleNode newCycleNode = new CycleNode(this, newSensorEvent);

		long mergedDuration = nodeList.get(previousCycleNodeIndex).getDataPointDurationNano()
				+ newCycleNode.getDataPointDurationNano();
		newCycleNode.setDataPointDurationNano(mergedDuration);

		nodeList.remove(previousCycleNodeIndex);
		nodeList.add(previousCycleNodeIndex, newCycleNode);
		logger.info("New cycle Node merged with previous node, merged node: " + newCycleNode.toString()
				+ ", cycle list: " + nodeList.toString());
		predictions.add(new CyclePrediction(nodeList.get(previousCycleNodeIndex + 1).getSensorEvent()));
//		previousCycleNodeIndex = incrementIndex(previousCycleNodeIndex);
		return predictions;
	}

	void adjustCycleStartTimeToClosestEnclosingCycle(SensorEvent sensorEvent) {
		if ((sensorEvent.getTimestamp() - getCycleStartTimeNano()) > getCycleDurationTimeNano()) {

			// Calculate closest enclosing cycle
			long cyclesToSkip = (sensorEvent.getTimestamp() - getCycleStartTimeNano())
					/ getCycleDurationTimeNano();

			// Reset previous node pointer to first object of the new cycle
//			if (cyclesToSkip > 0)
//				previousCycleNodeIndex = 0;

			cycleStartTimeNano = cycleStartTimeNano + (cyclesToSkip * getCycleDurationTimeNano());
			logger.info("Incoming event was past our current cycle duration so the new cycleStartTime ("
					+ cycleStartTimeNano + "), sensor event timestamp (" + sensorEvent.getTimestamp() + ")");
		}
	}

	boolean eventIsOutOfOrder(SensorEvent sensorEvent) {

		// If event occured prior to the previous event then out of order
		if (previousCycleNodeIndex >= 0) {
			long previousEventTimestamp = nodeList.get(previousCycleNodeIndex).getSensorEvent()
					.getTimestamp();
			if (sensorEvent.getTimestamp() < previousEventTimestamp)
				return true;
			else
				return false;
		}
		return false;
	}

	/**
	 * @return the cycleStartTimeNano
	 */
	public abstract long calculateCycleStartTimeNano();

	/**
	 * @return the cycleDurationTimeNano
	 */
	public abstract long calculateCycleDurationNano();

	/**
	 * @return the cycleStartTimeNano
	 */
	public long getCycleStartTimeNano() {
		return cycleStartTimeNano;
	}

	/**
	 * @return the cycleDurationTimeNano
	 */
	public long getCycleDurationTimeNano() {
		return cycleDurationTimeNano;
	}

	/**
	 * @return the cycle
	 */
	public CircularList<CycleNode> getNodeList() {
		return nodeList;
	}

	/**
	 * @return the key for this cycle
	 */
	@JsonIgnore
	public String getKey() {
		return "" + sensorType + sensorLocation + cycleType;
	}

	@Override
	public String toString() {
		return "Cycle [cycleStartTimeNano=" + cycleStartTimeNano + ", cycleDurationTimeNano="
				+ cycleDurationTimeNano + ", nodeList=" + nodeList + ", previousCycleNodeIndex="
				+ previousCycleNodeIndex + ", cycleType=" + cycleType + ", sensorLocation=" + sensorLocation
				+ ", sensorType=" + sensorType + ", allowableStandardDeviationForEquality="
				+ allowableStandardDeviationForEquality + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(allowableStandardDeviationForEquality);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (cycleDurationTimeNano ^ (cycleDurationTimeNano >>> 32));
		result = prime * result + ((cycleType == null) ? 0 : cycleType.hashCode());
		result = prime * result + ((nodeList == null) ? 0 : nodeList.hashCode());
		result = prime * result + sensorLocation;
		result = prime * result + sensorType;
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
		Cycle other = (Cycle) obj;
//		if (Double.doubleToLongBits(allowableStandardDeviationForEquality) != Double
//				.doubleToLongBits(other.allowableStandardDeviationForEquality))
//			return false;
		if (cycleDurationTimeNano != other.cycleDurationTimeNano)
			return false;
		if (cycleType == null) {
			if (other.cycleType != null)
				return false;
		} else if (!cycleType.equals(other.cycleType))
			return false;
		if (nodeList == null) {
			if (other.nodeList != null)
				return false;
		} else if (!nodeList.equals(other.nodeList))
			return false;
		if (sensorLocation != other.sensorLocation)
			return false;
		if (sensorType != other.sensorType)
			return false;
		return true;
	}

	/**
	 * @return the cycleType
	 */
	public String getCycleType() {
		return cycleType;
	}

	/**
	 * @return the sensorLocation
	 */
	public int getSensorLocation() {
		return sensorLocation;
	}

	/**
	 * @return the sensorType
	 */
	public int getSensorType() {
		return sensorType;
	}

	/**
	 * @param nodeList the nodeList to set
	 */
	public void setNodeList(CircularList<CycleNode> nodeList) {
		this.nodeList = nodeList;
	}

	/**
	 * @param cycleStartTimeNano the cycleStartTimeNano to set
	 */
	public void setCycleStartTimeNano(long cycleStartTimeNano) {
		this.cycleStartTimeNano = cycleStartTimeNano;
	}

	/**
	 * @param previousCycleNodeIndex the previousCycleNodeIndex to set
	 */
	public void setPreviousCycleNodeIndex(int previousCycleNodeIndex) {
		this.previousCycleNodeIndex = previousCycleNodeIndex;
	}

	/**
	 * @param cycleType the cycleType to set
	 */
	public void setCycleType(String cycleType) {
		this.cycleType = cycleType;
	}

	/**
	 * @param sensorLocation the sensorLocation to set
	 */
	public void setSensorLocation(int sensorLocation) {
		this.sensorLocation = sensorLocation;
	}

	/**
	 * @param sensorType the sensorType to set
	 */
	public void setSensorType(int sensorType) {
		this.sensorType = sensorType;
	}

	public void setCycleDurationTimeNano(long cycleDurationNano) {
		this.cycleDurationTimeNano = cycleDurationNano;
		// TODO Auto-generated method stub

	}

	void addSensorEvent(SensorEvent sensorEvent) {
		CycleNode newCycleNode = new CycleNode(this, sensorEvent);

		if (isEmpty()) {
			addFirstCycleNode(newCycleNode);
		} else {
			this.nodeList.add(newCycleNode);
		}
		incrementPreviousNodeIndex();
	}

	/**
	 * @param allowableStandardDeviationForEquality the
	 *                                              allowableStandardDeviationForEquality
	 *                                              to set
	 */
	public void setAllowableStandardDeviationForEquality(double allowableStandardDeviationForEquality) {
		this.allowableStandardDeviationForEquality = allowableStandardDeviationForEquality;
	}

	public double getAllowableStandardDeviationForEquality() {
		return allowableStandardDeviationForEquality;
	}

	public int getPreviousCycleNodeIndex() {
		return previousCycleNodeIndex;
	}

	public boolean isEmpty() {
		return nodeList.isEmpty();
	}

	public void addCycleNode(CycleNode cycleNode) {
		nodeList.add(cycleNode);
	}

}
