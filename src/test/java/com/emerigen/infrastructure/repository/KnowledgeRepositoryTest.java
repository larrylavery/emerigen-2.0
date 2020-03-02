/**
 * 
 */
package com.emerigen.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import org.junit.Test;

import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepository;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;

/**
 * @author Larry
 *
 */
public class KnowledgeRepositoryTest {
	KnowledgeRepository knowledgeRepository = KnowledgeRepository.getInstance();
	CouchbaseRepository couchbaseRepository = CouchbaseRepository.getInstance();

	@Test
	public void GivenFiveSensorEventsLogged_whenCountRequestedForSensorType_thenThreeShouldBeReturned() {

		// Given 5 valid SensorEvents logged, three by the same entity/channelType
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		Sensor accSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);
		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent1 = new SensorEvent(hrSensor, values);
		float[] values2 = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent2 = new SensorEvent(hrSensor, values2);
		float[] values3 = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent3 = new SensorEvent(accSensor, values3);
		float[] values4 = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent4 = new SensorEvent(accSensor, values4);
		float[] values5 = new float[] { rd.nextFloat(), 1.2f };
		SensorEvent sensorEvent5 = new SensorEvent(accSensor, values5);

		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent1.getKey(),
				sensorEvent1, true);
		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent2.getKey(),
				sensorEvent2, true);
		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent3.getKey(),
				sensorEvent3, true);
		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent4.getKey(),
				sensorEvent4, true);
		KnowledgeRepository.getInstance().logSensorEvent(sensorEvent5.getKey(),
				sensorEvent5, true);

		int count = KnowledgeRepository.getInstance()
				.getSensorEventCountForSensorTypeAndLocation(Sensor.TYPE_HEART_RATE,
						Sensor.LOCATION_PHONE);
		assertThat(count).isGreaterThan(2);
	}

	// Test
	/**
	 * public void
	 * GivenNumberOfPatternsAndPredictions_whenLogged_thenTheyShouldExecuteInLessThanXmillis()
	 * { SoftAssertions softly = new SoftAssertions();
	 * 
	 * //Clean up both buckets first
	 * //CouchbaseRepository.getInstance().removeAllDocuments("pattern");
	 * //CouchbaseRepository.getInstance().removeAllDocuments("prediction");
	 * 
	 * //Do Patterns first Instant start = Instant.now();
	 * GivenXPatternsLogged_whenExecutedManyTimes_thenTheyShouldExecuteInLessThanXmillis(50);
	 * Instant finish = Instant.now();
	 * 
	 * long timeElapsedForPatterns = Duration.between(start, finish).toMillis();
	 * System.out.println("Patterns elapsedMillis: " + timeElapsedForPatterns);
	 * 
	 * long thresholdInMillis = 2500; softly.assertThat(timeElapsedForPatterns <
	 * thresholdInMillis).isTrue();
	 * 
	 * //Do Predictions next start = Instant.now();
	 * GivenXPredictionsLogged_whenExecutedManyTimes_thenTheyShouldExecuteInLessThanXmillis(20);
	 * finish = Instant.now();
	 * 
	 * long timeElapsedForPredictions = Duration.between(start, finish).toMillis();
	 * System.out.println("Prediction elapsedMillis: " + timeElapsedForPredictions);
	 * 
	 * thresholdInMillis = 1000; softly.assertThat(timeElapsedForPredictions <
	 * thresholdInMillis).isTrue();
	 * 
	 * }
	 * 
	 * //@Test public void
	 * GivenXPatternsLogged_whenExecutedManyTimes_thenTheyShouldExecuteInLessThanXmillis(int
	 * numberExecuted) {
	 * 
	 * String entityUuid = UUID.randomUUID().toString(); SensorEvent sensorEvent;
	 * 
	 * for (int i = 0; i < numberExecuted; i++) { List<Double> sensorPattern = new
	 * ArrayList<Double>(); sensorPattern.add(i + 1.1); sensorPattern.add(i+2.1);
	 * sensorEvent = new SensorEvent(entityUuid + String.valueOf(i), "channelType1",
	 * sensorPattern);
	 * KnowledgeRepository.getInstance().newSensorEvent(sensorEvent); }
	 * 
	 * }
	 * 
	 * //@Test public void
	 * GivenXPredictionsLogged_whenExecutedManyTimes_thenTheyShouldExecuteInLessThanXmillis(int
	 * numberExecuted) {
	 * 
	 * String entityUuid = UUID.randomUUID().toString(); Prediction prediction;
	 * 
	 * for (int i = 0; i < numberExecuted; i++) { prediction = new
	 * Prediction(entityUuid + String.valueOf(i), "channelType" +
	 * String.valueOf(i));
	 * KnowledgeRepository.getInstance().predictionMade(prediction); }
	 * 
	 * }
	 * 
	 */

}
