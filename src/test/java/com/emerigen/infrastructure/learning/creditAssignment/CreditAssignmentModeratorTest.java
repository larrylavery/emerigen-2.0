package com.emerigen.infrastructure.learning.creditAssignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.learning.creditassignment.Bid;
import com.emerigen.infrastructure.learning.creditassignment.CreditAssignmentModerator;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class CreditAssignmentModeratorTest {

	private long minimumDelayBetweenReadings = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"))
			* 1000000;

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
						new Prediction(new SensorEvent()), null));

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

		PredictionService ps = new PredictionService(sensor);
		ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2);
		ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent3);
		CreditAssignmentModerator cam = new CreditAssignmentModerator(
				new PredictionService());

		List<Bid> bids = cam
				.locatePotentialConsumersForPrediction(new Prediction(sensorEvent1));
		assertThat(bids.size()).isEqualTo(2);
	}

	@Test
	public final void givenThreeBidders_whenSelectWinnerAndNotifyBidder_thenLargestBidderNotified() {
		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = new float[] { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		float[] values4 = new float[] { rd.nextFloat() + 1000, rd.nextFloat() + 1000 };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		SensorEvent sensorEvent3 = new SensorEvent(sensor, values3);
		SensorEvent sensorEvent4 = new SensorEvent(sensor, values4);

		PredictionService ps = new PredictionService(sensor);

		String pcKey1 = ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2);

		String pcKey2 = ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent3);

		String pcKey3 = ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent4);

		CreditAssignmentModerator cam = new CreditAssignmentModerator(
				new PredictionService());

		List<Bid> bids = cam
				.locatePotentialConsumersForPrediction(new Prediction(sensorEvent1));

		assertThat(bids.size()).isEqualTo(3);
		bids.get(0).setAmount(1);
		bids.get(1).setAmount(11);
		bids.get(2).setAmount(10);
		Bid bid = cam.selectAndNotifyWinningBidder(bids);
		assertThat(bid.getAmount()).isEqualTo(11);
	}

	@Test
	public final void givenMultipleBiddersWithTwoChainsOfPredictions_whenFirstLinkPredictionInitiated_thenPredictionsEndWhenLastLinkMakesPrediction() {

		// Given enough sensorEvents for two prediction links
		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = new float[] { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		float[] values4 = new float[] { rd.nextFloat() + 1000, rd.nextFloat() + 1000 };
		float[] values5 = new float[] { rd.nextFloat() + 10000, rd.nextFloat() + 10000 };
		float[] values6 = new float[] { rd.nextFloat() + 100000,
				rd.nextFloat() + 100000 };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);

		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		sensorEvent2
				.setTimestamp(minimumDelayBetweenReadings + sensorEvent1.getTimestamp());

		SensorEvent sensorEvent3 = new SensorEvent(sensor, values3);
		sensorEvent3
				.setTimestamp(minimumDelayBetweenReadings + sensorEvent2.getTimestamp());

		SensorEvent sensorEvent4 = new SensorEvent(sensor, values4);
		sensorEvent4
				.setTimestamp(minimumDelayBetweenReadings + sensorEvent3.getTimestamp());

		SensorEvent sensorEvent5 = new SensorEvent(sensor, values5);
		sensorEvent5
				.setTimestamp(minimumDelayBetweenReadings + sensorEvent4.getTimestamp());

		SensorEvent sensorEvent6 = new SensorEvent(sensor, values6);
		sensorEvent6
				.setTimestamp(minimumDelayBetweenReadings + sensorEvent5.getTimestamp());

		// And Given two Prediction (Transition) links
		PredictionService ps = new PredictionService(sensor);

		String pcKey1 = ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2);

		String pcKey2 = ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent3);

		String pcKey3 = ps.createPredictionFromSensorEvents(sensorEvent2, sensorEvent4);

		String pcKey5 = ps.createPredictionFromSensorEvents(sensorEvent2, sensorEvent5);

		String pcKey6 = ps.createPredictionFromSensorEvents(sensorEvent3, sensorEvent6);

		CreditAssignmentModerator cam = new CreditAssignmentModerator(
				new PredictionService());

		// locate two 1st link predictionConsumers (ie where firstEventKey =
		// sensorEvent1
		List<Bid> bids = cam
				.locatePotentialConsumersForPrediction(new Prediction(sensorEvent1));

		assertThat(bids.size()).isEqualTo(2);

		// update their cash on hand
		bids.get(0).setAmount(.1);
		bids.get(0).getPredictionConsumer().setCashOnHand(1);

		bids.get(1).setAmount(1.1);
		bids.get(1).getPredictionConsumer().setCashOnHand(11);

		// Notify the winning bidder and cause payment to be made
		Bid bid = cam.selectAndNotifyWinningBidder(bids);
		assertThat(bid.getAmount()).isEqualTo(1.1);
		assertThat(bid.getPredictionConsumer().getCashOnHand()).isEqualTo(9.9);

		// Next, locate the potential bidders for the 2nd link prediction winning
		// bidder's
		// prediction
		bids = cam.locatePotentialConsumersForPrediction((new Prediction(sensorEvent2)));
//		(bid.getPredictionConsumer().getPrediction()));

		assertThat(bids.size()).isEqualTo(2);

		// update their cash on hand
		bids.get(0).setAmount(.2);
		bids.get(0).getPredictionConsumer().setCashOnHand(2);

		bids.get(1).setAmount(9.0);
		bids.get(1).getPredictionConsumer().setCashOnHand(90.0);

		// Notify the winning bidder and cause payment to be made
		bid = cam.selectAndNotifyWinningBidder(bids);
		assertThat(bid.getAmount()).isEqualTo(9);
		assertThat(bid.getPredictionConsumer().getCashOnHand()).isEqualTo(81);

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
