package com.emerigen.infrastructure.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.Cycle;
import com.emerigen.infrastructure.learning.CycleNode;
import com.emerigen.infrastructure.learning.WeeklyCycle;
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
	public final void givenNodesWithinWithinEqualityBounds_whenCheckedForEquality_thenTrue() {
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		float[] values = { 1.18f, 2.28f };
		float[] values2 = { 1.17f, 2.26f };
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		CycleNode node1 = new CycleNode(myCycle, event1, 1);
		CycleNode node2 = new CycleNode(myCycle, event2, 1);
		assertThat(node1.equals(node2)).isTrue();
	}

	@Test
	public final void givenNodesWithinEqualityBounds_whenCheckedForEquality_thenFalse() {
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		float[] values = { 1.1f, 2.2f };
		float[] values2 = { 1.25f, 2.45f };
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		CycleNode node1 = new CycleNode(myCycle, event1, 1);
		CycleNode node2 = new CycleNode(myCycle, event2, 1);
		assertThat(node1.equals(node2)).isTrue();
	}

	@Test
	public final void givenNodesNotWithinEqualityBounds_whenCheckedForEquality_thenFalse() {
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		float[] values = { 1.1f, 2.2f };
		float[] values2 = { 11.11f, 12.21f };
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		CycleNode node1 = new CycleNode(myCycle, event1, 1);
		CycleNode node2 = new CycleNode(myCycle, event2, 1);
		assertThat(node1.equals(node2)).isFalse();
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
