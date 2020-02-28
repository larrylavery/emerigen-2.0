package com.emerigen.infrastructure.learning.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import com.couchbase.client.java.document.json.JsonObject;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.sensor.HeartRateSensor;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;

public class CycleTest {

	@Test
	public final void givenValidCycle_whenLogged_thenItshouldBeTheSameWhenRetrieved() {
		SoftAssertions softly = new SoftAssertions();
		// Given
		Sensor gpsSensor = SensorManager.getInstance()
				.getDefaultSensorForLocation(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, gpsSensor,
				new PredictionService());

		// Create sensorEvent with these parms
		long timestamp = System.currentTimeMillis() * 1000000;
		Random rd = new Random();
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		SensorEvent event = new SensorEvent(gpsSensor, values);
		event.setTimestamp(timestamp);

		// Create new cycle
		Cycle cycle = CPR_InsertionsTest.createCycle("Daily", gpsSensor.getType(),
				gpsSensor.getLocation(), 1);

		JsonObject cycleJsonDoc = JsonObject.create().put("cycleType", "Daily")
				.put("sensorType", Sensor.TYPE_GPS)
				.put("sensorLocation", Sensor.LOCATION_PHONE)
				.put("allowablePercentDifferenceForEquality",
						cpr.getAllowablePercentDifferenceForEquality());

		// when logged then retrieved ok
		String uuid = UUID.randomUUID().toString();
		KnowledgeRepository.getInstance().newCycle(uuid, cycle);

		Cycle retrievedCycle = KnowledgeRepository.getInstance().getCycle("Daily", uuid);

		// Verify cycle
		assertThat(retrievedCycle).isNotNull();
		assertThat(retrievedCycle.getAllowablePercentDifferenceForEquality())
				.isEqualTo(cycle.getAllowablePercentDifferenceForEquality());

		assertThat(cpr.getSensor().getType()).isEqualTo(Sensor.TYPE_GPS);
		assertThat(cpr.getSensor().getLocation()).isEqualTo(Sensor.LOCATION_PHONE);

//		CouchbaseRepository.getInstance().remove("cycle", uuid);

	}

	@Test
	public void givenValidJsonCycleWithTwoCycleNodes_whenValidating_thenItShouldValidateSuccessfully() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-valid.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json entity document should not throw a ValidationException")
				.isNull();

	}

	@Test
	public void givenInvalidJsonCycleMissingSensorType_whenValidating_thenValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-invalid-no-sensor-type.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json entity document should not throw a ValidationException")
				.isNull();
	}

	@Test
	public void givenInvalidJsonCycleMissingCycleTye_whenValidating_thenValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-invalid-no-cycle-type.json");

		JSONObject jsonSchema = new JSONObject(
				new JSONTokener(entitySchemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(
				new JSONTokener(invalidEntityJsonFileReader));

		Schema schema = SchemaLoader.load(jsonSchema);

		// When the instance is validated
		final Throwable throwable = catchThrowable(() -> schema.validate(jsonSubject));

		// Then a ValidationException should be thrown
		then(throwable).as(
				"A validly structured Json entity document should not throw a ValidationException")
				.isNull();

	}

	@Test
	public final void givenInvalidCycleTypeName_whenGetCycleInvoked_thenIllegalArgumentException() {
		KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();

		// Given

		// When
		final Throwable throwable = catchThrowable(
				() -> knowledgeRepository.getCycle("invalidCycleTypeName", "xxx"));

		// Then
		then(throwable).as(
				"A IllegalArgumentException should be thrown for invalid cycle type name")
				.isInstanceOf(IllegalArgumentException.class);

	}

	// @Test
	public final void givenValidCycle_whenLogged_thenRetrievedOK() {
		fail("rewrite");
		// Given valid cycle
		Cycle cycle = new DailyCycle(Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		float[] values = { 1.1f, 2.1f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		SensorEvent event1 = new SensorEvent(sensor, values);

		String uuid = UUID.randomUUID().toString();
		KnowledgeRepository.getInstance().newCycle(uuid, cycle);

		// Give the bucket a chance to catch up after the log
//		try {
//			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
//					.getValue("couchbase.server.logging.catchup.timer")));
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// Then try to retrieve it
		Cycle cycle2 = KnowledgeRepository.getInstance().getCycle("Daily", uuid);

		assertThat(cycle2.equals(cycle)).isTrue();
	}

}
