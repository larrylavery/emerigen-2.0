package com.emerigen.infrastructure.sensor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.InputStream;
import java.util.Random;

import org.assertj.core.api.SoftAssertions;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.utils.Utils;

public class SensorEventTest {

	@Test
	public final void givenValidSensorEvent_whenLogged_thenItshouldBeTheSameWhenRetrieved() {
		SoftAssertions softly = new SoftAssertions();
		// Given
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_WATCH);
		sensor.setMinimumDelayBetweenReadings(1000);
		sensor.setReportingMode(Sensor.DELAY_NORMAL);
		sensor.setWakeUpSensor(false);

		// Create sensorEvent with these parms
		long timestamp = System.currentTimeMillis() * 1000000;
		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		SensorEvent event = new SensorEvent(sensor, values);
		event.setTimestamp(timestamp);

		String key = KnowledgeRepository.getInstance().newSensorEvent(event);
		Utils.allowDataUpdatesTimeToCatchUp();
		SensorEvent retrievedSensorEvent = KnowledgeRepository.getInstance()
				.getSensorEvent(key);
		assertThat(retrievedSensorEvent).isNotNull();
		assertThat(retrievedSensorEvent.getTimestamp()).isEqualTo(timestamp);
		assertThat(retrievedSensorEvent.getSensorType())
				.isEqualTo(Sensor.TYPE_HEART_RATE);
		assertThat(retrievedSensorEvent.getSensorLocation())
				.isEqualTo(Sensor.LOCATION_WATCH);
		assertThat(retrievedSensorEvent.getValues().length).isEqualTo(2);

		assertThat(retrievedSensorEvent.getSensor()).isNotNull();
		assertThat(retrievedSensorEvent.getSensor().getSensorType())
				.isEqualTo(Sensor.TYPE_HEART_RATE);
		assertThat(retrievedSensorEvent.getSensor().getSensorLocation())
				.isEqualTo(Sensor.LOCATION_WATCH);
		assertThat(retrievedSensorEvent.getSensor().getMinimumDelayBetweenReadings())
				.isEqualTo(1000);
		assertThat(retrievedSensorEvent.getSensor().getReportingMode())
				.isEqualTo(Sensor.DELAY_NORMAL);
		assertThat(retrievedSensorEvent.getSensor().isWakeUpSensor()).isFalse();
		softly.assertAll();
	}

	@Test
	public void givenJsonSensorEventWithoutValues_whenValidating_thenItShouldThrowValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream sensorEventSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("sensor-event.json");
		InputStream invalidSensorEventJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/sensor-event-invalid-empty-values.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(sensorEventSchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidSensorEventJsonFileReader));
		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A invalidly structured Json sensor event document should throw a ValidationException")
				.isInstanceOf(ValidationException.class);

	}

	@Test
	public void givenValidJsonSensorEvent_whenValidating_thenItShouldValidateSuccessfully() {

		// Given the schema and the instance json docs have been read in
		InputStream sensorEventSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("sensor-event.json");
		InputStream invalidSensorEventJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/sensor-event-valid.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(sensorEventSchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidSensorEventJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json sensorEvent document should not throw a ValidationException")
				.isNull();

	}

	@Test
	public void givenJsonSensorEventWithEmptySensorValues_whenValidating_thenValidationExceptionIsThrown() {

		// Given the schema and the instance json docs have been read in
		InputStream sensorEventSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("sensor-event.json");
		InputStream invalidSensorEventJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/sensor-event-invalid-empty-values.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(sensorEventSchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidSensorEventJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A invalidly structured Json sensorEvent document should throw a ValidationException")
				.isInstanceOf(ValidationException.class);
	}

	@Test
	public void givenJsonSensorEventWithoutTimestamp_whenValidating_thenValidationExceptionIsThrown() {

		// Given the schema and the instance json docs have been read in
		InputStream sensorEventSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("sensor-event.json");
		InputStream invalidSensorEventJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/sensor-event-invalid-no-timestamp.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(sensorEventSchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidSensorEventJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A invalidly structured Json sensorEvent document should throw a ValidationException")
				.isInstanceOf(ValidationException.class);
	}

	@Test
	public void givenJsonSensorEventWithoutSensorType_whenValidating_thenValidationExceptionIsThrown() {

		// Given the schema and the instance json docs have been read in
		InputStream sensorEventSchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("sensor-event.json");
		InputStream invalidSensorEventJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/sensor-event-invalid-no-sensor-type.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(sensorEventSchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidSensorEventJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A invalidly structured Json sensorEvent document should throw a ValidationException")
				.isInstanceOf(ValidationException.class);
	}

	@Test
	public final void givenJsonSensorEventWithEmptyValues_whenCreated_thenIllegalArgumentException() {
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		float[] values = {};
		final Throwable throwable = catchThrowable(() -> new SensorEvent(sensor, values));

		then(throwable).as("empty values throws a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullValues_whenCreated_thenIllegalArgumentException() {
		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);

		float[] values = { 10.1f, 20.2f, 30.3f };
		final Throwable throwable = catchThrowable(() -> new SensorEvent(sensor, null));

		then(throwable).as("null values throws a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNullSensor_whenCreated_thenIllegalArgumentException() {

		float[] values = { 10.1f, 20.2f, 30.3f };
		final Throwable throwable = catchThrowable(() -> new SensorEvent(null, values));

		then(throwable).as("A null sensor throws a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
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
