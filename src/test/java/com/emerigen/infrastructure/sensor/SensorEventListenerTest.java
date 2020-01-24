package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepository;
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
		public void onCreate() {
		}

		@Override
		public boolean onSensorChanged(SensorEvent sensorEvent) {
			eventListenerOnSensorChanged++;
			return true;
		}

		@Override
		public void onAccuracyChanged() {
			eventListenerOnAccuracyChanged++;
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
	public void givenTenHeartRateSensorReadings_whenFedIntoOnSensorChanged_thenTenPatternsLogged()
			throws Exception {
		// Given
		// Remove all sensor related records prior to test or get false results
		CouchbaseRepository.getInstance().removeAllDocuments("sensor-event");
//		CouchbaseRepository.getInstance().removeAllDocuments("prediction");
//		CouchbaseRepository.getInstance().removeAllDocuments("transition");

		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

		// Create sensor mock with the sensor and listener that will receive events
		SensorEventListener listener = new HeartRateSensorEventListener();
		SensorMock sensorMock = new SensorMock(sensor, listener,
				minDelayBetweenReadingsMillis, 0);

		// Read from previously built file and feed events to the listener
		sensorMock.startGeneratingSensorEvents();

		// Verify that 10 HeartRate patterns were logged for those sensor events
		int patternCount = KnowledgeRepository.getInstance()
				.getSensorEventCountForSensorTypeAndLocation(sensor.getSensorType(),
						sensor.getLocation());
		assertThat(patternCount).isGreaterThan(9);
	}

	@Test
	public void gvenValidHeartRateSensorListenerRegistered_whenExecutedMultipleTimes_thenOnSensorChangedThrottled()
			throws Exception {

		// TODO test minimumDelayBewteenReadings during sensorMock testing
//		SensorManager sensorManager = SensorManager.getInstance();
//		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
//
//		SensorEventListener listener = new HeartRateSensorEventListener();
//		float[] values = { 10.1f, 20.2f, 30.3f };
//		SensorEvent event1 = new SensorEvent(sensor, values);
//		SensorEvent event2 = new SensorEvent(sensor, values);
//		SensorEvent event3 = new SensorEvent(sensor, values);
//		SensorEvent event4 = new SensorEvent(sensor, values);
//		SensorEvent event5 = new SensorEvent(sensor, values);
//		SensorEvent event6 = new SensorEvent(sensor, values);
//		SensorEvent event7 = new SensorEvent(sensor, values);
//		SensorEvent event8 = new SensorEvent(sensor, values);
//		SensorEvent event9 = new SensorEvent(sensor, values);
//		sensorManager.registerListenerForSensorWithFrequency(listener, sensor,
//				Sensor.DELAY_NORMAL);
//
//		assertThat(listener.onSensorChanged(event1)).isEqualTo(true);
//		assertThat(listener.onSensorChanged(event2)).isEqualTo(false);
//		assertThat(listener.onSensorChanged(event3)).isEqualTo(false);
//		assertThat(listener.onSensorChanged(event4)).isEqualTo(false);
//		assertThat(listener.onSensorChanged(event5)).isEqualTo(false);
//
//		Thread.sleep(2 * minDelayBetweenReadingsMillis);
//		assertThat(listener.onSensorChanged(event6)).isEqualTo(true);
//		assertThat(listener.onSensorChanged(event7)).isEqualTo(false);
//		assertThat(listener.onSensorChanged(event8)).isEqualTo(false);
//		assertThat(listener.onSensorChanged(event9)).isEqualTo(false);

	}

	@Test
	public void gvenValidAccelerometerSensorListenerRegistered_whenExecutedMultipleTimes_thenOnSensorChangedThrottled()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		SensorEventListener listener = new AccelerometerSenorEventListener();

		// Create a 2nd event that surpasses the shake threshold
		float[] values = { 10.1f, 20.2f, 30.3f };
		SensorEvent event = new SensorEvent(sensor, values);
		float[] valuesPastShakeThreshold = { 2000000.0f, 3000000.1f, 4000000.1F };
		SensorEvent event2 = new SensorEvent(sensor, valuesPastShakeThreshold);

		sensorManager.registerListenerForSensorWithFrequency(listener, sensor,
				Sensor.DELAY_NORMAL);

		// assertThat(listener.onSensorChanged(event)).isEqualTo(true);
		assertThat(listener.onSensorChanged(event)).isEqualTo(false);
		assertThat(listener.onSensorChanged(event)).isEqualTo(false);
		assertThat(listener.onSensorChanged(event)).isEqualTo(false);
		assertThat(listener.onSensorChanged(event)).isEqualTo(false);

		Thread.sleep(minDelayBetweenReadingsMillis);

		assertThat(listener.onSensorChanged(event2)).isEqualTo(true);
		assertThat(listener.onSensorChanged(event2)).isEqualTo(false);
		assertThat(listener.onSensorChanged(event2)).isEqualTo(false);
		assertThat(listener.onSensorChanged(event2)).isEqualTo(false);

	}

	@Test
	public void gvenValidSensorListenerRegistered_whenOnPauseInvoked_thenSensorIsNotRegistered()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		SensorEventListener listener = new AccelerometerSenorEventListener();
		sensorManager.registerListenerForSensorWithFrequency(listener, sensor,
				Sensor.DELAY_NORMAL);

		listener.onPause();

		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor))
				.isFalse();
	}

	@Test
	public void gvenValidSensorListenerRegistered_whenOnPauseThenOnResumeInvoked_thenRegistrationCorrect()
			throws Exception {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		SensorEventListener listener = new AccelerometerSenorEventListener();
		sensorManager.registerListenerForSensorWithFrequency(listener, sensor,
				Sensor.DELAY_NORMAL);

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
