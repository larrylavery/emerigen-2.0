package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SensorManagerTest {

	public static int eventListenerOnSensorChanged = 0;

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
		}

		@Override
		public void onPause() {
		}

		@Override
		public void onResume() {
		}
	}

	@Test
	public final void givenRegisteredEventListener_whenUnregistered_thenIsRegisteredIsFalse() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		SensorEventListener listener = new EventListener();
		sensorManager.registerListenerForSensorWithFrequency(listener, sensor, Sensor.DELAY_NORMAL);
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor)).isTrue();

		// simulate the h/w sensor publishing an event by invoking the onSensorChanged
		// with an event
		float[] values = { 10.1f, 20.2f, 30.3f };
		SensorEvent event = new SensorEvent(sensor, values);

		sensorManager.unregisterListenerFromSensor(listener, sensor);
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor)).isFalse();
	}

	@Test
	public final void givenNullSensor_whenRegistered_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		
		SensorEventListener listener = new EventListener();
		
		final Throwable throwable = catchThrowable(() -> sensorManager
				.registerListenerForSensorWithFrequency(listener, 
						null, Sensor.DELAY_NORMAL));

		then(throwable)
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNonPositiveMinDelay_whenRegistered_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		
		SensorEventListener listener = new EventListener();
		
		final Throwable throwable = catchThrowable(() -> sensorManager
				.registerListenerForSensorWithFrequency(listener, 
						sensor, -1));

		then(throwable)
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullEventListener_whenRegistered_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		
		SensorEventListener listener = new EventListener();
		
		final Throwable throwable = catchThrowable(() -> sensorManager.registerListenerForSensorWithFrequency(null, sensor, Sensor.DELAY_NORMAL));

		then(throwable)
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenEventListener_whenRegistered_thenIsRegisteredIsTrue() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		SensorEventListener listener = new EventListener();
		sensorManager.registerListenerForSensorWithFrequency(listener, sensor, Sensor.DELAY_NORMAL);

		// simulate the h/w sensor publishing an event by invoking the onSensorChanged
		// with an event
		float[] values = { 10.1f, 20.2f, 30.3f };
		SensorEvent event = new SensorEvent(sensor, values);
//		listener.onSensorChanged(event);

		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor)).isTrue();
	}

	@Test
	public final void givenTwoSensors_whenGetAllSensors_thenBothAreReturned() {
		// given
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

		// when
		List<Sensor> allSensors = sensorManager.getAllSensors();

		then(allSensors.size()).isEqualTo(2);
	}

	@Test
	public final void givenOneSensor_whenGetAllSensors_thenOneIsReturned() {
		// given
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// when
		List<Sensor> allSensors = sensorManager.getAllSensors();

		then(allSensors.size()).isEqualTo(1);
	}

	@Test
	public final void givenTheSecondValidSensorType_whenCreated_thenReturned() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		then(sensor).isInstanceOf(AccelerometerSensor.class);
	}

	@Test
	public final void givenValidSensorType_whenCreated_thenReturned() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

		then(sensor).isInstanceOf(TemperatureSensor.class);

	}

	@Test
	public final void givenInvalidNumberForDefaultSensorParm_whenRequested_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();
		// Sensor sensor = sensorManager.getDefaultSensor(null);

		final Throwable throwable = catchThrowable(() -> sensorManager.getDefaultSensor(100));

		then(throwable).as("A 100 sensor type throws a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidNegativeDefaultSensorParm_whenRequested_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();
		// Sensor sensor = sensorManager.getDefaultSensor(null);

		final Throwable throwable = catchThrowable(() -> sensorManager.getDefaultSensor(-1));

		then(throwable).as("A -1 sensor type throws a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullDefaultSensorParm_whenRequested_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();
		// Sensor sensor = sensorManager.getDefaultSensor(null);

		final Throwable throwable = catchThrowable(() -> sensorManager.getDefaultSensor(0));

		then(throwable).as("A 0 sensor type throws a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		SensorManager.reset();
		eventListenerOnSensorChanged = 0;
	}

	@After
	public void tearDown() throws Exception {
	}

}
