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
//Couchbase
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.sensor.HeartRateSensor;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.Utils;

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
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		SensorEvent event1 = new SensorEvent(hrSensor, values);
		float[] values2 = new float[] { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event2 = new SensorEvent(hrSensor, values2);
		float[] values3 = new float[] { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event3 = new SensorEvent(hrSensor, values3);
		float[] values4 = new float[] { rd.nextFloat() + 1000, rd.nextFloat() + 1000 };
		SensorEvent event4 = new SensorEvent(hrSensor, values4);

		// create 2 transitions
		PredictionService ps = new PredictionService();
		ps.createPredictionFromSensorEvents(event1, event2);
		ps.createPredictionFromSensorEvents(event1, event3);

		Utils.allowDataUpdatesTimeToCatchUp();
		String prefix = "SELECT predictedSensorEvent FROM `knowledge` ";
		String conditional = "WHERE firstSensorEventKey = \"" + event1.getKey() + "\""
				+ " AND type = \"transition\"";
		QueryResult result = CouchbaseRepository.getInstance()
				.query(prefix + conditional);

		List<JsonObject> jsonObjects = result.rowsAsObject();
		assertThat(jsonObjects).isNotNull();
		assertThat(jsonObjects.size()).isEqualTo(2);

		softly.assertAll();

	}

	@Test
	public final void givenTwoTransitionsLogged_WhenPredictionCountForFirstSensorEventIsQueried_thenTwoIsReturned() {

		// Given - A Connection to the repository has been established
		SoftAssertions softly = new SoftAssertions();

		// Given
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		SensorEvent event1 = new SensorEvent(hrSensor, values);
		float[] values2 = new float[] { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event2 = new SensorEvent(hrSensor, values2);
		float[] values3 = new float[] { rd.nextFloat() + 100, rd.nextFloat() + 100 };
		SensorEvent event3 = new SensorEvent(hrSensor, values3);
		float[] values4 = new float[] { rd.nextFloat() + 1000, rd.nextFloat() + 1000 };
		SensorEvent event4 = new SensorEvent(hrSensor, values4);

		// Create two transition Documents
		PredictionService ps = new PredictionService();
		ps.createPredictionFromSensorEvents(event1, event2);
		ps.createPredictionFromSensorEvents(event1, event3);

		Utils.allowDataUpdatesTimeToCatchUp();
		String statement = "SELECT COUNT(*) FROM `knowledge` WHERE type= \"transition\" AND firstSensorEventKey = \""
				+ event1.getKey() + "\"";

		QueryResult result = CouchbaseRepository.getInstance().query(statement);

		List<JsonObject> jsonObjects = result.rowsAsObject();
		int count = jsonObjects.get(0).getInt("$1");
		softly.assertThat(count).isEqualTo(2);
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

		JsonObject sensorEventJsonDoc = JsonObject.create().put("type", "sensor-event")
				.put("sensorType", Sensor.TYPE_HEART_RATE)
				.put("sensorLocation", Sensor.LOCATION_PHONE).put("timestamp", "" + 1)
				.put("values", JsonArray.from(2.1, 2.2));

		// Log using our repository under test
		String uuid = UUID.randomUUID().toString();
		CouchbaseRepository.getInstance().log(uuid, sensorEventJsonDoc, true);

		JsonObject doc = CouchbaseRepository.getInstance().get(uuid, "");

		assertThat(doc).isNotNull();
		assertThat(doc.getInt("sensorType")).isEqualTo(Sensor.TYPE_HEART_RATE);

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
