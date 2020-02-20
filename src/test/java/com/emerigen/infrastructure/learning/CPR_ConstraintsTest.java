package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;

public class CPR_ConstraintsTest {

	public static Cycle createCycle(String cycleType, int sensorType, int sensorLocation, int numberOfNodes) {
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
			throw new IllegalArgumentException("cycle type must be valid, but was (" + cycleType + ")");

		// Set attributes
//		cycle.setPreviousCycleNodeIndex(0);
//		CycleNode cycleNode;
//
//		for (int i = 0; i < numberOfNodes; i++) {
//			cycleNode = new CycleNode();
//			int minimumDelayBetweenReadings;
//			int reportingMode;
//			boolean wakeUpSensor;
//			SensorEvent sensorEvent = new SensorEvent();
//			sensorEvent.setTimestamp(i * sensorEvent.getTimestamp());
//			Sensor sensor;
//
//			// Create SensorEvent
//			sensorEvent.setSensorType(sensorType);
//			sensorEvent.setSensorLocation(sensorLocation);
//
//			// Set sensor event values
//			float[] values = { 1.0f + (i + 1) * 100.0f, 2.0f + (i + 1) * 100.0f };
//			sensorEvent.setValues(values);
//
//			// create and set event sensor
//			minimumDelayBetweenReadings = Sensor.DELAY_NORMAL;
//			reportingMode = Sensor.REPORTING_MODE_ON_CHANGE;
//			wakeUpSensor = false;
//			sensor = SensorManager.getInstance().getDefaultSensorForLocation(sensorType, sensorLocation);
//			sensor.setMinimumDelayBetweenReadings(minimumDelayBetweenReadings);
//			sensor.setWakeUpSensor(wakeUpSensor);
//			sensor.setReportingMode(reportingMode);
//			sensorEvent.setSensor(sensor);
//
//			// Set up the rest of the CycleNode fields
//			cycleNode.setSensorEvent(sensorEvent);
//			cycleNode.setStartTimeOffsetNano(100 * (i + 1));
//			cycleNode.setDataPointDurationNano(1000 * (i + 1));
//			cycleNode.setProbability(0.3);
//			cycleNode.setMyCycle(cycle);
//			cycle.addCycleNode(cycleNode);
//		}
		return cycle;
	}

	@Test
	public final void givenValidCycle_whenEventWithDifferentSensorType_thenIllegalArgumentException() {

		// Given
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_PHONE);
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat(), rd.nextFloat() };
		SensorEvent event1 = new SensorEvent(hrSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);

		final Throwable throwable = catchThrowable(() -> cpr.onSensorChanged(event1));

		then(throwable).as("An invalid sensor throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenValidCycle_whenEventWithDifferentSensorLocation_thenIllegalArgumentException() {

		// Given
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_PHONE);
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Sensor gpsSensor2 = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_WATCH);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat(), rd.nextFloat() };
		SensorEvent event1 = new SensorEvent(gpsSensor2, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);

		event2.setTimestamp(event2.getTimestamp() - cpr.cycleDurationTimeNano);

		final Throwable throwable = catchThrowable(() -> cpr.onSensorChanged(event1));

		then(throwable).as("An invalid sensor location throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenValidCycleAndNewEventExistingPriorToCycleStart_whenOnSensorChangedCalled_thenCorrectTransitionCreated()
			throws InterruptedException {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(event1.getTimestamp() - cpr.cycleDurationTimeNano - 1000);

		cpr.onSensorChanged(event1);
//		Thread.sleep(1000);
		List<Prediction> predictions = cpr.onSensorChanged(event2);
		assertThat(predictions.size()).isGreaterThan(0);
	}

	@Test
	public final void givenNullPredictionService_whenCPRCreated_thenIllegalArgumentException() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());

		// When
		final Throwable throwable = catchThrowable(
				() -> new CyclePatternRecognizer(new DailyCycle(), gpsSensor, null));

		assertThat(throwable).as("A null predictionService throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidSensorLocation_whenCycleCreated_thenIllegalArgumentException() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		final Throwable throwable = catchThrowable(() -> new DailyCycle(Sensor.TYPE_GPS, -1));

		assertThat(throwable).as("A invalid sensorLocation for Cycle throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidSensorType_whenCycleCreated_thenIllegalArgumentException() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		final Throwable throwable = catchThrowable(() -> new DailyCycle(-1, Sensor.LOCATION_PHONE));

		assertThat(throwable).as("A invalid sensorType for Cycle throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullSensor_whenCPRCreated_thenIllegalArgumentException() {

		// Given

		// When
		final Throwable throwable = catchThrowable(
				() -> new CyclePatternRecognizer(new DailyCycle(), null, new PredictionService()));

		assertThat(throwable).as("A null sensor throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNewEventExistingPriorToPreviousEvent_whenOnSensorChangedCalled_thenTransitionCreated() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values);
		event2.setTimestamp(event1.getTimestamp() - 500);

		cpr.onSensorChanged(event1);
		List<Prediction> predictions = cpr.onSensorChanged(event2);
		predictions.contains(event1);

	}

	@Test
	public final void givenValidSensorEvents_whenDurationsAdded_thenLastEventDurationMustBeDiffence() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values);
		event2.setDataPointDurationNano(event1.getDataPointDurationNano() + 20000);

		cpr.onSensorChanged(event1);
		long beforeDuration = event1.getDataPointDurationNano();
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		predictions = cpr.onSensorChanged(event2);
		long afterDuration = event2.getDataPointDurationNano();

		long difference = Math.abs(afterDuration - beforeDuration);
		assertThat(difference).isEqualTo(20000);
	}

	@Test
	public final void givenNewSensorEventWithDifferentSensorType_whenOnNewEvent_thenIllegalArgumentException() {
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_PHONE);

		float[] values = { 1.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(hrSensor, values);

		final Throwable throwable = catchThrowable(() -> cpr.onSensorChanged(event2));

		assertThat(throwable).as("A different sensor type event  throws an  IllegalArgumentException")
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
	}

	@After
	public void tearDown() throws Exception {
	}

}
