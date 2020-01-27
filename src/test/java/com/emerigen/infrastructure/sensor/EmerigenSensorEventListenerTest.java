package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.emerigen.infrastructure.utils.EmerigenProperties;

public class EmerigenSensorEventListenerTest {
	private final int minDelayBetweenReadingsMillis = Integer.parseInt(EmerigenProperties
			.getInstance().getValue("sensor.default.minimum.delay.between.readings.millis"));

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

}
