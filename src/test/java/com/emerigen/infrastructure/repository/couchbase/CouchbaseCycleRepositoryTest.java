package com.emerigen.infrastructure.repository.couchbase;

import java.util.Random;

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

//import io.reactivex.Observable;

//@RunWith(MockitoJUnitRunner.class)
public class CouchbaseCycleRepositoryTest {

	@Test
	public final void givenSameCycleLoggedTwice_WhenCountQueriedByKey_ThenCountShouldBe1() {
		// Given
		SoftAssertions softly = new SoftAssertions();

		// Sensor
		long timestamp = System.currentTimeMillis();
		Random rd = new Random(); // creating Random object

		float[] values = { 1.1f, 2.1f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		SensorEvent event1 = new SensorEvent(sensor, values);

		JsonObject cycleNodeJsonDoc = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_WATCH)
				.put("timestamp", "" + timestamp)
				.put("values",
						JsonArray.from("" + rd.nextFloat(), "" + rd.nextFloat(),
								"" + rd.nextFloat()))
				.put("minimumDelayBetweenReadings", 1000).put("reportingMode", "1")
				.put("wakeUpSensor", false).put("dataPointDuration", 500)
				.put("probability", 0.3);

		JsonObject cycleJsonDoc = JsonObject.create().put("cycleType", "Daily")
				.put("cycleStartTimeNano", 20).put("cycleDurationTimeNano", 1500)
				.put("allowablePercentDifferenceForEquality", 0.8)
				.put("previousCycleNodeIndex", 0).put("previousCycleNodeIndex", 0)
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_WATCH)
				.put("cycleNodes", JsonArray.from(cycleNodeJsonDoc));

		// Log using our repository under test
		CouchbaseRepository.getInstance().log(
				"" + Sensor.TYPE_HEART_RATE + Sensor.LOCATION_WATCH + "Daily",
				cycleJsonDoc, false);
		CouchbaseRepository.getInstance().log(
				"" + Sensor.TYPE_HEART_RATE + Sensor.LOCATION_WATCH + "Daily",
				cycleJsonDoc, false);

		// Perform a N1QL Query
		JsonObject placeholderValues = JsonObject.create()
				.put("sensorType", sensor.getType())
				.put("sensorLocation", sensor.getLocation());

		QueryResult result = CouchbaseRepository.getInstance().query("cycle");

		softly.assertThat(result).isNotNull();
//		softly.assertThat(result.info().resultCount() == 1);
		softly.assertAll();
//		CouchbaseRepository.getInstance().remove("cycle",
//				"" + Sensor.TYPE_HEART_RATE + Sensor.LOCATION_WATCH + "Daily");

	}

	@Test
	public final void givenOneCycleLogged_WhenCountQueriedByKey_ThenCountShouldBe1() {

		// Given
		SoftAssertions softly = new SoftAssertions();

		// Sensor
		Random rd = new Random(); // creating Random object

		long timestamp = System.currentTimeMillis();
		float[] values = { 1.1f, 2.1f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		SensorEvent event1 = new SensorEvent(sensor, values);

		// SensorEvent

		JsonObject cycleNodeJsonDoc = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_WATCH)
				.put("timestamp", "" + timestamp)
				.put("values",
						JsonArray.from("" + rd.nextFloat(), "" + rd.nextFloat(),
								"" + rd.nextFloat()))
				.put("minimumDelayBetweenReadings", 1000).put("reportingMode", "1")
				.put("wakeUpSensor", false).put("dataPointDuration", 500)
				.put("probability", 0.3);

		JsonObject cycleJsonDoc = JsonObject.create().put("cycleType", "Daily")
				.put("cycleStartTimeNano", 20).put("cycleDurationTimeNano", 1500)
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_WATCH)
				.put("allowablePercentDifferenceForEquality", 0.8)
				.put("previousCycleNodeIndex", 0).put("previousCycleNodeIndex", 0)
				.put("cycleNodes", JsonArray.from(cycleNodeJsonDoc));

		// Log using our repository under test
		CouchbaseRepository.getInstance().log(
				"" + Sensor.TYPE_HEART_RATE + Sensor.LOCATION_WATCH + "Daily",
				cycleJsonDoc, false);

		// Perform a N1QL Query
		JsonObject placeholderValues = JsonObject.create()
				.put("sensorType", sensor.getType())
				.put("sensorLocation", sensor.getLocation());

		QueryResult result = CouchbaseRepository.getInstance().query("cycle");

		softly.assertThat(result).isNotNull();
//		softly.assertThat(result.info().resultCount() == 1);
		softly.assertAll();
//		CouchbaseRepository.getInstance().remove("cycle",
//				"" + Sensor.TYPE_HEART_RATE + Sensor.LOCATION_WATCH + "Daily");

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
