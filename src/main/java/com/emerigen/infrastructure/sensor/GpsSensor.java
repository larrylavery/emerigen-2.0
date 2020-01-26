package com.emerigen.infrastructure.sensor;

public class GpsSensor extends Sensor {

	public GpsSensor(int reportingMode, int minimumDelay, boolean isWakeUpSensor) {
		super(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE, reportingMode, minimumDelay, isWakeUpSensor);
	}

	public GpsSensor(int sensorLocation, int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_GPS, sensorLocation, reportingMode, minimumDelay, isWakeUpSensor);
	}

	public GpsSensor(int sensorType, int sensorLocation, int reportingMode, int minimumDelay,
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

	@Override
	public boolean isActivated() {
		// TODO Add code to activate the heart rate sensor

		// Return true until activate code implemented
		return super.isActivated();
	}

}
