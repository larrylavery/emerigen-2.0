/**
 * 
 */
package com.emerigen.infrastructure.repository;

import com.emerigen.infrastructure.learning.Cycle;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.knowledge.Entity;
import com.emerigen.knowledge.Transition;

/**
 * @author Larry
 * 
 *         <h1>Knowledge Repository</h1>
 *         <p>
 *         Represents the service that uses a Repository to log client documents
 *         related to the concept of "Knowledge". Knowledge is a (potentially
 *         infinite) series of immutable facts that capture the "learned"
 *         associations between "Sensory Events" on different "Entities" with
 *         one or more channels ("senses").
 *         </p>
 *         <p>
 *         When a new sensory event is encountered/classified, it is logged as a
 *         new Pattern for a specific entity on a specific channel.
 *         </p>
 *         <p>
 *         When a prediction is made, using a transition, it is logged as a new
 *         prediction for an entity on a channel.
 *         </p>
 *         <p>
 *         While "learning" or "predicting", the system will attempt to locate
 *         all transitions that contain the current pattern as the first part of
 *         the transition. The list of those instances represent the possible
 *         predictions for the next sensory event (pattern).
 * 
 *         Uniqueness Requirements: - Pattern (entityID + ChannelType +
 *         sensorAttributesHash) are unique repository wide - Entity (entityID
 *         is unique repository wide (UUIDs) - Transition entries are keyed by
 *         the first pattern in the transition
 * 
 *         Key/Index Requirements - Entity: - entityID + channelType (primary
 *         key) - Channel Type: (Embeded in Entity, so NOT needed) - channelType
 *         (primary key) - Similar to our senses (visual, touch, taste, etc) -
 *         Pattern: - id: the composite key below
 *         (entityID+ChannelType+sensorAttributesHash). Must be unique for all
 *         patterns. Since explicitly passed in, it will be assigned to the
 *         builtin field "id" ("_id" in Java Beans) - entityID + channelType +
 *         sensorEventHash(ensures no dups when inserting) AND ensures no dups
 *         when comparing patterns later (without having to query
 *         sensorAttributes since their current implementation may change.)
 * 
 *         - logNewSensoryEvent(entityID+channelType+sensorAttributesHash,
 *         patternJsonObject) - CREATE INDEX `ix_pattern_unique` ON
 *         `prediction`(`entityID`,`channelType`, `sensorAttributesHash`)
 *
 *         - getPatternCountForEntityOnChannel(entityID, channelType) - CREATE
 *         INDEX `ix_pattern` ON `prediction`(`entityID`,`channelType`) - SELECT
 *         count(*) FROM pattern WHERE entityID=eee AND channelType=ccc -
 *         Prediction: - entityID + channelType + transition.firstPattern
 * 
 *         - getPredictionAccuracyForEntityOnChannel():
 *         predictionCount/sensoryEventCount - SELECT count(*) FROM prediction
 *         WHERE entityID=eee AND channelType=ct
 * 
 *         - getPredictionCountForEntityOnChannel(entityID, channelType) -
 *         CREATE INDEX `ix_prediction_unique` ON
 *         `prediction`(`entityID`,`channelType`) - Transition: - primary key:
 *         (entityID, channelType + patternOneKey) - patternOneKey + entityID +
 *         channelType - relates Transition to its patterns and an entity on a
 *         channel. - getPredictionsForPattern(transition.patternOneKey,
 *         entityID, channelType) - SELECT transition.patternTwoContent FROM
 *         transition WHERE entityID= "ttt" AND channelType="yyy" AND
 *         transition.patternOneKey="p1EntityID + p1ChannelType +
 *         p1sensorAttributesHash") Couchbase Console Index commands: CREATE
 *         INDEX ix_entity ON `entity` (entity-id, channel-type) USING GSI
 *         CREATE PRIMARY INDEX ix_pattern_primary ON `pattern` (pattern-id)
 *         USING GSI CREATE PRIMARY INDEX ix_pattern_primary ON `pattern` USING
 *         GSI CREATE INDEX ix_pattern ON `pattern` (entity-id, channel-type)
 *         USING GSI CREATE INDEX ix_prediction ON `prediction` (transition-id,
 *         entity-id, channel-type) USING GSI CREATE INDEX ix_transition ON
 *         `transition` (pattern-id, predicted-pattern-id, entity-id,
 *         channel-type) USING GSI
 *
 *         Index usage: - getPredictionAccuracy(): - int
 *         getCountOfPatternsProcessedForEntityOnChannel(entityID, channelType)
 *         patternDB - int getCountOfPredictionsMadeForEntityOnChannel(entityID,
 *         channelType) predictionDB List<Pattern>
 *         getPredictionsForPattern(patternID, entityID, channelType)
 *         transitionDB
 * 
 * 
 */
public abstract class AbstractKnowledgeRepository {

	// Add new Entity to the knowledgebase
	public abstract void newEntity(Entity entity);

	// Log a neww SensorEvent
	public abstract void newSensorEvent(SensorEvent sensorEvent);

	// Log a new Cycle
//	public abstract void newCycle(Cycle cycle);

	// Log that a sensor event transition has been learned

	public abstract int getSensorEventCountForSensorTypeAndLocation(int sensorType, int sensorLocation);

	// Get the prediction accuracy fro a specific entity on the channel
	public double getPredictionAccuracyForSensorTypeAndLocation(int sensorType, int sensorLocation) {

		int sensorEventCount = getSensorEventCountForSensorTypeAndLocation(sensorType, sensorLocation);
		/**
		 * TODO Add code to search for all SensorEvents that are the "predicted Events"
		 * in all transitions where the firstEventKey = sensorType+sensorLocation+values
		 */
		// TODO How does KnowledgeRepository get access to PredictionService?
//		int predictionCount = getPredictionCountForSensorTypeAndLocation(sensorType,
//				sensorLocation);

//		}
		return 0.0;
	}

	public abstract Entity getEntity(String entityKey);

	public abstract SensorEvent getSensorEvent(String sensorEventKey);

	public abstract Cycle getCycle(String cycleTypeName, String cycleKey);

	public abstract Transition getTransition(String transitionKey);

	public void newCycle(String cycleKey, Cycle cycle) {
		// TODO Auto-generated method stub

	}
}
