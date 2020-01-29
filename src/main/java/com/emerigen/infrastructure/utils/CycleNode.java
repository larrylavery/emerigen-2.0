package com.emerigen.infrastructure.utils;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * @IDEA Cycles/Nodes/Fields with standard deviation-based equality. Allows each
 *       field to adapts its equality based on it current value relating to any
 *       other value. This is much better than a static range which does not
 *       take into account the number magnitude. With this the range
 *       automatically grows as the value grows; or shrinks as the case may be.
 * 
 * @author Larry
 *
 * @param <T>
 */
public class CycleNode {

	/**
	 * The event that defines this node. It may be a GPS, heart rate, etc... If GPS
	 * then the event defines the GPS coordinates of the location represented by
	 * this node. If heart rate, the event represents the heart rate at a point in
	 * time.
	 */
	private SensorEvent sensorEvent;

	/**
	 * The length of time that the data point [measurement] (as measured by the
	 * sensor event) is valid. If the event is a GPS measurement, then the
	 * dataPointDurationMillis is the length of time the GPS coordinates did not
	 * significantly change (ie how long the user stayed at [visited] this location.
	 * 
	 * For a heart rate sensor, it represents the lengh of time the heart rate
	 * stayed at the given heart rate (plus or minus the standard deviation of
	 * course.
	 */
	private long dataPointDurationMillis;

	/**
	 * The start time that this dataPoint was encountered.
	 */
	private long startTimeMillis;

	/**
	 * The standard deviation for equality is used during comparisons of data in
	 * this class. For example, if two field values are within this maximum standard
	 * deviation then they are considered equal.
	 * 
	 * This field enables "Cycle Node", and overall "Cycle Fuzziness" meaning that
	 * the individual nodes will match even when, in the case of GPS, the user does
	 * not exactly match previously learned cycle nodes. Larger values increase
	 * fuzziness whereas smaller values enforce more "strict" adherence to the
	 * learned cycle.
	 * 
	 * Finally, the deviation is node-specific to support a mixture of overall cycle
	 * fuzziness (as in real life). For example, when one goes to lunch for an hour
	 * every day the fuzziness is relatively greater since the destination (for
	 * lunch) may be radically different, while the visitation and start times may
	 * be less fuzzy.
	 * 
	 * TODO - should there be field-specific std deviation or node-specific
	 * deviation? Field-specific fuzziness supports different destinations for lunch
	 * while time frames are much more strict. Utils.equals() allows any numeric
	 * field to be compared using a standard deviation. Compromise on field-level
	 * fuzziness.
	 * 
	 * Fuzziness types: Cycle, Node, node fields. Hierarchy: Cycle provides default
	 * for Nodes and node fields if not specified there. Node overrides Cycle and
	 * provides default for node fields. Field override Node and Cycle to provide
	 * field-specific standard deviation variance.
	 * 
	 * TODO implement Node-specific std deviation for now. add Cycle/Field later
	 */
	private static double allowableStandardDeviationForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.std.deviation.for.equality"));

	private static long defaultDataPointDurationMillis = Long.parseLong(
			EmerigenProperties.getInstance().getValue("cycle.default.data.point.duration.millis"));

	private static final Logger logger = Logger.getLogger(CycleNode.class);

	private CycleNode(SensorEvent sensorEvent, long startTimeMillis, long dataPointDurationMillis,
			double allowableStandardDeviationForEquality) {

		// Validate parms
		if (sensorEvent == null)
			throw new IllegalArgumentException("SensorEvent must not be null");
		if (startTimeMillis < 0)
			throw new IllegalArgumentException("startTimeMillis must be zero or more");
		if (dataPointDurationMillis <= 0)
			throw new IllegalArgumentException("dataPointDurationMillis must be positive");
		if (allowableStandardDeviationForEquality < 0)
			throw new IllegalArgumentException(
					"allowableStandardDeviationForEquality must not be negative");

		this.dataPointDurationMillis = dataPointDurationMillis;
		this.allowableStandardDeviationForEquality = allowableStandardDeviationForEquality;
		this.startTimeMillis = startTimeMillis;
		this.sensorEvent = sensorEvent;

	}

