package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
			System.out.println("invoked count is " + invokedCount);
			return new ArrayList<Prediction>();
		}

	}

	private final int minDelayBetweenReadingsMillis = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"));

	@Test
	public void givenValidPatternRecognizers_whenRegistered_thenThePatternRecognizerReceivesAllEvents()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		SensorEventListener listener = new EmerigenSensorEventListener();
		SensorEventListener listener2 = new EventListener();
		float[] values = { 1.2f };

		sensorManager.registerListenerForSensorWithFrequency(listener2, sensor, 1);
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener2, sensor))
				.isTrue();

		listener.onSensorChanged(new SensorEvent(sensor, values));
		assertThat(invokedCount).isEqualTo(1);

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
	public void givenOneCycleAndOneTransitionPatternRecognizers_whenRegistered_thenBothPatternRecognizersReceivesAllEvents()
			throws Exception {
		SensorManager sm = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		SensorEventListener listener2 = new EventListener();
		sm.registerListenerForSensorWithFrequency(listener2, sensor, 2);
		float[] values = { 1.2f, 2.3f };
		float[] values2 = { 11.2f, 12.3f };

		SensorEvent se = new SensorEvent(sensor, values);
		SensorEvent se2 = new SensorEvent(sensor, values2);

		listener.onSensorChanged(se);
		listener.onSensorChanged(se2);
		assertThat(invokedCount).isEqualTo(2);
	}

	@Test
	public void givenMultiplePRs_whenRegistered_thenTheyReceiveAllEventsAfterConfiguredDelayTime()
			throws Exception {
		fail("Not implemented yet");
	}

	@Test
	public void givenMultiplePRs_whenRegistered_thenTheyReceiveAllEventsThatAreSignificantlyDifferent()
			throws Exception {
		SensorManager sm = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		SensorEventListener listener2 = new EventListener();
		sm.registerListenerForSensorWithFrequency(listener2, sensor, 2);
		float[] values = { 1.2f, 2.3f };
		float[] values2 = { 11.2f, 12.3f };

		SensorEvent se = new SensorEvent(sensor, values);
		SensorEvent se2 = new SensorEvent(sensor, values2);

		listener.onSensorChanged(se);
		listener.onSensorChanged(se2);
		assertThat(invokedCount).isEqualTo(2);
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
		SensorManager sm = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		assertThat(sm.getRegistrationsForSensor(sensor).size()).isEqualTo(2);

		listener.onPause();
		assertThat(sm.getRegistrationsForSensor(sensor)).isNull();

		listener.onResume();
		assertThat(sm.getRegistrationsForSensor(sensor).size()).isEqualTo(2);

	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		SensorManager.getInstance().reset();
	}

	@After
	public void tearDown() throws Exception {
	}

}
