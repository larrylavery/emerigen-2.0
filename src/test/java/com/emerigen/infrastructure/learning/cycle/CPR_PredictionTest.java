
package com.emerigen.infrastructure.learning.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

public class CPR_PredictionTest {
	private static long defaultCycleNodeDurationNano = Long.parseLong(EmerigenProperties
			.getInstance().getValue("cycle.default.data.point.duration.nano"));

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
	public final void givenExpectedProbability_whenCreated_thenProbabilityCorrect() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNewEventOnExistingCycle_whenOnSensorChangedCalled_thenEventAddedAt1stPositionAndFollowingEventIsReturnedAsPrediction() {
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
		event2.setTimestamp(event1.getTimestamp() + 1500);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event3.setTimestamp(event1.getTimestamp() + 2000);
		SensorEvent event4 = new SensorEvent(gpsSensor, values4);
		event4.setTimestamp(event1.getTimestamp() - 1000 + cpr.cycleDurationTimeNano);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event4);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(4))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event4);

//		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event4);
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event2, event3);
		verify(mockPredictionService).createPredictionFromSensorEvents(event4, event3);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService).getPredictionsForSensorEvent(event4);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event2,
				event3);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event4,
				event3);
		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenNewEventOnExistingCycle_whenOnSensorChangedCalled_thenEventAddedAtAppropriatePositionAndFollowingEventIsReturnedAsPrediction() {
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
		event2.setTimestamp(event1.getTimestamp() + minimumDelayBetweenReadings);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event3.setTimestamp(event1.getTimestamp() + 2 * minimumDelayBetweenReadings);
		SensorEvent event4 = new SensorEvent(gpsSensor, values4);
		event4.setTimestamp(event1.getTimestamp() + 3 * cpr.cycleDurationTimeNano);

		List<Prediction> mockPredictions = new ArrayList<>();
		mockPredictions.add(new Prediction(event1));
		List<Prediction> mockPredictions4 = new ArrayList<>();
		mockPredictions4.add(new Prediction(event2));

		when(mockPredictionService.createPredictionFromSensorEvents(event1, event2))
				.thenReturn("");
		when(mockPredictionService.getPredictionsForSensorEvent(event1))
				.thenReturn(new ArrayList<Prediction>());
		when(mockPredictionService.getPredictionsForSensorEvent(event2))
				.thenReturn(new ArrayList<Prediction>());
		when(mockPredictionService.getPredictionsForSensorEvent(event3))
				.thenReturn(new ArrayList<Prediction>());
		when(mockPredictionService.getPredictionsForSensorEvent(event4))
				.thenReturn(mockPredictions4);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event4);
		assertThat(predictions).isNotNull().isNotEmpty();
		assertThat(predictions.size()).isEqualTo(1);
		assertThat(predictions.get(0).getSensorEvent()).isEqualTo(event2);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(3))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).setCurrentPredictions(mockPredictions4);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event4);

//		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event4);
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event2, event3);
		verify(mockPredictionService).createPredictionFromSensorEvents(event4, event3);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService).getPredictionsForSensorEvent(event4);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event2,
				event3);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event4,
				event3);
		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenPreviousEventAndCurrentEventInOrder_whenOnSensorChangedCalled_thenEventAddedToEndAndNoPredictionReturned() {

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
		event2.setTimestamp(event1.getTimestamp() + minimumDelayBetweenReadings);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event3.setTimestamp(event1.getTimestamp() + 2 * minimumDelayBetweenReadings);

		List<Prediction> mockPredictions = new ArrayList<>();
		mockPredictions.add(new Prediction(event1));

		when(mockPredictionService.createPredictionFromSensorEvents(event1, event2))
				.thenReturn("");
		when(mockPredictionService.getPredictionsForSensorEvent(event1))
				.thenReturn(new ArrayList<Prediction>());
		when(mockPredictionService.getPredictionsForSensorEvent(event2))
				.thenReturn(new ArrayList<Prediction>());
		when(mockPredictionService.getPredictionsForSensorEvent(event3))
				.thenReturn(new ArrayList<Prediction>());

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isEmpty();

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(3))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event3);

//		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event4);
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event2, event3);

		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event2,
				event3);
		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenPreviousEventAndEventToGoAtEndOfCycle_whenOnSensorChangedCalled_thenCycleRolledOverEventAddedToCycleEndFirstEventReturnedAsPrediction() {

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
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event2.setTimestamp(event1.getTimestamp() + minimumDelayBetweenReadings);
		event3.setTimestamp(event1.getTimestamp() - 100000 + cpr.cycleDurationTimeNano);

		List<Prediction> mockPredictions = new ArrayList<>();
		mockPredictions.add(new Prediction(event1));

		when(mockPredictionService.createPredictionFromSensorEvents(event1, event2))
				.thenReturn("");
		when(mockPredictionService.getPredictionsForSensorEvent(event1))
				.thenReturn(new ArrayList<Prediction>());
		when(mockPredictionService.getPredictionsForSensorEvent(event2))
				.thenReturn(new ArrayList<Prediction>());
		when(mockPredictionService.getPredictionsForSensorEvent(event3))
				.thenReturn(mockPredictions);
//		when(mockPredictionService.setCurrentPredictions(new ArrayList<Prediction>()))
//		.thenReturn();

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isNotEmpty();
		assertThat(predictions.size()).isEqualTo(1);
		assertThat(predictions.get(0).getSensorEvent()).isEqualTo(event1);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).setCurrentPredictions(mockPredictions);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event3);

