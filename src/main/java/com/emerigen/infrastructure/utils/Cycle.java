/**
 * 
 */
package com.emerigen.infrastructure.utils;

import java.time.Duration;

/**
 * @author Larry
 *
 */
public class Cycle<T> {

	private CircularList<CycleNode> cycle;
	private long startTime;
	private Duration cycleDuration;
	private float stdDeviation;

	public Cycle(long startTime, Duration cycleDuration, float stdDeviation) {
		if (startTime < 0)
			throw new IllegalArgumentException("startTime must be zero or more");
		if (cycleDuration == null)
			throw new IllegalArgumentException("cycleDuration must not be null");
		if (stdDeviation < 0)
			throw new IllegalArgumentException("stdDeviation must be positive");

		this.cycleDuration = cycleDuration;
		this.startTime = startTime;
		this.stdDeviation = stdDeviation; // Standard Deviation for equality purposes
	}

//	public float getDistanceBetweenNodes(CycleNode node1, CycleNode node2) {
//		return Math.abs(startTime)
//	}

}
