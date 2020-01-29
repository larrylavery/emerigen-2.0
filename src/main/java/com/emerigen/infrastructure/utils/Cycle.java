/**
 * 
 */
package com.emerigen.infrastructure.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * @author Larry
 *
 */
public class Cycle<T> {

	private CircularList<CycleNode> cycle;
	private long startTimeMillis;
	private long cycleDurationMillis;
	private float allowableStandardDeviation;
	private List<SensorEvent> sensorEvents;

	private static final Logger logger = Logger.getLogger(Cycle.class);

	public Cycle(long startTimeMillis, long cycleDurationMillis, float allowableStandardDeviation) {
		if (startTimeMillis < 0)
			throw new IllegalArgumentException("startTimeMillis must be zero or more");
		if (cycleDurationMillis <= 0)
			throw new IllegalArgumentException("cycleDurationMillis must be positive");
		if (allowableStandardDeviation < 0)
			throw new IllegalArgumentException("allowableStandardDeviation must be positive");

		this.cycleDurationMillis = cycleDurationMillis;
		this.startTimeMillis = startTimeMillis;
		this.allowableStandardDeviation = allowableStandardDeviation; // Standard Deviation for
																		// equality purposes
	}

	/**
	 * Construct a cycle from the given collection of SensorEvents
	 * 
	 * @param startTimeMillis
	 * @param cycleDurationMillis
	 * @param stdDeviation
	 */
	public Cycle(List<SensorEvent> sensorEvents, long startTimeMillis, long cycleDurationMillis,
			float allowableStandardDeviation) {

		if (sensorEvents == null || sensorEvents.isEmpty())
			throw new IllegalArgumentException("sensorEvents must not be null o empty");
		if (startTimeMillis < 0)
			throw new IllegalArgumentException("startTimeMillis must be zero or more");
		if (cycleDurationMillis <= 0)
			throw new IllegalArgumentException("cycleDurationMillis must be positive");
		if (allowableStandardDeviation < 0)
			throw new IllegalArgumentException("allowableStandardDeviation must be positive");

		this.cycleDurationMillis = cycleDurationMillis;
		this.sensorEvents = sensorEvents;
		this.startTimeMillis = startTimeMillis;
		this.allowableStandardDeviation = allowableStandardDeviation; // Standard Deviation for
																		// equality purposes
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
	 * Fortunately, this logic is not specific to any type of data point and should
	 * work just as well for most sensors (one exception is Accelerometer).
	 * 
	 * @param allSensorEvents
	 * @return a list of merged sensor events representing the data points with the
	 *         most accumulated "time at data point" values. These are the potential
	 *         noeds in a cycle.
	 */
	public List<CycleNode> generatePotentialNodes(List<SensorEvent> allSensorEvents) {

		if (allSensorEvents == null || allSensorEvents.isEmpty())
			throw new IllegalArgumentException("allSensorEvents must not be null or empty");

		// First, transform sensor events into CycleNodes for learning purposes
		List<CycleNode> cycleNodes = allSensorEvents.stream()
				.map(sensorEvent -> new CycleNode(sensorEvent)).collect(Collectors.toList());

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
				CycleNode currentCycleNode = cycleNodes.get(index);
				CycleNode nextCycleNode = cycleNodes.get(index + 1);

				long mergedDuration = currentCycleNode.getDataPointDurationMillis()
						+ nextCycleNode.getDataPointDurationMillis();
				nextCycleNode.setDataPointDurationMillis(mergedDuration);
				nextCycleNode.setStartTimeMillis(currentCycleNode.getStartTimeMillis());

				/**
				 * Finally, remove the current node since it has been merged into the next node.
				 */
				cycleNodes.remove(index);
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

	// stream list and compare item n with n+1
//    integerList.stream()
//            .reduce((integer1, integer2) -> {
//                assert integer1 < integer2 : "ordering must be ascending";
//                // return second value (which will be processed as "integer1" in the next iteration
//                return integer2;
//            });
//

}
