/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonIgnore;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.utils.CircularList;
import com.emerigen.infrastructure.utils.EmerigenProperties;

/**
 * @author Larry
 *
 */
public abstract class Cycle implements Serializable {

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
	protected final long cycleDurationNano;

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

	private final double allowableStandardDeviationForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.std.deviation.for.equality"));

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
		this.cycleDurationNano = calculateCycleDurationNano();
	}

	public Cycle(String cycleType) {
		nodeList = new CircularList<>();
//		this.sensorType = Sensor.TYPE_GPS; //TODO what is the default sensor type?
		this.cycleType = cycleType;
		this.cycleStartTimeNano = calculateCycleStartTimeNano();
		this.cycleDurationNano = calculateCycleDurationNano();
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
	public boolean onSensorChanged(SensorEvent sensorEvent) {

		// Validate parms
		if (sensorEvent == null)
			throw new IllegalArgumentException("sensorEvent must not be null");
		if (!(sensorType == sensorEvent.getSensorType()))
			throw new IllegalArgumentException("given sensor type (" + sensorEvent.getSensorType()
					+ "), does not match cycle sensor type (" + sensorType + ")");

		if (eventIsOutOfOrder(sensorEvent))
			throw new IllegalArgumentException("sensorEvent out of order received.");

		// Roll over n cycles if the event timestamp is past our current end time
		adjustCycleStartTimeToClosestEnclosingCycle(sensorEvent);

		// Create new cycle node based on the adjusted cycle start time
		CycleNode newCycleNode = new CycleNode(this, sensorEvent);

		// Empty cycle list?
		if (previousCycleNodeIndex < 0) {
			addFirstCycleNode(newCycleNode);
			return true;

		} else {

			// Locate the right position to insert the new cycle node
			for (int i = 0; i < nodeList.size(); i++) {

				// Previous and new sensor events are equal?
				if (sensorEventsAreEqual(sensorEvent,
						nodeList.get(previousCycleNodeIndex).getSensorEvent())) {
					return mergeAndReplacePreviousNode(newCycleNode);

					// Have we passed the most recent previous cycle node?
				} else if (previousNodeTimeIsGreaterThanNewNodeTime(newCycleNode)) {
					boolean result = insertBeforePreviousNode(newCycleNode);
					previousCycleNodeIndex--;
					return result;
				}
				previousCycleNodeIndex = wrapIndex(++previousCycleNodeIndex);

			} // end for

			/**
			 * Non-empty list, no equals found, no prior closest found, add to the current
			 * "end", which is really at the end of one circular cycle traversal.
			 */
			nodeList.add(newCycleNode);
			return true;
		}
	}

	public int wrapIndex(int index) {
		return index % nodeList.size();
	}

	private boolean previousNodeTimeIsGreaterThanNewNodeTime(CycleNode newCycleNode) {
		return nodeList.get(previousCycleNodeIndex).getStartTimeOffsetNano() > newCycleNode
				.getStartTimeOffsetNano();
	}

	private boolean sensorEventsAreEqual(SensorEvent firstSensorEvent,
			SensorEvent secondSensorEvent) {
		return firstSensorEvent.getSensor().equals(firstSensorEvent, secondSensorEvent);
	}

	/**
	 * We have moved just past the closest event in the past. Insert new node before
	 * the previous node and return
	 */
	private boolean insertBeforePreviousNode(CycleNode newCycleNode) {
		nodeList.add(previousCycleNodeIndex, newCycleNode);
		return true;
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
	private boolean mergeAndReplacePreviousNode(CycleNode newCycleNode) {
		long mergedDuration = nodeList.get(previousCycleNodeIndex).getDataPointDurationNano()
				+ newCycleNode.getDataPointDurationNano();
		newCycleNode.setDataPointDurationNano(mergedDuration);

		nodeList.remove(previousCycleNodeIndex);
		nodeList.add(previousCycleNodeIndex, newCycleNode);
		logger.info("New cycle Node merged with previous node, merged node: "
				+ newCycleNode.toString() + ", cycle list: " + nodeList.toString());
		return true;
	}

	private void adjustCycleStartTimeToClosestEnclosingCycle(SensorEvent sensorEvent) {
		if ((sensorEvent.getTimestamp() - getCycleStartTimeNano()) > getCycleDurationNano()) {

			// Calculate closest enclosing cycle
			long cyclesToSkip = (sensorEvent.getTimestamp() - getCycleStartTimeNano())
					/ getCycleDurationNano();

			// Reset previous node pointer to first object of the new cycle
			if (cyclesToSkip > 0)
				previousCycleNodeIndex = 0;

			cycleStartTimeNano = cycleStartTimeNano + (cyclesToSkip * getCycleDurationNano());
			logger.info(
					"Incoming event was past our current cycle duration so the new cycleStartTime ("
							+ cycleStartTimeNano + "), sensor event timestamp ("
							+ sensorEvent.getTimestamp() + ")");
		}
	}

	private boolean eventIsOutOfOrder(SensorEvent sensorEvent) {

		// If event occured prior to the previous event then out of order
		if (previousCycleNodeIndex >= 0) {
			long previousEventTimestamp = nodeList.get(previousCycleNodeIndex).getSensorEvent()
					.getTimestamp();
			if (sensorEvent.getTimestamp() < previousEventTimestamp)
				return true;
		}
		return false;
	}

	/**
	 * @return the cycleStartTimeNano
	 */
	public abstract long calculateCycleStartTimeNano();

	/**
	 * @return the cycleDurationNano
	 */
	public abstract long calculateCycleDurationNano();

	/**
	 * @return the cycleStartTimeNano
	 */
	public long getCycleStartTimeNano() {
		return cycleStartTimeNano;
	}

	/**
	 * @return the cycleDurationNano
	 */
	public long getCycleDurationNano() {
		return cycleDurationNano;
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
		return "Cycle [cycleStartTimeNano=" + cycleStartTimeNano + ", cycleDurationNano="
				+ cycleDurationNano + ", cycle=" + nodeList + ", previousCycleNodeIndex="
				+ previousCycleNodeIndex + ", cycleType=" + cycleType + ", sensorLocation="
				+ sensorLocation + ", sensorType=" + sensorType
				+ ", allowableStandardDeviationForEquality=" + allowableStandardDeviationForEquality
				+ "]";
	}

	public void addCycleNode(CycleNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cycleDurationNano ^ (cycleDurationNano >>> 32));
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
		if (cycleDurationNano != other.cycleDurationNano)
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

	// stream list and compare item n with n+1
//    integerList.stream()
//            .reduce((integer1, integer2) -> {
//                assert integer1 < integer2 : "ordering must be ascending";
//                // return second value (which will be processed as "integer1" in the next iteration
//                return integer2;
//            });
//

}
