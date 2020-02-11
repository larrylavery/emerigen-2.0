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
import com.emerigen.infrastructure.learning.Cycle;
import com.emerigen.infrastructure.learning.CycleNode;
import com.emerigen.infrastructure.learning.DailyCycle;
import com.emerigen.infrastructure.learning.MonthlyCycle;
import com.emerigen.infrastructure.learning.WeeklyCycle;
import com.emerigen.infrastructure.learning.YearlyCycle;
import com.emerigen.infrastructure.repository.RepositoryException;

public class CustomCycleDeserializer extends StdDeserializer<Cycle> {

	private static Logger logger = Logger.getLogger(CustomCycleDeserializer.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomCycleDeserializer() {
		this(null);
	}

	public CustomCycleDeserializer(Class<?> se) {
		super(se);
	}

	@Override
	public Cycle deserialize(JsonParser parser, DeserializationContext deserializer)
			throws JsonProcessingException {

		Cycle cycle;
		SensorEvent sensorEvent;

		try {

			ObjectCodec codec = parser.getCodec();
			JsonNode node = codec.readTree(parser);

			// Create the cycle with basic information
			String cycleType = node.get("cycleType").asText();
			cycle = createCycle(cycleType);
			int sensorType = node.get("sensorType").asInt();
			cycle.setSensorType(sensorType);
			int sensorLocation = node.get("sensorLocation").asInt();
			cycle.setSensorLocation(sensorLocation);
			long startTime = node.get("cycleStartTimeNano").asLong();
			cycle.setCycleStartTimeNano(startTime);
			long durationTime = node.get("cycleDurationTimeNano").asLong();
			cycle.setCycleDurationTimeNano(durationTime);
			cycle.setAllowableStandardDeviationForEquality(
					node.get("allowableStandardDeviationForEquality").asDouble());
			cycle.setPreviousCycleNodeIndex(0);

			// Next add all the Cycle Nodes
			JsonNode cycleNodes = node.get("nodeList");
			CycleNode cycleNode = new CycleNode();

			Iterator<JsonNode> cycleJsonNodes = cycleNodes.elements();
			while (cycleJsonNodes.hasNext()) {
				JsonNode cycleNodeJsonNode = cycleJsonNodes.next();

				// First, extract the sensor event
				sensorEvent = extractSensorEvent(cycleNodeJsonNode);
				cycleNode.setSensorEvent(sensorEvent);
				cycleNode.setStartTimeOffsetNano(
						cycleNodeJsonNode.get("cycleStartTimeOffsetNano").asLong());
				cycleNode.setDataPointDurationNano(
						cycleNodeJsonNode.get("dataPointDurationNano").asLong());
				cycleNode.setProbability(cycleNodeJsonNode.get("probability").asDouble());
				cycleNode.setMyCycle(cycle);
				cycle.addCycleNode(cycleNode);
			}
			return cycle;
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

	private Cycle createCycle(String cycleType) {

		if ("Daily".contentEquals(cycleType))
			return new DailyCycle();
		else if ("Weekly".equals(cycleType))
			return new WeeklyCycle();
		else if ("Monthly".equals(cycleType))
			return new MonthlyCycle();
		else if ("Yearly".equals(cycleType))
			return new YearlyCycle();
		else
			throw new IllegalArgumentException(
					"Cycle type of (" + cycleType + ") is not valid.");
	}
}
