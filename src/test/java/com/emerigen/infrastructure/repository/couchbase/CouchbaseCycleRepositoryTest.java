package com.emerigen.infrastructure.repository.couchbase;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.emerigen.infrastructure.sensor.HeartRateSensor;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;

//import io.reactivex.Observable;

//@RunWith(MockitoJUnitRunner.class)
public class CouchbaseCycleRepositoryTest {

	@Test
	public final void givenSameCycleLoggedTwice_WhenCountQueriedByKey_ThenCountShouldBe1() {
		// Given
		SoftAssertions softly = new SoftAssertions();

		// Sensor
		long timestamp = System.currentTimeMillis();
		float[] values = { 1.1f, 2.1f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.TYPE_HEART_RATE, Sensor.DELAY_NORMAL,
				false);
		SensorEvent event1 = new SensorEvent(sensor, values);

		// SensorEvent
		JsonObject sensorEventJsonDoc = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE).put("timestamp", "" + timestamp)
				.put("values", JsonArray.from("4.1", "4.2", "4.3"));

		// CycleNode
//	 	"timestamp": 123,
//		"startTimeOffsetNano": 2,
//		"dataPointDuration": 1,
//		"probability": 0.4
		JsonObject cycleNodeJsonDoc = JsonObject.create().put("timestamp", 123)
				.put("startTimeOffsetNano", 24).put("dataPointDurationNano", 1)
				.put("probability", 0.3).put("sensorEvent", sensorEventJsonDoc);

		// Cycle json doc
//		"cycletype": "Daily",
//		"cycleStartTimeNano" : 1,
//		"cycleDurationTimeNano": 4,
//		"allowableStandardDeviationForEquality": 1.1,
//		"cycleNodes" : [
		JsonObject cycleJsonDoc = JsonObject.create().put("cycleType", "Daily")
				.put("cycleTimeNano", 24).put("cycleDurationTimeNano", 1)
				.put("allowableStandardDeviationForEquality", 1.1)
				.put("cycleNodes", JsonArray.from(cycleNodeJsonDoc));

		// Log using our repository under test
		CouchbaseRepository.getInstance().logWithOverwrite("cycle",
				"" + Sensor.TYPE_HEART_RATE + Sensor.LOCATION_PHONE, cycleJsonDoc);
		CouchbaseRepository.getInstance().logWithOverwrite("cycle",
				"" + Sensor.TYPE_HEART_RATE + Sensor.LOCATION_PHONE, cycleJsonDoc);

		// Perform a N1QL Query
		JsonObject placeholderValues = JsonObject.create().put("sensorType", sensor.getType())
				.put("sensorLocation", sensor.getLocation());

		N1qlQueryResult result = CouchbaseRepository.getInstance()
				.query("sensor-event",
						N1qlQuery.parameterized(
								"SELECT COUNT(*) FROM `cycle` WHERE sensorType = $sensorType"
										+ " AND sensorLocation = $sensorLocation",
								placeholderValues));

		softly.assertThat(result).isNotNull().isNotEmpty();
		softly.assertThat(result.info().resultCount() == 1);
		softly.assertAll();

	}

	@Test
	public final void givenOneCycleLogged_WhenCountQueriedByKey_ThenCountShouldBe1() {
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
//				"dataPointDuration": 1,
//				"probability": 0.4
//			}	
//		]

		// Given
		SoftAssertions softly = new SoftAssertions();

		// Sensor
		long timestamp = System.currentTimeMillis();
		float[] values = { 1.1f, 2.1f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.TYPE_HEART_RATE, Sensor.DELAY_NORMAL,
				false);
		SensorEvent event1 = new SensorEvent(sensor, values);

		// SensorEvent
		JsonObject sensorEventJsonDoc = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE).put("timestamp", "" + timestamp)
				.put("values", JsonArray.from("4.1", "4.2", "4.3"));

		// CycleNode
//	 	"timestamp": 123,
//		"startTimeOffsetNano": 2,
//		"dataPointDuration": 1,
//		"probability": 0.4
		JsonObject cycleNodeJsonDoc = JsonObject.create().put("timestamp", 123)
				.put("startTimeOffsetNano", 24).put("dataPointDurationNano", 1)
				.put("probability", 0.3).put("sensorEvent", sensorEventJsonDoc);

		// Cycle json doc
//		"cycletype": "Daily",
//		"cycleStartTimeNano" : 1,
//		"cycleDurationTimeNano": 4,
//		"allowableStandardDeviationForEquality": 1.1,
//		"cycleNodes" : [
		JsonObject cycleJsonDoc = JsonObject.create().put("cycleType", "Daily")
				.put("cycleTimeNano", 24).put("cycleDurationTimeNano", 1)
				.put("allowableStandardDeviationForEquality", 1.1)
				.put("cycleNodes", JsonArray.from(cycleNodeJsonDoc));

		// Log using our repository under test
		CouchbaseRepository.getInstance().logWithOverwrite("cycle",
				"" + Sensor.TYPE_HEART_RATE + Sensor.LOCATION_PHONE, cycleJsonDoc);

		// Perform a N1QL Query
		JsonObject placeholderValues = JsonObject.create().put("sensorType", sensor.getType())
				.put("sensorLocation", sensor.getLocation());

		N1qlQueryResult result = CouchbaseRepository.getInstance()
				.query("sensor-event",
						N1qlQuery.parameterized(
								"SELECT COUNT(*) FROM `cycle` WHERE sensorType = $sensorType"
										+ " AND sensorLocation = $sensorLocation",
								placeholderValues));

		softly.assertThat(result).isNotNull().isNotEmpty();
		softly.assertThat(result.info().resultCount() == 1);
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
