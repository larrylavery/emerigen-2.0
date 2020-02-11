package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.Cycle;
import com.emerigen.infrastructure.learning.CycleNode;
import com.emerigen.infrastructure.learning.CyclePatternRecognizer;
import com.emerigen.infrastructure.learning.DailyCycle;
import com.emerigen.infrastructure.learning.MonthlyCycle;
import com.emerigen.infrastructure.learning.PatternRecognizer;
import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.learning.TransitionPatternRecognizer;
import com.emerigen.infrastructure.learning.WeeklyCycle;
import com.emerigen.infrastructure.learning.YearlyCycle;

public class SensorManagerTest {

	public static int eventListenerOnSensorChanged = 0;

	public Cycle createCycle(String cycleType, int sensorType, int sensorLocation,
			int numberOfNodes) {
		Cycle cycle;

		if ("Daily".equals(cycleType))
			cycle = new DailyCycle(sensorType, sensorLocation);
		else if ("Weekly".equals(cycleType))
			cycle = new WeeklyCycle(sensorType, sensorLocation);
		else if ("Monthly".equals(cycleType))
			cycle = new MonthlyCycle(sensorType, sensorLocation);
		else if ("Yearly".equals(cycleType))
			cycle = new YearlyCycle(sensorType, sensorLocation);
		else
			throw new IllegalArgumentException(
					"cycle type must be valid, but was (" + cycleType + ")");

		// Set attributes
		cycle.setPreviousCycleNodeIndex(0);
		CycleNode cycleNode;

		for (int i = 0; i < numberOfNodes; i++) {
			cycleNode = new CycleNode();
			int minimumDelayBetweenReadings;
			int reportingMode;
			boolean wakeUpSensor;
			SensorEvent sensorEvent = new SensorEvent();
			sensorEvent.setTimestamp(i * sensorEvent.getTimestamp());
			Sensor sensor;

			// Create SensorEvent
			sensorEvent.setSensorType(sensorType);
			sensorEvent.setSensorLocation(sensorLocation);

			// Set sensor event values
			float[] values = { 1.0f + (i + 1) * 100.0f, 2.0f + (i + 1) * 100.0f };
			sensorEvent.setValues(values);

			// create and set event sensor
			minimumDelayBetweenReadings = Sensor.DELAY_NORMAL;
			reportingMode = Sensor.REPORTING_MODE_ON_CHANGE;
			wakeUpSensor = false;
			sensor = SensorManager.getInstance().getDefaultSensorForLocation(sensorType,
					sensorLocation);
			sensor.setMinimumDelayBetweenReadings(minimumDelayBetweenReadings);
			sensor.setWakeUpSensor(wakeUpSensor);
			sensor.setReportingMode(reportingMode);
			sensorEvent.setSensor(sensor);

			// Set up the rest of the CycleNode fields
			cycleNode.setSensorEvent(sensorEvent);
			cycleNode.setStartTimeOffsetNano(100 * (i + 1));
			cycleNode.setDataPointDurationNano(1000 * (i + 1));
			cycleNode.setProbability(0.3);
			cycleNode.setMyCycle(cycle);
			cycle.addCycleNode(cycleNode);
		}
		return cycle;
	}

	public class EventListener implements SensorEventListener {

		@Override
		public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {
			eventListenerOnSensorChanged++;
			return null;
		}

		@Override
		public void onPause() {
		}

		@Override
		public void onResume() {
		}

	}

