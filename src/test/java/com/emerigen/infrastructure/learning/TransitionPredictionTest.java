package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class TransitionPredictionTest {

	@Test
	public final void givenPredictionCountZeroOrLess_whenSetProbability_thenIllegalArgumentException() {
		Prediction prediction = new Prediction(new SensorEvent());

		final Throwable throwable = catchThrowable(() -> prediction.setProbability(0));

		then(throwable).as(
				"Zero prediction countfor setProbability throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullSensorEvent_whenTransitionCreated_thenIllegalArgumentException() {

		final Throwable throwable = catchThrowable(() -> new Prediction(null));

		then(throwable).as(
				"Null sensor event creating Transition prediction throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenTwoTransitionPredictions_whenProbabilitySet_thenProbabilityEquals50Percent() {
		Prediction prediction = new Prediction(new SensorEvent());
		prediction.setProbability(2);
		assertThat(prediction.getProbability()).isEqualTo(0.5);
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
