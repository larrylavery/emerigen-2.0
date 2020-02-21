package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.EmerigenProperties;

@RunWith(MockitoJUnitRunner.class)
public class CPR_ConstraintsTest {

	private long minimumDelayBetweenReadings = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"))
			* 1000000;

	@Mock
	PredictionService mockPredictionService;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	public static Cycle createCycle(String cycleType, int sensorType, int sensorLocation,
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

		return cycle;
	}

	@Test
	public final void givenCycle_whenOnChangedCalledWithEventOfDifferentSensorType_thenIllegalArgumentException() {

		// Given
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat(), rd.nextFloat() };
		SensorEvent event1 = new SensorEvent(hrSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);

		final Throwable throwable = catchThrowable(() -> cpr.onSensorChanged(event1));

		then(throwable)
				.as("An event with invalid sensor type throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenValidCycle_whenEventWithDifferentSensorLocation_thenIllegalArgumentException() {

		// Given
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Sensor gpsSensor2 = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_WATCH);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

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
	public final void givenNewEventExistingPriorToCycleStart_whenOnSensorChangedCalled_thenCorrectTransitionCreated() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(event1.getTimestamp() - cpr.cycleDurationTimeNano - 1000);

		// Define predictionService behavior for this test case
		List<Prediction> mockPredictions = new ArrayList<>();
		mockPredictions.add(new CyclePrediction(event1));

//		when(mockPredictionService.createPredictionFromSensorEvents(event2, event1))
//				.thenReturn(anyString());
//		when(mockPredictionService.getPredictionsForSensorEvent(event1))
//				.thenReturn(new ArrayList<Prediction>());
//		when(mockPredictionService.getPredictionsForSensorEvent(event2))
//				.thenReturn(mockPredictions);
//		when(mockPredictionService.setCurrentPredictions(new ArrayList<Prediction>()))
//		.thenReturn();

		cpr.onSensorChanged(event1);
		List<Prediction> predictions = cpr.onSensorChanged(event2);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).createPredictionFromSensorEvents(event2, event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event2,
				event1);

		verifyNoMoreInteractions(mockPredictionService);
	}

	@Test
	public final void givenNullPredictionService_whenCPRCreated_thenIllegalArgumentException() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				new PredictionService());

		// When
		final Throwable throwable = catchThrowable(
				() -> new CyclePatternRecognizer(new DailyCycle(), gpsSensor, null));

		assertThat(throwable)
				.as("A null predictionService throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidSensorLocation_whenCycleCreated_thenIllegalArgumentException() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		// When
		final Throwable throwable = catchThrowable(
				() -> new DailyCycle(Sensor.TYPE_GPS, -1));

		assertThat(throwable).as(
				"A invalid sensorLocation for Cycle throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidSensorType_whenCycleCreated_thenIllegalArgumentException() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		// When
		final Throwable throwable = catchThrowable(
				() -> new DailyCycle(-1, Sensor.LOCATION_PHONE));

		assertThat(throwable)
				.as("A invalid sensorType for Cycle throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullSensor_whenCPRCreated_thenIllegalArgumentException() {

		// Given

		// When
		final Throwable throwable = catchThrowable(() -> new CyclePatternRecognizer(
				new DailyCycle(), null, new PredictionService()));

		assertThat(throwable).as("A null sensor throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNewEventExistingPriorToPreviousEvent_whenOnSensorChangedCalled_thenTransitionCreated() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(event1.getTimestamp() - 50000);

		// Define predictionService behavior for this test case
		List<Prediction> mockPredictions = new ArrayList<>();
		mockPredictions.add(new CyclePrediction(event1));

		cpr.onSensorChanged(event1);
		List<Prediction> predictions = cpr.onSensorChanged(event2);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).createPredictionFromSensorEvents(event2, event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event2,
				event1);

		verifyNoMoreInteractions(mockPredictionService);
	}

	@Test
	public final void givenTwoEqualSensorEvents_whenDurationsAdded_thenFirstEventDurationMustBeSum() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values);
		event2.setTimestamp(event2.getTimestamp() + minimumDelayBetweenReadings);
		event2.setDataPointDurationNano(event1.getDataPointDurationNano() + 20000);

		// Define predictionService behavior for this test case
//		List<Prediction> mockPredictions = new ArrayList<>();
//		mockPredictions.add(new CyclePrediction(event1));

		long beforeDuration = event1.getDataPointDurationNano();
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		predictions = cpr.onSensorChanged(event2);
		long afterDuration = event1.getDataPointDurationNano();

		long difference = Math.abs(afterDuration - beforeDuration);
		assertThat(difference).isEqualTo(20100);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
//		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
//		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
//		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(2)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(2)).getPredictionsForSensorEvent(event2);

		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenNewSensorEventWithDifferentSensorType_whenOnNewEvent_thenIllegalArgumentException() {
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				new PredictionService());
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		float[] values = { 1.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(hrSensor, values);

		final Throwable throwable = catchThrowable(() -> cpr.onSensorChanged(event2));

		assertThat(throwable)
				.as("A different sensor type event  throws an  IllegalArgumentException")
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
