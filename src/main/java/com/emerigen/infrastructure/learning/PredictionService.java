package com.emerigen.infrastructure.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.couchbase.client.deps.com.fasterxml.jackson.core.Version;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.module.SimpleModule;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.emerigen.infrastructure.repository.RepositoryException;
import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepository;
import com.emerigen.infrastructure.sensor.CustomSensorEventDeserializer;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.knowledge.Transition;

/**
 * This class provides all prediction-related services for a PatternRecognizer
 * associated with a specific sensor
 */
public class PredictionService {

	private long predictionCount = 0;
	private List<Prediction> currentPredictions;

	private double defaultProbability = Double.parseDouble(
			EmerigenProperties.getInstance().getValue("prediction.default.probability"));

	private Sensor sensor;
	private static final Logger logger = Logger.getLogger(PredictionService.class);

	public PredictionService(Sensor sensor) {
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		this.sensor = this.sensor;
	}

	public String createPredictionFromSensorEvents(SensorEvent firstSensorEvent,
			SensorEvent predictedSensorEvent) {

		// Validate parms
		if (firstSensorEvent == null || predictedSensorEvent == null)
			throw new IllegalArgumentException(
					"firstSensorEvent and predictedSensorEvent must not be null");

		if (!(firstSensorEvent.getSensorType() == predictedSensorEvent.getSensorType()))
			throw new IllegalArgumentException(
					"firstSensorEvent and predictedSensorEvent sensor locations  must be the same");

		if (!(firstSensorEvent.getSensorLocation() == predictedSensorEvent
				.getSensorLocation()))
			throw new IllegalArgumentException(
					"firstSensorEvent and predictedSensorEvent sensor locations must be the same");

		Sensor sensor = SensorManager.getInstance().getDefaultSensorForLocation(
				firstSensorEvent.getSensor().getType(),
				firstSensorEvent.getSensor().getLocation());

		// Create ti mestamp and id
		long timestamp = System.currentTimeMillis() * 1000000;
		String uuid = UUID.randomUUID().toString();

		// Create predicted sensorEvent json document
		JsonArray jsonArray2 = JsonArray.create();
		for (int i = 0; i < predictedSensorEvent.getValues().length; i++) {
			jsonArray2.add(predictedSensorEvent.getValues()[i]);
		}

		JsonObject predictedEventJsonDoc = JsonObject.create()
				.put("sensorType", sensor.getType())
				.put("sensorLocation", sensor.getLocation())
				.put("timestamp", predictedSensorEvent.getTimestamp())
				.put("values", jsonArray2)
				.put("minimumDelayBetweenReadings",
						sensor.getMinimumDelayBetweenReadings())
				.put("reportingMode", sensor.getReportingMode())
				.put("wakeUpSensor", sensor.isWakeUpSensor());

		JsonObject transitionJsonObject = JsonObject.create()
				.put("sensorType", sensor.getType())
				.put("sensorLocation", sensor.getLocation())
				.put("probability", defaultProbability).put("timestamp", timestamp)
				.put("dataPointDurationNano", Transition.defaultDataPointDurationNano)
				.put("firstSensorEventKey", firstSensorEvent.getKey())
				.put("predictedSensorEvent", predictedEventJsonDoc);

		// Log the transition object
		CouchbaseRepository.getInstance().log("transition", uuid, transitionJsonObject);
		return uuid;
	}

	public PredictionService() {
	}

	public int getPredictionCountForSensorTypeAndLocation(int sensorType,
			int sensorLocation) {

		String queryString = "SELECT COUNT(*) FROM `transition` WHERE sensorType = "
				+ sensorType + " AND sensorLocation = " + sensorLocation;
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("sensor-event",
				N1qlQuery.simple(queryString));

		logger.info(" query result: " + result);

		int count = result.allRows().get(0).value().getInt("$1");
		return count;
	}

	/**
	 * Retrieve all predictions for the given SensorEvent that occured prior to the
	 * ending Timestamp
	 * 
	 * @param sensorEvent
	 * @return
	 */
	public List<Prediction> getPredictionsForSensorEventBeforeTimestamp(
			SensorEvent sensorEvent, long timestamp) {
		if (sensorEvent == null)
			throw new IllegalArgumentException("sensorEvent must not be null or empty");
		if (timestamp < 1)
			throw new IllegalArgumentException("timestamp must be greater than 1");

		List<Prediction> predictions = new ArrayList<Prediction>();
		ObjectMapper mapper = new ObjectMapper();

		registerCustomDeserializer(mapper);
		N1qlQueryResult result = retrievePredictedEventsFromTransitionRecordsBeforeTimestamp(
				sensorEvent, timestamp);
//		N1qlQueryResult result = retrievePredictedEventsFromTransitionRecords(sensorEvent);
		List<SensorEvent> predictedSensorEvents = convertFromJsonToSensorEvents(mapper,
				result);
		predictions = convertToPredictions(predictedSensorEvents);
		setProbabilitiesForEachPrediction(predictions);
		return predictions;
	}

