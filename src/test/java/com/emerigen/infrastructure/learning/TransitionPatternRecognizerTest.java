package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.sensor.EmerigenSensorEventListener;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorEventListener;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.knowledge.Transition;

public class TransitionPatternRecognizerTest {

	private long minimumDelayBetweenReadings = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"))
			* 1000000;

	@Test
	public final void givenNoDefaultSensor_whenRetrieved_thenTheAssociatedTransitionPRIsRegistered() {
		SensorManager sm = SensorManager.getInstance();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE,
				Sensor.LOCATION_WATCH);
		TransitionPatternRecognizer tpr = new TransitionPatternRecognizer(sensor);

		assertThat(sm.listenerIsRegisteredToSensor(tpr, sensor)).isTrue();
	}

	@Test
	public final void givenNewEventWithoutPredictions_whenGetPredictionsCalled_thenEmptyPredictionListReturned() {

		// Given a new valid SensorEvent logged
		Random rd = new Random();
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		float[] values = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent1 = new SensorEvent(hrSensor, values);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);

		TransitionPatternRecognizer pr = new TransitionPatternRecognizer(hrSensor);
		List<Prediction> predictions = pr.getPredictionsForSensorEvent(sensorEvent1);
//		List<Prediction> predictions = pr.onSensorChanged(sensorEvent1);

		assertThat(predictions).isNotNull().isEmpty();
	}

	@Test
	public final void givenOneTransition_whenOnSensorChangedCalledWithFirstEvent_thenPredictionListReturned()
			throws InterruptedException {

		// Given a new valid SensorEvent logged
		Random rd = new Random();
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_WATCH);
		float[] values = new float[] { rd.nextFloat(), 1.2f };
		float[] values2 = new float[] { rd.nextFloat(), 281.2f };
		SensorEvent sensorEvent1 = new SensorEvent(hrSensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(hrSensor, values2);
		sensorEvent2
				.setTimestamp(sensorEvent2.getTimestamp() + minimumDelayBetweenReadings);
		SensorEventListener listener = new EmerigenSensorEventListener();

		List<Prediction> predictions = listener.onSensorChanged(sensorEvent1);

		predictions = listener.onSensorChanged(sensorEvent2);
		// Give the bucket a chance to catch up after the log

		// event timestamp must be after the last event
		sensorEvent1
				.setTimestamp(sensorEvent2.getTimestamp() + minimumDelayBetweenReadings);

		Thread.sleep(100);
		predictions = listener.onSensorChanged(sensorEvent1);

		assertThat(predictions).isNotNull().isNotEmpty();
		assertThat(predictions.size()).isEqualTo(1);
	}

	@Test
	public final void givenNoPreviousEventAndEventWithoutPredictions_whenOnSensorChangedCalled_thenEmptyPredictionListReturned() {
		// Given a new valid SensorEvent logged
		Random rd = new Random();
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		float[] values = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent1 = new SensorEvent(hrSensor, values);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);

		TransitionPatternRecognizer pr = new TransitionPatternRecognizer(hrSensor);
		List<Prediction> predictions = pr.onSensorChanged(sensorEvent1);

		assertThat(predictions).isNotNull().isEmpty();
	}

	@Test
	public final void givenPreviousEventAndNewEvent_whenOnSensorChangedCalled_thenNewTransitionCreatedAndEmptyPredictionListReturned() {
		// Given a new valid SensorEvent logged
		Random rd = new Random();
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		float[] values = new float[] { rd.nextFloat(), 1.2f };
		float[] values2 = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent1 = new SensorEvent(hrSensor, values);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);
		TransitionPatternRecognizer pr = new TransitionPatternRecognizer(hrSensor);
		List<Prediction> predictions = pr.onSensorChanged(sensorEvent1);

		SensorEvent sensorEvent2 = new SensorEvent(hrSensor, values2);
		List<Prediction> predictions2 = pr.onSensorChanged(sensorEvent2);
		assertThat(predictions2).isNotNull().isEmpty();

		Transition newTransition = KnowledgeRepository.getInstance()
				.getTransition(sensorEvent1.getKey() + sensorEvent2.getKey());
		assertThat(newTransition).isNotNull();

	}

	@Test
	public final void givenExistingEventWithoutPredictions_whenOnSensorChangedCalled_thenEmptyPredictionListReturned() {
		// Given a new valid SensorEvent logged
		Random rd = new Random();
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		float[] values = new float[] { rd.nextFloat(), 1.2f };
		float[] values2 = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent1 = new SensorEvent(hrSensor, values);

		TransitionPatternRecognizer pr = new TransitionPatternRecognizer(hrSensor);
		List<Prediction> predictions = pr.onSensorChanged(sensorEvent1);

		SensorEvent sensorEvent2 = new SensorEvent(hrSensor, values2);
		List<Prediction> predictions2 = pr.onSensorChanged(sensorEvent2);
		assertThat(predictions2).isNotNull().isEmpty();

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
