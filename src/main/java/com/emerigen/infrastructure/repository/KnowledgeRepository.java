package com.emerigen.infrastructure.repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.couchbase.client.core.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.SerializationFeature;
import com.couchbase.client.java.json.JsonObject;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.learning.cycle.Cycle;
import com.emerigen.infrastructure.learning.cycle.CyclePatternRecognizer;
import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepository;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorEventListener;
import com.emerigen.infrastructure.utils.EmerigenProperties;

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

	public static final String KNOWLEDGE = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.knowledge");
	public static final String TRANSITION = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.transition");
	public static final String SENSOR_EVENT = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.sensor.event");
	public static final String CYCLE = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.cycle");

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
	public int getSensorEventCountForSensorTypeAndLocation(int sensorType,
			int sensorLocation) {

		String queryString = "SELECT COUNT(*) FROM `knowledge` WHERE sensorType = "
				+ sensorType + " AND sensorLocation = " + sensorLocation
				+ " AND type = \"sensor-event\"";
		com.couchbase.client.java.query.QueryResult result = CouchbaseRepository
				.getInstance().query(queryString);

		logger.info(" query result: " + result);
		List<JsonObject> jsonObjects = result.rowsAsObject();
		int count = jsonObjects.get(0).getInt("$1");
		return count;
	}

	@Override
	public SensorEvent getSensorEvent(String sensorEventKey) {

		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		SensorEvent sensorEvent;

		JsonObject jsonObject = repo.get(sensorEventKey);
		logger.info(" after objectMapping, JsonObject: " + jsonObject);

		if (jsonObject == null)
			return null;

		try {
			sensorEvent = mapper.readValue(jsonObject.toString(), SensorEvent.class);
			return sensorEvent;
		} catch (Exception e) {
			throw new RepositoryException(e);
		}

	}

	@Override
	public Transition getTransition(String transitionKey) {

		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		JsonObject jsonObject = repo.get(transitionKey, "transition");
		logger.info(" after objectMapping, JsonDocument: " + jsonObject);
		Transition transition;

		try {
			transition = mapper.readValue(jsonObject.toString(), Transition.class);
			return transition;
		} catch (Exception e) {
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
		List<Cycle> cycles = getCyclesForSensor(sensor);
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
	private List<Cycle> getCyclesForSensor(Sensor sensor) {

		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		Cycle cycle;

		// For now call the DB once for each type to reuse code
		List<Cycle> cycles = new ArrayList<Cycle>();

		cycle = getCycle("Daily",
				"" + sensor.getSensorType() + sensor.getSensorLocation() + "Daily");
		if (cycle != null)
			cycles.add(cycle);

		cycle = getCycle("Weekly",
				"" + sensor.getSensorType() + sensor.getSensorLocation() + "Weekly");
		if (cycle != null)
			cycles.add(cycle);

		cycle = getCycle("Monthly",
				"" + sensor.getSensorType() + sensor.getSensorLocation() + "Monthly");
		if (cycle != null)
			cycles.add(cycle);

		cycle = getCycle("Yearly",
				"" + sensor.getSensorType() + sensor.getSensorLocation() + "Yearly");
		if (cycle != null)
			cycles.add(cycle);
		return cycles;
	}

	@Override
	public Cycle getCycle(String cycleType, String cycleKey) {

		if (invalidCycleType(cycleType))
			throw new IllegalArgumentException(
					"CycleType must be valid, but was (" + cycleType + ")");

		// Initialize couchbase
		CouchbaseRepository repo = CouchbaseRepository.getInstance();
		ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		JsonObject jsonObject = repo.get(cycleKey);
		logger.info(" after objectMapping, JsonDocument: " + jsonObject);
		Cycle cycle;

		if (jsonObject == null)
			return null;

		try {
			cycle = mapper.readValue(jsonObject.toString(), Cycle.class);
			cycle.setCycleType(cycle.getCycleType());
			return cycle;
		} catch (Exception e) {
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

	@Override
	public String logTransition(String key, Transition transition, boolean synchronous) {

		try {
			JsonObject jsonObject = validateJson(transition, "transition.json");

			try {
				repository.log(key, jsonObject, synchronous);
			} catch (Exception e) {
				// TODO Ignoring this exception
				logger.warn("Ignoring Exception  - " + transition, e);
			}
		} catch (JsonProcessingException e) {
			throw new RepositoryException(e);
		}
		return key;
	}

	@Override
	public String replaceTransition(String key, Transition transition,
			boolean synchronous) {
		try {
			JsonObject jsonObject = validateJson(transition, "transition.json");

			try {
				repository.replace(key, jsonObject, synchronous);
			} catch (Exception e) {
				// TODO Ignoring this exception
				logger.warn("Ignoring Exception  - " + transition, e);
			}
		} catch (JsonProcessingException e) {
			throw new RepositoryException(e);
		}
		return key;
	}

	@Override
	public String logCycle(String key, Cycle cycle, boolean synchronous) {
		try {
			JsonObject jsonObject = validateJson(cycle, "cycle.json");

			try {
				repository.log(key, jsonObject, synchronous);
			} catch (Exception e) {
				// TODO Ignoring this exception
				logger.warn("Ignoring Exception  - " + cycle, e);
			}
		} catch (JsonProcessingException e) {
			throw new RepositoryException(e);
		}
		return key;
	}

	@Override
	public String replaceCycle(String key, Cycle cycle, boolean synchronous) {
		try {
			JsonObject jsonObject = validateJson(cycle, "cycle.json");

			try {
				repository.replace(key, jsonObject, synchronous);
			} catch (Exception e) {
				// TODO Ignoring this exception
				logger.warn("Ignoring Exception  - " + cycle, e);
			}
		} catch (JsonProcessingException e) {
			throw new RepositoryException(e);
		}
		return key;
	}

	@Override
	public String logSensorEvent(String key, SensorEvent sensorEvent,
			boolean synchronous) {

		try {
			JsonObject jsonObject = validateJson(sensorEvent, "sensor-event.json");

			try {
				repository.replace(key, jsonObject, synchronous);
			} catch (Exception e) {
				// TODO Ignoring this exception
				logger.warn("Ignoring Exception - " + sensorEvent, e);
			}
		} catch (JsonProcessingException e) {
			throw new RepositoryException(e);
		}
		return key;
	}

	@Override
	public String replaceSensorEvent(String key, SensorEvent sensorEvent,
			boolean synchronous) {

		try {
			JsonObject jsonObject = validateJson(sensorEvent, "sensor-event.json");

			try {
				repository.replace(key, jsonObject, synchronous);
			} catch (Exception e) {
				// TODO Ignoring this exception
				logger.warn("Ignoring Exception - " + sensorEvent, e);
			}
		} catch (JsonProcessingException e) {
			throw new RepositoryException(e);
		}
		return key;
	}

	private JsonObject validateJson(Object object, String schema)
			throws JsonProcessingException {

		// Convert to json
		ObjectMapper mapper = new ObjectMapper();
		JsonObject jsonObject = JsonObject.fromJson(mapper.writeValueAsString(object));
		logger.info(" jsonObject: " + jsonObject);

		// Validate the JsonDocument against the supplied schema
		InputStream schemaJsonFileReader = getClass().getClassLoader()
				.getResourceAsStream(schema);
		JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaJsonFileReader));
		JSONObject jsonSubject = new JSONObject(jsonObject.toString());
		Schema newSchema = SchemaLoader.load(jsonSchema);
		logger.info("schema: " + newSchema + ", object: " + jsonObject);
		newSchema.validate(jsonSubject);
		logger.info(" JsonObject validated successfully");
		return jsonObject;
	}

	@Override
	protected void finalize() {
		logger.warn("KnowledgeRepository disconnecting from Couchbase server");
		CouchbaseRepository.getInstance().disconnect();
	}

}
