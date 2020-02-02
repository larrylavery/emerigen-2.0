package com.emerigen.infrastructure.repository.couchbase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//Couchbase
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.emerigen.infrastructure.sensor.HeartRateSensor;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;

//@RunWith(MockitoJUnitRunner.class)
public class CouchbaseSensorEventRepositoryTest {

	String uuid = UUID.randomUUID().toString();

	@Test
	public final void givenTwoSensorEventsLogged_WhenCountQueriedByKey_ThenCountShouldBe2() {

		// Given - A Connection to the repository has been established
		SoftAssertions softly = new SoftAssertions();

		// Given
		// Create two JSON Documents
		long timestamp = System.currentTimeMillis();
		float[] values = { 1.1f, 2.1f };
		float[] values2 = { 4.1f, 2.1f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.TYPE_HEART_RATE, Sensor.DELAY_NORMAL,
				false);
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);

		JsonObject sensorEventJsonDoc = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE).put("timestamp", "" + timestamp)
				.put("values", JsonArray.from("4.1", "4.2", "4.3"));

		JsonObject sensorEventJsonDoc2 = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE).put("timestamp", "" + timestamp)
				.put("values", JsonArray.from("41.1", "411.2", "4.3"));

		// Log using our repository under test
		CouchbaseRepository.getInstance().log("sensor-event", event1.getKey(), sensorEventJsonDoc);
		CouchbaseRepository.getInstance().log("sensor-event", event2.getKey(), sensorEventJsonDoc);

		// Perform a N1QL Query
		JsonObject placeholderValues = JsonObject.create().put("sensorType", sensor.getType())
				.put("sensorLocation", sensor.getLocation());

		N1qlQueryResult result = CouchbaseRepository.getInstance()
				.query("sensor-event",
						N1qlQuery.parameterized(
								"SELECT COUNT(*) FROM `sensor-event` WHERE sensorType = $sensorType"
										+ " AND sensorLocation = $sensorLocation",
								placeholderValues));

		softly.assertThat(result).isNotNull().isNotEmpty();
		softly.assertThat(result.info().resultCount() == 2);
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
		HeartRateSensor sensor = new HeartRateSensor(Sensor.TYPE_HEART_RATE, Sensor.DELAY_NORMAL,
				false);
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);

		JsonObject sensorEvent = JsonObject.create().put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("timestamp", timestamp).put("values", JsonArray.from("4.1", "4.2", "4.3"));

		// Store the Document
		CouchbaseRepository.getInstance().log("sensor-event", event1.getKey(), sensorEvent);

		// Retrieve by primary using the repository under test
		JsonDocument getDoc = CouchbaseRepository.getInstance().get("sensor-event",
				event1.getKey());

		assertThat(getDoc).isNotNull();
		softly.assertThat(getDoc.content().getString("timestamp")).isEqualTo(timestamp);
		softly.assertThat(getDoc.content().getInt("sensorType")).isEqualTo(Sensor.TYPE_HEART_RATE);
		softly.assertThat(
				getDoc.content().get("values").equals(JsonArray.from("4.1", "4.2", "4.3")));

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
