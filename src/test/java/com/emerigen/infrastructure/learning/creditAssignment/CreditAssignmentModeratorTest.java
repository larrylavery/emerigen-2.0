package com.emerigen.infrastructure.learning.creditAssignment;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.learning.TransitionPrediction;
import com.emerigen.infrastructure.learning.creditassignment.CreditAssignmentModerator;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;

public class CreditAssignmentModeratorTest {

	@Test
	public final void givenNullPrediction_whenLocatePotentialConsumersForPrediction_thenIllegalArgumentException() {
		Transition t = new Transition();
		CreditAssignmentModerator cam = new CreditAssignmentModerator(
				new PredictionService());

		final Throwable throwable = catchThrowable(
				() -> cam.locatePotentialConsumersForPrediction(null));

		then(throwable).as("A null prediction throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullSensor_whenRegisterSensorForPredictionConsumers_thenIllegalArgumentException() {
		Transition t = new Transition();
		CreditAssignmentModerator cam = new CreditAssignmentModerator(
				new PredictionService());

		final Throwable throwable = catchThrowable(
				() -> cam.registerSensorForPredictionConsumers(null));

		then(throwable).as("A sensor throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullPrediction_whenRegisterConsumerPredictionForSensor_thenIllegalArgumentException() {
		Transition t = new Transition();
		CreditAssignmentModerator cam = new CreditAssignmentModerator(
				new PredictionService());
		Sensor s = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_BLOOD_PRESSURE, Sensor.LOCATION_BODY);

		final Throwable throwable = catchThrowable(
				() -> cam.registerConsumerPredictionForSensor(null, s));

		then(throwable).as("A null consumerPrediction throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullSensor_whenRegisterConsumerPredictionForSensor_thenIllegalArgumentException() {
		Transition t = new Transition();
		CreditAssignmentModerator cam = new CreditAssignmentModerator(
				new PredictionService());
		Sensor s = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_BLOOD_PRESSURE, Sensor.LOCATION_BODY);

		final Throwable throwable = catchThrowable(
				() -> cam.registerConsumerPredictionForSensor(
						new TransitionPrediction(new SensorEvent()), null));

		then(throwable).as("A null consumerPrediction throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullSensor_whenResetPredictionPoolForSensor_thenIllegalArgumentException() {
		Transition t = new Transition();
		CreditAssignmentModerator cam = new CreditAssignmentModerator(
				new PredictionService());
		Sensor s = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_BLOOD_PRESSURE, Sensor.LOCATION_BODY);

		final Throwable throwable = catchThrowable(
				() -> cam.resetPredictionPoolForSensor(null));

		then(throwable).as("A null sensor on reset throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullPredictionService_whenCreated_thenIllegalArgumentException() {

		final Throwable throwable = catchThrowable(
				() -> new CreditAssignmentModerator(null));

		then(throwable).as("A null sensor on reset throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenTwoPotentialConsumers_whenLocatePotentialConsumersForPrediction_thenBothReturned() {
		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = new float[] { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		SensorEvent sensorEvent3 = new SensorEvent(sensor, values3);

		Transition t1 = new Transition(sensorEvent1, sensorEvent2);
		Transition t2 = new Transition(sensorEvent1, sensorEvent3);
		CreditAssignmentModerator cam = new CreditAssignmentModerator(
				new PredictionService());

		List<Transition> predCons = cam
				.locatePotentialConsumersForPrediction(new Prediction(sensorEvent1));
		Sensor s = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_BLOOD_PRESSURE, Sensor.LOCATION_BODY);

		final Throwable throwable = catchThrowable(
				() -> cam.resetPredictionPoolForSensor(null));

		then(throwable).as("A null sensor on reset throws an IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenThreeBidders_whenSelectWinnerAndNotifyBidder_thenLargestBidderNotified() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenThreeBidders_whenSelectWinnerAndNotifyBidder_thenLargestBidderCashOnHandReducedByBidAmount() {
		fail("Not yet implemented"); // TODO
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
