package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;

public class CyclePredictionTest {

	@Test
	public final void givenExpectedProbability_whenCreated_thenProbabilityCorrect() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNullCycleNode_whenCreated_thenIllegalArgumentException() {

		final Throwable throwable = catchThrowable(() -> new CyclePrediction((CycleNode) null));

		then(throwable).as("Null cycle node on creation throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
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
