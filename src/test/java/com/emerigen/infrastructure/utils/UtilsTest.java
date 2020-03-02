package com.emerigen.infrastructure.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.cycle.Cycle;
import com.emerigen.infrastructure.learning.cycle.WeeklyCycle;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;

public class UtilsTest {
	Cycle myCycle = new WeeklyCycle(1, 1);

	@Test
	public final void givenTwoDoubles_whenPercentDifferenceCalculated_then20PercentOrLessReturned() {

		// Test that two values are within 20% of each other

		double diff = Utils.getPercentDifference(1.0, .85);
		assertThat(diff).isLessThan(0.20).isGreaterThan(.14);
	}

	@Test
	public final void givenTwoDoubles_whenEqualityChecked_then20PercentReturned() {
//		return Math.sqrt(Math.pow((value - mean), 2) / 2.0);
		double percentDiff = Utils.getPercentDifference(1, .85);
		assertThat(percentDiff).isLessThan(0.16).isGreaterThan(.14);
	}

	@Test
	public final void givenDifferencePastEqualityThreshold_whenGetStandardDeviation_thenNotEqual() {
		assertThat(Utils.equals(1, 5)).isFalse();
	}

	@Test
	public final void givenFloatsWithinEqualityBoundaries_whenEqualityChecked_thenValuesAreEqual() {
//		double std = Utils.getStandardDeviation(4.0f, 0f);
		assertThat(Utils.equals(4.0f, 3.4, .2)).isTrue();
		assertThat(Utils.equals(4.0f, 3.4)).isTrue();
	}

	@Test
	public final void givenDifferenceOf25Percent_whenPercentDifferenceCalculated_then25Percentreturned() {
		assertThat(Utils.getPercentDifference(4.0f, 3.0f)).isEqualTo(0.25);

		assertThat(Utils.equals(4.0, 3.5, .2)).isTrue();
	}

	@Test
	public final void givenSensorEvantValuesWithinEqualityBounds_whenCheckedForEquality_thenTrue() {
		Sensor sensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		float[] values = { 1.18f, 2.28f };
		float[] values2 = { 1.17f, 2.26f };
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		boolean firstEquals = Utils.equals(values[0], values2[0]);
		boolean secondEquals = Utils.equals(values[1], values2[1]);
		assertThat(firstEquals && secondEquals).isTrue();
	}

	@Test
	public final void givenSensorEventValuesNotWithinEqualityBounds_whenCheckedForEquality_thenFalse() {
		Sensor sensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		float[] values = { 1.1f, 2.2f };
		float[] values2 = { 1.11f, 2.21f };
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		boolean firstEquals = Utils.equals(values[0], values2[0]);
		boolean secondEquals = Utils.equals(values[1], values2[1]);
		assertThat(firstEquals && secondEquals).isTrue();
	}

	@Test
	public final void givenIntegersNotWithinEqualityBounds_whenCheckedForEquality_thenFalse() {
		assertThat(Utils.equals(10, 7)).isFalse();
	}

	@Test
	public final void givenIntegersWithinEqualityBounds_whenCheckedForEquality_thenTrue() {
		assertThat(Utils.equals(10, 9)).isTrue();
	}

	@Test
	public final void givenLongsWithinEqualityBounds_whenCheckedForEquality_thenTrue() {
		assertThat(Utils.equals(10l, 9l)).isTrue();
	}

	@Test
	public final void givenLongsNotWithinEqualityBounds_whenCheckedForEquality_thenFalse() {
		assertThat(Utils.equals(10l, 7l)).isFalse();
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
