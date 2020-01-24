package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.utils.EmerigenProperties;

public class EmerigenSensorEventListenerTest {
	private final int minDelayBetweenReadingsMillis = Integer.parseInt(EmerigenProperties
			.getInstance().getValue("sensor.default.minimum.delay.between.readings.millis"));

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void gvenValidSensorListenerRegistered_whenOnPauseThenOnResumeInvoked_thenRegistrationCorrect()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
		Sensor tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

		SensorEventListener listener = new EmerigenSensorEventListener();

		sensorManager.registerListenerForSensorWithFrequency(listener, accSensor,
				Sensor.DELAY_NORMAL);

		sensorManager.registerListenerForSensorWithFrequency(listener, hrSensor,
				Sensor.DELAY_NORMAL);

		sensorManager.registerListenerForSensorWithFrequency(listener, tempSensor,
				Sensor.DELAY_NORMAL);

		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, accSensor)).isTrue();
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, hrSensor)).isTrue();
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, tempSensor)).isTrue();

		listener.onPause();

		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, accSensor)).isFalse();
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, hrSensor)).isFalse();
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, tempSensor)).isFalse();

		listener.onResume();
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, accSensor)).isTrue();
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, hrSensor)).isTrue();
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, tempSensor)).isTrue();

	}

	@Test
	public final void testOnCreate() {
		// TODO onCreate when known if listener owns or "Activity" owns in android
	}

}