//		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event4);
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event3, event2);

		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event1,
				event2);
		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event3,
				event2);
		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenNewCycle_whenOnSensorChangedCalled_thenEventAddedToCycleAndEmptyPredictionListReturned() {
		// Given

		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				mockPredictionService);

		SensorManager.getInstance().registerListenerForSensor(cpr, gpsSensor);
		// When
		float[] values = { 1.0f, 4.0f }; // gps sensors require lat and long floats
		float[] values2 = { 5.0f, 2.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);

		when(mockPredictionService.getPredictionsForSensorEvent(event1))
				.thenReturn(new ArrayList<Prediction>());

		// Set the event timestamp to after the cycle duration
		event1.setTimestamp(event1.getTimestamp() + cpr.cycleDurationTimeNano);

		long previousCycleStartTime = cpr.getCycleStartTimeNano();
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		long currentCycleStartTime = cpr.getCycleStartTimeNano();

		assertThat(currentCycleStartTime - previousCycleStartTime)
				.isEqualTo(cpr.cycleDurationTimeNano);
		assertThat(predictions).isNotNull().isEmpty();

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(1))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);

		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenMultipleCycleRollovers_whenCurrentSensorEventMatchesPriorInRollover_thenMergedAndFollowingNodeIsPredicted() {

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
		SensorEvent event3 = new SensorEvent(gpsSensor, values);
		event2.setTimestamp(event1.getTimestamp() + minimumDelayBetweenReadings);
		event3.setTimestamp(
				event1.getTimestamp() - 10000 + 2 * cpr.cycleDurationTimeNano);

		List<Prediction> mockPredictions = new ArrayList<>();
		mockPredictions.add(new Prediction(event1));

		when(mockPredictionService.createPredictionFromSensorEvents(event1, event2))
				.thenReturn("");
		when(mockPredictionService.getPredictionsForSensorEvent(event1))
				.thenReturn(new ArrayList<Prediction>());
		when(mockPredictionService.getPredictionsForSensorEvent(event2))
				.thenReturn(new ArrayList<Prediction>());

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		when(mockPredictionService.getPredictionsForSensorEvent(event3))
				.thenReturn(mockPredictions);

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isNotEmpty();
		assertThat(predictions.size()).isEqualTo(1);
		assertThat(predictions.get(0).getSensorEvent()).isEqualTo(event1);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).setCurrentPredictions(mockPredictions);
		verify(mockPredictionService, times(2)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService, times(2)).getPredictionsForSensorEvent(event3);
		verify(mockPredictionService, times(2)).createPredictionFromSensorEvents(event1,
				event2);
		verify(mockPredictionService, times(2)).createPredictionFromSensorEvents(event3,
				event2);
//
//		verify(mockPredictionService).createPredictionFromSensorEvents(event1
//				, event2);
//TODO infinite loop iterating Transitions?
		// TODO multiple sensor events that are equals causes mockito issues. fix??
//		verify(mockPredictionService).createPredictionFromSensorEvents(event3
//				, event2);

//		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
//		verify(mockPredictionService).getPredictionsForSensorEvent(event3);
//		verify(mockPredictionService, times(1)).createPredictionFromSensorEvents(event3,
//				event2);
		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenOneCycleEvent_whenCurrentSensorEventMatches_thenDataPointDurationsMergedAndEventDiscardedNoPredictions() {
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
		SensorEvent event2 = new SensorEvent(gpsSensor, values);
		event2.setTimestamp(event2.getTimestamp() + minimumDelayBetweenReadings);

		List<Prediction> mockPredictions = new ArrayList<>();
		mockPredictions.add(new Prediction(event1));

		when(mockPredictionService.createPredictionFromSensorEvents(event1, event2))
				.thenReturn("");
		when(mockPredictionService.getPredictionsForSensorEvent(event1))
				.thenReturn(new ArrayList<Prediction>());

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(2)).getPredictionsForSensorEvent(event1);
		verifyNoMoreInteractions(mockPredictionService);

	}

	@Test
	public final void givenNonEmptyCycle_whenSignificantlyDifferentEventDataPointArrives_thenNewNodeAddedToEndOfCycle() {
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
		event2.setTimestamp(event2.getTimestamp() + minimumDelayBetweenReadings);

		List<Prediction> mockPredictions = new ArrayList<>();
		mockPredictions.add(new Prediction(event1));

		when(mockPredictionService.createPredictionFromSensorEvents(event1, event2))
				.thenReturn("");
		when(mockPredictionService.getPredictionsForSensorEvent(event1))
				.thenReturn(new ArrayList<Prediction>());
		when(mockPredictionService.getPredictionsForSensorEvent(event2))
				.thenReturn(new ArrayList<Prediction>());

		cpr.onSensorChanged(event1);
		cpr.onSensorChanged(event2);

		// Verify appropriate methods called AND how many times and that no other
		// methods were called
		verify(mockPredictionService, times(2))
				.setCurrentPredictions(new ArrayList<Prediction>());
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService, times(1)).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService).getPredictionsForSensorEvent(event1);
		verify(mockPredictionService).getPredictionsForSensorEvent(event2);
		verify(mockPredictionService).createPredictionFromSensorEvents(event1, event2);

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
//		CouchbaseRepository.getInstance().removeAllDocuments("transition");
	}

	@After
	public void tearDown() throws Exception {
	}

}
