package com.emerigen.infrastructure.sensor;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.SerializerProvider;
import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.emerigen.infrastructure.repository.RepositoryException;

public class CustomSensorEventSerializer extends StdSerializer<SensorEvent> {

	private static Logger logger = Logger.getLogger(CustomSensorEventSerializer.class);

	public CustomSensorEventSerializer() {
		this(null);
	}

	public CustomSensorEventSerializer(Class<SensorEvent> t) {
		super(t);
	}

	@Override
	public void serialize(SensorEvent sensorEvent,
			com.couchbase.client.core.deps.com.fasterxml.jackson.core.JsonGenerator jsonGenerator,
			SerializerProvider serializer) {
		try {

			// Write the Sensor event fields
			jsonGenerator.writeStartObject();
			jsonGenerator.writeNumberField("sensorType", sensorEvent.getSensorType());
			jsonGenerator.writeNumberField("sensorLocation",
					sensorEvent.getSensorLocation());
			jsonGenerator.writeNumberField("timestamp", sensorEvent.getTimestamp());
			jsonGenerator.writeNumberField("dataPointDurationNano",
					sensorEvent.getDataPointDurationNano());
//			jsonGenerator.writeNumberField("timestamp", sensorEvent.getTimestamp());
			logger.info("Current json before values parsed: " + jsonGenerator.toString());

			jsonGenerator.writeArrayFieldStart("values");

			for (int i = 0; i < sensorEvent.getValues().length; i++) {
				jsonGenerator.writeNumber(sensorEvent.getValues()[i]);
				logger.info("next values, float value: " + sensorEvent.getValues()[i]);
			}
			jsonGenerator.writeEndArray();
			logger.info("Current json after Event related filds parsed: "
					+ jsonGenerator.toString());

			// Write sensor related fields
			jsonGenerator.writeNumberField("minimumDelayBetweenReadings",
					sensorEvent.getSensor().getMinimumDelayBetweenReadings());
			jsonGenerator.writeNumberField("reportingMode",
					sensorEvent.getSensor().getReportingMode());
			jsonGenerator.writeBooleanField("wakeUpSensor",
					sensorEvent.getSensor().isWakeUpSensor());
			jsonGenerator.writeEndObject(); // End of all SensorEvent fields
			logger.info("final json after all fields serialized: "
					+ jsonGenerator.toString());
		} catch (IOException e) {
			throw new RepositoryException("IO exception thrown: ", e);
		}
	}

//	@Override
//	public void serialize(SensorEvent value, JsonGenerator gen,
//			SerializerProvider provider) throws IOException {
//		// TODO Auto-generated method stub
//
//	}

}
