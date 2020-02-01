package com.emerigen.infrastructure.learning;

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

public class CycleLearningTest {

	Cycle myCycle2 = new DailyCycle(1);

	@Test
	public final void givenNodeWithValidStartTimeAndOffst_whenRetrieved_thenCorrect() {
		fail("Not yet implemented"); // TODO
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
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS);

		final Throwable throwable = catchThrowable(() -> gpsCycle.onNewSensorEvent(event2));

		then(throwable).as("A different sensor type event  throws an  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenValidCycleNodes_whenDurationsAdded_thenMustEqualCycleDuration() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenValidSensorEvents_whenCycleCreated_thenNodesMustBeInAscendingTimeOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenValidCycle_whenCurrentNodeBeforeLastNodeSelected_thenPredictionsMustBeValid() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenValidSortedSensorEvents_whenNodesCreated_thenTimestampsAndOffsetsValid() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenValidSortedSensorEvents_whenNodesCreated_thenTimestampsAndOffsetsInAscendingOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenMultipleValidCyclesForSensor_whenCurrentNodeBeforeLastNodeSelectedOnAnyCycle_thenPredictionsOnAllCyclesMustBeReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenMultipleCyclesForSensor_whenCurrentSensorEventMatches_thenCorrectCycleNodesArePredicted() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenMultipleCyclesForSensor_whenCyclesMerged_thenCyclesMergedAtJoinedPoints() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenMultipleCyclesForSensor_whenCyclesMerged_thenCyclesBranchedAtDistinctPoints() {
		fail("Not yet implemented"); // TODO
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
