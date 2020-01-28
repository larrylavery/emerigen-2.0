package com.emerigen.infrastructure.utils;

import java.time.Duration;
import java.time.Instant;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * @IDEA Cycles/Nodes/Fields with standard deviation-based equality
 * 
 * @author Larry
 *
 * @param <T>
 */
public class CycleNode<T> {

	/**
	 * The event that defines this node. It may be a GPS, heart rate, etc... If GPS
	 * then the event defines the GPS coordinates of the location represented by
	 * this node. If heart rate, the event represents the heart rate at a point in
	 * time.
	 */
	private SensorEvent sensorEvent;

	/**
	 * The length of time that the data point [measurement] (as given by the sensor
	 * event) is valid. If the event is a GPS measurement, then the
	 * dataPointDuration is the length of time the GPS coordinates did not
	 * significantly change (ie how long the user stayed at [visited] this location.
	 * 
	 * For a heart rate sensor, it represents the lengh of time the heart rate
	 * stayed at the given heart rate (plus or minus the standard deviation of
	 * course.
	 */
	private Duration dataPointDuration;

	/**
	 * The start time that this dataPoint was encountered.
	 */
	private Instant startTime;

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
	 * be less fuzzy. TODO - should there be field-specific std deviation or
	 * node-specific deviation? Field-specific fuzziness supports different
	 * destinations for lunch while time frames are much more strict.
	 * 
	 * 
	 * Fuzziness types: Cycle, Node, node fields. Hierarchy: Cycle provides default
	 * for Nodes and node fields if not specified there. Node overrides Cycle and
	 * provides default for node fields. Field override Node and Cycle to provide
	 * field-specific standard deviation variance.
	 * 
	 * TODO implement Node-specific std deviation for now. add Cycle/Field later
	 */
	private static double maxStandardDeviationForCompares = Double.parseDouble(
			EmerigenProperties.getInstance().getValue("cycle.max.std.deviation.for.equality"));

	private static final Logger logger = Logger.getLogger(CycleNode.class);

	private CycleNode(SensorEvent sensorEvent, Instant startTime, Duration dataPointDuration,
			double maxStandardDeviationForCompares) {

		// Validate parms
		if (sensorEvent == null)
			throw new IllegalArgumentException("SensorEvent must not be null");
		if (startTime == null)
			throw new IllegalArgumentException("startTime must not be null");
		if (dataPointDuration == null)
			throw new IllegalArgumentException("dataPointDuration must not be null");
		if (maxStandardDeviationForCompares < 0)
			throw new IllegalArgumentException(
					"maxStandardDeviationForCompares must not be negative");

		this.dataPointDuration = dataPointDuration;
		this.maxStandardDeviationForCompares = maxStandardDeviationForCompares;
		this.startTime = startTime;
		this.sensorEvent = sensorEvent;

	}

	private CycleNode(SensorEvent sensorEvent, Instant startTime, Duration dataPointDuration) {
		this(sensorEvent, startTime, dataPointDuration, maxStandardDeviationForCompares);
	}

}
