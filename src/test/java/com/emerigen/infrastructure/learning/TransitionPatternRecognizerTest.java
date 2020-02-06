package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;

public class TransitionPatternRecognizerTest {

	@Test
	public final void givenNewEventWithoutPredictions_whenGetPredictionsCalled_thenEmptyPredictionListReturned() {

		// Given a new valid SensorEvent logged
		Random rd = new Random();
		Sensor hrSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		float[] values = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent1 = new SensorEvent(hrSensor, values);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);

		TransitionPatternRecognizer pr = new TransitionPatternRecognizer();
		List<Prediction> predictions = pr.getPredictionsForSensorEvent(sensorEvent1);
//		List<Prediction> predictions = pr.onSensorChanged(sensorEvent1);

		assertThat(predictions).isNotNull().isEmpty();
	}

	@Test
	public final void givenPreviousEventAndEventWithPredictions_whenOnSensorChangedCalled_thenNewTransitionCreatedAndPredictionListReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNoPreviousEventAndEventWithPredictions_whenOnSensorChangedCalled_thenPredictionListReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNoPreviousEventAndEventWithoutPredictions_whenOnSensorChangedCalled_thenEmptyPredictionListReturned() {
		// TODO define the couchbase Sensor, PatternRecognizer, and Cycle buckets
		// Given a new valid SensorEvent logged
		Random rd = new Random();
		Sensor hrSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		float[] values = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent1 = new SensorEvent(hrSensor, values);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);

		TransitionPatternRecognizer pr = new TransitionPatternRecognizer();
		List<Prediction> predictions = pr.onSensorChanged(sensorEvent1);

		assertThat(predictions).isNotNull().isEmpty();
	}

	@Test
	public final void givenPreviousEventAndNewEvent_whenOnSensorChangedCalled_thenNewTransitionCreatedAndEmptyPredictionListReturned() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenExistingEventWithoutPredictions_whenOnSensorChangedCalled_thenEmptyPredictionListReturned() {
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
