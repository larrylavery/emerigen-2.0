package com.emerigen.infrastructure.utils;

public class Utils {

	// This the default if one is not specified
	private static double allowableStandardDeviationForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.std.deviation.for.equality"));

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
		if (stdDeviation < allowableStandardDeviationForEquality)
			return true;
		else
			return false;
	}

	public static final boolean equals(double firstValue, double secondValue) {
		return Utils.equals(firstValue, secondValue,
				allowableStandardDeviationForEquality);
	}

	public static final boolean equals(float firstValue, float secondValue,
			double allowableDeviation) {
		return Utils.equals((double) firstValue, (double) secondValue,
				allowableDeviation);
	}

	public static final boolean equals(float firstValue, float secondValue) {
		return Utils.equals(firstValue, secondValue,
				allowableStandardDeviationForEquality);
	}

	public static final boolean equals(int firstValue, int secondValue,
			double allowableDeviation) {
		return Utils.equals((double) firstValue, (double) secondValue,
				allowableDeviation);
	}

	public static final boolean equals(int firstValue, int secondValue) {
		return Utils.equals(firstValue, secondValue,
				allowableStandardDeviationForEquality);
	}

	public static final boolean equals(long firstValue, long secondValue) {
		return Utils.equals((double) firstValue, (double) secondValue,
				allowableStandardDeviationForEquality);
	}

	/**
	 * 
	 * @param mean
	 * @param value
	 * @return the standard deviation for the mean and one other value
	 */
	public static final double getStandardDeviation(int mean, int value) {
		return Utils.getStandardDeviation((double) mean, (double) value);
	}

	public static final double getStandardDeviation(double mean, double value) {
		return Math.sqrt(Math.pow((value - mean), 2) / 2.0);
	}

	/**
	 * Calculate the standard deviation given the difference between the mean and a
	 * value.
	 * 
	 * @param difference the difference between the mean and the value
	 * @return
	 */
	public static final double getStandardDeviation(double difference) {
		return Math.sqrt(Math.pow(difference, 2) / 2.0);
	}

}
