package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.InputStream;
import java.util.List;
import java.util.Random;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.infrastructure.utils.Utils;

public class TransitionTest {

	private long minimumDelayBetweenReadings = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"))
			* 1000000;

	@Test
	public void givenValidJsonTransition_whenValidating_thenItShouldValidateSuccessfully() {

		// Given the schema and the instance json docs have been read in
		InputStream transitionSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("transition.json");
		InputStream invalidTransitionJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/transition-valid.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(transitionSchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidTransitionJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json transition document should not throw a ValidationException")
				.isNull();

	}

	// @Test
	public void givenJsonTransitionWithoutTimestamp_whenValidating_thenValidationExceptionIsThrown() {

		// Given the schema and the instance json docs have been read in
		InputStream transitionSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("transition.json");
		InputStream invalidTransitionJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/transition-invalid-no-timestamp.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(transitionSchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidTransitionJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A invalidly structured Json transition document should throw a ValidationException")
				.isInstanceOf(ValidationException.class);
	}

	// @Test
	public void givenJsonTransitionWithNoSensorEvents_whenValidating_thenValidationExceptionIsThrown() {

		// Given the schema and the instance json docs have been read in
		InputStream transitionSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("transition.json");
		InputStream invalidTransitionJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/transition-invalid-no-sensor-events.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(transitionSchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidTransitionJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A invalidly structured Json transition document should throw a ValidationException")
				.isInstanceOf(ValidationException.class);
	}

	// @Test
	public void givenJsonTransitionWithOnlyOneSensorEventn_whenValidating_thenValidationExceptionIsThrown() {

		// Given the schema and the instance json docs have been read in
		InputStream transitionSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("transition.json");
		InputStream invalidTransitionJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream(
						"test/transition-invalid-only-one-sensor-event.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(transitionSchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidTransitionJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A invalidly structured Json transition document should throw a ValidationException")
				.isInstanceOf(ValidationException.class);
	}

	@Test
	public final void givenTwoValidTransitionsWithSameFirstEvents_whenLogged_thenGetPredictionsReturnsTwo() {

		// Given

		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = new float[] { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		SensorEvent sensorEvent3 = new SensorEvent(sensor, values3);
		sensorEvent2
				.setTimestamp(sensorEvent1.getTimestamp() + minimumDelayBetweenReadings);
		sensorEvent3
				.setTimestamp(sensorEvent2.getTimestamp() + minimumDelayBetweenReadings);

		PredictionService ps = new PredictionService(sensor);
		ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2);
		ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent3);
		Utils.allowDataUpdatesTimeToCatchUp();
		// Query all transitions where the firstSensorEvent key equals the supplied
		// sensorEvent i.e. getPredictions!
		List<Prediction> predictions = ps.getPredictionsForSensorEvent(sensorEvent1);

		assertThat(predictions).isNotNull().isNotEmpty();
		assertThat(predictions.size() == 2).isTrue();

//		assertThat(predictions.get(1).getSensorEvent()).isEqualTo(sensorEvent2);
//		assertThat(predictions.get(0).getSensorEvent()).isEqualTo(sensorEvent3);
	}

	// @Test
	public final void givenInvalidTransitionWithoutFirstSensorEvent_whenTranslatedAndLogged_thenItshouldThrowValidationException() {
		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 2.1f, 2.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent1.getKey(),
				sensorEvent1, false);
		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent2.getKey(),
				sensorEvent2, false);

		SensorEvent predictedSensorEvent = new SensorEvent(sensor, values2);

		// When the invalid transition
		PredictionService ps = new PredictionService(sensor);

		final Throwable throwable = catchThrowable(
				() -> ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2));

		// Then ValidationException should occur
		then(throwable).as(
				"A IllegalArgumentException should be thrown for an invalid schema validation")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenValidTransition_whenLogged_thenGetPredictionsRetrunsOne() {

		// Given

		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat(), rd.nextFloat() };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		PredictionService ps = new PredictionService(sensor);
		ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2);

		Utils.allowDataUpdatesTimeToCatchUp();

		ps = new PredictionService(sensor);
		List<Prediction> predictions = ps.getPredictionsForSensorEvent(sensorEvent1);

		assertThat(predictions).isNotNull().isNotEmpty();
		assertThat(predictions.size()).isEqualTo(1);

	}

	@Test
	public final void givenValidTransition_whenLogged_thenTransitionRetrieved() {

		// Given

		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat(), rd.nextFloat() };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		PredictionService ps = new PredictionService(sensor);
		String uuid = ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2);

		// Query all transitions where the firstSensorEvent key equals the supplied
		// sensorEvent i.e. getPredictions!
		Transition newTransition = KnowledgeRepository.getInstance().getTransition(uuid);

		assertThat(newTransition).isNotNull();

	}

	@Test
	public final void givenTwoTransitionsWithSameFirstSensorEvent_whenGetPredictionsForFirstSensorEventInvoked_thenTwoPredictionsShouldBeReturned() {

		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		float[] values3 = new float[] { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		// Given three valid SensorEvents logged
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		sensorEvent2
				.setTimestamp(sensorEvent1.getTimestamp() + minimumDelayBetweenReadings);
		SensorEvent sensorEvent3 = new SensorEvent(sensor, values3);
		sensorEvent3
				.setTimestamp(sensorEvent2.getTimestamp() + minimumDelayBetweenReadings);

		PredictionService ps = new PredictionService(sensor);
		ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2);
		ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent3);
		Utils.allowDataUpdatesTimeToCatchUp();
		// Then getPredictionsForSensorEvent() should return the two predicted
		// sensorEvents

		List<Prediction> predictedSensorEvents = ps
				.getPredictionsForSensorEvent(sensorEvent1);

		assertThat(predictedSensorEvents).isNotNull().isNotEmpty();
		assertThat(predictedSensorEvents.size() == 2).isTrue();
	}

	@Test
	public final void givenTransitionWithDifferentSenorTypes_whenLogged_thenRepositoryExceptionShouldBeThrown() {

		// Given two valid SensorEvents from different entities
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 2.1f, 2.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		Sensor sensor2 = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor2, values2);

		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent1.getKey(),
				sensorEvent1, true);
		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent2.getKey(),
				sensorEvent2, true);

		// And given 1 transition with different entities
		SensorEvent firstSensorEvent = new SensorEvent(sensor, values);
		SensorEvent predictedSensorEvent = new SensorEvent(sensor2, values2);

		// When
		PredictionService ps = new PredictionService(sensor);
