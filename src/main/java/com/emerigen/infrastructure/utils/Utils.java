package com.emerigen.infrastructure.utils;

public class Utils {

	/**
	 * Compare two values allowing them to deviate by up to the given standard
	 * deviation. Create methods for all number types.
	 * 
	 * @param firstValue
	 * @param secondValue
	 * @param allowableDeviation
	 * @return true if the values are within the allowable standard deviation of
	 *         each other.
	 */
	public static final boolean equals(double firstValue, double secondValue,
			double allowableDeviation) {

		double stdDeviation = getStandardDeviation(firstValue, secondValue);
		if (stdDeviation < allowableDeviation)
			return true;
		else
			return false;
	}

	public static final boolean equals(float firstValue, float secondValue,
			double allowableDeviation) {
		return equals((double) firstValue, (double) secondValue, allowableDeviation);
	}

	public static final boolean equals(int firstValue, int secondValue, double allowableDeviation) {
		return equals((double) firstValue, (double) secondValue, allowableDeviation);
	}

	/**
	 * 
	 * @param mean
	 * @param value
	 * @return the standard deviation for the mean and one other value
	 */
	public static final double getStandardDeviation(int mean, int value) {
		return getStandardDeviation((double) mean, (double) value);
	}

	public static final double getStandardDeviation(double mean, double value) {
		return Math.sqrt(Math.pow((value - mean), 2) / 2);
	}

}
