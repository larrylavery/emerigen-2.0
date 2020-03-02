package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.sensor.SensorEvent;

public class Transition_MetadataTest {

	@Test
	public final void givenManyPredictionAttemptsButNoSuccesses_whenAccuracyRequested_thenZeroReturned() {

		Transition t = new Transition();
		t.setNumberOfPredictionAttempts(100);
		t.setNumberOfSuccessfulPredictions(0);
		double accuracy = t.getPredictionAccuracy();
		then(accuracy).isEqualTo(0.0);
	}

	@Test
	public final void givenNoPredictionAttempts_whenAccuracyRequested_thenZeroReturned() {

		Transition t = new Transition();
		t.setNumberOfPredictionAttempts(0);
		t.setNumberOfSuccessfulPredictions(0);
		double accuracy = t.getPredictionAccuracy();
		then(accuracy).isEqualTo(0.0);
	}

	@Test
	public final void givenValidAttemptsAndSuccesses_whenAccuracyRequested_thenReturned() {

		Transition t = new Transition();
		t.setNumberOfPredictionAttempts(10);
		t.setNumberOfSuccessfulPredictions(2);
		double accuracy = t.getPredictionAccuracy();
		then(accuracy).isEqualTo(0.2);
	}

	@Test
	public final void givenValidPrediction_whenSet_thenReturned() {

		Transition t = new Transition();
		t.setPredictedSensorEvent(new SensorEvent());
		Prediction p = t.getPrediction();
		then(p).isNotNull();
	}

	@Test
	public final void givenNullPrediction_whenSet_thenIllegalArgumentException() {

		Transition t = new Transition();

		final Throwable throwable = catchThrowable(() -> t.setPredictedSensorEvent(null));

		// Then
		then(throwable).as("Null prediction gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenValidCashonHand_whenSet_thenReturned() {

		Transition t = new Transition();
		t.setCashOnHand(20.0);
		double cash = t.getCashOnHand();
		then(cash).isEqualTo(20.0);
	}

	@Test
	public final void givenNegativeCashOnHand_whenSet_thenIllegalArgumentException() {

		Transition t = new Transition();

		final Throwable throwable = catchThrowable(() -> t.setCashOnHand(-1.0));

		// Then
		then(throwable).as("Negative cash on hand gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenValidDataPointDuration_whenSet_thenReturned() {

		Transition t = new Transition();
		t.setDataPointDurationNano(20);
		long dpDuration = t.getDataPointDurationNano();
		then(dpDuration).isEqualTo(20);
	}

	@Test
	public final void givenNegativeDataPointDuration_whenSet_thenIllegalArgumentException() {

		Transition t = new Transition();

		final Throwable throwable = catchThrowable(() -> t.setDataPointDurationNano(-1));

		// Then
		then(throwable).as("Negative data point duration gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenValidProbability_whenSet_thenReturned() {

		Transition t = new Transition();
		t.setProbability(0.2);
		double prob = t.getProbability();
		then(prob).isEqualTo(0.2);
	}

	@Test
	public final void givenNegativeProbability_whenSet_thenIllegalArgumentException() {

		Transition t = new Transition();

		final Throwable throwable = catchThrowable(() -> t.setProbability(-1.0));

		// Then
		then(throwable).as("Negative probability  gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenOutOfRangeProbability_whenSet_thenIllegalArgumentException() {

		Transition t = new Transition();

		final Throwable throwable = catchThrowable(() -> t.setProbability(1.01));

		// Then
		then(throwable).as("probability > 1  gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenLastSuccessfulPredictionTimestamp_whenSet_thenReturned() {

		Transition t = new Transition();
		t.setLastSuccessfulPredictionTimestamp(20);
		long lastSuccessfulPredictions = t.getLastSuccessfulPredictionTimestamp();
		then(lastSuccessfulPredictions).isEqualTo(20);
	}

	@Test
	public final void givenNegativeLastSuccessfulPredictionTimestamp_whenSet_thenIllegalArgumentException() {

		Transition t = new Transition();

		final Throwable throwable = catchThrowable(
				() -> t.setLastSuccessfulPredictionTimestamp(-1));

		// Then
		then(throwable).as(
				"Negative timestamp for last successful prediction gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNumberOfPredictionAttempts_whenSet_thenReturned() {

		Transition t = new Transition();
		t.setNumberOfPredictionAttempts(20);
		long attempts = t.getNumberOfPredictionAttempts();
		then(attempts).isEqualTo(20);
	}

	@Test
	public final void givenNegativePredictionAttempts_whenSet_thenIllegalArgumentException() {

		Transition t = new Transition();

		final Throwable throwable = catchThrowable(
				() -> t.setNumberOfPredictionAttempts(-1));

		// Then
		then(throwable)
				.as("Negative number of prediction attempts get IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNumberOfSuccessfulPredictions_whenSet_thenReturned() {

		Transition t = new Transition();
		t.setNumberOfSuccessfulPredictions(20);
		long successes = t.getNumberOfSuccessfulPredictions();
		then(successes).isEqualTo(20);
	}

	@Test
	public final void givenNegativeSuccessfulPredictions_whenSet_thenIllegalArgumentException() {

		Transition t = new Transition();

		final Throwable throwable = catchThrowable(
				() -> t.setNumberOfSuccessfulPredictions(-1));

		// Then
		then(throwable).as(
				"Negative number of successful predictions get IllegalArgumentException")
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
