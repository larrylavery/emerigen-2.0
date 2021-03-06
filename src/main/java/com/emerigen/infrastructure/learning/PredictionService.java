package com.emerigen.infrastructure.learning;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import com.emerigen.infrastructure.learning.creditassignment.PredictionConsumer;
import com.emerigen.infrastructure.repository.RepositoryException;
import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepository;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.EmerigenProperties;

/**
 * This class provides all prediction-related services for a PatternRecognizer
 * associated with a specific sensor
 */
public class PredictionService {

	// TODO refactor hard coded strings into constants

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
				firstSensorEvent.getSensor().getSensorType(),
				firstSensorEvent.getSensor().getSensorLocation());

		// Create timestamp and id
		long timestamp = System.currentTimeMillis() * 1000000;
		String uuid = UUID.randomUUID().toString();

		// Create predicted sensorEvent json document
		JsonArray jsonArray2 = JsonArray.create();
		for (int i = 0; i < predictedSensorEvent.getValues().length; i++) {
			jsonArray2.add(predictedSensorEvent.getValues()[i]);
		}

		JsonObject sensorJsonDoc = JsonObject.create()
				.put("sensorType", sensor.getSensorType())
				.put("sensorLocation", sensor.getSensorLocation())
				.put("wakeUpSensor", sensor.isWakeUpSensor())
				.put("minimumDelayBetweenReadings",
						sensor.getMinimumDelayBetweenReadings())
				.put("reportingMode", sensor.getReportingMode()).put("type", "sensor");

		JsonObject predictedEventJsonDoc = JsonObject.create()
				.put("sensorType", sensor.getSensorType())
				.put("sensorLocation", sensor.getSensorLocation())
				.put("type", "sensor-event")
				.put("timestamp", predictedSensorEvent.getTimestamp())
				.put("values", jsonArray2).put("sensor", sensorJsonDoc);

		JsonObject transitionJsonObject = JsonObject.create()
				.put("cashOnHand", Transition.defaultCashOnHand)
				.put("probability", defaultProbability).put("timestamp", timestamp)
				.put("type", "transition")
				.put("dataPointDurationNano", Transition.defaultDataPointDurationNano)
				.put("lastSuccessfulPredictionTimestamp",
						System.currentTimeMillis() * 1000000)
				.put("numberOfPredictionAttempts", 0)
				.put("numberOfSuccessfulPredictions", 0)
				.put("firstSensorEventKey", firstSensorEvent.getKey())
				.put("predictedSensorEvent", predictedEventJsonDoc);

		// Log the transition object
		CouchbaseRepository.getInstance().log(uuid, transitionJsonObject, true);
		return uuid;
	}

	public PredictionService() {
	}

	public int getPredictionCountForSensorTypeAndLocation(int sensorType,
			int sensorLocation) {

		String queryString = "SELECT COUNT(*) FROM `knowledge` WHERE sensorType = "
				+ sensorType + " AND sensorLocation = " + sensorLocation
				+ " AND type = \"transition\"";
		QueryResult result = CouchbaseRepository.getInstance().query(queryString);
		logger.info(" query result: " + result);

		List<JsonObject> jsonObjects = result.rowsAsObject();
		int count = jsonObjects.get(0).getInt("$1");
		return count;
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

//		registerCustomDeserializer(mapper);
		QueryResult result = retrievePredictedEventsFromTransitionRecords(sensorEvent);
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
				.map(event -> new Prediction(event)).collect(Collectors.toList());
		return newPredictions;
	}

	private QueryResult retrievePredictedEventsFromTransitionRecordsBeforeTimestamp(
			SensorEvent sensorEvent, long timestamp) {
		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		String statement = "SELECT predictedSensorEvent FROM `knowledge` WHERE "
				+ "firstSensorEventKey = \"" + sensorEvent.getKey() + "\""
				+ "AND timestamp < " + timestamp + " AND type = \"transition\"";
		QueryResult result = CouchbaseRepository.getInstance().query(statement);
		return result;
	}

	private QueryResult retrievePredictedEventsFromTransitionRecords(
			SensorEvent sensorEvent) {
		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		String statement = "SELECT predictedSensorEvent FROM `knowledge` WHERE "
				+ "firstSensorEventKey = \"" + sensorEvent.getKey() + "\""
				+ " AND type = \"transition\"";
		QueryResult result = CouchbaseRepository.getInstance().query(statement);
		return result;
	}