	@Test
	public final void givenPatternRecognizerRegistrationsExist_whenUnregistered_thenIsRegisteredIsFalse() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEventListener listener = new TransitionPatternRecognizer(sensor);
		sensorManager.registerListenerForSensorWithFrequency(listener, sensor,
				Sensor.DELAY_NORMAL);
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor)).isTrue();

		sensorManager.unregisterListenerFromSensor(listener, sensor);
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor))
				.isFalse();

	}

	@Test
	public final void givenPatternRecognizerRegistrationsExist_whenNewCycleRegistered_thenIsRegisteredIsTrue() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor accSensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		PatternRecognizer cpr = new CyclePatternRecognizer(new DailyCycle());
		sensorManager.registerListenerForSensorWithFrequency(cpr, accSensor,
				Sensor.DELAY_NORMAL);
		assertThat(sensorManager.listenerIsRegisteredToSensor(cpr, accSensor)).isTrue();
	}

	@Test
	public final void givenMultiplePatternRecognizersInRepository_whenAppStartup_thenIsRegisteredIsTrueForEach() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor accSensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		PatternRecognizer cpr = new CyclePatternRecognizer(new DailyCycle());
		sensorManager.registerListenerForSensorWithFrequency(cpr, accSensor,
				Sensor.DELAY_NORMAL);
		assertThat(sensorManager.listenerIsRegisteredToSensor(cpr, accSensor)).isTrue();
	}

	@Test
	public final void givenOnePatternRecognizerInRepository_whenUnregistered_thenNoRegistrationsInRepository() {
		SensorManager sm = SensorManager.getInstance();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_WATCH);

		List<SensorEventListener> listeners = sm.getRegistrationsForSensor(sensor);
		assertThat(listeners).isNotNull();
		assertThat(listeners.size()).isEqualTo(2);
		sm.unregisterListenerFromSensor(listeners.get(0), sensor);

		List<SensorEventListener> listeners2 = sm.getRegistrationsForSensor(sensor);
		assertThat(listeners).isNotNull();
		assertThat(listeners.size()).isEqualTo(1);
	}

	@Test
	public final void givenMultiplePatternRecognizersInRepository_whenAllUnregistered_thenNoRegistrationsInRepositoryForThose() {
		SensorManager sm = SensorManager.getInstance();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_WATCH);

		List<SensorEventListener> listeners = sm.getRegistrationsForSensor(sensor);
		assertThat(listeners).isNotNull();
		assertThat(listeners.size()).isEqualTo(2);
		sm.unregisterListenerFromSensor(listeners.get(0), sensor);

		List<SensorEventListener> listeners2 = sm.getRegistrationsForSensor(sensor);
		sm.unregisterListenerFromSensor(listeners.get(0), sensor);
		listeners2 = sm.getRegistrationsForSensor(sensor);
		assertThat(listeners2).isNotNull();
		assertThat(listeners2.size()).isEqualTo(0);
	}

	@Test
	public final void givenMultiplePatternRecognizersInRepository_whenUnregistered_thenNoRegistration() {
		SensorManager sm = SensorManager.getInstance();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_WATCH);

		sm.unregisterListenerFromSensor(null, sensor);
		List<SensorEventListener> listeners = sm.getRegistrationsForSensor(sensor);
		assertThat(listeners).isNotNull();
		assertThat(listeners.size()).isEqualTo(2);
	}

	@Test
	public final void givenOnePatternRecognizerInRepository_whenAppStartup_thenIsRegisteredIsTrue() {
		SensorManager sm = SensorManager.getInstance();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_WATCH);

		SensorEventListener listener = new TransitionPatternRecognizer(sensor);
		sm.unregisterListenerFromSensor(listener, sensor);
		List<SensorEventListener> listeners = sm.getRegistrationsForSensor(sensor);
		assertThat(listeners).isNotNull();
		assertThat(listeners.size()).isEqualTo(1);
	}

	@Test
	public final void givenUnRegisteredPatternRecognizer_whenRegistered_thenIsRegisteredIsTrue() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEventListener listener = new TransitionPatternRecognizer(sensor);
		sensorManager.registerListenerForSensorWithFrequency(listener, sensor,
				Sensor.DELAY_NORMAL);
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor)).isTrue();
	}

	@Test
	public final void givenRegisteredEventListener_whenUnregistered_thenIsRegisteredIsFalse() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEventListener listener = new EventListener();
		sensorManager.registerListenerForSensorWithFrequency(listener, sensor,
				Sensor.DELAY_NORMAL);
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor)).isTrue();

		// simulate the h/w sensor publishing an event by invoking the onSensorChanged
		// with an event
		float[] values = { 10.1f, 20.2f, 30.3f };
		SensorEvent event = new SensorEvent(sensor, values);

		sensorManager.unregisterListenerFromSensor(listener, sensor);
		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor))
				.isFalse();
	}

	@Test
	public final void givenNullSensor_whenRegistered_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEventListener listener = new EventListener();

		final Throwable throwable = catchThrowable(
				() -> sensorManager.registerListenerForSensorWithFrequency(listener, null,
						Sensor.DELAY_NORMAL));

		then(throwable).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNonPositiveMinDelay_whenRegistered_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEventListener listener = new EventListener();

		final Throwable throwable = catchThrowable(() -> sensorManager
				.registerListenerForSensorWithFrequency(listener, sensor, -1));

		then(throwable).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullEventListener_whenRegistered_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEventListener listener = new EventListener();

		final Throwable throwable = catchThrowable(
				() -> sensorManager.registerListenerForSensorWithFrequency(null, sensor,
						Sensor.DELAY_NORMAL));

		then(throwable).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenEventListener_whenRegistered_thenIsRegisteredIsTrue() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEventListener listener = new EventListener();
		sensorManager.registerListenerForSensorWithFrequency(listener, sensor,
				Sensor.DELAY_NORMAL);

		// simulate the h/w sensor publishing an event by invoking the onSensorChanged
		// with an event
		float[] values = { 10.1f, 20.2f, 30.3f };
		SensorEvent event = new SensorEvent(sensor, values);
