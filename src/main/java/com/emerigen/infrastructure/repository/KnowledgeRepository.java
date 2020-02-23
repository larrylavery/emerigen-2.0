package com.emerigen.infrastructure.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonParseException;
import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.deps.com.fasterxml.jackson.core.Version;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.JsonMappingException;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.SerializationFeature;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.module.SimpleModule;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.learning.cycle.Cycle;
import com.emerigen.infrastructure.learning.cycle.CyclePatternRecognizer;
import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepository;
import com.emerigen.infrastructure.sensor.CustomCycleDeserializer;
import com.emerigen.infrastructure.sensor.CustomCycleSerializer;
import com.emerigen.infrastructure.sensor.CustomSensorEventDeserializer;
import com.emerigen.infrastructure.sensor.CustomSensorEventSerializer;
import com.emerigen.infrastructure.sensor.CustomTransitionDeserializer;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorEventListener;
import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.knowledge.Entity;

/**
 * @author Larry
 * 
 *         <h1>Knowledge Repository</h1> Represents the service that uses a
 *         Repository to log client documents related to the concept of
 *         "Knowledge". Knowledge is a (potentially infinite) series of
 *         immutable facts that capture the "learned" associations between
 *         "Sensory Events" on different "Entities" with one or more "senses"
 *         (channels).
 *         <p>
 *         When a new sensory event is encountered/classified, it is logged as a
 *         new SensorEvent for a specific entity on a specific channel (i.e.
 *         sense)
 *         </p>
 *         <p>
 *         When a prediction is made, using a transition, it is logged as a new
 *         prediction using an existing transition.
 *         </p>
 *         <p>
 *         While "learning" or "predicting", the system will attempt to locate
 *         all transitions that contain the current sensorEvent as the first
 *         part of the transition. The list of those instances represent the
 *         possible predictions for the next sensory event (sensorEvent).
 * 
 *         Uniqueness Requirements: - SensorEvent (entityID + ChannelType +
 *         sensorAttributesHash) are unique repository wide - Prediction IDs are
 *         unique repository wide (UUIDs) - Transition IDs are unique repository
 *         wide (UUIDs)
 * 
 *         Key/Index Requirements - Entity: - entityID + channelType (primary
 *         key) - Channel Type: (Embeded in Entity, so NOT needed) - channelType
 *         (primary key) - Similar to our senses (visual, touch, taste, etc) -
 *         SensorEvent: - id: the composite key below
 *         (entityID+ChannelType+sensorAttributesHash). Must be unique for all
 *         sensorEvents. Since explicitly passed in, it will be assigned to the
 *         builtin field "id" ("_id" in Java Beans) - entityID + channelType +
 *         sensorEventHash(ensures no dups when inserting) AND ensures no dups
 *         when comparing sensorEvents later (without having to query
 *         sensorAttributes since their current implementation may change.)
 * 
 *         - logNewSensoryEvent(entityID+channelType+sensorAttributesHash,
 *         sensorEventJsonObject) - CREATE INDEX `ix_sensorEvent_unique` ON
 *         `prediction`(`entityID`,`channelType`, `sensorAttributesHash`)
 *
 *         - getSensorEventCountForEntityOnChannel(entityID, channelType) -
 *         CREATE INDEX `ix_sensorEvent` ON
 *         `prediction`(`entityID`,`channelType`) - SELECT count(*) FROM
 *         sensorEvent WHERE entityID=eee AND channelType=ccc - Prediction: -
 *         entityID + channelType + transition.firstSensorEvent
 * 
 *         - getPredictionAccuracyForEntityOnChannel():
 *         predictionCount/sensoryEventCount - SELECT count(*) FROM prediction
 *         WHERE entityID=eee AND channelType=ct
 * 
 *         - getPredictionCountForEntityOnChannel(entityID, channelType) -
 *         CREATE INDEX `ix_prediction_unique` ON
 *         `prediction`(`entityID`,`channelType`) - Transition: - primary key:
 *         (entityID, channelType + sensorEventOneKey) - sensorEventOneKey +
 *         entityID + channelType - relates Transition to its sensorEvents and
 *         an entity on a channel. -
 *         getPredictionsForSensorEvent(transition.sensorEventOneKey, entityID,
 *         channelType) - SELECT transition.sensorEventTwoContent FROM
 *         transition WHERE entityID= "ttt" AND channelType="yyy" AND
 *         transition.sensorEventOneKey="p1EntityID + p1ChannelType +
 *         p1sensorAttributesHash") Couchbase Console Index commands:
 *
 *         CREATE PRIMARY INDEX `#primary` ON `sensorEvent` CREATE INDEX
 *         `ix_sensorEvent_id` ON `sensorEvent`(`sensorEventID`) CREATE INDEX
 *         `ix_sensorEvent_primary` ON
 *         `sensorEvent`(`entityID`,`channelType`,`sensorAttributesHash`)
 * 
 *         CREATE INDEX `ix_prediction_unique` ON
 *         `prediction`(`entityID`,`channelType`)
 * 
 *         CREATE INDEX `ix_transition` ON
 *         `transition`(`firstSensorEventKey`,`predictedSensorEventKey`)
 * 
 *         CREATE INDEX `en_index` ON `entity`(`entityID`)
 * 
 * 
 *
 *         Index usage: - getPredictionAccuracy(): - int
 *         getCountOfSensorEventsProcessedForEntityOnChannel(entityID,
 *         channelType) - sensorEventDB Index - (none for entityID +
 *         channelType; ???) - int
 *         getCountOfPredictionsMadeForEntityOnChannel(entityID, channelType) -
 *         predictionDB - Index - ix_prediction_unique List<SensorEvent>
 *         getPredictionsForSensorEvent(sensorEventID, entityID, channelType) -
 *         transitionDB - Index - ( none for firstSensorEventKey because not
 *         unique ???)
 * 
 * 
 */

