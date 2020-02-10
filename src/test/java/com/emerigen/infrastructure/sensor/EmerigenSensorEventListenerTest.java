package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;

import org.junit.Test;

import com.emerigen.infrastructure.learning.CyclePatternRecognizer;
import com.emerigen.infrastructure.learning.DailyCycle;
import com.emerigen.infrastructure.learning.PatternRecognizer;
import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class EmerigenSensorEventListenerTest {

	int invokedCount = 0;

	public class EventListener implements SensorEventListener {

		@Override
		public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {
			invokedCount++;
			return null;
		}

	}

	private final int minDelayBetweenReadingsMillis = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"));

	@Test
	public void givenValidTransitionPatternRecognizer_whenRegistered_thenThePatternRecognizerReceivesAllEvents()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		SensorEventListener listener = new EmerigenSensorEventListener();
		SensorEventListener listener2 = new EventListener();
		float[] values = { 1.2f };

		sensorManager.registerListenerForSensorWithFrequency(listener2, sensor, 1);
//		TransitionPatternRecognizer tpr = new TransitionPatternRecognizer();
//		Sensor hrSensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_GPS,
//				Sensor.LOCATION_PHONE);
//
//		sensorManager.registerListenerForSensorWithFrequency(listener2, sensor,
//				minDelayBetweenReadingsMillis);
		assertThat(sensorManager.listenerIsRegisteredToAnySensor(listener2)).isTrue();
		listener.onSensorChanged(new SensorEvent(sensor, values));
		assertThat(invokedCount).isEqualTo(1);

	}

	@Test
	public void givenValidCyclePatternRecognizer_whenRegistered_thenThePatternRecognizerReceivesAllEvents()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenCycleWithInvalidSensorType_whenRegistered_thenIllegalArgumentException()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor accSensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		PatternRecognizer cpr = new CyclePatternRecognizer(
				new DailyCycle(accSensor.getType(), accSensor.getLocation()));

		sensorManager.registerListenerForSensorWithFrequency(cpr, accSensor,
				Sensor.DELAY_NORMAL);
		assertThat(sensorManager.listenerIsRegisteredToSensor(cpr, accSensor)).isTrue();
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

	@Test
	public void givenValidCyclePatternRecognizer_whenOnPauseThenOnResumeInvoked_thenCyclePatternRecognizersMatch()
			throws Exception {
		fail("not yet implemented");

		SensorManager sensorManager = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();

		listener.onPause();
		assertThat(sensorManager.listenerIsRegisteredToAnySensor(listener)).isFalse();

		listener.onResume();
		assertThat(sensorManager.listenerIsRegisteredToAnySensor(listener)).isTrue();

	}

}