	/**
	 * Construct a new CycleNode from a sensor event. This used during cycle
	 * creation.
	 * 
	 * @param sensorEvent
	 */
	public CycleNode(SensorEvent sensorEvent) {
		if (sensorEvent == null)
			throw new IllegalArgumentException("SensorEvent must not be null");

		this.startTimeMillis = sensorEvent.getTimestamp();
		this.dataPointDurationMillis = defaultDataPointDurationMillis;
		this.allowableStandardDeviationForEquality = allowableStandardDeviationForEquality;
		this.sensorEvent = sensorEvent;
	}

	/**
	 * Merge information from the given cycle node into this cycle node. Use my
	 * start time as the new node's start time. Add the duration time from both
	 * events.
	 * 
	 * @param nodeToMergeWith
	 * @return a new node with accumulated "time at data point"
	 */
	public CycleNode merge(CycleNode nodeToMergeWith) {
		if (nodeToMergeWith == null)
			throw new IllegalArgumentException("nodeToMergeWith must not be null");

		CycleNode newCycleNode = new CycleNode(nodeToMergeWith.sensorEvent,
				this.getStartTimeMillis(),
				this.defaultDataPointDurationMillis + nodeToMergeWith.dataPointDurationMillis);
		return newCycleNode;

//		this.startTimeMillis = nodeToMergeWith.getTimestamp();
//		this.dataPointDurationMillis = this.defaultDataPointDurationMillis
//				+ nodeToMergeWith.dataPointDurationMillis;
//		this.allowableStandardDeviationForEquality = allowableStandardDeviationForEquality;
//		this.sensorEvent = sensorEvent;
	}

	private CycleNode(SensorEvent sensorEvent, long startTimeMillis, long dataPointDurationMillis) {
		this(sensorEvent, startTimeMillis, dataPointDurationMillis,
				allowableStandardDeviationForEquality);
	}

	private double getStandardDeviation(double mean, double value) {
		return Utils.getStandardDeviation(mean, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sensorEvent == null) ? 0 : sensorEvent.hashCode());
		return result;
	}

	/**
	 * Return true if their data point measurements are statistically equal (ie
	 * equal within a specified standard deviation).
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CycleNode other = (CycleNode) obj;

		double difference = sensorEvent.getSensor().getDifferenceBetweenReadings(sensorEvent,
				other.getSensorEvent());
		if (Utils.getStandardDeviation(difference) > allowableStandardDeviationForEquality) {
			logger.info(
					"this cycleNode IS NOT equal to the next cycleNode [ in terms of data point difference]");
			return false;
		} else {
			logger.info(
					"this cycleNode IS equal to the next cycleNode [ in terms of data point difference]");
			return true;
		}
	}

	/**
	 * @return the sensorEvent
	 */
	public SensorEvent getSensorEvent() {
		return sensorEvent;
	}

	/**
	 * @return the dataPointDurationMillis
	 */
	public long getDataPointDurationMillis() {
		return dataPointDurationMillis;
	}

	/**
	 * @return the startTimeMillis
	 */
	public long getStartTimeMillis() {
		return startTimeMillis;
	}

	/**
	 * @return the allowableStandardDeviationForEquality
	 */
	public static double getAllowableStandardDeviationForEquality() {
		return allowableStandardDeviationForEquality;
	}

	@Override
	public String toString() {
		return "CycleNode [sensorEvent=" + sensorEvent + ", dataPointDurationMillis="
				+ dataPointDurationMillis + ", startTimeMillis=" + startTimeMillis + "]";
	}

	/**
	 * @return the defaultDataPointDurationMillis
	 */
	public static long getDefaultDataPointDurationMillis() {
		return defaultDataPointDurationMillis;
	}

	/**
	 * @param sensorEvent the sensorEvent to set
	 */
	public void setSensorEvent(SensorEvent sensorEvent) {
		this.sensorEvent = sensorEvent;
	}

	/**
	 * @param dataPointDurationMillis the dataPointDurationMillis to set
	 */
	public void setDataPointDurationMillis(long dataPointDurationMillis) {
		this.dataPointDurationMillis = dataPointDurationMillis;
	}

	/**
	 * @param startTimeMillis the startTimeMillis to set
	 */
	public void setStartTimeMillis(long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
	}

	/**
	 * @param allowableStandardDeviationForEquality the
	 *                                              allowableStandardDeviationForEquality
	 *                                              to set
	 */
	public static void setAllowableStandardDeviationForEquality(
			double allowableStandardDeviationForEquality) {
		CycleNode.allowableStandardDeviationForEquality = allowableStandardDeviationForEquality;
	}

}
