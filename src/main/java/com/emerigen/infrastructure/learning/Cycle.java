/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.util.List;
import java.util.stream.Collectors;

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
	 * All cycles are implemented as a circular list
	 */
	protected CircularList<CycleNode> cycle;

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
	protected final long cycleStartTimeMillis;

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
	private List<SensorEvent> sensorEvents;

	private final double allowableStandardDeviationForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.std.deviation.for.equality"));

	private static final Logger logger = Logger.getLogger(Cycle.class);

	public Cycle() {
		this.cycleStartTimeMillis = calculateCycleStartTimeMillis();
		this.cycleDurationMillis = calculateCycleDurationMillis();
	}

	/**
	 * Construct a cycle from the given collection of SensorEvents
	 * 
	 * @param sensorEvents
	 */
	public Cycle(List<SensorEvent> sensorEvents) {
		this();
		if (sensorEvents == null || sensorEvents.isEmpty())
			throw new IllegalArgumentException("sensorEvents must not be null o empty");
	}

	/**
	 * Merge all consecutive sensor events (data points) that have insignificantly
	 * different data points,accumulating their durations. When complete, the
	 * remaining cycle nodes have the potential to become part of the cycle being
	 * learned because they are the data points with the longest durations. For
	 * example, GPS data points remaining will have the longest visitation times
	 * making them a good predictor of future cycle-based (hour, daily, weekly, etc)
	 * destinations.
	 * 
	 * Fortunately, this logic is not specific to one type of data point and should
	 * work just as well for most sensors (one exception is Accelerometer).
	 * 
	 * @param allSensorEvents
	 * @return a list of merged sensor events representing the data points with the
	 *         most accumulated "time at data point" values. These are the potential
	 *         nodes in a cycle.
	 */
	public List<CycleNode> generateCycleNodes(List<SensorEvent> allSensorEvents) {

		if (allSensorEvents == null || allSensorEvents.isEmpty())
			throw new IllegalArgumentException("allSensorEvents must not be null or empty");

		// First, transform sensor events into CycleNodes for learning purposes
		long originTimeStamp = allSensorEvents.get(0).getTimestamp(); // This is the original
																		// timestamp
		List<CycleNode> cycleNodes = allSensorEvents.stream()
				.map(sensorEvent -> new CycleNode(this, sensorEvent, originTimeStamp))
				.collect(Collectors.toList());

		// Next, merge consecutive nodes and accumulate data point duration times
		int index = 0;

		while (index < cycleNodes.size()) {
			if (index == cycleNodes.size() - 1) {

				// No more consecutive nodes, return potential data point candidate nodes
				logger.info("Returning potential cycle nodes: " + cycleNodes.toString());
				return cycleNodes;
			} else if (cycleNodes.get(index).equals(cycleNodes.get(index + 1))) {

				/**
				 * Merge the Node information, accumulating the duration of this data point and
				 * adjust the start time of the next data point to include the current
				 * measurement start time.
				 */
				long mergedDuration = cycleNodes.get(index).getDataPointDurationMillis()
						+ cycleNodes.get(index + 1).getDataPointDurationMillis();
				CycleNode newCycleNode = new CycleNode(this,
						cycleNodes.get(index + 1).getSensorEvent(), mergedDuration);

				/**
				 * Finally, replace the two consecutive nodes with the new one
				 */
				cycleNodes.remove(index);
				cycleNodes.remove(index);
				cycle.add(index, newCycleNode);
			} else {

				/**
				 * The current and next cycle nodes are statistically different so examine the
				 * next two consecutive entries.
				 */
				index++;
			}
		} // end-while there are more consecutive cycle nodes
		logger.info("Returning potential cycle nodes: " + cycleNodes.toString());
		return cycleNodes;
	}

	/**
	 * @return the cycle
	 */
	public CircularList<CycleNode> getCycle() {
		return cycle;
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
	 * @return the sensorEvents
	 */
	public List<SensorEvent> getSensorEvents() {
		return sensorEvents;
	}

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

	// stream list and compare item n with n+1
//    integerList.stream()
//            .reduce((integer1, integer2) -> {
//                assert integer1 < integer2 : "ordering must be ascending";
//                // return second value (which will be processed as "integer1" in the next iteration
//                return integer2;
//            });
//

}