	/**
	 * Retrieve all predictions for the given SensorEvent
	 * 
	 * @param sensorEvent
	 * @return
	 */
	public List<Prediction> getPredictionsForSensorEvent(SensorEvent sensorEvent) {
		if (sensorEvent == null)
			throw new IllegalArgumentException("sensorEvent must not be null or empty");
		List<Prediction> predictions = new ArrayList<Prediction>();
		ObjectMapper mapper = new ObjectMapper();

		registerCustomDeserializer(mapper);
		N1qlQueryResult result = retrievePredictedEventsFromTransitionRecords(
				sensorEvent);
		List<SensorEvent> predictedSensorEvents = convertFromJsonToSensorEvents(mapper,
				result);
		predictions = convertToPredictions(predictedSensorEvents);
		setProbabilitiesForEachPrediction(predictions);
		return predictions;
	}

	/**
	 * By default set probabilities for TransitionPatternRecognizer
	 * 
	 * @param predictions
	 * @return
	 */
	private List<Prediction> setProbabilitiesForEachPrediction(
			List<Prediction> predictions) {
		if (predictions != null && predictions.size() > 0) {
			double probability = 1.0 / predictions.size();
			predictions.forEach(prediction -> prediction.setProbability(probability));
		}
		return predictions;
	}

	private List<Prediction> convertToPredictions(
			List<SensorEvent> predictedSensorEvents) {
		List<Prediction> newPredictions = predictedSensorEvents.stream()
				.map(event -> new TransitionPrediction(event))
				.collect(Collectors.toList());
		return newPredictions;
	}

	private N1qlQueryResult retrievePredictedEventsFromTransitionRecordsBeforeTimestamp(
			SensorEvent sensorEvent, long timestamp) {
		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		String statement = "SELECT predictedSensorEvent FROM `transition` WHERE "
				+ "firstSensorEventKey = \"" + sensorEvent.getKey() + "\""
				+ "AND timestamp < " + timestamp;
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
				N1qlQuery.simple(statement));
		return result;
	}

	private N1qlQueryResult retrievePredictedEventsFromTransitionRecords(
			SensorEvent sensorEvent) {
		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		String statement = "SELECT predictedSensorEvent FROM `transition` WHERE "
				+ "firstSensorEventKey = \"" + sensorEvent.getKey() + "\"";
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
				N1qlQuery.simple(statement));
		return result;
	}

	private void registerCustomDeserializer(ObjectMapper mapper) {
		SimpleModule module = new SimpleModule("CustomSensorEventDeserializer",
				new Version(1, 0, 0, null, null, null));
		module.addDeserializer(SensorEvent.class, new CustomSensorEventDeserializer());
		mapper.registerModule(module);
	}

	private List<SensorEvent> convertFromJsonToSensorEvents(ObjectMapper mapper,
			N1qlQueryResult result) {
		SensorEvent sensorEvent;
		List<SensorEvent> predictedSensorEvents = new ArrayList<SensorEvent>();
		try {
			for (N1qlQueryRow row : result) {
				logger.debug(
						"Adding sensorEvent to predicted sensorEvents: " + row.value());
				sensorEvent = mapper.readValue(row.value().toString(), SensorEvent.class);
				predictedSensorEvents.add(sensorEvent);
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		return predictedSensorEvents;
	}

	public void incrementPredictionsForSensor() {
		predictionCount++;
	}

	/**
	 * @return the predictionCount
	 */
	public long getPredictionCount() {
		return predictionCount;
	}

	/**
	 * @param predictionCount the predictionCount to set
	 */
	public void setPredictionCount(long predictionCount) {
		this.predictionCount = predictionCount;
	}

	/**
	 * @return the sensor
	 */
	public Sensor getSensor() {
		return sensor;
	}

	/**
	 * @param sensor the sensor to set
	 */
	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public List<Prediction> getCurrentPredictions() {
		if (currentPredictions == null)
			currentPredictions = new ArrayList<Prediction>();
		return currentPredictions;
	}

	public void setCurrentPredictions(List<Prediction> currentPredictions) {
		currentPredictions = currentPredictions;
	}

	public List<SensorEvent> getPriorEventsThatPredictSensorEvent(
			SensorEvent currentSensorEvent) {
		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		String statement = "SELECT predictedSensorEvent FROM `transition` WHERE "
				+ "firstSensorEventKey = \"" + currentSensorEvent.getKey() + "\"";
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("transition",
				N1qlQuery.simple(statement));
		return null;
	}

}