//	private void registerCustomDeserializer(ObjectMapper mapper) {
//		SimpleModule module = new SimpleModule("CustomSensorEventDeserializer",
//				new Version(1, 0, 0, null, null, null));
//		module.addDeserializer(SensorEvent.class, new CustomSensorEventDeserializer());
//		mapper.registerModule(module);
//	}

//	private void registerCustomTransitionDeserializer(ObjectMapper mapper) {
//
//		// Register custom deserializer
//		SimpleModule module = new SimpleModule("CustomTransitionDeserializer",
//				new Version(1, 0, 0, null, null, null));
//		module.addDeserializer(Transition.class, new CustomTransitionDeserializer());
//		mapper.registerModule(module);
//	}

	private List<SensorEvent> convertFromJsonToSensorEvents(ObjectMapper mapper,
			QueryResult result) {
		SensorEvent sensorEvent;
		Sensor sensor;
		List<SensorEvent> predictedSensorEvents = new ArrayList<SensorEvent>();
		try {

			List<JsonObject> jsonObjects = result.rowsAsObject();
			JsonObject sensorEventJsonObject, sensorJsonObject;
			for (JsonObject jsonObject : jsonObjects) {

				sensorEventJsonObject = jsonObject.getObject("predictedSensorEvent");
				sensorJsonObject = sensorEventJsonObject.getObject("sensor");
				logger.info("sensorJson: " + sensorJsonObject.toString());
				logger.info("sensorEventJson: " + sensorEventJsonObject.toString());

				sensor = mapper.readValue(sensorJsonObject.toString(), Sensor.class);
				sensorEvent = mapper.readValue(sensorEventJsonObject.toString(),
						SensorEvent.class);

				sensorEvent.setSensor(sensor);
				logger.debug("Adding sensorEvent to predicted sensorEvents: "
						+ sensorEvent.toString());
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
		String statement = "SELECT predictedSensorEvent FROM `knowledge` WHERE "
				+ "firstSensorEventKey = \"" + currentSensorEvent.getKey() + "\""
				+ " AND type = \"transition\"";
		QueryResult result = CouchbaseRepository.getInstance().query(statement);
		return null;
	}

	/**
	 * Retrieve all Transitions (PredictionConsumers) where the firstSensorEvent is
	 * equal to the specified sensor event.
	 * 
	 * @param sensorEvent The sensorEvent to match transitions on
	 * @return The potential consumers for the supplied sensor event
	 */
	public List<PredictionConsumer> getPredictionConsumersForSensorEvent(
			SensorEvent sensorEvent) {
		if (sensorEvent == null)
			throw new IllegalArgumentException("sensorEvent must not be null or empty");

		List<PredictionConsumer> predictors = new ArrayList<PredictionConsumer>();
		ObjectMapper mapper = new ObjectMapper();

//		registerCustomTransitionDeserializer(mapper);
		QueryResult result = retrievePredictionConsumersForSensorEvent(sensorEvent);
		List<PredictionConsumer> predictionConsumers = convertFromJsonToPredictionConsumer(
				mapper, result);
		return predictionConsumers;
	}

	private QueryResult retrievePredictionConsumersForSensorEvent(
			SensorEvent sensorEvent) {
		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		String statement = "SELECT * FROM `knowledge` WHERE firstSensorEventKey=\""
				+ sensorEvent.getKey() + "\"" + " AND type = \"transition\"";
		QueryResult result = CouchbaseRepository.getInstance().query(statement);
		return result;
	}

	private List<PredictionConsumer> convertFromJsonToPredictionConsumer(
			ObjectMapper mapper, QueryResult result) {
		Transition transition;
		PredictionConsumer predictionConsumer;
		List<PredictionConsumer> predictionConsumers = new ArrayList<PredictionConsumer>();

		try {

			List<JsonObject> jsonObjects = result.rowsAsObject();
			JsonObject transitionJsonObject, sensorEventJsonObject, sensorJsonObject;
			for (JsonObject jsonObject : jsonObjects) {

				transitionJsonObject = jsonObject.getObject("knowledge");
				transition = mapper.readValue(transitionJsonObject.toString(),
						Transition.class);
				predictionConsumers.add(transition);
			}
		} catch (Exception e) {
			throw new RepositoryException(e);
		}
		return predictionConsumers;
	}

}
