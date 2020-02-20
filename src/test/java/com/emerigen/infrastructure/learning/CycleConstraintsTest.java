package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
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

	int sensorType = 1;
	Cycle myCycle = new WeeklyCycle(1, 1);
	Cycle myCycle2 = new MonthlyCycle(1, 1);

	@Test
	public final void givenNonPositiveSensorType_whenCycleCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> new DailyCycle(-1, 1));

		then(throwable).as("A non positive sensorType throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenNonPositiveSensorLocation_whenCycleCreated_thenIllegalArgurmentException() {
		SoftAssertions softly = new SoftAssertions();

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> new DailyCycle(1, -1));

		then(throwable).as("A non positive sensorLocation throws an IllegalArgumentException")
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
		DailyCycle dc = new DailyCycle(1, 1);
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());

		ZoneId zoneId = ZoneId.systemDefault();
		ZonedDateTime todayStart = ZonedDateTime.now(zoneId).toLocalDate().atStartOfDay(zoneId);

		assertThat(cpr.getCycleStartTimeNano()).isEqualTo(todayStart.toEpochSecond() * 1000 * 1000000l);
	}

	@Test
	public final void givenDailyCycle_whenCreated_thenDurationMustBe24Hours() {

		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());

		long duration = 24 * 60 * 60 * 1000 * 1000000l;
		assertThat(cpr.getCycleDurationTimeNano()).isEqualTo(duration);
	}

	@Test
	public final void givenWeeklyCycle_whenCycleCreated_thenDurationMustBe168Hours() {
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new WeeklyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());
		assertThat(cpr.getCycleDurationTimeNano()).isEqualTo(7l * 24 * 60 * 60 * 1000 * 1000000l);
	}

	@Test
	public final void givenMonthlyCycle_whenCycleCreated_thenCycleStartTimeMustBe12amFirstDayOfMonth() {
//		Utils.equals(time, yrCycle.getCycleDurationMillis());
		MonthlyCycle mc = new MonthlyCycle(1, 1);
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new MonthlyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(mc, gpsSensor, new PredictionService());

		ZoneId zoneId = ZoneId.systemDefault();
		LocalDate today = LocalDate.now();
		LocalDate firstDayOfCurrentMonth = today.with(TemporalAdjusters.firstDayOfMonth());

		// Get the start of that day
		ZonedDateTime firtDayStartTime = firstDayOfCurrentMonth.atStartOfDay(zoneId);
		assertThat((firtDayStartTime.toEpochSecond() * 1000 * 1000000l) == cpr.getCycleStartTimeNano())
				.isTrue();

	}

	@Test
	public final void givenMonthlyCycle_whenCycleCreated_thenDurationMustBeApproximately30Days() {
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new MonthlyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());
		MonthlyCycle dc = new MonthlyCycle(1, 1);
//		System.out.println("monthly duration is: " + dc.getCycleDurationTimeNano());
		assertThat(Utils.equals(cpr.getCycleDurationTimeNano(), 2629746000000000f, 10.0)).isTrue();
	}

	@Test
	public final void givenYearlyCycle_whenCycleCreated_thenCycleStartTimeMustBeJan1_12am() {
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new YearlyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor, new PredictionService());
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);// I might have the wrong Calendar constant...
		cal.set(Calendar.MONTH, 0);// -1 as month is zero-based
		cal.set(Calendar.YEAR, 2020);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		long time = cal.getTimeInMillis();
		Utils.equals(time, cpr.getCycleDurationTimeNano());

	}

	@Test
	public final void givenNonPositiveDataPointDuration_whenCycleNodeCreated_thenIllegalArgurmentException() {

		final Throwable throwable = catchThrowable(() -> new CycleNode(myCycle, new SensorEvent(), -1));

		then(throwable).as("A non positive data point duration throws a  IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	public final void givenNonPositiveDataPointDuration_whenSetOnNode_thenIllegalArgumentException() {
		SoftAssertions softly = new SoftAssertions();

		// public CycleNode(SensorEvent sensorEvent, long originStartTimeMillis) {
		CycleNode cn = new CycleNode(myCycle, new SensorEvent(), 11);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> cn.setDataPointDurationNano(-1));

		then(throwable).as("A non positive duration throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

		softly.assertAll();
	}

	@Test
	public final void givenNullCycleNode_whenCreated_thenIllegalArgumentException() {

		final Throwable throwable = catchThrowable(() -> new CyclePrediction((CycleNode) null));

		then(throwable).as("Null cycle node on creation throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
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
