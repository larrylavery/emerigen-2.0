package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class CycleTest {

	@Test
	public final void givenDifferentCycleTypes_whenCycleMerged_thenIllegalArgurmentException() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNullSensorEvents_whenCycleCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> new DailyCycle(null));

		then(throwable)
				.as("A null sensorEvent list for cycle CTOR throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenEmptySensorEvents_whenCycleCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// When the instance is validated
		final Throwable throwable = catchThrowable(
				() -> new DailyCycle(new ArrayList<SensorEvent>()));

		then(throwable)
				.as("A empty sensorEvent list for cycle ctor throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenNullSensorEvent_whenCycleNodeCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// public CycleNode(SensorEvent sensorEvent, long originStartTimeMillis) {

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> new CycleNode(null, 1));

		then(throwable).as("A null sensorEvent list throws a CycleNode  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenNonPositiveCycleStartTime_whenCycleNodeCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// public CycleNode(SensorEvent sensorEvent, long originStartTimeMillis) {

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> new CycleNode(new SensorEvent(), -11));

		then(throwable)
				.as("A non positive cycle start time throws a CycleNode  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenDailyCycle_whenCycleCreated_thenCycleStartTimeMustBe12am() {
		DailyCycle dc = new DailyCycle();

		ZoneId zoneId = ZoneId.of("US/Central");
		ZonedDateTime todayStart = ZonedDateTime.now(zoneId).toLocalDate().atStartOfDay(zoneId);

		assertThat(dc.getCycleStartTimeMillis()).isEqualTo(todayStart.getSecond() * 1000);
	}

	@Test
	public final void givenDailyCycle_whenCycleCreated_thenDurationMustBe24Hours() {
		DailyCycle dc = new DailyCycle();
		assertThat(dc.getCycleDurationMillis()).isEqualTo(24 * 60 * 60 * 1000);
	}

	@Test
	public final void givenHourlyCycle_whenCycleCreated_thenOriginTimeMustBe0Minutes() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenWeeklyCycle_whenCycleCreated_thenOriginTimeMustBeSunday12am() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenWeeklyCycle_whenCycleCreated_thenDurationMustBe168Hours() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenMonthlyCycle_whenCycleCreated_thenOriginTimeMustBe12amFirstDayOfMonth() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenMonthlyCycle_whenCycleCreated_thenDurationMustBeApproximately30Days() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenYearlyCycle_whenCycleCreated_thenOriginTimeMustBeJan1_12am() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenYearlyCycle_whenCycleCreated_thenDurationMustBeApproximately365DaysTimes24Hours() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNonPositiveDataPointDuration_whenCycleNodeCreated_thenIllegalArgurmentException() {
//		private CycleNode(SensorEvent sensorEvent, long startTimeMillis, long cycleStartTimeMillis,
//				long dataPointDurationMillis, double allowableStandardDeviationForEquality) {

		final Throwable throwable = catchThrowable(
				() -> new CycleNode(new SensorEvent(), 1, 1, -1, 1.2));

		then(throwable).as("A non positive data point duration throws a  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNonPositiveStandardDeviation_whenCycleNodeCreated_thenIllegalArgurmentException() {
//		private CycleNode(SensorEvent sensorEvent, long startTimeMillis, long cycleStartTimeMillis,
//		long dataPointDurationMillis, double allowableStandardDeviationForEquality) {

		final Throwable throwable = catchThrowable(
				() -> new CycleNode(new SensorEvent(), 1, 1, 1, -1.2));

		then(throwable).as("A non positive data point duration throws a  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenDifferenceDouble_whenGetStandardDeviation_thenValid() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenValidParameters_whenDifferenceCalculatedBySensor_thenCorrect() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenDifferencePastEqualityThreshold_whenGetStandardDeviation_thenNotEqual() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenInts_whenGetStandardDeviation_thenValueCorrect() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenFloats_whenGetStandardDeviation_thenValueCorrect() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenDoubles_whenGetStandardDeviation_thenValueCorrect() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNodesWithinStandardDeviationForEquality_whenCheckedForEquality_thenTrue() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNodesNotWithinStandardDeviationForEquality_whenCheckedForEquality_thenFalse() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNodeWithValidStartTimeAndOffst_whenRetrieved_thenCorrect() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenValidNodeStartTime_whenSetOnNode_thenTrue() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenInvalidNodeStartTime_whenSetOnNode_thenFalse() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenSensorEventListWithDifferentSensorTypes_whenCreatingCycle_thenIllegalArgumentException() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenValidCycleNodes_whenDurationsAdded_thenMustEqualCycleDuration() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNonPositiveDataPointDuration_whenSetOnNode_thenIllegalArgumentException() {
		SoftAssertions softly = new SoftAssertions();

		// public CycleNode(SensorEvent sensorEvent, long originStartTimeMillis) {
		CycleNode cn = new CycleNode(null, 1);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> cn.setDataPointDurationMillis(-1));

		then(throwable).as("A non positive duration throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenNullSensorEvent_whenSetOnNode_thenIllegalArgumentException() {
		SoftAssertions softly = new SoftAssertions();

		// public CycleNode(SensorEvent sensorEvent, long originStartTimeMillis) {
		CycleNode cn = new CycleNode(new SensorEvent(), 1);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> cn.setSensorEvent(null));

		then(throwable).as("A null sensor event on cycle node throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenValidSensorEvents_whenCycleCreated_thenNodesMustBeInAscendingTimeOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNullCycleNode_whenMerged_thenIllegalArgumentException() {
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
