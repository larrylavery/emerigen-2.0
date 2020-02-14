package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class GpsSensorTest {

	@Test
	public final void givenTwoCloseGpsSensorReadings_whenDistanceCalculated_thenItIsLessThanRequired() {
		SensorManager sensorManager = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();
		GpsSensorEventListener gpsListener = new GpsSensorEventListener();
		Sensor gpsSensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		SensorManager.getInstance().registerListenerForSensor(gpsListener, gpsSensor);

		float[] firstSensorReading = { 29.738547f, -98.687334f }; // home address
		float[] secondSensorReading = { 29.591613f, -98.596793f };
		double expectedDistance = 11.52; // 18.53 Kilometers, 11.52 miles

		SensorEvent event1 = new SensorEvent(gpsSensor, firstSensorReading);
		SensorEvent event2 = new SensorEvent(gpsSensor, secondSensorReading);
		double distance = gpsSensor.getDifferenceBetweenReadings(event1, event2);
		double distanceError = Math.abs(distance - expectedDistance);

		// Allow the distance to be +- 0.5 kilometers
		assertThat(distanceError < 0.1);
	}

}
