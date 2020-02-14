package com.emerigen.infrastructure.sensor;

import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.infrastructure.utils.Utils;

public class HeartRateSensor extends Sensor {

	private long minimumDelayBetweenReadings = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"))
			* 1000000;

	private long minimumDelayBetweenReadingsForMe = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("sensor.heartrate.minimum.delay.between.readings.millis"))
			* 1000000;

	public HeartRateSensor(int sensorLocation, int reportingMode,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_HEART_RATE, sensorLocation, reportingMode, isWakeUpSensor);
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
			throw new IllegalArgumentException("first event heartrate not be null.");
		if (secondSensorEvent == null)
			throw new IllegalArgumentException("second heartrate must not be null.");

		if (Utils.equals(firstSensorEvent.getValues()[0],
				secondSensorEvent.getValues()[0])) {
			return true;
		} else {
			return false;
		}
	}

}
