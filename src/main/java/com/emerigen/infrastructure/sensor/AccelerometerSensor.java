package com.emerigen.infrastructure.sensor;

public class AccelerometerSensor extends Sensor {

	public AccelerometerSensor(int reportingMode, int minimumDelay, boolean isWakeUpSensor) {
		super(Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE, reportingMode, minimumDelay, isWakeUpSensor);
	}

	public AccelerometerSensor(int sensorLocation, int reportingMode, int minimumDelay, boolean isWakeUpSensor) {
		super(Sensor.TYPE_ACCELEROMETER, sensorLocation, reportingMode, minimumDelay, isWakeUpSensor);
	}

	public AccelerometerSensor(int sensorType, int sensorLocation, int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
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

}
