package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.Test;

import com.emerigen.infrastructure.utils.EmerigenProperties;

public class EmerigenSensorEventListenerTest {
	private final int minDelayBetweenReadingsMillis = Integer.parseInt(EmerigenProperties
			.getInstance().getValue("sensor.default.minimum.delay.between.readings.millis"));

	@Test
	public void givenValidTransitionPatternRecognizer_whenRegistered_thenThePatternRecognizerReceivesAllEvents()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenValidCyclePatternRecognizer_whenRegistered_thenThePatternRecognizerReceivesAllEvents()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenCycleWithInvalidSensorType_whenRegistered_thenIllegalArgumentException()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenOneCycleAndOneTransitionPatternRecognizers_whenRegistered_thenBothatternRecognizersReceivesAllEvents()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenMultipleCyclePRsAndOneTransitionPatternRecognizer_whenRegistered_thenAllReceiveAllEvents()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenMultiplePRs_whenRegistered_thenTheyReceiveAllEventsAfterConfiguredDelayTime()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenMultiplePRs_whenRegistered_thenTheyReceiveAllEventsThatAreSignificantlyDifferent()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenPRsRequestingInsignificantChanges_whenRegistered_thenTheyReceiveAllEventsIncludingInsignificantChanges()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenPRsRequestingNoDelayDuration_whenRegistered_thenTheyReceiveAllEventsWithoutDelayinserted()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenPRsRequestingNoDelayOrSignificantDifferent_whenRegistered_thenTheyReceiveAllEventsWithoutDelayOrSignificanceChecked()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenValidSensorListenerRegistered_whenOnPauseThenOnResumeInvoked_thenRegistrationCorrect()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();

		listener.onPause();
		assertThat(sensorManager.listenerIsRegisteredToAnySensor(listener)).isFalse();

		listener.onResume();
		assertThat(sensorManager.listenerIsRegisteredToAnySensor(listener)).isTrue();

	}

}
