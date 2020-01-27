package com.emerigen.infrastructure.sensor;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.utils.EmerigenProperties;

public class AccelerometerSenorEventListener extends EmerigenSensorEventListener {

	private static final Logger logger = Logger.getLogger(AccelerometerSensor.class);

	private static final float SHAKE_THRESHOLD = Float.parseFloat(EmerigenProperties.getInstance()
			.getValue("sensor.accelerometer.shake.threshold.millis"));

	public AccelerometerSenorEventListener() {
	}

	@Override
	protected boolean significantChangeHasOccurred(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {

		// Retrieve the parameters from the sensor events
		float currX = currentSensorEvent.getValues()[0];
		float currY = currentSensorEvent.getValues()[1];
		float currZ = currentSensorEvent.getValues()[2];

		float lastX = previousSensorEvent.getValues()[0];
		float lastY = previousSensorEvent.getValues()[1];
		float lastZ = previousSensorEvent.getValues()[2];

		long elapseTime = Long.parseLong(currentSensorEvent.getTimestamp())
				- Long.parseLong(previousSensorEvent.getTimestamp());

		// Calculate the device's speed
		float speed = Math.abs(currX + currY + currZ - lastX - lastY - lastZ) / elapseTime * 1000;
		logger.info("The device speed is (" + speed + ")");
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
