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

	@Override
	protected boolean significantChangeHasOccurred(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {

		double gpsLocationDifferenceInMiles = previousSensorEvent.getSensor()
				.getDifferenceBetweenReadings(previousSensorEvent, currentSensorEvent);
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
