package com.emerigen.infrastructure.sensor;

import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.infrastructure.utils.Utils;

public class TemperatureSensor extends Sensor {

	public TemperatureSensor(int sensorLocation, int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_ACCELEROMETER, sensorLocation, reportingMode, minimumDelay,
				isWakeUpSensor);
	}

	public TemperatureSensor(int sensorType, int sensorLocation, int reportingMode,
			int minimumDelay, boolean isWakeUpSensor) {
		super(sensorType, sensorLocation, reportingMode, minimumDelay, isWakeUpSensor);
	}

	public TemperatureSensor(int reportingMode, int minimumDelay, boolean isWakeUpSensor) {
		super(Sensor.TYPE_TEMPERATURE, Sensor.LOCATION_PHONE, reportingMode, minimumDelay,
				isWakeUpSensor);
	}

	private float previousTempurature;

	/**
	 * A temperature change of at least significantChangeThreshold causes an event
	 * to be published
	 */
	private float significantChangeThreshold = Float.parseFloat(EmerigenProperties.getInstance()
			.getValue("sensor.temperature.significant.change.threshold"));

	/**
	 * Use sensor-specific logic to access the current value of this h/w sensor.
	 * 
	 * @return the current temperature.
	 */
	public float getCurrentTemperature() {

		// TODO get current temperature from a temperature sensor
		return 0;
	}

	/**
	 * TODO Research how often this should occur is based on
	 * minimumReportingFrequency or aysynchronous if temperature sensor invokes a
	 * method on change.
	 * 
	 * @return true if the temperature has experienced a significant change,
	 *         otherwise false.
	 */
	public boolean hasSignificantChanges() {
		float temperatureDifference = (getCurrentTemperature() - previousTempurature);
		return Math.abs(temperatureDifference) >= significantChangeThreshold;
	}

	@Override
	public boolean activate() {
		// TODO How to activate this temperature sensor. Research
		return true;
	}

	@Override
	public boolean deactivate() {
		// TODO How to deactivate this temperature sensor. Research
		return true;
	}

	@Override
	public double getDifferenceBetweenReadings(SensorEvent firstSensorEvent,
			SensorEvent secondSensorEvent) {
		return secondSensorEvent.getValues()[0] - firstSensorEvent.getValues()[0];
	}

	@Override
	public boolean equals(SensorEvent firstSensorEvent, SensorEvent secondSensorEvent) {
		if (firstSensorEvent == null)
			throw new IllegalArgumentException("first event temperature not be null.");
		if (secondSensorEvent == null)
			throw new IllegalArgumentException("second temperature must not be null.");

		if (Utils.equals(firstSensorEvent.getValues()[0], secondSensorEvent.getValues()[0])) {
			return true;
		} else {
			return false;
		}
	}

}
