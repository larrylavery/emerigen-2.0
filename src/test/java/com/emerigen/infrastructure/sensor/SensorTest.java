package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.Test;

public class SensorTest {

	@Test
	public final void givenInvalidReportingMode_whenCreated_thenIllegalArgumentException() {
		// given

		// when
		final Throwable throwable = catchThrowable(
				() -> new HeartRateSensor(Sensor.LOCATION_WATCH, 0, false));

		// Then a ValidationException should be thrown
		then(throwable)
				.as("An invalid reporting mode should  throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenValidReportingMode_whenQueried_thenRetrieved() {
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		assertThat(sensor.getReportingMode()).isEqualTo(Sensor.REPORTING_MODE_CONTINUOUS);
	}

	@Test
	public final void givenActivatedSensor_whenDeactivated_thenIsActivatedFalse() {
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);

		sensor.deactivate();

		assertThat(sensor.isActivated()).isFalse();
	}

	@Test
	public final void givenDeactivatedSensor_whenActivated_thenIsActivatedTrue() {
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		assertThat(sensor.isActivated()).isTrue();

		sensor.deactivate();
		assertThat(sensor.isActivated()).isFalse();

		sensor.activate();
		assertThat(sensor.isActivated()).isTrue();

	}

	@Test
	public final void givenDifferentSensorsWithSameValues_whenEqualityChecked_thenTrue() {
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		HeartRateSensor sensor2 = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		assertThat(sensor.equals(sensor2)).isTrue();
	}

	@Test
	public final void testIsWakUpSensor() {
		// TODO implement wakeup sensor test
	}

}
