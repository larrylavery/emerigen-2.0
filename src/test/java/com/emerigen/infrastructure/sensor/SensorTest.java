package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SensorTest {

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

	@Test
	public final void givenNegativeMinimumDelay_whenCreated_thenIllegalArgumentException() {
		// given

		// when
		final Throwable throwable = catchThrowable(
				() -> new HeartRateSensor(Sensor.REPORTING_MODE_CONTINUOUS, -1, false));

		// Then a ValidationException should be thrown
		then(throwable).as("A negative minimum delay should  throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenInvalidReportingMode_whenCreated_thenIllegalArgumentException() {
		// given

		// when
		final Throwable throwable = catchThrowable(() -> new HeartRateSensor(0, 0, false));

		// Then a ValidationException should be thrown
		then(throwable).as("An invalid reporting mode should  throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenValidReportingMode_whenQueried_thenRetrieved() {
		HeartRateSensor sensor = new HeartRateSensor(Sensor.REPORTING_MODE_CONTINUOUS, 0, false);
		assertThat(sensor.getReportingMode()).isEqualTo(Sensor.REPORTING_MODE_CONTINUOUS);
	}

	@Test
	public final void givenValidMinimumDelay_whenQueried_thenRetrieved() {
		HeartRateSensor sensor = new HeartRateSensor(Sensor.REPORTING_MODE_CONTINUOUS, 0, false);
		assertThat(sensor.getMinimumDelayBetweenReadings()).isEqualTo(0);
	}

	@Test
	public final void givenActivatedSensor_whenDeactivated_thenIsActivatedFalse() {
		HeartRateSensor sensor = new HeartRateSensor(Sensor.REPORTING_MODE_CONTINUOUS, 0, false);

		sensor.deactivate();

		assertThat(sensor.isActivated()).isFalse();
	}

	@Test
	public final void givenDeactivatedSensor_whenActivated_thenIsActivatedTrue() {
		HeartRateSensor sensor = new HeartRateSensor(Sensor.REPORTING_MODE_CONTINUOUS, 0, false);
		assertThat(sensor.isActivated()).isTrue();

		sensor.deactivate();
		assertThat(sensor.isActivated()).isFalse();

		sensor.activate();
		assertThat(sensor.isActivated()).isTrue();

	}

	@Test
	public final void givenDifferentSensorsWithSameValues_whenEqualityChecked_thenTrue() {
		HeartRateSensor sensor = new HeartRateSensor(Sensor.REPORTING_MODE_CONTINUOUS, 0, false);
		HeartRateSensor sensor2 = new HeartRateSensor(Sensor.REPORTING_MODE_CONTINUOUS, 0, false);
		assertThat(sensor.equals(sensor2)).isTrue();
	}

	@Test
	public final void testIsWakUpSensor() {
		// TODO implement wakeup sensor test
	}

}
