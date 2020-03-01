package com.emerigen.infrastructure.sensor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.core.deps.com.fasterxml.jackson.core.JsonParser;
import com.couchbase.client.core.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.DeserializationContext;
import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.emerigen.infrastructure.repository.RepositoryException;

public class CustomSensorEventDeserializer extends StdDeserializer<SensorEvent> {

	private static Logger logger = Logger.getLogger(CustomSensorEventDeserializer.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomSensorEventDeserializer() {
		this(null);
	}

	public CustomSensorEventDeserializer(Class<?> se) {
		super(se);
	}

	@Override
	public SensorEvent deserialize(JsonParser parser, DeserializationContext deserializer)
			throws JsonProcessingException {

		// Temporary fields for Sensor object
		int sensorType;
		int sensorLocation;
		int minimumDelayBetweenReadings;
		int reportingMode;
		boolean wakeUpSensor;

		SensorEvent sensorEvent = new SensorEvent();
		Sensor sensor;

		try {

			com.couchbase.client.core.deps.com.fasterxml.jackson.core.ObjectCodec codec = parser
					.getCodec();
			com.couchbase.client.core.deps.com.fasterxml.jackson.databind.JsonNode node = codec
					.readTree(parser);

			// Retrieve the sensor event fields
			System.out.println("SensorEvent node: " + node.toString());

			// Next token a predicted event? consume it
			com.couchbase.client.core.deps.com.fasterxml.jackson.databind.JsonNode predictedSensorEventNode = node
					.get("predictedSensorEvent");
			if (predictedSensorEventNode != null) {
				System.out.println("SensorEvent node was the predicted sensor event"
						+ node.toString());
				node = predictedSensorEventNode;
			}

			sensorType = node.get("sensorType").asInt();
			sensorEvent.setSensorType(sensorType);
			sensorLocation = node.get("sensorLocation").asInt();
			sensorEvent.setSensorLocation(sensorLocation);
			sensorEvent.setTimestamp(node.get("timestamp").asLong());
			sensorEvent
					.setDataPointDurationNano(node.get("dataPointDurationNano").asLong());
			logger.info("Partial SensorEvent without values set: " + sensorEvent);

			// Retrieve the sensor event values
			com.couchbase.client.core.deps.com.fasterxml.jackson.databind.JsonNode valuesNode = node
					.get("values");
			List<Float> valuesList = new ArrayList<Float>();
			Iterator<com.couchbase.client.core.deps.com.fasterxml.jackson.databind.JsonNode> valuesJsonNode = valuesNode
					.elements();

			while (valuesJsonNode.hasNext()) {
				com.couchbase.client.core.deps.com.fasterxml.jackson.databind.JsonNode valueJsonNode = valuesJsonNode
						.next();
				valuesList.add(valueJsonNode.floatValue());
			}
			float[] values = new float[valuesList.size()];
			for (int i = 0; i < valuesList.size(); i++) {
				values[i] = valuesList.get(i);
			}
			sensorEvent.setValues(values);
			logger.info("SensorEvent values without sensor: " + sensorEvent);

			// Retrieve the Sensor attributes and create a new sensor
			minimumDelayBetweenReadings = node.get("minimumDelayBetweenReadings").asInt();
			reportingMode = node.get("reportingMode").asInt();
			wakeUpSensor = node.get("wakeUpSensor").asBoolean();
			sensor = SensorManager.getInstance().getDefaultSensorForLocation(sensorType,
					sensorLocation);
			sensor.setMinimumDelayBetweenReadings(minimumDelayBetweenReadings);
			sensor.setWakeUpSensor(wakeUpSensor);
			sensor.setReportingMode(reportingMode);

			sensorEvent.setSensor(sensor);
			logger.info("Sensor created: " + sensor + ", sensorEvent complete: "
					+ sensorEvent);
			return sensorEvent;
		} catch (IOException e) {
			throw new RepositoryException("IO exception thrown: ", e);
		}
	}
}
