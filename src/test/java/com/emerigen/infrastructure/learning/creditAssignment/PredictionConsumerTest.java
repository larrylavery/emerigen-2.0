package com.emerigen.infrastructure.learning.creditAssignment;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PredictionConsumerTest {

	@Test
	public final void givenValidPrediction_whenMatchingPredictionInvoked_thenBidOfDefaultPercentageReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNullPrediction_whenMatchingPrediction_thenIllegalArgumentException() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNegativeWinningBid_whenMakePayment_thenIllegalArgumentException() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNullPrediction_whenReturnedFromMakePayment_thenIllegalArgumentException() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNegativePayment_whenMakePayment_thenIllegalArgumentException() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNullSensor_whePredictionConsumerConstructed_thenIllegalArgumentException() {
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
