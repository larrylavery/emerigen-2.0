package com.emerigen.infrastructure.repository.couchbase;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.emerigen.infrastructure.utils.EmerigenProperties;

//import io.reactivex.Observable;

//@RunWith(MockitoJUnitRunner.class)
public class CouchbaseRepositoryQueryTest {

	@Test
	public final void givenTwoTransitionsAndFourRelatedSensorEventsLogged_WhenRelationshipsQueried_ThenTheyShouldBeValid() {

		// Given
		// Load 4 different sensorEvents
		SoftAssertions softly = new SoftAssertions();
		long timestamp = System.currentTimeMillis() * 1000000;
		float[] values = { 14.1f, 4.2f, 4.3f };
		float[] values2 = { 24.11f, 4.21f, 4.31f };
		float[] values3 = { 34.111f, 4.211f, 4.311f };
		float[] values4 = { 44.1111f, 4.2111f, 4.3111f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.TYPE_HEART_RATE,
				Sensor.DELAY_NORMAL, false);
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		SensorEvent event3 = new SensorEvent(sensor, values3);
		SensorEvent event4 = new SensorEvent(sensor, values4);

		JsonObject sensorEventJsonDoc = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", sensor.getLocation())
				.put("timestamp", "" + event1.getTimestamp())
				.put("values", JsonArray.from(14.1, 4.2, 4.3));

		JsonObject sensorEventJsonDoc2 = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", sensor.getLocation())
				.put("timestamp", "" + event2.getTimestamp())
				.put("values", JsonArray.from(24.11, 4.21, 4.31));

		JsonObject sensorEventJsonDoc3 = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", sensor.getLocation())
				.put("timestamp", "" + event3.getTimestamp())
				.put("values", JsonArray.from(34.111, 4.211, 4.311));

		JsonObject sensorEventJsonDoc4 = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("timestamp", "" + event4.getTimestamp())
				.put("values", JsonArray.from(44.1111, 4.2111, 4.3111));

		// Log using our repository under test
		CouchbaseRepository.getInstance().log("sensor-event", event1.getKey(),
				sensorEventJsonDoc);
		CouchbaseRepository.getInstance().log("sensor-event", event2.getKey(),
				sensorEventJsonDoc2);
		CouchbaseRepository.getInstance().log("sensor-event", event3.getKey(),
				sensorEventJsonDoc3);
		CouchbaseRepository.getInstance().log("sensor-event", event4.getKey(),
				sensorEventJsonDoc4);

		// And Given
		// Load 2 different transitions related to the sensorEvents logged
		// Create two transition Documents
		JsonObject transitionJsonDoc = JsonObject.create().put("timestamp", "4")
				.put("firstSensorEventKey", event1.getKey())
				.put("predictedSensorEventKey", event2.getKey());

		JsonObject transitionJsonDoc2 = JsonObject.create().put("timestamp", "4")
				.put("firstSensorEventKey", event1.getKey())
				.put("predictedSensorEventKey", event4.getKey());

		// Log using our repository under test
		CouchbaseRepository.getInstance().log("transition",
				event1.getKey() + event2.getKey(), transitionJsonDoc);

		CouchbaseRepository.getInstance().log("transition",
				event1.getKey() + event4.getKey(), transitionJsonDoc2);
		try {
			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
					.getValue("couchbase.server.logging.catchup.timer")));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// When
		// Transitions queried
		String statement = "SELECT predictedSensorEventKey FROM `transition` WHERE firstSensorEventKey = \""
				+ event1.getKey() + "\"";
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
				N1qlQuery.simple(statement));
		// result.forEach(System.out::println);

		// Then there should be one of them with valid predictedSensorEvent key
		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.info().resultCount() == 2);
		// result.forEach(System.out::println);

		String predictedKey = result.allRows().get(0).value()
				.getString("predictedSensorEventKey");
		assertThat(event2.getKey().contentEquals(predictedKey)
				|| event4.getKey().equals(predictedKey)).isTrue();
