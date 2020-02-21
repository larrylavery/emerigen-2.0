package com.emerigen.infrastructure.sensor;

import com.emerigen.infrastructure.utils.Utils;

public class BloodPressureSensor extends Sensor {

	public BloodPressureSensor(int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_BLOOD_PRESSURE, Sensor.LOCATION_MACHINE, reportingMode,
				minimumDelay, isWakeUpSensor);
	}

	public BloodPressureSensor(int sensorLocation, int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_BLOOD_PRESSURE, sensorLocation, reportingMode, minimumDelay,
				isWakeUpSensor);
	}

	public BloodPressureSensor(int sensorType, int sensorLocation, int reportingMode,
			int minimumDelay, boolean isWakeUpSensor) {
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
	public double getDifferenceBetweenReadings(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		// TODO implement getDifferenceBetweenReadings
		return 0;
	}

	@Override
	public boolean equals(SensorEvent firstSensorEvent, SensorEvent secondSensorEvent) {
		if (firstSensorEvent == null)
			throw new IllegalArgumentException("first blood pressure must not be null.");
		if (secondSensorEvent == null)
			throw new IllegalArgumentException("second blood pressure must not be null.");

		boolean systolicEquals = Utils.equals(firstSensorEvent.getValues()[0],
				firstSensorEvent.getValues()[0]);
		boolean dyastolicEquals = Utils.equals(firstSensorEvent.getValues()[1],
				firstSensorEvent.getValues()[1]);
		if (systolicEquals && dyastolicEquals) {
			return true;
		} else {
			return false;
		}
	}

}
