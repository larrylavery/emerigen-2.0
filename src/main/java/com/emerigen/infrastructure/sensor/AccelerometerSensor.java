package com.emerigen.infrastructure.sensor;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.utils.Utils;

public class AccelerometerSensor extends Sensor {

	private static final Logger logger = Logger.getLogger(AccelerometerSensor.class);

	public AccelerometerSensor(int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE, reportingMode,
				minimumDelay, isWakeUpSensor);
	}

	public AccelerometerSensor(int sensorLocation, int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_ACCELEROMETER, sensorLocation, reportingMode, minimumDelay,
				isWakeUpSensor);
	}

	public AccelerometerSensor(int sensorType, int sensorLocation, int reportingMode,
			int minimumDelay, boolean isWakeUpSensor) {
		super(sensorType, sensorLocation, reportingMode, minimumDelay, isWakeUpSensor);
	}

	@Override
	public boolean activate() {
		// TODO Figure out how should the accelerometer sensor activate
		return true;
	}

	@Override
	public boolean deactivate() {
		// TODO Figure out how should the accelerometer sensor deactivate
		return false;
	}

	/**
	 * Calculate the speed since the last reading
	 * 
	 * @param previousSensorEvent
	 * @param currentSensorEvent
	 * @return
	 */
	@Override
	public double getDifferenceBetweenReadings(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		if (currentSensorEvent == null)
			throw new IllegalArgumentException("Current Sensor event must not be null.");

		// Retrieve the parameters from the sensor events
		float currX = currentSensorEvent.getValues()[0];
		float currY = currentSensorEvent.getValues()[1];
		float currZ = currentSensorEvent.getValues()[2];

		float lastX = previousSensorEvent.getValues()[0];
		float lastY = previousSensorEvent.getValues()[1];
		float lastZ = previousSensorEvent.getValues()[2];

		long elapseTime = currentSensorEvent.getTimestamp()
				- previousSensorEvent.getTimestamp();

		// Calculate the device's speed
		double speed = Math.abs(currX + currY + currZ - lastX - lastY - lastZ)
				/ elapseTime * 1000;
		logger.info("The device speed is (" + speed + ")");
		return speed;
	}

	@Override
	public boolean equals(SensorEvent firstSensorEvent, SensorEvent secondSensorEvent) {
		if (firstSensorEvent == null)
			throw new IllegalArgumentException("first reading must not be null.");
		if (secondSensorEvent == null)
			throw new IllegalArgumentException("second reading must not be null.");

		boolean xEquals = Utils.equals(firstSensorEvent.getValues()[0],
				firstSensorEvent.getValues()[0]);
		boolean yEquals = Utils.equals(firstSensorEvent.getValues()[1],
				firstSensorEvent.getValues()[1]);
		boolean zEquals = Utils.equals(firstSensorEvent.getValues()[2],
				firstSensorEvent.getValues()[2]);
		if (xEquals && yEquals && zEquals) {
			return true;
		} else {
			return false;
		}
	}

}
