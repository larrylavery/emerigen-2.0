package com.emerigen.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.InputStream;
import java.util.Random;

import org.assertj.core.api.SoftAssertions;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.emerigen.infrastructure.learning.Cycle;
import com.emerigen.infrastructure.learning.CycleNode;
import com.emerigen.infrastructure.learning.DailyCycle;
import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.sensor.HeartRateSensor;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.CircularList;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class CycleTest {

	@Test
	public final void givenValidCycle_whenLogged_thenItshouldBeTheSameWhenRetrieved() {
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

		// Add these fields to the cycle
		Cycle cycle = new DailyCycle();
		cycle.setCycleStartTimeNano(20);
		cycle.setSensorType(sensor.getType());
		cycle.setSensorLocation(sensor.getLocation());
		cycle.setCycleDurationTimeNano(1500);
		cycle.setAllowableStandardDeviationForEquality(0.8);
		cycle.setPreviousCycleNodeIndex(0);

		CycleNode cycleNode = new CycleNode();
		cycleNode.setSensorEvent(event);

		JsonObject cycleNodeJsonDoc = JsonObject.create()
//				.put("sensorType", Sensor.TYPE_HEART_RATE)
//				.put("sensorLocation", Sensor.LOCATION_WATCH)
				.put("timestamp", "" + timestamp)
				.put("values",
						JsonArray.from("" + rd.nextFloat(), "" + rd.nextFloat(),
								"" + rd.nextFloat()))
				.put("minimumDelayBetweenReadings", 1000).put("reportingMode", "1")
				.put("wakeUpSensor", false).put("dataPointDuration", 500)
				.put("probability", 0.3);

		JsonObject cycleJsonDoc = JsonObject.create().put("cycleType", "Daily")
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_WATCH)
				.put("cycleStartTimeNano", 20).put("cycleDurationTimeNano", 1500)
				.put("allowableStandardDeviationForEquality", 0.8)
				.put("previousCycleNodeIndex", 0).put("previousCycleNodeIndex", 0)
				.put("nodeList", JsonArray.from(cycleNodeJsonDoc));

		cycleNode.setDataPointDurationNano(500);
		cycleNode.setallowableStandardDeviationForEquality(0.8);
		cycleNode.setProbability(0.3);
		cycleNode.setDataPointDurationNano(500);
		cycleNode.setSensorEvent(event);
		cycleNode.setMyCycle(cycle);
		cycle.addCycleNode(cycleNode);

		// when logged then retrieved ok
		KnowledgeRepository.getInstance().newCycle(cycle);

		Cycle retrievedCycle = KnowledgeRepository.getInstance().getCycle("Daily",
				cycle.getKey());

		// Verify cycle
		assertThat(retrievedCycle).isNotNull();
		assertThat(retrievedCycle.getCycleStartTimeNano()).isEqualTo(20);
		assertThat(retrievedCycle.getCycleDurationTimeNano()).isEqualTo(1500);
		assertThat(retrievedCycle.getAllowableStandardDeviationForEquality())
				.isEqualTo(0.8);
		assertThat(retrievedCycle.getPreviousCycleNodeIndex()).isEqualTo(0);

		// Verify cycle node
		CircularList<CycleNode> newCycleNodes = retrievedCycle.getNodeList();
		assertThat(newCycleNodes).isNotNull().isNotEmpty();
		CycleNode newCycleNode = newCycleNodes.get(0);
		assertThat(newCycleNode).isNotNull();
		assertThat(newCycleNode.getDataPointDurationNano()).isEqualTo(500);
		assertThat(newCycleNode.getProbability()).isEqualTo(0.3);

		SensorEvent se = newCycleNode.getSensorEvent();
//		assertThat(se.getTimestamp()).isEqualTo(timestamp);

		assertThat(se.getSensorType()).isEqualTo(Sensor.TYPE_HEART_RATE);
		assertThat(se.getSensorLocation()).isEqualTo(Sensor.LOCATION_WATCH);
		assertThat(se.getValues().length).isEqualTo(2);

		// Verify the sensor
		assertThat(se.getSensor()).isNotNull();
		assertThat(se.getSensor().getType()).isEqualTo(Sensor.TYPE_HEART_RATE);
		assertThat(se.getSensor().getLocation()).isEqualTo(Sensor.LOCATION_WATCH);
		assertThat(se.getSensor().getMinimumDelayBetweenReadings()).isEqualTo(1000);
		assertThat(se.getSensor().getReportingMode()).isEqualTo(Sensor.DELAY_NORMAL);
		assertThat(se.getSensor().isWakeUpSensor()).isFalse();
		softly.assertAll();
	}

	@Test
	public void givenValidJsonCycleWithTwoCycleNodes_whenValidating_thenItShouldValidateSuccessfully() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-valid-two-cycle-nodes.json");

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
	public void givenValidJsonCycle_whenValidating_thenItShouldValidateSuccessfully() {

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
	public void givenInvalidJsonCycleMissingCycleNode_whenValidating_thenValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-invalid-no-cycle-node.json");

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
	public void givenInvalidJsonCycleMissingSensorEvent_whenValidating_thenValidationException() {

		// Given the schema and the instance json docs have been read in
		InputStream entitySchemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("cycle.json");
		InputStream invalidEntityJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream("test/cycle-invalid-no-sensor-event.json");

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
//		"cycletype": "Daily",
//		"cycleStartTimeNano" : 1,
//		"cycleDurationTimeNano": 4,
//		"allowableStandardDeviationForEquality": 1.1,
//		"cycleNodes" : [
//			 {
//			 "sensorEvent" : {
//			 	"timestamp": 123,
//			 	"sensorType": 1,
//			 	"sensorLocation": 1,
//			 	"values": [1.1,2.2]
//			 },
//			 	"timestamp": 123,
//				"startTimeOffsetNano": 2,
//				"dataPointDurationNano": 1,
//				"probability": 0.4
//			}	
//		]
//
		// Given valid cycle
		Cycle cycle = new DailyCycle(Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		float[] values = { 1.1f, 2.1f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.TYPE_HEART_RATE,
				Sensor.DELAY_NORMAL, false);
		SensorEvent event1 = new SensorEvent(sensor, values);

		// add 1 node
		CycleNode node = new CycleNode(cycle, event1);
		cycle.getNodeList().add(node);

		KnowledgeRepository.getInstance().newCycle(cycle);

		// Give the bucket a chance to catch up after the log
		try {
			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
					.getValue("couchbase.server.logging.catchup.timer")));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Then try to retrieve it
		Cycle cycle2 = KnowledgeRepository.getInstance().getCycle("Daily",
				cycle.getKey());
//
//		String statement = "SELECT predictedSensorEventKey FROM `transition` WHERE firstSensorEventKey = \""
//				+ sensorEventKey + "\"";
//		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
//				N1qlQuery.simple(statement));
//		List<String> predictedSensorEventKeys = new ArrayList<String>();
//		for (N1qlQueryRow row : result) {
//			predictedSensorEventKeys.add(row.value().toString());
//		}

		assertThat(cycle2.equals(cycle)).isTrue();
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
