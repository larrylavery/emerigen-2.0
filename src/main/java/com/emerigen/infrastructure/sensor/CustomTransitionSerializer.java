package com.emerigen.infrastructure.sensor;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonGenerator;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.SerializerProvider;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.repository.RepositoryException;

public class CustomTransitionSerializer extends StdSerializer<Transition> {

	private static Logger logger = Logger.getLogger(CustomTransitionSerializer.class);

	public CustomTransitionSerializer() {
		this(null);
	}

	public CustomTransitionSerializer(Class<Transition> t) {
		super(t);
	}

	@Override
	public void serialize(Transition transition, JsonGenerator jsonGenerator,
			SerializerProvider serializer) {
		try {

			// create transition-specific fields
			jsonGenerator.writeStartObject();
			jsonGenerator.writeNumberField("sensorType", transition.getSensorType());
			jsonGenerator.writeNumberField("sensorLocation",
					transition.getSensorLocation());
			jsonGenerator.writeNumberField("probability", transition.getProbability());
			jsonGenerator.writeNumberField("dataPointDurationNano",
					transition.getDataPointDurationNano());

			// Create firstSensorEvent fields
			jsonGenerator.writeStringField("firstSensorEventKey",
					transition.getFirstSensorEventKey());

			// Create predictedSensorEvent fields
			jsonGenerator.writeStartObject("predictedSensorEvent");
			jsonGenerator.writeStartObject();
			writeTransitionNodeSensorEvent(jsonGenerator,
					transition.getPredictedSensorEvent());
			jsonGenerator.writeEndObject();

			jsonGenerator.writeEndObject(); // End of cycle nodes and all other data
		} catch (IOException e) {
			throw new RepositoryException("IO exception thrown: ", e);
		}
	}

	/**
	 * Write the given sensor event to the output stream
	 * 
	 * @param jsonGenerator
	 * @param sensorEvent
	 * @throws IOException
	 */
	private void writeTransitionNodeSensorEvent(JsonGenerator jsonGenerator,
			SensorEvent sensorEvent) throws IOException {

		jsonGenerator.writeNumberField("sensorType", sensorEvent.getSensorType());
		jsonGenerator.writeNumberField("sensorLocation", sensorEvent.getSensorLocation());
		jsonGenerator.writeNumberField("timestamp", sensorEvent.getTimestamp());
		jsonGenerator.writeNumberField("minimumDelayBetweenReadings",
				sensorEvent.getSensor().getMinimumDelayBetweenReadings());
		jsonGenerator.writeNumberField("reportingMode",
				sensorEvent.getSensor().getReportingMode());
		jsonGenerator.writeBooleanField("wakeUpSensor",
				sensorEvent.getSensor().isWakeUpSensor());
		logger.info("Current json before values parsed: " + jsonGenerator.toString());

		// Write sensor event values
		jsonGenerator.writeArrayFieldStart("values");

		for (int i = 0; i < sensorEvent.getValues().length; i++) {
			jsonGenerator.writeNumber(sensorEvent.getValues()[i]);
			logger.info("next values, float value: " + sensorEvent.getValues()[i]);
		}
		jsonGenerator.writeEndArray();
		logger.info("Current json after Event related filds parsed: "
				+ jsonGenerator.toString());
	}

}
