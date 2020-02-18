package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
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
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class CPR_PredictionTest {
	private static long defaultCycleNodeDurationNano = Long
			.parseLong(EmerigenProperties.getInstance().getValue("cycle.default.data.point.duration.nano"));

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
			sensor = SensorManager.getInstance().getDefaultSensorForLocation(sensorType, sensorLocation);
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

	@Test
	public final void givenExpectedProbability_whenCreated_thenProbabilityCorrect() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNegativeProbability_whenCreated_thenIllegalArgumentException() {
		Cycle cycle = new DailyCycle(Sensor.TYPE_GPS, 1);
		CycleNode node = new CycleNode(cycle, new SensorEvent());

		CyclePrediction prediction = new CyclePrediction(node);

		final Throwable throwable = catchThrowable(() -> prediction.setProbability(-1));

		then(throwable).as("negative probability throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNewEventOnExistingCycle_whenOnSensorChangedCalled_thenEventAddedAt1stPositionAndFollowingEventIsReturnedAsPrediction() {
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

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
		event4.setTimestamp(event1.getTimestamp() - 1000 + gpsCycle.cycleDurationTimeNano);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event4);
		assertThat(predictions).isNotNull().isNotEmpty();
		assertThat(predictions.size()).isEqualTo(1);
		assertThat(predictions.get(0).getSensorEvent()).isEqualTo(event1);
	}

	@Test
	public final void givenNewEventOnExistingCycle_whenOnSensorChangedCalled_thenEventAddedAtAppropriatePositionAndFollowingEventIsReturnedAsPrediction() {
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

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
		event4.setTimestamp(event1.getTimestamp() + 1000 + gpsCycle.cycleDurationTimeNano);

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
	}

	@Test
	public final void givenPreviousEventAndCurrentEventLastInOrder_whenOnSensorChangedCalled_thenEventAddedToCycleAndEmptyPredictionListReturned() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
//		event3.setTimestamp(event1.getTimestamp() - 10000 + gpsCycle.cycleDurationTimeNano);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isEmpty();
	}

	@Test
	public final void givenPreviousEventAndCurrentEventInOrder_whenOnSensorChangedCalled_thenEventAddedToEndAndNoPredictionReturned() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isEmpty();
	}

	@Test
	public final void givenPreviousEventAndEventToGoAtEndOfCycle_whenOnSensorChangedCalled_thenEventAddedToCycleEndCycleRolledOverAndFirstPredictionListReturned() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event3.setTimestamp(event1.getTimestamp() - 10000 + gpsCycle.cycleDurationTimeNano);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isNotEmpty();
		assertThat(predictions.size()).isEqualTo(1);
		assertThat(predictions.get(0).getSensorEvent()).isEqualTo(event1);
	}

	@Test
	public final void givenNonEmptyCycle_whenOnSensorChangedCalledWithRolloverTime_thenEventAddedToCycleEndAndEmptyPredictionListReturned() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
//		event3.setTimestamp(event3.getTimestamp() - 1000 + gpsCycle.cycleDurationTimeNano);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isEmpty();
		assertThat(gpsCycle.getNodeList().get(0).getSensorEvent()).isEqualTo(event1);
		assertThat(gpsCycle.getNodeList().get(1).getSensorEvent()).isEqualTo(event2);
		assertThat(gpsCycle.getNodeList().get(2).getSensorEvent()).isEqualTo(event3);
	}

	@Test
	public final void givenNewCycle_whenOnSensorChangedCalled_thenEventAddedToCycleAndEmptyPredictionListReturned() {
		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		SensorManager.getInstance().registerListenerForSensor(cpr, gpsSensor);
		// When
		float[] values = { 1.0f, 4.0f }; // gps sensors require lat and long floats
		float[] values2 = { 5.0f, 2.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);

		// Set the event timestamp to after the cycle duration
		event1.setTimestamp(event1.getTimestamp() + gpsCycle.cycleDurationTimeNano);

		long previousCycleStartTime = gpsCycle.getCycleStartTimeNano();
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		long currentCycleStartTime = gpsCycle.getCycleStartTimeNano();

		assertThat(currentCycleStartTime - previousCycleStartTime).isEqualTo(gpsCycle.cycleDurationTimeNano);
		assertThat(predictions).isNotNull().isEmpty();
	}

	@Test
	public final void givenNewCycle_whenOnSensorChangedCalledWithEventPastCycleDuration_thenCycleRolledOverAndEventAddedAndEmptyPredictionListReturned() {
		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f, 4.0f }; // gps sensors require lat and long floats
		float[] values2 = { 5.0f, 2.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);

		// Set the event timestamp to after the cycle duration
		event1.setTimestamp(event1.getTimestamp() + gpsCycle.cycleDurationTimeNano);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

		long previousCycleStartTime = gpsCycle.getCycleStartTimeNano();
		List<Prediction> predictions = cpr.onSensorChanged(event1);
		long currentCycleStartTime = gpsCycle.getCycleStartTimeNano();

		assertThat(currentCycleStartTime - previousCycleStartTime).isEqualTo(gpsCycle.cycleDurationTimeNano);
		assertThat(predictions).isNotNull().isEmpty();
	}

	@Test
	public final void givenMultipleCycleRollovers_whenCurrentSensorEventMatchesPriorInRollover_thenMergedAndFollowingNodeIsPredicted() {

		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		SensorEvent event3 = new SensorEvent(gpsSensor, values);
		event3.setTimestamp(event1.getTimestamp() + 2 * gpsCycle.cycleDurationTimeNano);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();

		predictions = cpr.onSensorChanged(event3);
		assertThat(predictions).isNotNull().isNotEmpty();
		assertThat(predictions.size()).isEqualTo(1);
		assertThat(predictions.get(0).getSensorEvent()).isEqualTo(event2);
	}

	@Test
	public final void givenOneCycleNode_whenCurrentSensorEventMatches_thenDataPointDurationsMergedAndEventDiscardedNoPredictions() {
		// Given
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values);

		List<Prediction> predictions = cpr.onSensorChanged(event1);
		assertThat(predictions).isNotNull().isEmpty();

		long duration = gpsCycle.nodeList.get(0).getDataPointDurationNano();
		predictions = cpr.onSensorChanged(event2);
		assertThat(predictions).isNotNull().isEmpty();
		assertThat(gpsCycle.nodeList.size()).isEqualTo(1);
		assertThat(gpsCycle.nodeList.get(0).getDataPointDurationNano())
				.isEqualTo(defaultCycleNodeDurationNano + duration);
		assertThat(gpsCycle.nodeList.get(0).getSensorEvent()).isEqualTo(event2);
	}

	@Test
	public final void givenNonEmptyCycle_whenSignificantlyDifferentEventDataPointArrives_thenNewNodeAddedToEndOfCycle() {
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(0);

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		cpr.onSensorChanged(event1);
		cpr.onSensorChanged(event2);

		// Then
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(2);
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
