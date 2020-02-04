/**
 * 
 */
package com.emerigen.infrastructure.learning;

import java.util.List;

import com.emerigen.infrastructure.sensor.SensorEvent;

/**
 * This class represents all pattern recognizers and predictors. Currently there
 * are two types.
 * 
 * First, the TransitionPatternRecognizer consumes SensorEvents, logs transitons
 * from one event to the next, and [on subsequent runs] makes predictions based
 * on current sensor events. Since multiple transitions from an event may have
 * been logged, multiple predictions may be returned when a new sensor event is
 * received or when a request for predictions is invoked.
 * 
 * Second, the CyclePatternRecognizer may be one of several types (e.g. hourly,
 * daily, weekly, monthly, or yearly), consumes SensorEvents and builds cycles
 * of potential predictions. The easiest example to consider is a Daily GPS
 * cycle-based pattern recognizer. One cycle semantically represents a user's
 * "Geo-Fence" of travel destinations for a 24 hour period. It's more valuable
 * than the simpler Transition-based pattern recognizer because the app can tell
 * when a user is running late for his usual destinations, when they might be
 * lost based on receiving destinations outside of his geo-fence, when they
 * might be lost, or when they go to a different place than normal for lunch w/o
 * breaking their overall pattern.
 * 
 * @IDEA Moreover, all cycle-based patterns use a configurable
 *       standard-deviation-based equality when data points (i.e. destinations
 *       for a GPS cycle) are compared to determine if the user is inside, or
 *       outside, of their cycle. This allows "fuzzy" boundaries resulting in
 *       cycle patterns that more accurately predict the user's behavior and
 *       allow latitude with disruption. It is also configurable how quickly the
 *       "fuzziness" becomes ever more restricting (i.e. allows less exploration
 *       and more exploitation).
 * 
 *       Finally, the data for transition-based pattern recognition is
 *       immutable. Cycle-based pattern recognizers utilize real-time learning
 *       cycles whose accuracy constantly evolve and will continue to "rollover"
 *       for all subsequent cycle durations (i.e. Daily cycles rollover every
 *       night at 12am).
 * 
 * 
 * @author Larry
 *
 */
public abstract class PatternRecognizer {

	public PatternRecognizer() {
	}

	public abstract List<Prediction> onSensorChanged(SensorEvent sensorEvent);

	public abstract List<Prediction> getPredictionsForSensorEvent(SensorEvent sensorEvent);

}