public class KnowledgeRepository extends AbstractKnowledgeRepository {

	public static final String CYCLE = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.cycle");
	public static final String SENSOR_EVENT = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.sensor.event");
	public static final String TRANSITION = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.transition");
	public static final String ENTITY = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.entity");

	private static Logger logger = Logger.getLogger(KnowledgeRepository.class);

	// Construct singleton repository
	CouchbaseRepository repository = CouchbaseRepository.getInstance();
	private static KnowledgeRepository instance;

	public static KnowledgeRepository getInstance() {
		if (instance == null) {
			synchronized (KnowledgeRepository.class) {
				if (instance == null) {
					instance = new KnowledgeRepository();
				}
			}
		}
		// Return singleton CouchbaseRepository
		return instance;
	}

	@Override
	public String newSensorEvent(SensorEvent sensorEvent) throws ValidationException {

		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("CustomSensorEventSerializer",
				new Version(1, 0, 0, null, null, null));
		module.addSerializer(SensorEvent.class, new CustomSensorEventSerializer());
		mapper.registerModule(module);
		String uuid = UUID.randomUUID().toString();

		try {

			// Convert the Java object to a SensorEvent JsonDocument
			JsonObject jsonObject = JsonObject
					.fromJson(mapper.writeValueAsString(sensorEvent));
			logger.info(" jsonObject: " + jsonObject);

			// Validate the JsonDocument against the SensorEvent Schema
			InputStream sensorEventSchemaJsonFileReader = getClass().getClassLoader()
					.getResourceAsStream("sensor-event.json");

			JSONObject jsonSchema = new JSONObject(
					new JSONTokener(sensorEventSchemaJsonFileReader));
			JSONObject jsonSubject = new JSONObject(jsonObject.toString());

			Schema schema = SchemaLoader.load(jsonSchema);

			// Validate the JsonDocument against its' schema, ValidationException
			schema.validate(jsonSubject);
			logger.info(" JsonObject validated successfully");

			try {
				repository.log(SENSOR_EVENT, uuid, jsonObject);
			} catch (DocumentAlreadyExistsException e) {

				// Ignoring these to contend with Listeners default behavior of always
				// logging events
				logger.warn("Ignoring DocumentAlreadyExistsException for sensor event - "
						+ sensorEvent);
			}

		} catch (JsonProcessingException e) {
			throw new RepositoryException(e);
		}
		return uuid;
	}

