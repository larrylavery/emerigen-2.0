package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class SensorEventListenerTest {
	public static int eventListenerOnSensorChanged = 0;

	public static int eventListenerOnPause = 0;
	public static int eventListenerOnResume = 0;
	public static int eventListenerOnAccuracyChanged = 0;

	private final int minDelayBetweenReadingsMillis = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"));

	public class EventListener implements SensorEventListener {

		@Override
		public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {
			eventListenerOnSensorChanged++;
			return null;
		}

		@Override
		public void onPause() {
			eventListenerOnPause++;
		}

		@Override
		public void onResume() {
			eventListenerOnResume++;
		}
	}

	@Test
	public void givenTenHeartRateSensorReadings_whenFedIntoOnSensorChanged_thenTenSensorEventsLogged()
			throws Exception {
		/**
		 * ensure
		 */
		// Given
		// Remove all sensor related records prior to test or get false results
//		CouchbaseRepository.getInstance().removeAllDocuments("sensor-event");
//		CouchbaseRepository.getInstance().removeAllDocuments("transition");

		SensorManager sensorManager = SensorManager.getInstance();
		SensorEventListener listener = new EmerigenSensorEventListener();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_PHONE);

		// Create sensor mock with the sensor and listener that will receive events
		SensorMock sensorMock = new SensorMock(sensor, listener,
				minDelayBetweenReadingsMillis, 0);

		// Read from previously built file and feed events to the listener
		sensorMock.startGeneratingSensorEvents();

		// Verify that 10 HeartRate patterns were logged for those sensor events
		int patternCount = KnowledgeRepository.getInstance()
				.getSensorEventCountForSensorTypeAndLocation(sensor.getType(),
						sensor.getLocation());
		assertThat(patternCount).isGreaterThan(9);
	}

	@Test
	public void gvenValidHeartRateSensorListenerRegistered_whenExecutedMultipleTimes_thenOnSensorChangedThrottled()
			throws Exception {
		SensorEventListener listener = new EmerigenSensorEventListener();
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		SensorEventListener myListener = new EventListener();
		SensorManager.getInstance().registerListenerForSensor(myListener, sensor);
		// Create sensor mock with the sensor and listener that will receive events
		SensorMock sensorMock = new SensorMock(sensor, myListener, 0, 0);

		// Read from previously built file and feed events to the listener
		sensorMock.startGeneratingSensorEvents();

		assertThat(eventListenerOnSensorChanged).isEqualTo(10);
	}

	@Test
	public void gvenValidHeartRateSensorListenerRegistered_whenExecutedTwiceWithoutSufficientDifference_thenOnSensorChangedReturnsEmptyPredictions()
			throws Exception {
		SensorManager.reset();
		Random rd = new Random();
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEventListener listener = new EmerigenSensorEventListener();
		float[] values1 = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat(), rd.nextFloat() };

		// Create a 2nd event that surpasses the shake threshold
		SensorEvent event = new SensorEvent(hrSensor, values1);

		assertThat(listener.onSensorChanged(event)).isNotNull().isEmpty();
		SensorEvent event2 = new SensorEvent(hrSensor, values2);
		assertThat(listener.onSensorChanged(event2)).isNotNull().isEmpty();

	}

	@Test
	public void gvenValidSensorListenerRegistered_whenOnPauseInvoked_thenSensorIsNotRegistered()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEventListener listener = new EmerigenSensorEventListener();

		listener.onPause();

		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor))
				.isFalse();
	}

	@Test
	public void gvenValidSensorListenerRegistered_whenOnPauseThenOnResumeInvoked_thenRegistrationCorrect()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEventListener listener = new EmerigenSensorEventListener();
		sensorManager.registerListenerForSensor(listener, sensor);

		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor)).isTrue();

		listener.onPause();

		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor))
				.isFalse();

		listener.onResume();
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor)).isTrue();

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

		eventListenerOnSensorChanged = 0;
		eventListenerOnPause = 0;
		eventListenerOnResume = 0;

	}

	@After
	public void tearDown() throws Exception {
	}

}