//		softly.assertThat(
//				result.allRows().get(0).value().getString("predictedSensorEventKey"))
//				.isEqualTo(event2.getKey() );

		// And When their related sensorEvents are retrieved
		JsonDocument jsonSensorEvent = CouchbaseRepository.getInstance()
				.get("sensor-event", event1.getKey());
		assertThat(jsonSensorEvent.content()).isNotNull();
		softly.assertAll();

	}

	@Test
	public final void givenTwoTransitionsLogged_WhenQueriedByFirstSensorEventKey_ThenCountShouldBe2() {

		// Given - A Connection to the repository has been established
		SoftAssertions softly = new SoftAssertions();

		// Given
		// Create two JSON Documents
		float[] values = { 4.1f, 4.2f, 4.3f };
		float[] values2 = { 4.11f, 4.21f, 4.31f };
		float[] values3 = { 4.111f, 4.211f, 4.311f };
		float[] values4 = { 4.1111f, 4.2111f, 4.3111f };
		HeartRateSensor sensor = new HeartRateSensor(Sensor.TYPE_HEART_RATE,
				Sensor.DELAY_NORMAL, false);
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		SensorEvent event3 = new SensorEvent(sensor, values3);
		SensorEvent event4 = new SensorEvent(sensor, values4);

		// Create two transition Documents
		JsonObject transitionJsonDoc = JsonObject.create().put("timestamp", "4")
				.put("firstSensorEventKey", event1.getKey())
				.put("predictedSensorEventKey", event2.getKey());

		JsonObject transitionJsonDoc2 = JsonObject.create().put("timestamp", "4")
				.put("firstSensorEventKey", event1.getKey())
				.put("predictedSensorEventKey", event4.getKey());

		// Log using our repository under test
		CouchbaseRepository.getInstance().log("transition",
				event1.getKey() + event2.getKey(), transitionJsonDoc);

		CouchbaseRepository.getInstance().log("transition",
				event1.getKey() + event4.getKey(), transitionJsonDoc2);
		try {
			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
					.getValue("couchbase.server.logging.catchup.timer")));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String prefix = "SELECT predictedSensorEventKey FROM `transition` ";
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
		HeartRateSensor sensor = new HeartRateSensor(Sensor.TYPE_HEART_RATE,
				Sensor.DELAY_NORMAL, false);
		SensorEvent event1 = new SensorEvent(sensor, values);
		SensorEvent event2 = new SensorEvent(sensor, values2);
		SensorEvent event3 = new SensorEvent(sensor, values3);
		SensorEvent event4 = new SensorEvent(sensor, values4);

		// Create two transition Documents
		JsonObject transitionJsonDoc = JsonObject.create().put("timestamp", "4")
				.put("firstSensorEventKey", event1.getKey())
				.put("predictedSensorEventKey", event2.getKey());

		JsonObject transitionJsonDoc2 = JsonObject.create().put("timestamp", "4")
				.put("firstSensorEventKey", event1.getKey())
				.put("predictedSensorEventKey", event4.getKey());

		// Log using our repository under test
		CouchbaseRepository.getInstance().log("transition",
				event1.getKey() + event2.getKey(), transitionJsonDoc);

		CouchbaseRepository.getInstance().log("transition",
				event1.getKey() + event4.getKey(), transitionJsonDoc2);
		try {
			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
					.getValue("couchbase.server.logging.catchup.timer")));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

		HeartRateSensor sensor = new HeartRateSensor(Sensor.TYPE_HEART_RATE,
				Sensor.DELAY_NORMAL, false);
		SensorEvent event1 = new SensorEvent(sensor, values);

		JsonObject sensorEventJsonDoc = JsonObject.create()
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_PHONE).put("timestamp", "" + 1)
				.put("values", JsonArray.from(2.1, 2.2));

		// Log using our repository under test
		CouchbaseRepository.getInstance().log("sensor-event", event1.getKey(),
				sensorEventJsonDoc);

		try {
			Thread.sleep(Long.parseLong(EmerigenProperties.getInstance()
					.getValue("couchbase.server.logging.catchup.timer")));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonDocument doc = CouchbaseRepository.getInstance().get("sensor-event",
				event1.getKey());

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
