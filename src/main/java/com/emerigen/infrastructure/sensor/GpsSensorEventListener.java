/**
 * 
 */
package com.emerigen.infrastructure.sensor;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.utils.EmerigenProperties;

/**
 * @author Larry
 *
 */
public class GpsSensorEventListener extends EmerigenSensorEventListener {

	private static final Logger logger = Logger.getLogger(GpsSensorEventListener.class);

	private float GPS_DIFFERENCE_THRESHOLD = Float.parseFloat(
			EmerigenProperties.getInstance().getValue("sensor.gps.difference.threshold.miles"));

	public GpsSensorEventListener() {
	}

	/**
	 * Calculate the distance between GPS coordinates using the Haversine algorithm
	 * 
	 * @param previousGpsCoordinates
	 * @param currentGpsCoordinates
	 * @return
	 */
	public double getDistanceBetweenGpsCoordinates(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		if (currentSensorEvent == null)
			throw new IllegalArgumentException("Current gps coordinates must not be null.");

		// No previous GPS coordinates, return distance 0.0
		if (previousSensorEvent == null)
			return 0.0;

		double initialLat = previousSensorEvent.getValues()[0],
				initialLong = previousSensorEvent.getValues()[1],
				finalLat = currentSensorEvent.getValues()[0],
				finalLong = currentSensorEvent.getValues()[1];
		float earthRadiusInMiles = 3958.8f; // Miles (Earth radius)
//		int earthRadiusInKilometers = 6371; // Kilometers (Earth radius)
		double distanceBetweenLat = toRadians(finalLat - initialLat);
		double distanceBetweenLong = toRadians(finalLong - initialLong);

		finalLat = toRadians(finalLat);
		initialLat = toRadians(initialLat);

		double a = Math.sin(distanceBetweenLat / 2) * Math.sin(distanceBetweenLat / 2)
				+ Math.sin(distanceBetweenLong / 2) * Math.sin(distanceBetweenLong / 2)
						* Math.cos(initialLat) * Math.cos(finalLat);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		logger.info("returning GPS distance of (" + earthRadiusInMiles * c + ")");
		return earthRadiusInMiles * c;

	}

	private double toRadians(double deg) {
		return deg * (Math.PI / 180);
	}

	@Override
	protected boolean significantChangeHasOccurred(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {

		double gpsLocationDifferenceInMiles = getDistanceBetweenGpsCoordinates(previousSensorEvent,
				currentSensorEvent);
		logger.info(
				"The GPS location difference, in miles, is (" + gpsLocationDifferenceInMiles + ")");

		if (gpsLocationDifferenceInMiles > GPS_DIFFERENCE_THRESHOLD) {
			logger.info("The difference in miles (" + gpsLocationDifferenceInMiles
					+ ") exceeds the threshold of " + GPS_DIFFERENCE_THRESHOLD);
			return true;
		} else
			return false;
	}

}
