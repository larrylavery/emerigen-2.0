package com.emerigen.infrastructure.sensor;

public class HeartRateSensor extends Sensor {

	public HeartRateSensor(int reportingMode, int minimumDelay, boolean isWakeUpSensor) {
		super(Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE, reportingMode, minimumDelay, isWakeUpSensor);
	}

	public HeartRateSensor(int sensorLocation, int reportingMode, int minimumDelay, boolean isWakeUpSensor) {
		super(Sensor.TYPE_ACCELEROMETER, sensorLocation, reportingMode, minimumDelay, isWakeUpSensor);
	}

	public HeartRateSensor(int sensorType, int sensorLocation, int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(sensorType, sensorLocation, reportingMode, minimumDelay, isWakeUpSensor);
	}

	@Override
	public boolean activate() {
		super.activate();

		// TODO Figure out how should the accelerometer sensor activate
		return true;
	}

	@Override
	public boolean deactivate() {
		super.deactivate();

		// TODO Figure out how should the accelerometer sensor deactivate
		return true;
	}

	public boolean isActivated() {
		// TODO Add code to activate the heart rate sensor

		// Return true until activate code implemented
		return super.isActivated();
	}

}
