package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;

public class CyclePatternRecognizerTest {

	@Test
	public final void givenNewDefaultSensor_whenCreated_thenAllCyclePatternRecognizersAndTransitionPRsAreRegistered() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNewEvent_whenOnSensorChangedCalled_thenEventAddedAtAppropriatePositionAndFollowingEventIsReturnedAsPrediction() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenEventExistingInCycle_whenOnSensorChangedCalled_thenPositionAfterEventReturnedAsPrediction() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNewEventPastCycleEnd_whenOnSensorChangedCalled_thenCycleRolledOverAndEventInsertAndFollowingEventReturnedAsPrediction() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNewEventExistingPriorToCycleStart_whenOnSensorChangedCalled_thenEventOutOfOrderException() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNewEventExistingPriorToPreviousEvent_whenOnSensorChangedCalled_thenEventOutOfOrderException() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenPreviousEventAndCurrentEventLastInOrder_whenOnSensorChangedCalled_thenEventAddedToCycleAndEmptyPredictionListReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenPreviousEventAndCurrentEventInOrder_whenOnSensorChangedCalled_thenEventAddedToCycleAndOnePredictionReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenPreviousEventAndEventToGoAtEndOfCycle_whenOnSensorChangedCalled_thenEventAddedToCycleEndCycleRolledOverAndFirstPredictionListReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNewCycle_whenOnSensorChangedCalled_thenEventAddedToCycleAndEmptyPredictionListReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNewCycle_whenOnSensorChangedCalledWithEventPastCycleDuration_thenCycleRolledOverAndEventAddedAndEmptyPredictionListReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenValidCycleNodes_whenDurationsAdded_thenMustBeLessThanCycleDuration() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenMultipleCyclesForSensor_whenCurrentSensorEventMatches_thenCorrectCycleNodesArePredicted() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNonEmptyCycle_whenNewSensorEventWithTimePastSeveralExistingEvents_thenAddedAfterMostRecentPriorEvent() {

		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f, 1.0f };
		float[] values2 = { 10.0f, 10.0f };
		float[] values3 = { 100.0f, 100.0f };
		float[] values4 = { 1000.0f, 1000.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);

		// this event will go between event2 and event4 after cycle rolled over 1 period
		// (24 hours in this case)
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		event3.setTimestamp(event3.getTimestamp() + gpsCycle.cycleDurationTimeNano);
		SensorEvent event4 = new SensorEvent(gpsSensor, values4);
		event4.setTimestamp(event3.getTimestamp() + 100);
		gpsCycle.onSensorChanged(event1);
		gpsCycle.onSensorChanged(event2);
		gpsCycle.onSensorChanged(event4);
		gpsCycle.onSensorChanged(event3);

