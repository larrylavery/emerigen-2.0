package com.emerigen.infrastructure.sensor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonParser;
import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.deps.com.fasterxml.jackson.core.ObjectCodec;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.DeserializationContext;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.JsonNode;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.repository.RepositoryException;

public class CustomTransitionDeserializer extends StdDeserializer<Transition> {

	private static Logger logger = Logger.getLogger(CustomTransitionDeserializer.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomTransitionDeserializer() {
		this(null);
	}

	public CustomTransitionDeserializer(Class<?> se) {
		super(se);
	}

	@Override
	public Transition deserialize(JsonParser parser, DeserializationContext deserializer)
			throws JsonProcessingException {

		Transition transition = new Transition();
		SensorEvent sensorEvent;

		try {

			ObjectCodec codec = parser.getCodec();
			JsonNode node = codec.readTree(parser);

			// Create the transition with basic information
//			transition.setSensorType(node.get("sensorType").asInt());
//			transition.setSensorLocation(node.get("sensorLocation").asInt());

			double probability = node.get("probability").asDouble();
			transition.setProbability(probability);

			long lastSuccessfulPredictionTimestamp = node
					.get("lastSuccessfulPredictionTimestamp").asLong();
			transition.setLastSuccessfulPredictionTimestamp(
					lastSuccessfulPredictionTimestamp);

			long numberOfPredictionAttempts = node.get("numberOfPredictionAttempts")
					.asLong();
			transition.setNumberOfPredictionAttempts(numberOfPredictionAttempts);

			long numberOfSuccessfulPredictions = node.get("numberOfSuccessfulPredictions")
					.asLong();
			transition.setNumberOfSuccessfulPredictions(numberOfSuccessfulPredictions);

			double cashOnHand = node.get("cashOnHand").asDouble();
			transition.setCashOnHand(cashOnHand);

			long timestamp = node.get("timestamp").asLong();
			transition.setTimestamp(timestamp);

			long duration = node.get("dataPointDurationNano").asLong();
			transition.setDataPointDurationNano(duration);

			String firstSensorEventKey = node.get("firstSensorEventKey").asText();
			transition.setFirstSensorEventKey(firstSensorEventKey);

			JsonNode predictedSensorEventNode = node.get("predictedSensorEvent");
			SensorEvent predictedSensorEvent = extractSensorEvent(
					predictedSensorEventNode);
			transition.setPredictedSensorEvent(predictedSensorEvent);

			return transition;
		} catch (IOException e) {
			throw new RepositoryException("IO exception thrown: ", e);
		}
	}

	private SensorEvent extractSensorEvent(JsonNode node) {
		int sensorType;
		int sensorLocation;
		int minimumDelayBetweenReadings;
		int reportingMode;
		boolean wakeUpSensor;
		SensorEvent sensorEvent = new SensorEvent();
		Sensor sensor;

		// Retrieve the sensor event fields
		sensorType = node.get("sensorType").asInt();
		sensorEvent.setSensorType(sensorType);
		sensorLocation = node.get("sensorLocation").asInt();
		sensorEvent.setSensorLocation(sensorLocation);
		sensorEvent.setTimestamp(node.get("timestamp").asLong());
		logger.info("Partial SensorEvent without values set: " + sensorEvent);

		sensorEvent.setValues(getSensorEventValues(node));
		logger.info("SensorEvent with values, without sensor: " + sensorEvent);

		// Retrieve the Sensor attributes, create and set event sensor
		minimumDelayBetweenReadings = node.get("minimumDelayBetweenReadings").asInt();
		reportingMode = node.get("reportingMode").asInt();
		wakeUpSensor = node.get("wakeUpSensor").asBoolean();
		sensor = SensorManager.getInstance().getDefaultSensorForLocation(sensorType,
				sensorLocation);
		sensor.setMinimumDelayBetweenReadings(minimumDelayBetweenReadings);
		sensor.setWakeUpSensor(wakeUpSensor);
		sensor.setReportingMode(reportingMode);
		sensorEvent.setSensor(sensor);
		logger.info(
				"Sensor created: " + sensor + ", sensorEvent complete: " + sensorEvent);
		return sensorEvent;
	}

	private float[] getSensorEventValues(JsonNode node) {
		// Retrieve the sensor event values
		JsonNode valuesNode = node.get("values");
		List<Float> valuesList = new ArrayList<Float>();
		Iterator<JsonNode> valuesJsonNode = valuesNode.elements();

		while (valuesJsonNode.hasNext()) {
			JsonNode valueJsonNode = valuesJsonNode.next();
			valuesList.add(valueJsonNode.floatValue());
		}
		float[] values = new float[valuesList.size()];
		for (int i = 0; i < valuesList.size(); i++) {
			values[i] = valuesList.get(i);
		}
		return values;
	}
}
