package com.emerigen.infrastructure.environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MessageSpreadingTest {

	@Test
	public final void givenValidMessage_whenHopsIncrementedPastMaxHops_thenHopsExceededReturnsTrue() {

		// Given
		MessageToSpread msg = new MessageToSpread(" ", 2, 2, (obj1, obj2) -> {
			return null;
		});

		// When hops incremented
		assertThat(msg.hopsExceeded()).isEqualTo(false);
		MessageToSpread newMsg = msg.incrementHops();
		assertThat(newMsg.hopsExceeded()).isEqualTo(true);
	}

	@Test
	public final void givenValidMessage_whenHopsIncremented_thenHopsShouldBeValid() {

		// Given
		MessageToSpread msg = new MessageToSpread(" ", 1, 5, (obj1, obj2) -> {
			return null;
		});

		// When hops incremented
		int hopsBeforeIncrement = msg.getMessageHops();
		MessageToSpread newMessage = msg.incrementHops();
		assertThat(newMessage.getMessageHops()).isEqualTo(++hopsBeforeIncrement);
	}

	@Test
	public final void givenSpreadMewssageWithNonPositiveHops_whenConstructed_thenIllegalArgumentException() {

		// When
		final Throwable throwable = catchThrowable(
				() -> new MessageToSpread(null, 0, 5, (obj2, obj1) -> {
					return null;
				}));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("A non-positive Hops should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenSpreadMewssageWithNonPositiveMaxHops_whenConstructed_thenIllegalArgumentException() {

		// When the message with null content update Content Update function called
		final Throwable throwable = catchThrowable(
				() -> new MessageToSpread(null, 2, 0, (obj1, obj2) -> {
					return null;
				}));

		// Then a IllegalArgumentException should be thrown
		then(throwable)
				.as("A non-positive maxHops should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenSpreadMewssageWithNullMessage_whenConstructed_thenIllegalArgumentException() {
//		public MessageToSpread(String message, int messageHops, int maxHops, Function<Object, Object> function) {

		// When the message with null content update Content Update function called
		final Throwable throwable = catchThrowable(
				() -> new MessageToSpread(null, 2, 3, (obj1, obj2) -> {
					return null;
				}));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("A null message should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenSpreadMewssageIsEmptyMessage_whenConstructed_thenIllegalArgumentException() {
//		public MessageToSpread(String message, int messageHops, int maxHops, Function<Object, Object> function) {

		// When the message with null content update Content Update function called
		final Throwable throwable = catchThrowable(
				() -> new MessageToSpread("", 2, 3, (obj1, obj2) -> {
					return null;
				}));

		// Then
		then(throwable).as("A empty message should throw a IllegalArgumentException")
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