//		ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2);

		final Throwable throwable = catchThrowable(
				() -> ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2));

		// Then
		then(throwable).as(
				"An RepositoryException should be thrown when sensorEvents from different sensors are logged")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenTransitionWithDifferentSenorLocations_whenLogged_thenRepositoryExceptionShouldBeThrown() {

		// Given two valid SensorEvents from different entities
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 2.1f, 2.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		Sensor sensor2 = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_BODY);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor2, values2);

		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent1.getKey(),
				sensorEvent1, true);
		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent2.getKey(),
				sensorEvent2, true);

		// And given 1 transition with different sensor types

		// When
		PredictionService ps = new PredictionService(sensor);
//		ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2);
		final Throwable throwable = catchThrowable(
				() -> ps.createPredictionFromSensorEvents(sensorEvent1, sensorEvent2));

		// Then
		then(throwable).as(
				"An RepositoryException should be thrown when sensorEvents from different sensors are logged")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenInvalidTransitionWithoutPredictedSensorEvent_whenTranslatedAndLogged_thenItshouldThrowValidationException() {
		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 2.1f, 2.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent1.getKey(),
				sensorEvent1, true);
		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent2.getKey(),
				sensorEvent2, true);

		SensorEvent firstSensorEvent = new SensorEvent(sensor, values);

		// When the invalid transition
		PredictionService ps = new PredictionService(sensor);
		final Throwable throwable = catchThrowable(
				() -> ps.createPredictionFromSensorEvents(firstSensorEvent, null));

		// Then ValidationException should occur
		then(throwable).as(
				"A IllegalArgumentException should be thrown for an invalid schema validation")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNegativeProbability_whenSetOnTransition_thenIlegalArgumentException() {

		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 21.1f, 21.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		// When the invalid transition

		PredictionService ps = new PredictionService(sensor);
		Transition t1 = new Transition(sensorEvent1, sensorEvent2);
		final Throwable throwable = catchThrowable(() -> t1.setProbability(-1.0));

		// Then ValidationException should occur
		then(throwable).as(
				"A IllegalArgumentException should be thrown for a negative probability")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenOutOfRangeProbability_whenSetOnTransition_thenIlegalArgumentException() {

		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 21.1f, 21.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		// When the invalid transition

		PredictionService ps = new PredictionService(sensor);
		Transition t1 = new Transition(sensorEvent1, sensorEvent2);
		final Throwable throwable = catchThrowable(() -> t1.setProbability(1.0001));

		then(throwable).as(
				"A IllegalArgumentException should be thrown for a probability not between 0.0 and 1.0")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidFirstEventKey_whenSetOnTransition_thenIlegalArgumentException() {

		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 21.1f, 21.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		// When the invalid transition

		PredictionService ps = new PredictionService(sensor);
		Transition t1 = new Transition(sensorEvent1, sensorEvent2);
		final Throwable throwable = catchThrowable(() -> t1.setFirstSensorEventKey(null));
		final Throwable throwable2 = catchThrowable(() -> t1.setFirstSensorEventKey(""));

		then(throwable).as(
				"A IllegalArgumentException should be thrown for a null first  event key")
				.isInstanceOf(IllegalArgumentException.class);
		then(throwable2).as(
				"A IllegalArgumentException should be thrown for empty first  event key")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenInvalidPredictedEvent_whenSetOnTransition_thenIlegalArgumentException() {

		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 21.1f, 21.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		// When the invalid transition

		PredictionService ps = new PredictionService(sensor);
		Transition t1 = new Transition(sensorEvent1, sensorEvent2);
		final Throwable throwable = catchThrowable(
				() -> t1.setPredictedSensorEvent(null));

		then(throwable).as(
				"A IllegalArgumentException should be thrown for a null first  event key")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNegativeDataPointDuration_whenSetOnTransition_thenIlegalArgumentException() {

		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 2.1f, 2.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		Transition t1 = new Transition(sensorEvent1, sensorEvent2);
		final Throwable throwable = catchThrowable(() -> t1.setDataPointDurationNano(-1));

		// Then ValidationException should occur
		then(throwable).as(
				"A IllegalArgumentException should be thrown for a negative data point duration")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNegativeCashOnHand_whenSetOnTransition_thenIlegalArgumentException() {

		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 2.1f, 2.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		Transition t1 = new Transition(sensorEvent1, sensorEvent2);
		final Throwable throwable = catchThrowable(() -> t1.setCashOnHand(-0.01));

		// Then ValidationException should occur
		then(throwable).as(
				"A IllegalArgumentException should be thrown for a negative cash on hand")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNegativeTimestamp_whenSetOnTransition_thenIlegalArgumentException() {

		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 2.1f, 2.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		Transition t1 = new Transition(sensorEvent1, sensorEvent2);
		final Throwable throwable = catchThrowable(() -> t1.setTimestamp(-1));

		// Then ValidationException should occur
		then(throwable).as(
				"A IllegalArgumentException should be thrown for a negative timestamp")
				.isInstanceOf(IllegalArgumentException.class);

	}

}
