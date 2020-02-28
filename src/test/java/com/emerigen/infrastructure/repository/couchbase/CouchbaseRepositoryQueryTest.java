package com.emerigen.infrastructure.repository.couchbase;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.sensor.HeartRateSensor;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;

//import io.reactivex.Observable;

//@RunWith(MockitoJUnitRunner.class)
public class CouchbaseRepositoryQueryTest {

	@Test
	public final void givenTwoTransitionsLogged_WhenQueriedByFirstSensorEventKey_ThenCountShouldBe2()
			throws InterruptedException {

		// Given - A Connection to the repository has been established
		SoftAssertions softly = new SoftAssertions();

		// Given
		// Create two JSON Documents
		float[] values = { 4.1f, 4.2f, 4.3f };
		float[] values2 = { 4.11f, 4.21f, 4.31f };
		float[] values3 = { 4.111f, 4.211f, 4.311f };
		float[] values4 = { 4.1111f, 4.2111f, 4.3111f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		SensorEvent event3 = new SensorEvent(sensor, values3);
		SensorEvent event4 = new SensorEvent(sensor, values4);

		// create 2 transitions
		PredictionService ps = new PredictionService();
		ps.createPredictionFromSensorEvents(event1, event2);
		ps.createPredictionFromSensorEvents(event1, event3);

//		Thread.sleep(500);
		String prefix = "SELECT predictedSensorEvent FROM `transition` ";
		String conditional = "WHERE firstSensorEventKey = \"" + event1.getKey() + "\"";
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
				N1qlQuery.simple(prefix + conditional));

		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.info().resultCount() == 2);
		// result.forEach(System.out::println);

		assertThat(result.allRows().get(0).value().containsValue(event1.getKey()));
		softly.assertAll();

	}

	@Test
	public final void givenTwoTransitionsLogged_WhenPredictionCountForFirstSensorEventIsQueried_thenTwoIsReturned() {

		// Given - A Connection to the repository has been established
		SoftAssertions softly = new SoftAssertions();

		// Given
		// Create two transition JSON Documents
		float[] values = { 4.1f, 4.2f, 4.3f };
		float[] values2 = { 4.11f, 4.21f, 4.31f };
		float[] values3 = { 4.111f, 4.211f, 4.311f };
		float[] values4 = { 4.1111f, 4.2111f, 4.3111f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		SensorEvent event3 = new SensorEvent(sensor, values3);
		SensorEvent event4 = new SensorEvent(sensor, values4);

		// Create two transition Documents
		PredictionService ps = new PredictionService();
		ps.createPredictionFromSensorEvents(event1, event2);
		ps.createPredictionFromSensorEvents(event1, event3);

		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
				N1qlQuery.simple(
						"SELECT COUNT(*) FROM `transition` WHERE firstSensorEventKey = \""
								+ event1.getKey() + "\""));

		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.info().resultCount() == 2);
		softly.assertAll();

	}

	@Test
	public final void givenOneSensorEventLogged_WhenQueried_thenItIsReturned() {

		// Given - A Connection to the repository has been established
		SoftAssertions softly = new SoftAssertions();

		// Given
		// Create two transition JSON Documents
		float[] values = { 4.1f, 4.2f, 4.3f };

		HeartRateSensor sensor = new HeartRateSensor(Sensor.LOCATION_WATCH,
				Sensor.REPORTING_MODE_CONTINUOUS, false);
		SensorEvent event1 = new SensorEvent(sensor, values);

		JsonObject sensorEventJsonDoc = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_PHONE).put("timestamp", "" + 1)
				.put("values", JsonArray.from(2.1, 2.2));

		// Log using our repository under test
		String uuid = UUID.randomUUID().toString();
		CouchbaseRepository.getInstance().log("sensor-event", uuid, sensorEventJsonDoc,
				false);

//		try {
//			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
//					.getValue("couchbase.server.logging.catchup.timer")));
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		JsonDocument doc = CouchbaseRepository.getInstance().get("sensor-event", uuid);

		assertThat(doc).isNotNull();

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
