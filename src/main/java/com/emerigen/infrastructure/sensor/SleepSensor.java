package com.emerigen.infrastructure.sensor;

public class SleepSensor extends Sensor {

	public SleepSensor(int reportingMode, int minimumDelay, boolean isWakeUpSensor) {
		super(Sensor.TYPE_SLEEP, Sensor.LOCATION_WATCH, reportingMode, minimumDelay,
				isWakeUpSensor);
	}

	public SleepSensor(int sensorLocation, int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_SLEEP, sensorLocation, reportingMode, minimumDelay, isWakeUpSensor);
	}

	public SleepSensor(int sensorType, int sensorLocation, int reportingMode, int minimumDelay,
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
	public double getDifferenceBetweenReadings(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		// TODO Auto-generated method stub
		return 0;
	}

}