	@Override
	public int getSensorEventCountForSensorTypeAndLocation(int sensorType,
			int sensorLocation) {

		String queryString = "SELECT COUNT(*) FROM `sensor-event` WHERE sensorType = "
				+ sensorType + " AND sensorLocation = " + sensorLocation;
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("sensor-event",
				N1qlQuery.simple(queryString));

		logger.info(" query result: " + result);

		int count = result.allRows().get(0).value().getInt("$1");
		return count;
	}

	@Override
	public String newEntity(Entity entity) {
		ObjectMapper mapper = new ObjectMapper();
		String uuid = UUID.randomUUID().toString();

		try {

			// Convert the Java object to a JsonDocument
			JsonObject jsonObject = JsonObject
					.fromJson(mapper.writeValueAsString(entity));
			logger.info(" jsonObject: " + jsonObject);

			// Validate the JsonDocument against the SensorEvent Schema
			InputStream sensorEventSchemaJsonFileReader = getClass().getClassLoader()
					.getResourceAsStream("entity.json");

			JSONObject jsonSchema = new JSONObject(
					new JSONTokener(sensorEventSchemaJsonFileReader));
			JSONObject jsonSubject = new JSONObject(jsonObject.toString());

			Schema schema = SchemaLoader.load(jsonSchema);

			// Validate the JsonDocument against its' schema, ValidationException
			schema.validate(jsonSubject);
			logger.info(" JsonObject validated successfully");

			repository.log(ENTITY, uuid, jsonObject);

		} catch (JsonProcessingException e) {
			throw new RepositoryException(e);
		}
		return uuid;

	}