		// Then
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(4);
		assertThat(gpsCycle.getNodeList().get(0).getSensorEvent()).isEqualTo(event1);
		assertThat(gpsCycle.getNodeList().get(1).getSensorEvent()).isEqualTo(event2);
		assertThat(gpsCycle.getNodeList().get(2).getSensorEvent()).isEqualTo(event3);
		assertThat(gpsCycle.getNodeList().get(3).getSensorEvent()).isEqualTo(event4);

	}

	@Test
	public final void givenNonEmptyCycle_whenNewSensorEventPastAllExistingEvents_thenAddedToEnd() {

		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f, 1.0f };
		float[] values2 = { 10.0f, 10.0f };
		float[] values3 = { 100.0f, 100.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		SensorEvent event3 = new SensorEvent(gpsSensor, values3);
		gpsCycle.onSensorChanged(event1);
		gpsCycle.onSensorChanged(event2);
		gpsCycle.onSensorChanged(event3);

		// Then
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(3);
		assertThat(gpsCycle.getNodeList().get(0).getSensorEvent()).isEqualTo(event1);
		assertThat(gpsCycle.getNodeList().get(1).getSensorEvent()).isEqualTo(event2);
		assertThat(gpsCycle.getNodeList().get(2).getSensorEvent()).isEqualTo(event3);

	}

	@Test
	public final void givenNonEmptyCycle_whenNewEqualSensorEvent_thenMergedAndPreviousReplaced() {

		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f, 1.0f };
		float[] values2 = { 10.0f, 10.0f };
		float[] values3 = { 100.0f, 100.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values);
		gpsCycle.onSensorChanged(event1);
		gpsCycle.onSensorChanged(event2);

		// Then
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(1);
		assertThat(gpsCycle.getNodeList().get(0).getSensorEvent()).isEqualTo(event1);
	}

	@Test
	public final void givenEmptyCycle_whenNewSensorEvent_thenAddedAndTimeOffsetCorrect() {

		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f, 1.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		gpsCycle.onSensorChanged(event1);

		// Then
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(1);
		assertThat(gpsCycle.getNodeList().get(0).getSensorEvent()).isEqualTo(event1);
	}

	@Test
	public final void givenNewSensorEventWithDifferentSensorType_whenOnNewEvent_thenIllegalArgumentException() {
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Sensor hrSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		float[] values = { 1.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(hrSensor, values);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		final Throwable throwable = catchThrowable(() -> gpsCycle.onSensorChanged(event2));

		then(throwable).as("A different sensor type event  throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNonEmptyCycleList_whenSignificantlyDifferentEventDataPointArrives_thenNewNodeAddedToEndOfCycle() {

		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f, 1.0f };
		float[] values2 = { 10.0f, 10.0f };
		float[] values3 = { 100.0f, 100.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values3);
		gpsCycle.onSensorChanged(event1);
		gpsCycle.onSensorChanged(event2);

		// Then
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(2);
		assertThat(gpsCycle.getNodeList().get(0).getSensorEvent()).isEqualTo(event1);
		assertThat(gpsCycle.getNodeList().get(1).getSensorEvent()).isEqualTo(event2);
	}

	@Test
	public final void givenNonEmptyCycleList_whenEqualNewNodeArrives_thenCycleContainsMergedNode() {

		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f, 1.0f };
		float[] values2 = { 10.0f, 10.0f };
		float[] values3 = { 100.0f, 100.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values);
		gpsCycle.onSensorChanged(event1);
		gpsCycle.onSensorChanged(event2);

		// Then
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(1);
	}

	@Test
	public final void givenNonEmptyCycleList_whenEqualNewNodeArrives_thenMergedNodeDurationisAdditionOfPreviousAndNewNodes() {

		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f, 1.0f };
		float[] values2 = { 10.0f, 10.0f };
		float[] values3 = { 100.0f, 100.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values);
		event2.setTimestamp(event2.getTimestamp() + 10000);
		gpsCycle.onSensorChanged(event1);
		gpsCycle.onSensorChanged(event2);

		// Then
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(1);
		assertThat(gpsCycle.getNodeList().get(0).getDataPointDurationNano()).isGreaterThan(10000);
	}

	@Test
	public final void givenEmptyCycleList_whenNewNodeArrives_thenNodeTimeOffsetMustBeDifferenceOfcycleStartAndEventTimestamp() {

		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		gpsCycle.onSensorChanged(event1);

		// Then
		long timeOffset = event1.getTimestamp() - gpsCycle.getCycleStartTimeNano();
		assertThat(gpsCycle.getNodeList().get(0).getStartTimeOffsetNano()).isEqualTo(timeOffset);
	}

	@Test
	public final void givenNonEmptyCycleList_whenNewEventArrivesWithTimestampBeforePreviousEvent_thenIllegalArgumentException() {
		// Given
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);

		// When
		float[] values = { 1.0f, 4.0f }; // gps sensors require lat and long floats
		float[] values2 = { 5.0f, 2.0f };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		event1.setTimestamp(event1.getTimestamp() - 1000);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		gpsCycle.onSensorChanged(event2);
		final Throwable throwable = catchThrowable(() -> gpsCycle.onSensorChanged(event1));

		then(throwable).as("An out of order sensor event throws  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenCycleList_whenNewEventArrivesPastCycleDuration_thenCycleStartTimeUpdatedToStartTimeOf_Closest_NextCycle() {
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

		long previousCycleStartTime = gpsCycle.getCycleStartTimeNano();
		gpsCycle.onSensorChanged(event1);
		long currentCycleStartTime = gpsCycle.getCycleStartTimeNano();

		assertThat(currentCycleStartTime - previousCycleStartTime)
				.isEqualTo(gpsCycle.cycleDurationTimeNano);
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
