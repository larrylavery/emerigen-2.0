package com.emerigen.infrastructure.repository.couchbase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import com.emerigen.infrastructure.sensor.HeartRateSensor;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.utils.Utils;

//@RunWith(MockitoJUnitRunner.class)
public class CouchbaseSensorEventRepositoryTest {

	String uuid = UUID.randomUUID().toString();

	@Test
	public final void givenTwoSensorEventsLogged_WhenCountQueriedByKey_ThenCountShouldBe2() {

		// Given - A Connection to the repository has been established
		SoftAssertions softly = new SoftAssertions();
		Random rd = new Random();
		float[] values = new float[] { rd.nextFloat(), 1.2f };
		float[] values2 = new float[] { rd.nextFloat(), 1.2f };

		// Given
		// Create two JSON Documents
		long timestamp = System.currentTimeMillis();
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);

		// Create predicted sensorEvent json document
		JsonArray jsonArray1 = JsonArray.create();
		for (int i = 0; i < event1.getValues().length; i++) {
			jsonArray1.add(event1.getValues()[i]);
		}
		// Create predicted sensorEvent json document
		JsonArray jsonArray2 = JsonArray.create();
		for (int i = 0; i < event2.getValues().length; i++) {
			jsonArray2.add(event2.getValues()[i]);
		}

		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		JsonObject sensorEventJsonDoc = JsonObject.create().put("type", "sensor-event")
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_WATCH)
				.put("timestamp", "" + timestamp).put("values", jsonArray1);

		JsonObject sensorEventJsonDoc2 = JsonObject.create().put("type", "sensor-event")
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_WATCH)
				.put("timestamp", "" + timestamp).put("values", jsonArray1);

		// Log using our repository under test
		CouchbaseRepository.getInstance().log(uuid1, sensorEventJsonDoc, true);
		CouchbaseRepository.getInstance().log(uuid2, sensorEventJsonDoc, true);

		Utils.allowDataUpdatesTimeToCatchUp();
		String statement = "SELECT COUNT(*) FROM `knowledge` WHERE type= \"sensor-event\" AND sensorType= "
				+ sensor.getSensorType() + " AND sensorLocation= "
				+ sensor.getSensorLocation() + " AND timestamp = \"" + timestamp + "\"";
		QueryResult result = CouchbaseRepository.getInstance().query(statement);

		List<JsonObject> jsonObjects = result.rowsAsObject();
		int count = jsonObjects.get(0).getInt("$1");
		assertThat(count).isEqualTo(2);
		softly.assertAll();

	}

	@Test
	public final void givenValidSensorEventDocLogged_WhenQueriedByPrimaryKey_ThenDocShouldBeFound() {

		// Given - A Connection to the repository has been established
		SoftAssertions softly = new SoftAssertions();

		String timestamp = UUID.randomUUID().toString();
		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = new float[] { rd.nextFloat(), rd.nextFloat() };
		float[] values3 = new float[] { rd.nextFloat(), rd.nextFloat() };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);

		// Create predicted sensorEvent json document
		JsonArray jsonArray2 = JsonArray.create();
		for (int i = 0; i < event1.getValues().length; i++) {
			jsonArray2.add(event1.getValues()[i]);
		}

		JsonObject sensorEvent = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE).put("timestamp", timestamp)
				.put("values", jsonArray2);

		// Store the Document
		String uuid = UUID.randomUUID().toString();
		CouchbaseRepository.getInstance().log(uuid, sensorEvent, true);

		// Retrieve by primary using the repository under test
		JsonObject getDoc = CouchbaseRepository.getInstance().get(uuid, "");

		assertThat(getDoc).isNotNull();
		softly.assertThat(getDoc.getString("timestamp")).isEqualTo(timestamp);
		softly.assertThat(getDoc.getInt("sensorType")).isEqualTo(Sensor.TYPE_HEART_RATE);
		softly.assertThat(
				getDoc.getArray("values").equals(JsonArray.from("4.1", "4.2", "4.3")));

		softly.assertAll();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
