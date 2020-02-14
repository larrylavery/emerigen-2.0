package com.emerigen.infrastructure.sensor;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.utils.EmerigenProperties;

public class HeartRateSensorEventListener extends EmerigenSensorEventListener {
	private static final Logger logger = Logger
			.getLogger(HeartRateSensorEventListener.class);
	private float HEARTRATE_DIFFERENCE_THRESHOLD = Float.parseFloat(EmerigenProperties
			.getInstance().getValue("sensor.heartrate.difference.threshold"));

	public HeartRateSensorEventListener() {
	}

	public boolean significantChangeHasOccurred(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {

		float heartRateDifference = currentSensorEvent.getValues()[0]
				- previousSensorEvent.getValues()[0];
		logger.info("The heart rate difference is (" + heartRateDifference + ")");

		if (heartRateDifference > HEARTRATE_DIFFERENCE_THRESHOLD) {
			logger.info("The difference (" + heartRateDifference
					+ ") exceeds the threshold of " + HEARTRATE_DIFFERENCE_THRESHOLD);
			return true;
		} else
			return false;
	}

}
