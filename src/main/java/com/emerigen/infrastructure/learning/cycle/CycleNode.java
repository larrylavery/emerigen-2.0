package com.emerigen.infrastructure.learning.cycle;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.utils.EmerigenProperties;

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
	 * dataPointDurationNano is the length of time the GPS coordinates did not
	 * significantly change (ie how long the user stayed at [visited] this location.
	 * 
	 * For a heart rate sensor, it represents the lengh of time the heart rate
	 * stayed at the given heart rate (plus or minus the standard deviation of
	 * course.
	 */
	private long dataPointDurationNano;

	/**
	 * The Cycle that I belong to
	 */
	private Cycle myCycle;

	/**
	 * The offset, from the cycle start time, that this dataPoint was encountered,
	 */
	private long startTimeOffsetNano;

	/**
	 * The standard deviation for equality is used during comparisons of data in
	 * this class. For example, if two field values are within this maximum
	 * allowable standard deviation then they are considered equal.
	 * 
	 * This field enables "Cycle Node", and overall "Cycle" fuzziness" meaning that
	 * the individual nodes will match even when, in the case of GPS, the data
	 * points do not exactly match previously learned cycle nodes. Larger values
	 * increase fuzziness whereas smaller values enforce more "strict" adherence to
	 * the learned cycle.
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
	private static double allowablePercentDifferenceForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.percent.difference.for.equality"));

	private static long defaultCycleNodeDurationNano = Long.parseLong(EmerigenProperties
			.getInstance().getValue("cycle.default.data.point.duration.nano"));

	private static final Logger logger = Logger.getLogger(CycleNode.class);

	protected double probability;

	/**
	 * 
	 * @param myCycle
	 * @param sensorEvent
	 * @param dataPointDurationNano
	 */
	public CycleNode(Cycle myCycle, SensorEvent sensorEvent, long dataPointDurationNano) {
		if (myCycle == null)
			throw new IllegalArgumentException("myCycle must not be null");
		if (sensorEvent == null)
			throw new IllegalArgumentException("SensorEvent must not be null");
		if (dataPointDurationNano < 0)
			throw new IllegalArgumentException("dataPointDurationNano must be positive");

		this.myCycle = myCycle;
		this.dataPointDurationNano = dataPointDurationNano;
		this.startTimeOffsetNano = getTimeOffset(sensorEvent.getTimestamp());
		this.sensorEvent = sensorEvent;
	}

	public CycleNode(Cycle myCycle, SensorEvent sensorEvent) {
		this(myCycle, sensorEvent, defaultCycleNodeDurationNano);
	}

	public CycleNode() {
	}

	/**
	 * Merge information from the given cycle node into this cycle node. Use my
	 * start time offset as the new node's start time offset. Add the duration time
	 * from both events.
	 * 
	 * @param nodeToMergeWith
	 * @return a new node with accumulated "time at data point"
	 */
	public CycleNode merge(CycleNode nodeToMergeWith) {
		if (nodeToMergeWith == null)
			throw new IllegalArgumentException("nodeToMergeWith must not be null");
		if (nodeToMergeWith.myCycle != myCycle)
			throw new IllegalArgumentException(
					"Both nodes must be part of the same cycle");

		CycleNode newCycleNode = new CycleNode(myCycle, nodeToMergeWith.getSensorEvent(),
				this.dataPointDurationNano + nodeToMergeWith.dataPointDurationNano);
		return newCycleNode;
	}

	public long getTimeOffset(long timestamp) {
		return 1;
//		return timestamp - myCycle.getCycleStartTimeNano();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (dataPointDurationNano ^ (dataPointDurationNano >>> 32));
		long temp;
		temp = Double.doubleToLongBits(probability);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((sensorEvent == null) ? 0 : sensorEvent.hashCode());
		result = prime * result
				+ (int) (startTimeOffsetNano ^ (startTimeOffsetNano >>> 32));
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
		CycleNode other = (CycleNode) obj;
		if (dataPointDurationNano != other.dataPointDurationNano)
			return false;
		if (Double.doubleToLongBits(probability) != Double
				.doubleToLongBits(other.probability))
			return false;
		if (sensorEvent == null) {
			if (other.sensorEvent != null)
				return false;
		} else if (!sensorEvent.equals(other.sensorEvent))
			return false;
		if (startTimeOffsetNano != other.startTimeOffsetNano)
			return false;
		return true;
	}

	/**
	 * @return the dataPointDurationNano
	 */
	public long getDataPointDurationNano() {
		return dataPointDurationNano;
	}

	/**
	 * @param dataPointDurationNano the dataPointDurationNano to set
	 */
	public void setDataPointDurationNano(long dataPointDurationNano) {
		if (dataPointDurationNano <= 0)
			throw new IllegalArgumentException("data point duration must be positive");
		this.dataPointDurationNano = dataPointDurationNano;
	}

	/**
	 * @return the sensorEvent
	 */
	public SensorEvent getSensorEvent() {
		return sensorEvent;
	}

	@Override
	public String toString() {
		return "CycleNode [sensorEvent=" + sensorEvent + ", dataPointDurationNano="
				+ dataPointDurationNano + ", startTimeOffsetNano=" + startTimeOffsetNano
				+ ", probability=" + probability + "]";
	}

	/**
	 * @return the startTimeOffsetNano
	 */
	public long getStartTimeOffsetNano() {
		return startTimeOffsetNano;
	}

	/**
	 * @return the myCycle
	 */
	public Cycle getMyCycle() {
		return myCycle;
	}

	/**
	 * @param myCycle the myCycle to set
	 */
	public void setMyCycle(Cycle myCycle) {
		this.myCycle = myCycle;
	}

	/**
	 * @return the allowableStandardDeviationForEquality
	 */
	public static double getAllowablePercentDifferenceForEquality() {
		return allowablePercentDifferenceForEquality;
	}

	/**
	 * @param allowableStandardDeviationForEquality the
	 *                                              allowableStandardDeviationForEquality
	 *                                              to set
	 */
	public static void setAllowablePercentDifferenceForEquality(
			double allowablePercentDifferenceForEquality) {
		CycleNode.allowablePercentDifferenceForEquality = allowablePercentDifferenceForEquality;
	}

	/**
	 * @return the defaultCycleNodeDurationNano
	 */
	public static long getDefaultCycleNodeDurationNano() {
		return defaultCycleNodeDurationNano;
	}

	/**
	 * @param defaultCycleNodeDurationNano the defaultCycleNodeDurationNano to set
	 */
	public static void setDefaultCycleNodeDurationNano(
			long defaultCycleNodeDurationNano) {
		CycleNode.defaultCycleNodeDurationNano = defaultCycleNodeDurationNano;
	}

	/**
	 * @param sensorEvent the sensorEvent to set
	 */
	public void setSensorEvent(SensorEvent sensorEvent) {
		this.sensorEvent = sensorEvent;
	}

	/**
	 * @param startTimeOffsetNano the startTimeOffsetNano to set
	 */
	public void setStartTimeOffsetNano(long startTimeOffsetNano) {
		this.startTimeOffsetNano = startTimeOffsetNano;
	}

	/**
	 * @return the probability
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * @param probability the probability to set
	 */
	public void setProbability(double probability) {
		this.probability = probability;
	}

}