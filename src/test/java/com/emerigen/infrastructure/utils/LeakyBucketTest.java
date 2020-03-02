package com.emerigen.infrastructure.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.time.Duration;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LeakyBucketTest {

	static private int highExecuteCount;
	static private int lowExecuteCount;

	Runnable normalFrequency = new Runnable() {
		@Override
		public void run() {
			lowExecuteCount++;
		}
	};

	Runnable highFrequency = new Runnable() {
		@Override
		public void run() {
			highExecuteCount++;
		}
	};

	// @Test
	public final void givenFullBucket_whenLeakedThenFilled_thenValidates() {
		LeakyBucket leakyBucket = new LeakyBucket(5, Duration.ofSeconds(1), 2, 2,
				highFrequency, normalFrequency);
		leakyBucket.leak();
		assertThat(leakyBucket.isFull()).isFalse();
		leakyBucket.fill();
		assertThat(leakyBucket.isFull()).isTrue();
	}

	// @Test
	public final void givenFullBucket_whenLeaked_thenIsFullIsFalse() {
		LeakyBucket leakyBucket = new LeakyBucket(5, Duration.ofSeconds(1), 2, 2,
				highFrequency, null);
		leakyBucket.leak();
		assertThat(leakyBucket.isFull()).isFalse();
	}

	// @Test
	public final void givenNewFullLeakyBucket_whencheckingFull_thenTrue() {
		LeakyBucket leakyBucket = new LeakyBucket(5, Duration.ofSeconds(1), 2, 2,
				highFrequency, null);
		assertThat(leakyBucket.isFull()).isTrue();
	}

	// @Test
	public final void givenFullLeakyBucket_whenCheckingIsEmpty_thenFalse() {
		LeakyBucket leakyBucket = new LeakyBucket(5, Duration.ofSeconds(1), 2, 2,
				highFrequency, null);
		assertThat(leakyBucket.isEmpty()).isFalse();
	}

	// @Test
	public final void givenHighEventFrequencyHandler_whenOccurancesExceedThreshold_thenHandlerInvoked() {
		// Given

		LeakyBucket leakyBucket = new LeakyBucket(5, Duration.ofSeconds(1), 2, 2,
				highFrequency, null);
		// When high threshold exceeded
		for (int i = 0; i < 5; i++) {
			leakyBucket.fill();
		}

		// Then
		then(highExecuteCount).isGreaterThan(0);

	}

	// @Test
	public final void givenLowEventFrequencyHandler_whenOccurancesBelowThreshold_thenHandlerInvoked() {
		LeakyBucket leakyBucket = new LeakyBucket(5, Duration.ofSeconds(1), 2, 2, null,
				normalFrequency);
		// When low threshold exceeded by sleeping
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Then
		then(lowExecuteCount).isGreaterThan(0);

	}

	// @Test
	public final void givenTheOtherhHandlerNull_whenCreated_thenNoException() {
		final Throwable throwable = catchThrowable(() -> new LeakyBucket(1,
				Duration.ofSeconds(2), 1, 1, null, normalFrequency));

		then(throwable).isNull();

	}

	// @Test
	public final void givenOnehHandlerNull_whenCreated_thenNoException() {
		final Throwable throwable = catchThrowable(() -> new LeakyBucket(1,
				Duration.ofSeconds(2), 1, 1, highFrequency, null));

		then(throwable).isNull();

	}

	@Test
	public final void givenBothHandlersNull_whenCreated_thenIllegalArgumentException() {
		final Throwable throwable = catchThrowable(
				() -> new LeakyBucket(1, Duration.ofSeconds(2), 1, 1, null, null));

		then(throwable).isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNonPositiveNumOccurancesAtFull_whenCreated_thenIllegalArgumentException() {
		final Throwable throwable = catchThrowable(() -> new LeakyBucket(1,
				Duration.ofSeconds(2), 1, 0, highFrequency, normalFrequency));

		then(throwable).isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNonPositiveNumOccurancesAtEmpty_whenCreated_thenIllegalArgumentException() {
		final Throwable throwable = catchThrowable(() -> new LeakyBucket(1,
				Duration.ofSeconds(2), 0, 1, highFrequency, normalFrequency));

		then(throwable).isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenZeroDuration_whenCreated_thenIllegalArgumentException() {
		final Throwable throwable = catchThrowable(() -> new LeakyBucket(1,
				Duration.ofSeconds(0), 1, 1, highFrequency, normalFrequency));

		then(throwable).isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullDuration_whenCreated_thenIllegalArgumentException() {
		final Throwable throwable = catchThrowable(
				() -> new LeakyBucket(1, null, 1, 1, highFrequency, normalFrequency));

		then(throwable).isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNonPositiveNumOccurances_whenCreated_thenIllegalArgumentException() {
		final Throwable throwable = catchThrowable(() -> new LeakyBucket(-1,
				Duration.ofMinutes(1), 1, 1, highFrequency, normalFrequency));

		then(throwable).isInstanceOf(IllegalArgumentException.class);

	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		FrequencyAnnotationExample.highExceptionFrequencyHandlerCount = 0;
		FrequencyAnnotationExample.lowExceptionFrequencyHandlerCount = 0;
		highExecuteCount = 0;
		lowExecuteCount = 0;

	}

	@After
	public void tearDown() throws Exception {
		highExecuteCount = 0;
		lowExecuteCount = 0;

	}

}