	@Override
	public Entity getEntity(String entityKey) {

		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		JsonDocument entityJsonDoc = repo.get(ENTITY, entityKey);
		logger.info(" after objectMapping, JsonDocument: " + entityJsonDoc);
		Entity entity;

		try {
			entity = mapper.readValue(entityJsonDoc.content().toString(), Entity.class);
			return entity;
		} catch (JsonParseException e) {
			throw new RepositoryException(e);
		} catch (JsonMappingException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public SensorEvent getSensorEvent(String sensorEventKey) {

		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// Register custom deserializer
		SimpleModule module = new SimpleModule("CustomSensorEventDeserializer",
				new Version(1, 0, 0, null, null, null));
		module.addDeserializer(SensorEvent.class, new CustomSensorEventDeserializer());
		mapper.registerModule(module);
		SensorEvent sensorEvent;

		JsonDocument jsonDocument = repo.get(SENSOR_EVENT, sensorEventKey);
		logger.info(" after objectMapping, JsonDocument: " + jsonDocument);

		if (jsonDocument == null)
			return null;

		try {
			sensorEvent = mapper.readValue(jsonDocument.content().toString(),
					SensorEvent.class);
			return sensorEvent;
		} catch (JsonParseException e) {
			throw new RepositoryException(e);
		} catch (JsonMappingException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}

	}

	@Override
	public Transition getTransition(String transitionKey) {

		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// Register custom deserializer
		SimpleModule module = new SimpleModule("CustomTransitionDeserializer",
				new Version(1, 0, 0, null, null, null));
		module.addDeserializer(Transition.class, new CustomTransitionDeserializer());
		mapper.registerModule(module);

		JsonDocument jsonDocument = repo.get(TRANSITION, transitionKey);
		logger.info(" after objectMapping, JsonDocument: " + jsonDocument);
		Transition transition;

		try {
			transition = mapper.readValue(jsonDocument.content().toString(),
					Transition.class);
			return transition;
		} catch (JsonParseException e) {
			throw new RepositoryException(e);
		} catch (JsonMappingException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * Retrieve all cycles for this sensor and create PatternRecognizers
	 * 
	 * @param sensor
	 * @return
	 */
	public List<SensorEventListener> getPatternRecognizersForSensor(Sensor sensor) {

		// Load all cycle types for the supplied sensorType and location
		List<Cycle> cycles = getCycles(sensor);
		List<SensorEventListener> PRs = cycles.stream()
				.map(cycle -> new CyclePatternRecognizer(cycle, sensor,
						new PredictionService(sensor)))
				.collect(Collectors.toList());
		return PRs;
	}

	/**
	 * Return a list of all cycles that apply for this sensor type
	 * 
	 * @param sensorType
	 * @return
	 */
	private List<Cycle> getCycles(Sensor sensor) {

		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		Cycle cycle;

		// For now call the DB once for each type to reuse code
		List<Cycle> cycles = new ArrayList<Cycle>();

		cycle = getCycle("Daily", "" + sensor.getType() + sensor.getLocation() + "Daily");
		if (cycle != null)
			cycles.add(cycle);

		cycle = getCycle("Weekly",
				"" + sensor.getType() + sensor.getLocation() + "Weekly");
		if (cycle != null)
			cycles.add(cycle);

		cycle = getCycle("Monthly",
				"" + sensor.getType() + sensor.getLocation() + "Monthly");
		if (cycle != null)
			cycles.add(cycle);

		cycle = getCycle("Yearly",
				"" + sensor.getType() + sensor.getLocation() + "Yearly");
		if (cycle != null)
			cycles.add(cycle);
		return cycles;
	}

	@Override
	public String newCycle(String cycleKey, Cycle cycle) {
		String uuid = UUID.randomUUID().toString();
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("CustomCycleSerializer",
				new Version(1, 0, 0, null, null, null));
		module.addSerializer(Cycle.class, new CustomCycleSerializer());
		mapper.registerModule(module);

		try {

			// Convert the Java object to a JsonDocument
			JsonObject jsonObject = JsonObject.fromJson(mapper.writeValueAsString(cycle));
			logger.info(" jsonObject: " + jsonObject);

			// Validate the JsonDocument against the Cycle Schema
			InputStream sensorEventSchemaJsonFileReader = getClass().getClassLoader()
					.getResourceAsStream("cycle.json");

			JSONObject jsonSchema = new JSONObject(
					new JSONTokener(sensorEventSchemaJsonFileReader));
			JSONObject jsonSubject = new JSONObject(jsonObject.toString());

			Schema schema = SchemaLoader.load(jsonSchema);

			// Validate the JsonDocument against its' schema, ValidationException
			schema.validate(jsonSubject);
			logger.info(" JsonObject validated successfully");

			repository.log(CYCLE, cycleKey, jsonObject);

		} catch (JsonProcessingException e) {
			throw new RepositoryException(e);
		}
		return cycleKey;

	}

	@Override
	public Cycle getCycle(String cycleType, String cycleKey) {

		if (invalidCycleType(cycleType))
			throw new IllegalArgumentException(
					"CycleTypeName must be valid, but was (" + cycleType + ")");

		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// Register custom deserializer
		SimpleModule module = new SimpleModule("CustomCycleDeserializer",
				new Version(1, 0, 0, null, null, null));
		module.addDeserializer(Cycle.class, new CustomCycleDeserializer());
		mapper.registerModule(module);

		JsonDocument jsonDocument = repo.get(CYCLE, cycleKey);

		logger.info(" after objectMapping, JsonDocument: " + jsonDocument);
		Cycle cycle;

		if (jsonDocument == null)
			return null;

		try {
			cycle = mapper.readValue(jsonDocument.content().toString(), Cycle.class);
			return cycle;
		} catch (JsonParseException e) {
			throw new RepositoryException(e);
		} catch (JsonMappingException e) {
			throw new RepositoryException(e);
		} catch (IOException e) {
			throw new RepositoryException(e);
		}

	}

	private boolean invalidCycleType(String cycleType) {

		if ("Daily".contentEquals(cycleType) || "Weekly".equals(cycleType)
				|| "Monthly".equals(cycleType) || "Yearly".equals(cycleType))
			return false;
		else
			return true;
	}

}
