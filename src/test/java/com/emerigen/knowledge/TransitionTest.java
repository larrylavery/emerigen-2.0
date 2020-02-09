package com.emerigen.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepository;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class TransitionTest {

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

	@Test
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

	@Test
	public void givenJsonTransitionWithoutSensorType_whenValidating_thenValidationExceptionIsThrown() {

		// Given the schema and the instance json docs have been read in
		InputStream transitionSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("transition.json");
		InputStream invalidTransitionJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/transition-invalid-no-sensor-type.json");

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
	public final void givenValidTransition_whenCreated_thenPredictedSensorEventKeyRetrieved() {

		// Given

		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat(), rd.nextFloat() };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		sensor.setMinimumDelayBetweenReadings(1000);
		sensor.setReportingMode(Sensor.DELAY_NORMAL);
		sensor.setWakeUpSensor(false);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		KnowledgeRepository.getInstance().newTransition(sensorEvent1, sensorEvent2);

		try {
			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
					.getValue("couchbase.server.logging.catchup.timer")));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Query all transitions where the firstSensorEvent key equals the supplied
		// sensorEvent

		String sensorEventKey = sensorEvent1.getKey();
		String statement = "SELECT predictedSensorEventKey FROM `transition` WHERE firstSensorEventKey = \""
				+ sensorEventKey + "\"";
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
				N1qlQuery.simple(statement));
		List<String> predictedSensorEventKeys = new ArrayList<String>();
		for (N1qlQueryRow row : result) {
			predictedSensorEventKeys.add(row.value().toString());
		}

		assertThat(predictedSensorEventKeys.size()).isEqualTo(1);

	}

	@Test
	public final void givenTwoTransitionsWithSameFirstSensorEvent_whenGetPredictionsForFirstSensorEventInvoked_thenTwoPredictionsShouldBeReturned() {

		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values3 = new float[] { rd.nextFloat(), rd.nextFloat() };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		// Given three valid SensorEvents logged
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);
		SensorEvent sensorEvent3 = new SensorEvent(sensor, values3);

		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent2);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent3);

		// And given 2 valid transitions with same firstSensorEvents logged
		SensorEvent firstSensorEvent = new SensorEvent(sensor, values);
		SensorEvent predictedSensorEvent = new SensorEvent(sensor, values2);
		SensorEvent predictedSensorEvent2 = new SensorEvent(sensor, values3);

		KnowledgeRepository.getInstance().newTransition(firstSensorEvent,
				predictedSensorEvent);
		KnowledgeRepository.getInstance().newTransition(firstSensorEvent,
				predictedSensorEvent2);

		// Give the bucket a chance to catch up after the log
		try {
			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
					.getValue("couchbase.server.logging.catchup.timer")));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Then getPredictionsForSensorEvent() should return the two predicted
		// sensorEvents
		List<SensorEvent> predictedSensorEventKeys = KnowledgeRepository.getInstance()
				.getPredictionsForSensorEvent(firstSensorEvent);

		String predictedSensorEventKey = predictedSensorEvent.getKey();

		String predictedSensorEvent2Key = predictedSensorEvent2.getKey();
		assertThat(predictedSensorEventKeys.contains(predictedSensorEventKey));
		assertThat(predictedSensorEventKeys.contains(predictedSensorEvent2Key));
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

		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent2);

		// And given 1 transition with different entities
		SensorEvent firstSensorEvent = new SensorEvent(sensor, values);
		SensorEvent predictedSensorEvent = new SensorEvent(sensor2, values2);

		// When
		final Throwable throwable = catchThrowable(() -> KnowledgeRepository.getInstance()
				.newTransition(firstSensorEvent, predictedSensorEvent));

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

		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent2);

		// And given 1 transition with different entities
//		SensorEvent firstSensorEvent = new SensorEvent(sensor, values);
//		SensorEvent predictedSensorEvent = new SensorEvent(sensor2, values2);

		// When
		final Throwable throwable = catchThrowable(() -> KnowledgeRepository.getInstance()
				.newTransition(sensorEvent1, sensorEvent2));

		// Then
		then(throwable).as(
				"An RepositoryException should be thrown when sensorEvents from different sensors are logged")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenValidTransition_whenLogged_thenPredictionRetrieved() {

		// Given two valid SensorEvents logged
		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat(), rd.nextFloat() };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent2);

		// And given one valid transition logged
		SensorEvent firstSensorEvent = new SensorEvent(sensor, values);
		SensorEvent predictedSensorEvent = new SensorEvent(sensor, values2);

		KnowledgeRepository.getInstance().newTransition(firstSensorEvent,
				predictedSensorEvent);

		// Give the bucket a chance to catch up after the log
		try {
			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
					.getValue("couchbase.server.logging.catchup.timer")));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Then the transition predictions should be retrieved and validated
		// successfully

		String sensorEventKey = firstSensorEvent.getKey();

		String statement = "SELECT predictedSensorEventKey FROM `transition` WHERE firstSensorEventKey = \""
				+ sensorEventKey + "\"";
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
				N1qlQuery.simple(statement));
		List<String> predictedSensorEventKeys = new ArrayList<String>();
		for (N1qlQueryRow row : result) {
			predictedSensorEventKeys.add(row.value().toString());
		}

		assertThat(predictedSensorEventKeys.size()).isEqualTo(1);
	}

	@Test
	public final void givenInvalidTransitionWithoutFirstSensorEvent_whenTranslatedAndLogged_thenItshouldThrowValidationException() {
		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given two valid SensorEvents logged
		float[] values = new float[] { 1.1f, 1.2f };
		float[] values2 = new float[] { 2.1f, 2.2f };
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		SensorEvent sensorEvent1 = new SensorEvent(sensor, values);
		SensorEvent sensorEvent2 = new SensorEvent(sensor, values2);

		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent2);

		SensorEvent predictedSensorEvent = new SensorEvent(sensor, values2);

		// When the invalid transition
		final Throwable throwable = catchThrowable(
				() -> knowledgeRepository.newTransition(null, predictedSensorEvent));

		// Then ValidationException should occur
		then(throwable).as(
				"A IllegalArgumentException should be thrown for an invalid schema validation")
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

		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent1);
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent2);

		SensorEvent firstSensorEvent = new SensorEvent(sensor, values);

		// When the invalid transition
		final Throwable throwable = catchThrowable(
				() -> knowledgeRepository.newTransition(firstSensorEvent, null));

		// Then ValidationException should occur
		then(throwable).as(
				"A IllegalArgumentException should be thrown for an invalid schema validation")
				.isInstanceOf(IllegalArgumentException.class);

	}

}
