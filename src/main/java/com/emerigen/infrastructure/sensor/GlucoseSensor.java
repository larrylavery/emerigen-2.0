package com.emerigen.infrastructure.sensor;

import com.emerigen.infrastructure.utils.Utils;

public class GlucoseSensor extends Sensor {

	public GlucoseSensor(int reportingMode, int minimumDelay, boolean isWakeUpSensor) {
		super(Sensor.TYPE_GLUCOSE, Sensor.LOCATION_MACHINE, reportingMode, minimumDelay,
				isWakeUpSensor);
	}

	public GlucoseSensor(int sensorLocation, int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_GLUCOSE, sensorLocation, reportingMode, minimumDelay, isWakeUpSensor);
	}

	public GlucoseSensor(int sensorType, int sensorLocation, int reportingMode, int minimumDelay,
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

	@Override
	public double getDifferenceBetweenReadings(SensorEvent firstSensorEvent,
			SensorEvent secondSensorEvent) {
		return secondSensorEvent.getValues()[0] - firstSensorEvent.getValues()[0];
	}

	@Override
	public boolean equals(SensorEvent firstSensorEvent, SensorEvent secondSensorEvent) {
		if (firstSensorEvent == null)
			throw new IllegalArgumentException("first event glucose not be null.");
		if (secondSensorEvent == null)
			throw new IllegalArgumentException("second glucose must not be null.");

		if (Utils.equals(firstSensorEvent.getValues()[0], secondSensorEvent.getValues()[0])) {
			return true;
		} else {
			return false;
		}
	}

}
