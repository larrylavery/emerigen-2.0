package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.Utils;

public class CycleConstraintsTest {

	Cycle myCycle = new WeeklyCycle() {

		@Override
		public long calculateCycleStartTimeMillis() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long calculateCycleDurationMillis() {
			// TODO Auto-generated method stub
			return 0;
		}
	};
	Cycle myCycle2 = new MonthlyCycle() {

		@Override
		public long calculateCycleStartTimeMillis() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long calculateCycleDurationMillis() {
			// TODO Auto-generated method stub
			return 0;
		}
	};

	@Test
	public final void givenDifferentCycleTypes_whenCycleMerged_thenIllegalArgurmentException() {
		CycleNode node = new CycleNode(myCycle, new SensorEvent(), 1);
		CycleNode node2 = new CycleNode(myCycle2, new SensorEvent(), 1);

		final Throwable throwable = catchThrowable(() -> node.merge(node2));

		then(throwable).as("Cannot merge different cycle-based nodes gets IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullSensorEvents_whenCycleCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> new DailyCycle(null));

		then(throwable)
				.as("A null sensorEvent list for cycle CTOR throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenEmptySensorEvents_whenCycleCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// When the instance is validated
		final Throwable throwable = catchThrowable(
				() -> new DailyCycle(new ArrayList<SensorEvent>()));

		then(throwable)
				.as("A empty sensorEvent list for cycle ctor throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenNullCycle_whenCycleNodeCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> new CycleNode(null, new SensorEvent(), 0));

		then(throwable).as("A null cycle throws a CycleNode  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenNullSensorEvent_whenCycleNodeCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> new CycleNode(myCycle, null, 0));

		then(throwable).as("A null sensorEvent throws a CycleNode  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenDailyCycle_whenCycleCreated_thenCycleStartTimeMustBe12am() {
		DailyCycle dc = new DailyCycle();

		ZoneId zoneId = ZoneId.systemDefault();
		ZonedDateTime todayStart = ZonedDateTime.now(zoneId).toLocalDate().atStartOfDay(zoneId);

		assertThat(dc.getCycleStartTimeMillis()).isEqualTo(todayStart.getSecond() * 1000);
	}

	@Test
	public final void givenDailyCycle_whenCycleCreated_thenDurationMustBe24Hours() {
		DailyCycle dc = new DailyCycle();
		assertThat(dc.getCycleDurationMillis()).isEqualTo(24 * 60 * 60 * 1000);
	}

	@Test
	public final void givenWeeklyCycle_whenCycleCreated_thenDurationMustBe168Hours() {
		WeeklyCycle dc = new WeeklyCycle();
		assertThat(dc.getCycleDurationMillis()).isEqualTo(7 * 24 * 60 * 60 * 1000);
	}

	@Test
	public final void givenMonthlyCycle_whenCycleCreated_thenCycleStartTimeMustBe12amFirstDayOfMonth() {
//		Utils.equals(time, yrCycle.getCycleDurationMillis());
		MonthlyCycle mc = new MonthlyCycle();

		ZoneId zoneId = ZoneId.systemDefault();
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentMonth = today.with(TemporalAdjusters.firstDayOfMonth());

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentMonth.atStartOfDay(zoneId);
		assertThat((firtDayStartTime.getSecond() * 1000) == mc.getCycleStartTimeMillis()).isTrue();

	}

	@Test
	public final void givenMonthlyCycle_whenCycleCreated_thenDurationMustBeApproximately30Days() {
		MonthlyCycle dc = new MonthlyCycle();
		System.out.println("monthly duration is: " + dc.getCycleDurationMillis());
		assertThat(Utils.equals(dc.getCycleDurationMillis(), 2629746000f, 10.0)).isTrue();
	}

	@Test
	public final void givenYearlyCycle_whenCycleCreated_thenCycleStartTimeMustBeJan1_12am() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);// I might have the wrong Calendar constant...
		cal.set(Calendar.MONTH, 0);// -1 as month is zero-based
		cal.set(Calendar.YEAR, 2020);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long time = cal.getTimeInMillis();
		YearlyCycle yrCycle = new YearlyCycle();
		Utils.equals(time, yrCycle.getCycleDurationMillis());

	}

	@Test
	public final void givenNonPositiveDataPointDuration_whenCycleNodeCreated_thenIllegalArgurmentException() {

		final Throwable throwable = catchThrowable(
				() -> new CycleNode(myCycle, new SensorEvent(), -1));

		then(throwable).as("A non positive data point duration throws a  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenDifferencePastEqualityThreshold_whenGetStandardDeviation_thenNotEqual() {
		assertThat(Utils.equals(1, 5)).isFalse();
	}

	@Test
	public final void givenInts_whenGetStandardDeviation_thenValueCorrect() {
		double std = Utils.getStandardDeviation(4, 0);
		assertThat(Utils.equals(std, 2.82842712475, .2)).isTrue();
	}

	@Test
	public final void givenFloats_whenGetStandardDeviation_thenValueCorrect() {
		double std = Utils.getStandardDeviation(4.0f, 0f);
		assertThat(Utils.equals(std, 2.82842712475, .2)).isTrue();
	}

	@Test
	public final void givenDifferenceOf4_whenGetStandardDeviation_then2returned() {
		double std = Utils.getStandardDeviation(4);
		assertThat(Utils.equals(std, 2.82842712475, .2)).isTrue();
	}

	@Test
	public final void givenNodesWithinStandardDeviationForEquality_whenCheckedForEquality_thenTrue() {
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		float[] values = { 1.1f, 2.2f };
		float[] values2 = { 1.11f, 2.21f };
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		CycleNode node1 = new CycleNode(myCycle, event1, 1);
		CycleNode node2 = new CycleNode(myCycle2, event2, 1);
		assertThat(node1.equals(node2)).isTrue();
	}

	@Test
	public final void givenNodesNotWithinStandardDeviationForEquality_whenCheckedForEquality_thenFalse() {
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		float[] values = { 1.1f, 2.2f };
		float[] values2 = { 11.11f, 12.21f };
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		CycleNode node1 = new CycleNode(myCycle, event1, 1);
		CycleNode node2 = new CycleNode(myCycle2, event2, 1);
		assertThat(node1.equals(node2)).isFalse();
	}

	@Test
	public final void givenNonPositiveDataPointDuration_whenSetOnNode_thenIllegalArgumentException() {
		SoftAssertions softly = new SoftAssertions();

		// public CycleNode(SensorEvent sensorEvent, long originStartTimeMillis) {
		CycleNode cn = new CycleNode(myCycle, new SensorEvent(), 11);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> cn.setDataPointDurationMillis(-1));

		then(throwable).as("A non positive duration throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenNullCycleNode_whenMerged_thenIllegalArgumentException() {
		CycleNode node = new CycleNode(myCycle, new SensorEvent(), 1);
		CycleNode node2 = new CycleNode(myCycle, new SensorEvent(), 1);

		final Throwable throwable = catchThrowable(() -> node.merge(null));

		then(throwable).as("A null Cycle node during merge throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		// public CycleNode merge(CycleNode nodeToMergeWith) {
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