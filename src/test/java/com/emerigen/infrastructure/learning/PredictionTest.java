package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class PredictionTest {

	@Test
	public final void givenNullSensorEvent_whenCreated_thenIllegalArgumentException() {

		final Throwable throwable = catchThrowable(() -> new Prediction(null));

		then(throwable).as("null sensor event throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNegativeProbability_whenSet_thenIllegalArgumentException() {

		Prediction prediction = new Prediction(new SensorEvent());

		final Throwable throwable = catchThrowable(() -> prediction.setProbability(-1.0));

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
