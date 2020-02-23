package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.learning.cycle.Cycle;
import com.emerigen.infrastructure.learning.cycle.CyclePatternRecognizer;
import com.emerigen.infrastructure.learning.cycle.DailyCycle;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class EmerigenSensorEventListenerTest {

	int invokedCount = 0;
	private long minimumDelayBetweenReadings = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"))
			* 1000000;

	public class EventListener implements SensorEventListener {
		SensorEvent previousSensorEvent = null;

		@Override
		public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {
			long timestamp = System.currentTimeMillis();

			// Required elapse time has passed since last event?
			if (sensorEvent.getSensor().minimumDelayBetweenReadingsIsSatisfied(
					previousSensorEvent, sensorEvent)) {

				invokedCount++;
				System.out.println("invoked count is " + invokedCount);
			}
			previousSensorEvent = sensorEvent;
			return new ArrayList<Prediction>();
		}

	}

	@Test
	public void givenValidPatternRecognizers_whenRegistered_thenThePatternRecognizerReceivesAllEvents()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		SensorEventListener listener = new EmerigenSensorEventListener();
		SensorEventListener listener2 = new EventListener();
		float[] values = { 1.2f };

		sensorManager.registerListenerForSensor(listener2, sensor);
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

		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, accSensor,
				new PredictionService());

		sensorManager.registerListenerForSensor(cpr, accSensor);
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
		sm.registerListenerForSensor(listener2, sensor);
		float[] values = { 1.2f, 2.3f };
		float[] values2 = { 11.2f, 12.3f };

		SensorEvent se = new SensorEvent(sensor, values);
		SensorEvent se2 = new SensorEvent(sensor, values2);

		listener.onSensorChanged(se);
		se2.setTimestamp(se2.getTimestamp() + minimumDelayBetweenReadings);

		listener.onSensorChanged(se2);
		assertThat(invokedCount).isEqualTo(2);
	}

	@Test
	public void givenMultiplePRs_whenRegistered_thenTheyReceiveAllEventsAfterConfiguredDelayTime()
			throws Exception {
		SensorManager sm = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		SensorEventListener listener2 = new EventListener();
		sm.registerListenerForSensor(listener2, sensor);
		float[] values = { 1.2f, 2.3f };
		float[] values2 = { 11.2f, 12.3f };
		float[] values3 = { 211.2f, 212.3f };

		SensorEvent se = new SensorEvent(sensor, values);
		SensorEvent se2 = new SensorEvent(sensor, values2);
		se2.setTimestamp(se.getTimestamp());
		listener2.onSensorChanged(se);
		listener2.onSensorChanged(se2);
		se2.setTimestamp(se.getTimestamp());
		SensorEvent se3 = new SensorEvent(sensor, values3);
		se3.setTimestamp(se3.getTimestamp() + minimumDelayBetweenReadings);

		listener2.onSensorChanged(se3);
		assertThat(invokedCount).isEqualTo(2);
	}

	@Test
	public void givenMultiplePRs_whenRegistered_thenTheyReceiveAllEventsThatAreSignificantlyDifferent()
			throws Exception {
		SensorManager sm = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_WATCH);
		SensorEventListener listener2 = new EventListener();
		sm.registerListenerForSensor(listener2, sensor);
		float[] values = { 1.2f, 2.3f };
		float[] values2 = { 11.2f, 12.3f };

		SensorEvent se = new SensorEvent(sensor, values);
		SensorEvent se2 = new SensorEvent(sensor, values2);
		se2.setTimestamp(se2.getTimestamp() + minimumDelayBetweenReadings);

		listener.onSensorChanged(se);
		listener.onSensorChanged(se2);
		assertThat(invokedCount).isEqualTo(2);
	}

	@Test
	public void givenPRsRequestingNoDelayDuration_whenRegistered_thenTheyReceiveAllEventsWithoutDelayinserted()
			throws Exception {
		SensorManager sm = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_WATCH);
		SensorEventListener listener2 = new EventListener();
		sm.registerListenerForSensor(listener2, sensor);
		float[] values = { 1.2f, 2.3f };
		float[] values2 = { 11.2f, 12.3f };
		float[] values3 = { 111.2f, 112.3f };

		SensorEvent se = new SensorEvent(sensor, values);
		SensorEvent se2 = new SensorEvent(sensor, values2);
		se2.setTimestamp(se.getTimestamp() + minimumDelayBetweenReadings);
		SensorEvent se3 = new SensorEvent(sensor, values3);
		se3.setTimestamp(se2.getTimestamp() + minimumDelayBetweenReadings);

		listener.onSensorChanged(se);
		listener.onSensorChanged(se2);
		listener.onSensorChanged(se3);
		assertThat(invokedCount).isEqualTo(3);
	}

	@Test
	public void givenValidSensorListenerRegistered_whenOnPauseThenOnResumeInvoked_thenRegistrationCorrect()
			throws Exception {
//		SensorManager sm = SensorManager.getInstance();
		SensorManager.getInstance().reset();
		SensorEventListener listener = new EmerigenSensorEventListener();
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_WATCH);

		List<SensorEventListener> origListeners = SensorManager.getInstance()
				.getRegistrationsForSensor(sensor);
		int origSize = origListeners.size();
		assertThat(SensorManager.getInstance().getRegistrationsForSensor(sensor).size())
				.isEqualTo(6);

		listener.onPause();
		assertThat(SensorManager.getInstance().getRegistrationsForSensor(sensor).size())
				.isEqualTo(0);

		listener.onResume();
		assertThat(SensorManager.getInstance().getRegistrationsForSensor(sensor).size())
				.isEqualTo(origSize);

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
		invokedCount = 0;
	}

	@After
	public void tearDown() throws Exception {
	}

}
