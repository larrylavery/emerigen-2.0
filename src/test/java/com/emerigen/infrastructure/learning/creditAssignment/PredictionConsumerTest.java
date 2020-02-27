package com.emerigen.infrastructure.learning.creditAssignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.learning.creditassignment.Bid;
import com.emerigen.infrastructure.sensor.SensorEvent;

public class PredictionConsumerTest {

	@Test
	public final void givenValidPrediction_whenMatchingPredictionInvoked_thenBidOfDefaultPercentageReturned() {
		Transition t = new Transition();
		t.setCashOnHand(100.0);
		Prediction prediction = new Prediction(new SensorEvent());
		Bid bid = t.matchingPrediction(prediction);
		assertThat(bid.getAmount()).isEqualTo(10.0);
	}

	@Test
	public final void givenNullPrediction_whenMatchingPredictionInvoked_thenIllegalArgumentException() {
		Transition t = new Transition();
		final Throwable throwable = catchThrowable(() -> t.matchingPrediction(null));

		then(throwable).as("A null predictionhrows an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNegativeWinningBid_whenMakePayment_thenIllegalArgumentException() {
		Transition t = new Transition();
		final Throwable throwable = catchThrowable(() -> t.makePayment(-0.2));

		then(throwable).as("A negative winningBid an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNegativePayment_whenMakePayment_thenIllegalArgumentException() {
		Transition t = new Transition();
		final Throwable throwable = catchThrowable(() -> t.makePayment(-0.2));

		then(throwable).as("A negative payment an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullPrediction_whenReturnedFromMakePayment_thenIllegalArgumentException() {
		Transition t = new Transition();
		final Throwable throwable = catchThrowable(() -> t.makePayment(-0.2));

		then(throwable).as(
				"A null returned prediction from makePayment an IllegalArgumentException")
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
