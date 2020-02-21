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
	public final void givenTwoDifferentCyclesLogged_WhenCountQueriedByKey_ThenCountShouldBe2() {
	}

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
		JsonObject sensorEventJsonDoc = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("timestamp", "" + timestamp).put("values", jsonArray1);

		JsonObject sensorEventJsonDoc2 = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("timestamp", "" + timestamp).put("values", jsonArray1);

		// Log using our repository under test
		CouchbaseRepository.getInstance().log("sensor-event", uuid1, sensorEventJsonDoc);
		CouchbaseRepository.getInstance().log("sensor-event", uuid2, sensorEventJsonDoc);

		// Perform a N1QL Query
		JsonObject placeholderValues = JsonObject.create()
				.put("sensorType", sensor.getType())
				.put("sensorLocation", sensor.getLocation());

		N1qlQueryResult result = CouchbaseRepository.getInstance().query("sensor-event",
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
		CouchbaseRepository.getInstance().log("sensor-event", uuid, sensorEvent);

		// Retrieve by primary using the repository under test
		JsonDocument getDoc = CouchbaseRepository.getInstance().get("sensor-event", uuid);

		assertThat(getDoc).isNotNull();
		softly.assertThat(getDoc.content().getString("timestamp")).isEqualTo(timestamp);
		softly.assertThat(getDoc.content().getInt("sensorType"))
				.isEqualTo(Sensor.TYPE_HEART_RATE);
		softly.assertThat(getDoc.content().get("values")
				.equals(JsonArray.from("4.1", "4.2", "4.3")));

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
