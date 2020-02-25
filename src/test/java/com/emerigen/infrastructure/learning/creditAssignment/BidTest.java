package com.emerigen.infrastructure.learning.creditAssignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.learning.creditassignment.Bid;

public class BidTest {

	@Test
	public final void givenSetOfEqualBids_whenMaxBidRequested_thenAnyReturned() {
		Transition t = new Transition();
		Bid bid = new Bid(t, 1001.0);
		Bid bid2 = new Bid(t, 1001.0);
		Bid bid3 = new Bid(t, 999.0);
		Bid bid4 = new Bid(t, 1000.1);

		List<Bid> bidders = new ArrayList<Bid>();
		bidders.add(bid);
		bidders.add(bid2);
		bidders.add(bid3);
		bidders.add(bid4);

		Optional<Bid> winningBid = bidders.stream()
				.max((bidx, bidy) -> bidx.compareTo(bidy));

		assertThat(winningBid.get()).isEqualTo(bid2);
	}

	@Test
	public final void givenSetOfDifferentBids_whenMaxBidRequested_thenCorrectReturned() {
		Transition t = new Transition();
		Bid bid = new Bid(t, 1000.0);
		Bid bid2 = new Bid(t, 1001.0);
		Bid bid3 = new Bid(t, 999.0);
		Bid bid4 = new Bid(t, 1000.1);

		List<Bid> bidders = new ArrayList<Bid>();
		bidders.add(bid);
		bidders.add(bid2);
		bidders.add(bid3);
		bidders.add(bid4);

		Optional<Bid> winningBid = bidders.stream()
				.max((bidx, bidy) -> bidx.compareTo(bidy));

		assertThat(winningBid.get()).isEqualTo(bid2);
	}

	@Test
	public final void givenValidBid_whenEqualBidCompareTo_thenZeroReturned() {
		Transition t = new Transition();
		Bid bid = new Bid(t, 1000.0);
		Bid bid2 = new Bid(t, 1000.0);

		assertThat(bid2.compareTo(bid)).isEqualTo(0);
	}

	@Test
	public final void givenValidBid_whenLessorCompareTo_thenMinusOneReturned() {
		Transition t = new Transition();
		Bid bid = new Bid(t, 10002.0);
		Bid bid2 = new Bid(t, 1001.0);

		assertThat(bid2.compareTo(bid)).isEqualTo(-1);
	}

	@Test
	public final void givenValidBid_whenGreaterBidCompareTo_thenOneReturned() {
		Transition t = new Transition();
		Bid bid = new Bid(t, 10002.0);
		Bid bid2 = new Bid(t, 1001.0);

		assertThat(bid.compareTo(bid2)).isEqualTo(1);
	}

	@Test
	public final void givenValidBid_whenBidAmountRetrieved_thenEqualToSupplied() {
//		public Bid(PredictionConsumer bidder, double amount) {
		Transition t = new Transition();
		Bid bid = new Bid(t, 1000.0);

		assertThat(bid.getAmount()).isEqualTo(1000.0);
	}

	@Test
	public final void givenValidBid_whenPredictionConsumerRetrieved_thenEqualToSupplied() {
//		public Bid(PredictionConsumer bidder, double amount) {
		Transition t = new Transition();
		Bid bid = new Bid(t, 1000.0);

		assertThat(bid.getPredictionConsumer()).isEqualTo(t);
	}

	@Test
	public final void givenNullPredictionConsumer_whenSettingPredictionConsumer_thenIllegalArgumentException() {
//		public Bid(PredictionConsumer bidder, double amount) {
		Transition t = new Transition();
		Bid bid = new Bid(t, 100.0);
		final Throwable throwable = catchThrowable(() -> bid.setPredictionConsumer(null));

		then(throwable).as("A negative amount when bid set gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNegativeBidAmount_whenSettingBid_thenIllegalArgumentException() {
//		public Bid(PredictionConsumer bidder, double amount) {
		Transition t = new Transition();
		Bid bid = new Bid(t, 100.0);
		final Throwable throwable = catchThrowable(() -> bid.setAmount(-1.0));

		then(throwable).as("A negative amount when bid set gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNegativeBidAmount_whenBidCreated_thenIllegalArgumentException() {
//		public Bid(PredictionConsumer bidder, double amount) {
		Transition t = new Transition();
		final Throwable throwable = catchThrowable(() -> new Bid(new Transition(), -1.0));

		then(throwable).as("A negative amount gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullPredictionConsumer_whenBidCreated_thenIllegalArgumentException() {
//		public Bid(PredictionConsumer bidder, double amount) {
		Transition t = new Transition();
		final Throwable throwable = catchThrowable(() -> new Bid(null, 1.0));

		then(throwable).as("A null PredictionConsumer gets IllegalArgumentException")
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
