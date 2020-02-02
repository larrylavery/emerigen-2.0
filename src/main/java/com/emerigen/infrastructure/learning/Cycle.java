/**
 * 
 */
package com.emerigen.infrastructure.learning;

import org.apache.log4j.Logger;

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
	 * This is an absolute value of milliseconds since Jan 1, 1970. It is used to
	 * calculate offsets for data point timestamps in the sensor events. This field
	 * is rolled over to the next Cycle start time at the end of the duration of the
	 * present cycle (i.e. moved to the next 24 hours for DailyCycle, 7 days for a
	 * weekly cycle, etc.).
	 */
	protected long cycleStartTimeMillis;

	/**
	 * This is the duration for a cycle. Time zones and Daylight Savings times are
	 * taken into account. Generally, this will be 168 hours for a weekly cycle, 24
	 * hours for a daily cycle, etc.; all converted to milliseconds.
	 */
	protected final long cycleDurationMillis;

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
	protected CircularList<CycleNode> cycle;

	private int previousCycleNodeIndex = -1;
	private int sensorType;

	private final double allowableStandardDeviationForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.std.deviation.for.equality"));

	private static final Logger logger = Logger.getLogger(Cycle.class);

	public Cycle(int sensorType) {
		if (sensorType <= 0)
			throw new IllegalArgumentException("sensor type must be positive");
		cycle = new CircularList<>();
		this.sensorType = sensorType;
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
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
	 * This logic is not specific to one type of data point or cycle and should work
	 * just as well for most sensors (one exception is Accelerometer) and all cycle
	 * durations.
	 * 
	 * @param sensorEvent
	 * @return
	 */
	public CycleNode onNewSensorEvent(SensorEvent sensorEvent) {
		if (sensorEvent == null)
			throw new IllegalArgumentException("sensorEvent must not be null");
		if (!(sensorType == sensorEvent.getSensorType()))
			throw new IllegalArgumentException("given sensor type (" + sensorEvent.getSensorType()
					+ "), does not match cycle sensor type (" + sensorType + ")");

		// Verify new event is after the last event
		if (previousCycleNodeIndex >= 0) {
			long previousEventTimestamp = cycle.get(previousCycleNodeIndex).getSensorEvent()
					.getTimestamp();
			if (sensorEvent.getTimestamp() < previousEventTimestamp)
				throw new IllegalArgumentException("sensorEvent out of order received");
		}

		// Skip cycles if the event timestamp is past our current duration
		if ((sensorEvent.getTimestamp() - getCycleStartTimeMillis()) > getCycleDurationMillis()) {
			long cyclesToSkip = (sensorEvent.getTimestamp() - getCycleStartTimeMillis())
					/ getCycleDurationMillis();

			cycleStartTimeMillis = cycleStartTimeMillis + (cyclesToSkip * getCycleDurationMillis());
			logger.info(
					"Incoming event was past our current cycle duration so the new cycleStartTime ("
							+ cycleStartTimeMillis + "), sensor event timestamp ("
							+ sensorEvent.getTimestamp() + ")");
		}

		// Now that the cycle start time has been adjusted, create a new CycleNode
		CycleNode newCycleNode = new CycleNode(this, sensorEvent);

		if (previousCycleNodeIndex < 0) {

			/**
			 * If this is the first node in the cycle, then calulate the data point duration
			 * from the cycle start, add the node to the list, and update previous node
			 * index.
			 */
			newCycleNode.setDataPointDurationMillis(
					newCycleNode.getTimeOffset(sensorEvent.getTimestamp()));
			cycle.add(newCycleNode);
			previousCycleNodeIndex = 0;
			logger.info("New cycle list, adding first node: " + newCycleNode.toString());

		} else if (cycle.get(previousCycleNodeIndex).equals(newCycleNode)) {

			/**
			 * If the previous and current cycle nodes are statistically equal, merge the
			 * Node information, accumulating the duration of the data point and adjust the
			 * start time of the current data point to include the previous measurement
			 * start time. Then replace the previous node with the merged node.
			 */
			long mergedDuration = cycle.get(previousCycleNodeIndex).getDataPointDurationMillis()
					+ newCycleNode.getDataPointDurationMillis();
			newCycleNode.setDataPointDurationMillis(mergedDuration);

			cycle.remove(previousCycleNodeIndex);
			cycle.add(previousCycleNodeIndex, newCycleNode);
			logger.info("New Node merged with previous node, merged node: "
					+ newCycleNode.toString() + ", cycle list: " + cycle.toString());

		} else {

			/**
			 * If the previous and current cycle nodes are statistically different, add the
			 * current node and update previous node index.
			 */
			cycle.add(newCycleNode);
			previousCycleNodeIndex++;
			logger.info("Previous and new nodes are statistically different. adding New Node: "
					+ newCycleNode.toString() + ", cycle list: " + cycle.toString());
		}

		return newCycleNode;
	}

	/**
	 * @return the cycleStartTimeMillis
	 */
	public abstract long calculateCycleStartTimeMillis();

	/**
	 * @return the cycleDurationMillis
	 */
	public abstract long calculateCycleDurationMillis();

	/**
	 * @return the cycleStartTimeMillis
	 */
	public long getCycleStartTimeMillis() {
		return cycleStartTimeMillis;
	}

	/**
	 * @return the cycleDurationMillis
	 */
	public long getCycleDurationMillis() {
		return cycleDurationMillis;
	}

	/**
	 * @return the cycle
	 */
	public CircularList<CycleNode> getCycle() {
		return cycle;
	}

	@Override
	public String toString() {
		return "Cycle [cycleStartTimeMillis=" + cycleStartTimeMillis + ", cycleDurationMillis="
				+ cycleDurationMillis + ", cycle=" + cycle + ", previousCycleNodeIndex="
				+ previousCycleNodeIndex + ", sensorType=" + sensorType
				+ ", allowableStandardDeviationForEquality=" + allowableStandardDeviationForEquality
				+ "]";
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
