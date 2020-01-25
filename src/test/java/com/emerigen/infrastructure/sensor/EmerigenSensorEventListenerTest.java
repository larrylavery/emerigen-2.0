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
		SensorEventListener listener = new EmerigenSensorEventListener();

		listener.onPause();
		assertThat(sensorManager.listenerIsRegisteredToAnySensor(listener)).isFalse();

		listener.onResume();
		assertThat(sensorManager.listenerIsRegisteredToAnySensor(listener)).isTrue();

	}

	@Test
	public final void testOnCreate() {
		// TODO onCreate when known if listener owns or "Activity" owns in android
	}

}