//		listener.onSensorChanged(event);

		assertThat(sensorManager.listenerIsRegisteredToSensor(listener, sensor)).isTrue();
	}

	@Test
	public final void givenSensorsInitialized_whenGetDefaultSensorInvoked_thenExistingSensorReturned() {
		// given
		SensorManager sensorManager = SensorManager.getInstance();
		SensorEventListener totalListener = new EmerigenSensorEventListener();
		Sensor sensorOld = new GpsSensor(Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE,
				Sensor.REPORTING_MODE_CONTINUOUS, Sensor.DELAY_NORMAL, false);

		// when
		Sensor sensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_PHONE);

		then(!sensorOld.equals(sensor)).isTrue();
	}

	@Test
	public final void givenTwoSensors_whenGetAllSensors_thenBothAreReturned() {
		// given
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);
		sensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_TEMPERATURE,
				Sensor.LOCATION_PHONE);

		// when
		List<Sensor> allSensors = sensorManager.getAllSensors();

		then(allSensors.size()).isEqualTo(2);
	}

	@Test
	public final void givenOneSensor_whenGetAllSensors_thenOneIsReturned() {
		// given
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		// when
		List<Sensor> allSensors = sensorManager.getAllSensors();

		then(allSensors.size()).isEqualTo(1);
	}

	@Test
	public final void givenTheSecondValidSensorType_whenCreated_thenReturned() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		then(sensor).isInstanceOf(AccelerometerSensor.class);
	}

	@Test
	public final void givenValidSensorType_whenCreated_thenReturned() {
		SensorManager sensorManager = SensorManager.getInstance();
		Sensor sensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_TEMPERATURE,
				Sensor.LOCATION_PHONE);

		then(sensor).isInstanceOf(TemperatureSensor.class);

	}

	@Test
	public final void givenInvalidNumberForDefaultSensorParm_whenRequested_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();

		final Throwable throwable = catchThrowable(() -> sensorManager
				.getDefaultSensorForLocation(100, Sensor.LOCATION_PHONE));

		then(throwable).as("A 100 sensor type throws a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidNegativeDefaultSensorParm_whenRequested_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();

		final Throwable throwable = catchThrowable(() -> sensorManager
				.getDefaultSensorForLocation(-1, Sensor.LOCATION_PHONE));

		then(throwable).as("A -1 sensor type throws a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullDefaultSensorParm_whenRequested_thenIllegalArgumentException() {
		SensorManager sensorManager = SensorManager.getInstance();

		final Throwable throwable = catchThrowable(() -> sensorManager
				.getDefaultSensorForLocation(0, Sensor.LOCATION_PHONE));

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
