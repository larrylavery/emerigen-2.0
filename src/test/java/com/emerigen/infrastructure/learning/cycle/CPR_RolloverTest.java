package com.emerigen.infrastructure.learning.cycle;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class CPR_RolloverTest {

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
	public final void givenEmptyCycle_whenOnSensorChangedCalledWithEventPastCycleDuration_thenCycleRolledOverAndEventAddedAndEmptyPredictionListReturned() {
		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

		// When
		float[] values = { 1.0f, 4.0f }; // gps sensors require lat and long floats
		float[] values2 = { 5.0f, 2.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);

		// Set the event timestamp to after the cycle duration
		event1.setTimestamp(event1.getTimestamp() + cpr.getCycleDurationTimeNano());

		long previousCycleStartTime = cpr.getCycleStartTimeNano();
		List<Prediction> predictions = cpr.onSensorChanged(event1);

		// Define predictionService behavior for this test case
		List<Prediction> mockPredictions = new ArrayList<>();
		mockPredictions.add(new Prediction(event1));

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);

		verifyNoMoreInteractions(mockPredictionService);

		long currentCycleStartTime = cpr.getCycleStartTimeNano();

		assertThat(currentCycleStartTime - previousCycleStartTime)
				.isEqualTo(cpr.getCycleDurationTimeNano());
		assertThat(predictions).isNotNull().isEmpty();
	}

	/**
	 * The next several test verify the cycle rolling over capability
	 */
	@Test
	public final void givenOneNodeCycle_whenNewEventPastCycleDuration_thenCycleRolledOverAndEventAddedBeforeExisting() {

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
		event2.setTimestamp(event1.getTimestamp() - 500 + cpr.getCycleDurationTimeNano());

		// Test that event added before existing event
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();
		predictions = cpr.onSensorChanged(event2);

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
	public final void givenOneEventCycle_whenNewEventPastCycleDuration_thenCycleRolledOverAndEventAddedAfterExisting() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

		// When
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(
				event1.getTimestamp() + 100000 + cpr.getCycleDurationTimeNano());

		// Test that event added in between the existing events
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();
		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);

		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenTwoEventCycle_whenNewEventPastCycleDuration_thenCycleRolledOverAndEventAddedInbetween() {

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
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		// event1.setTimestamp(event1.getTimestamp() + gpsCycle.cycleDurationTimeNano);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(event1.getTimestamp() + 10000);

		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event3.setTimestamp(
				event1.getTimestamp() + 5000 + cpr.getCycleDurationTimeNano());

		// Test that event added in between the existing events
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event3, event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(3))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event3,
				event2);

		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenNonEmptyCycle_whenNewEventPastCycleDuration_thenCycleRolledOverAndEventAddedFirstInOrder() {

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
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(
				event1.getTimestamp() - 10000 + cpr.getCycleDurationTimeNano());

		// Test that event added to the beginning of the next cycle
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		predictions = cpr.onSensorChanged(event2);

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
	public final void givenNonEmptyCycle_whenNewEventPastCycleDuration_thenCycleRolledOverAndEventAddedAtEnd() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

		// When
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(
				event1.getTimestamp() + 1000 + cpr.getCycleDurationTimeNano());

		// Test that event added to the beginning of the next cycle
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		predictions = cpr.onSensorChanged(event2);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);
		verifyNoMoreInteractions(mockPredictionService);
	}

	@Test
	public final void givenEmptyCycle_whenNewEventPastCycleDuration_thenCycleRolledOverAndEventAdded() {

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
		event1.setTimestamp(event1.getTimestamp() + cpr.getCycleDurationTimeNano());

		// Test that added to the beginning of the next cycle
		List<Prediction> predictions = cpr.onSensorChanged(event1);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);

		verify(mockPredictionService, times(1))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verifyNoMoreInteractions(mockPredictionService);
	}

	@Test
	public final void givenEmptyCycle_whenNewEventPastTwoCyclesDuration_thenCycleRolledOverAndEventAdded() {

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
		event1.setTimestamp(event1.getTimestamp() + (2 * cpr.getCycleDurationTimeNano()));

		// Test that added to the beginning of the next cycle
		List<Prediction> predictions = cpr.onSensorChanged(event1);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verifyNoMoreInteractions(mockPredictionService);
	}

	@Test
	public final void givenNonEmptyCycle_whenNewSensorEventWithTimePastCycleDuration_thenAddedInNextCyleAfterMostRecentPriorEvent()
			throws InterruptedException {

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
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		float[] values4 = { rd.nextFloat() + 1000, rd.nextFloat() + 1000 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(event1.getTimestamp() + 10000);

		// this event will go between event2 and event4 after cycle rolled over 1 period
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event3.setTimestamp(
				event1.getTimestamp() + 5000 + cpr.getCycleDurationTimeNano());
		SensorEvent event4 = new SensorEvent(gpsSensor, values4);
		event4.setTimestamp(
				event1.getTimestamp() + 1000 + cpr.getCycleDurationTimeNano());
		cpr.onSensorChanged(event1);
		cpr.onSensorChanged(event2);
		cpr.onSensorChanged(event3);
		event3.setTimestamp(
				cpr.getCycleDurationTimeNano() + System.currentTimeMillis() * 1000000);
		cpr.onSensorChanged(event4);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called

		verify(mockPredictionService, times(4))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event4);

		// TODO something stinks about this test
		// verify(mockPredictionService).createPredictionFromSensorEvents(event1,
		// event4);
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event3, event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event4, event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService).getPredictionsForSensorEvent(event4);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event3,
				event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event4,
				event2);
		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenNonEmptyCycle_whenNewSensorEventWithTimePastCurrentEventAndCycleDuration_thenAdded2nd() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

		// When

		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(
				event1.getTimestamp() + 1000 + cpr.getCycleDurationTimeNano());

		// this event will go before the 1st event in the next cycle
		// (24 hours in this case)
		cpr.onSensorChanged(event1);
		cpr.onSensorChanged(event2);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);

		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);
		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenCycle_whenNewEventArrivesPastCycleDuration_thenCycleStartTimeUpdatedToStartTimeOfClosestNextCycle() {
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

		// Set the event timestamp to after the cycle duration
		event1.setTimestamp(event1.getTimestamp() + cpr.getCycleDurationTimeNano());

		long previousCycleStartTime = cpr.getCycleStartTimeNano();
		cpr.onSensorChanged(event1);
		long currentCycleStartTime = cpr.getCycleStartTimeNano();

		assertThat(currentCycleStartTime - previousCycleStartTime)
				.isEqualTo(cpr.getCycleDurationTimeNano());

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(1))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);

		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verifyNoMoreInteractions(mockPredictionService);

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
