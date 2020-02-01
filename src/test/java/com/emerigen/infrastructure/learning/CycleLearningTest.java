package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CycleLearningTest {

	Cycle myCycle = new WeeklyCycle() {

		@Override
		public long calculateCycleStartTimeMillis() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long calculateCycleDurationMillis() {
			// TODO Auto-generated method stub
			return 0;
		}
	};
	Cycle myCycle2 = new MonthlyCycle() {

		@Override
		public long calculateCycleStartTimeMillis() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long calculateCycleDurationMillis() {
			// TODO Auto-generated method stub
			return 0;
		}
	};

	@Test
	public final void givenNodeWithValidStartTimeAndOffst_whenRetrieved_thenCorrect() {
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
