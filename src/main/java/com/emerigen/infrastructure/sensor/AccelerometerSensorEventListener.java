package com.emerigen.infrastructure.sensor;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.utils.EmerigenProperties;

public class AccelerometerSensorEventListener extends EmerigenSensorEventListener {

	private static final Logger logger = Logger.getLogger(AccelerometerSensor.class);

	private static final float SHAKE_THRESHOLD = Float.parseFloat(EmerigenProperties.getInstance()
			.getValue("sensor.accelerometer.shake.threshold.millis"));

	public AccelerometerSensorEventListener() {
	}

	/**
	 * For accelerometer there is no difference (distance) between readings. So the
	 * distance is not important. We care about the speed of the device since the
	 * last reading.
	 * 
	 * If the speed is currently faster than a "shake threshold", it is significant.
	 * Even if the speed was slower or faster than the last speed it is not
	 * important. Only if speed since last measurement >= some threshold.
	 * 
	 */

	@Override
	protected boolean significantChangeHasOccurred(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {

		// Calculate the device's speed
		double speed = currentSensorEvent.getSensor()
				.getDifferenceBetweenReadings(previousSensorEvent, currentSensorEvent);

		if (speed > SHAKE_THRESHOLD) {
			logger.info("The speed  (" + speed + ") exceeds the 'shake threshold' of "
					+ SHAKE_THRESHOLD);
			return true;
		}

		logger.info("The speed (" + speed + ") did not exceed the 'shake threshold' of "
				+ SHAKE_THRESHOLD);
		return false;
	}

}
