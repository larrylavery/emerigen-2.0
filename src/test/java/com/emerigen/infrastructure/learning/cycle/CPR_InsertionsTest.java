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

public class CPR_InsertionsTest {

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
	public final void givenExistingCycleEvent_whenOnSensorChangedCalledWithPriorEvent_thenTransitionCreatedAnd2ndEventReturnedAsPrediction() {

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
		event2.setTimestamp(event1.getTimestamp() - 20000);

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
	public final void givenTwoEventCycle_whenNewEventWithLeastTimestamp_thenEventAddedToFirstPositionAndPredictionReturned() {
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
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(event1.getTimestamp() + minimumDelayBetweenReadings);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event3.setTimestamp(event1.getTimestamp() - 2 * minimumDelayBetweenReadings);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		predictions = cpr.onSensorChanged(event3);
//		assertThat(predictions).isNotNull().isNotEmpty();

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event3, event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(3))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event3,
				event2);

		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenNonEmptyCycle_whenNewSensorEventPastAllExistingEvents_thenAddedToEnd()
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
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		event2.setTimestamp(event1.getTimestamp() + 10000);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event3.setTimestamp(event2.getTimestamp() + 10000);
		cpr.onSensorChanged(event1);
		cpr.onSensorChanged(event2);
		cpr.onSensorChanged(event3);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event2, event3);
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
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event2,
				event3);

		verifyNoMoreInteractions(mockPredictionService);

		// Then

	}

	@Test
	public final void givenNonEmptyCycle_whenNewEqualSensorEvent_thenMergedAndPreviousReplaced() {
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
		event2.setTimestamp(event1.getTimestamp() + minimumDelayBetweenReadings);
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions.size()).isEqualTo(0);
		predictions = cpr.onSensorChanged(event2);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(2)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
//		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
//		verify(mockPredictionService).getPredictionsForSensorEvent(event1);

		verifyNoMoreInteractions(mockPredictionService);

		// Then
		assertThat(predictions.size()).isEqualTo(0);
	}

	@Test
	public final void givenNonEmptyCycle_whenEqualNewNodeArrives_thenCycleContainsMergedNode() {

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
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		predictions = cpr.onSensorChanged(event2);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(2)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());

		verifyNoMoreInteractions(mockPredictionService);

		// Then
		assertThat(predictions).isNotNull().isEmpty();
	}

	@Test
	public final void givenNonEmptyCycle_whenEqualNewEventArrives_thenMergedEventDurationisAdditionOfPreviousAndCurrentEvent() {
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
		float[] values3 = { rd.nextFloat(), rd.nextFloat() };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values);
		event2.setTimestamp(event2.getTimestamp() + minimumDelayBetweenReadings);

		cpr.onSensorChanged(event1);
		cpr.onSensorChanged(event2);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(2)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());

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
