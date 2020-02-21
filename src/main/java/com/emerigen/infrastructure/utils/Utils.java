package com.emerigen.infrastructure.utils;

public class Utils {

	// This the default if one is not specified
	private static double allowablePercentDifferenceForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.percent.difference.for.equality"));

	/**
	 * Compare two values allowing them to deviate by a default percent difference
	 * deviation. Create methods for all number types.
	 * 
	 * @param firstValue
	 * @param secondValue
	 * @return true if the values are within the allowable percent difference
	 */
	public static final boolean equals(float firstValue, float secondValue) {
		return Utils.equals(firstValue, secondValue,
				allowablePercentDifferenceForEquality);
	}

	public static final boolean equals(int firstValue, int secondValue) {
		return Utils.equals(firstValue, secondValue,
				allowablePercentDifferenceForEquality);
	}

	public static final boolean equals(long firstValue, long secondValue) {
		return Utils.equals(firstValue, secondValue,
				allowablePercentDifferenceForEquality);
	}

	public static double getPercentDifference(double i, double d) {
		double percentDifference = (Math.abs(i - d) / i);
		return percentDifference;
	}

	public static boolean equals(double num1, double num2) {
		double percentDifference = getPercentDifference(num1, num2);
		return percentDifference <= allowablePercentDifferenceForEquality;
	}

	public static boolean equals(double num1, double num2, double allowableDifference) {
		double percentDifference = Math.abs(getPercentDifference(num1, num2));
		return percentDifference <= allowablePercentDifferenceForEquality;
	}

	public static void allowDataUpdatesTimeToCatchUp() {
		try {
			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
					.getValue("couchbase.server.logging.catchup.timer")));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
