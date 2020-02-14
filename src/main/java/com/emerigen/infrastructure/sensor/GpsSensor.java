package com.emerigen.infrastructure.sensor;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.infrastructure.utils.Utils;

public class GpsSensor extends Sensor {

	private static final double GPS_DISTANCE_THRESHOLD = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("sensor.gps.distance.threshold.meters"));

	private static final Logger logger = Logger.getLogger(GpsSensor.class);

	public GpsSensor(int reportingMode, int minimumDelay, boolean isWakeUpSensor) {
		super(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE, reportingMode, minimumDelay,
				isWakeUpSensor);
	}

	public GpsSensor(int sensorLocation, int reportingMode, int minimumDelay,
			boolean isWakeUpSensor) {
		super(Sensor.TYPE_GPS, sensorLocation, reportingMode, minimumDelay,
				isWakeUpSensor);
	}

	public GpsSensor(int sensorType, int sensorLocation, int reportingMode,
			int minimumDelay, boolean isWakeUpSensor) {
		super(sensorType, sensorLocation, reportingMode, minimumDelay, isWakeUpSensor);
	}

	/**
	 * Calculate the distance between GPS coordinates using the Haversine algorithm
	 * 
	 * @param previousGpsCoordinates
	 * @param currentGpsCoordinates
	 * @return
	 */
	@Override
	public double getDifferenceBetweenReadings(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		if (currentSensorEvent == null)
			throw new IllegalArgumentException(
					"Current gps coordinates must not be null.");

		// No previous GPS coordinates, return distance 0.0
		if (previousSensorEvent == null)
			return 0.0;

		double initialLat = previousSensorEvent.getValues()[0],
				initialLong = previousSensorEvent.getValues()[1],
				finalLat = currentSensorEvent.getValues()[0],
				finalLong = currentSensorEvent.getValues()[1];
		float earthRadiusInMiles = 3958.8f; // Miles (Earth radius)
//		int earthRadiusInKilometers = 6371; // Kilometers (Earth radius)
		double distanceBetweenLat = toRadians(finalLat - initialLat);
		double distanceBetweenLong = toRadians(finalLong - initialLong);

		finalLat = toRadians(finalLat);
		initialLat = toRadians(initialLat);

		double a = Math.sin(distanceBetweenLat / 2) * Math.sin(distanceBetweenLat / 2)
				+ Math.sin(distanceBetweenLong / 2) * Math.sin(distanceBetweenLong / 2)
						* Math.cos(initialLat) * Math.cos(finalLat);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		logger.info("returning GPS distance of (" + earthRadiusInMiles * c + ")");
		return earthRadiusInMiles * c;

	}

	@Override
	public boolean equals(SensorEvent firstSensorEvent, SensorEvent secondSensorEvent) {
		if (firstSensorEvent == null)
			throw new IllegalArgumentException(
					"Current gps coordinates must not be null.");
		if (secondSensorEvent == null)
			throw new IllegalArgumentException(
					"Current gps coordinates must not be null.");

		boolean latitudeEquals = Utils.equals(firstSensorEvent.getValues()[0],
				secondSensorEvent.getValues()[0]);
		boolean longitudeEquals = Utils.equals(firstSensorEvent.getValues()[1],
				secondSensorEvent.getValues()[1]);
		if (latitudeEquals && longitudeEquals) {
			return true;
		} else {
			return false;
		}
	}

	private double toRadians(double deg) {
		return deg * (Math.PI / 180);
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
