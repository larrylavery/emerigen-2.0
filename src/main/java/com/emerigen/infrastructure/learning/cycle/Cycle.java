/**
 * 
 */
package com.emerigen.infrastructure.learning.cycle;

import org.apache.log4j.Logger;

import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonIgnore;
import com.emerigen.infrastructure.utils.EmerigenProperties;

/**
 * @author Larry
 *
 */
public abstract class Cycle {

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
	private String cycleType;
	private int sensorLocation;
	private int sensorType;

	private double allowablePercentDifferenceForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.percent.difference.for.equality"));

	private static final Logger logger = Logger.getLogger(Cycle.class);

	public Cycle(int sensorType, int sensorLocation, String cycleType) {
		if (sensorType <= 0)
			throw new IllegalArgumentException("sensor type must be positive");
		if (sensorLocation <= 0)
			throw new IllegalArgumentException("sensor location must be positive");
		if (cycleType == null || cycleType.isEmpty())
			throw new IllegalArgumentException("cycleType must not be null or empty");

		this.sensorType = sensorType;
		this.cycleType = cycleType;
		this.sensorLocation = sensorLocation;
	}

	public Cycle(String cycleType) {
		this.cycleType = cycleType;
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
	 * @return the key for this cycle
	 */
	@JsonIgnore
	public String getKey() {
		return "" + sensorType + sensorLocation + getCycleType();
	}

	@Override
	public String toString() {
		return "Cycle [cycleType=" + cycleType + ", sensorLocation=" + sensorLocation
				+ ", sensorType=" + sensorType
				+ ", allowablePercentDifferenceForEquality="
				+ allowablePercentDifferenceForEquality + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(allowablePercentDifferenceForEquality);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((cycleType == null) ? 0 : cycleType.hashCode());
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
//		if (Double.doubleToLongBits(PercentDifferenceForEquality) != Double
//				.doubleToLongBits(other.StandardDeviationForEquality))
//			return false;
		if (cycleType == null) {
			if (other.cycleType != null)
				return false;
		} else if (!cycleType.equals(other.cycleType))
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

	public void setAllowablePercentDifferenceForEquality(
			double allowablePercentDifferenceForEquality) {
		this.allowablePercentDifferenceForEquality = allowablePercentDifferenceForEquality;
	}

	public double getAllowablePercentDifferenceForEquality() {
		return allowablePercentDifferenceForEquality;
	}

	/**
	 * @return the sensorLocation
	 */
	public int getSensorLocation() {
		return sensorLocation;
	}

	/**
	 * @param sensorLocation the sensorLocation to set
	 */
	public void setSensorLocation(int sensorLocation) {
		this.sensorLocation = sensorLocation;
	}

	/**
	 * @return the sensorType
	 */
	public int getSensorType() {
		return sensorType;
	}

	/**
	 * @param sensorType the sensorType to set
	 */
	public void setSensorType(int sensorType) {
		this.sensorType = sensorType;
	}

	/**
	 * @param cycleType the cycleType to set
	 */
	public void setCycleType(String cycleType) {
		this.cycleType = cycleType;
	}

}
