package com.emerigen.infrastructure.learning.creditAssignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.learning.creditassignment.PredictionSupplier;

public class PredictionSupplierTest {

//	void acceptPaymentFromWinningBidder(double winningBid);

	@Test
	public final void givenValidWinningBid_whenacceptPaymentFromWinningBidderInvoked_thenCashOnHandReducedByThatAmount() {
		Transition t = new Transition();
		t.setCashOnHand(100.0);
		t.acceptPaymentFromWinningBidder(9.0);
		assertThat(t.getCashOnHand()).isEqualTo(109.0);
	}

	@Test
	public final void givenNegativeWinningBid_whenacceptPaymentFromWinningBidderInvoked_thenIllegalArgumentException() {
		PredictionSupplier t = new Transition();
		final Throwable throwable = catchThrowable(
				() -> t.acceptPaymentFromWinningBidder(-2.0));

		then(throwable).as(
				"A negative payment from winning bidder throws an IllegalArgumentException")
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
