package com.emerigen.infrastructure.sensor;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonGenerator;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.SerializerProvider;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.emerigen.infrastructure.learning.cycle.Cycle;
import com.emerigen.infrastructure.repository.RepositoryException;

public class CustomCycleSerializer extends StdSerializer<Cycle> {

	private static Logger logger = Logger.getLogger(CustomCycleSerializer.class);

	public CustomCycleSerializer() {
		this(null);
	}

	public CustomCycleSerializer(Class<Cycle> t) {
		super(t);
	}

	@Override
	public void serialize(Cycle cycle, JsonGenerator jsonGenerator,
			SerializerProvider serializer) {
		try {

			// create cycle-specific fields
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField("cycleType", cycle.getCycleType());
			jsonGenerator.writeNumberField("sensorType", cycle.getSensorType());
			jsonGenerator.writeNumberField("sensorLocation", cycle.getSensorLocation());
//			jsonGenerator.writeNumberField("cycleStartTimeNano", cycle.getCycleStartTimeNano());
//			jsonGenerator.writeNumberField("cycleDurationTimeNano", cycle.getCycleDurationTimeNano());
			jsonGenerator.writeNumberField("allowablePercentDifferenceForEquality",
					cycle.getAllowablePercentDifferenceForEquality());
			jsonGenerator.writeEndObject(); // End of cycle nodes and all other data
		} catch (IOException e) {
			throw new RepositoryException("IO exception thrown: ", e);
		}
	}

	/**
	 * Write the rest of the cycleNode attributes
	 * 
	 * @param jsonGenerator
	 * @param cycleNode
	 * @throws IOException
	 */

	/**
	 * Write the current sensor event to the output stream
	 * 
	 * @param jsonGenerator
	 * @param sensorEvent
	 * @throws IOException
	 */
	private void writeCycleNodeSensorEvent(JsonGenerator jsonGenerator,
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
