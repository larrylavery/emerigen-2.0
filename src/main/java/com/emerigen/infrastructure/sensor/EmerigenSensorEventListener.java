/**
 * 
 */
package com.emerigen.infrastructure.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.learning.PatternRecognizer;
import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.utils.EmerigenProperties;

/**
 * @author Larry
 *
 */
public class EmerigenSensorEventListener implements SensorEventListener {

	// My current sensors
	private Sensor accelerometerSensor;
	private Sensor heartRateSensor;
	private Sensor temperatureSensor;
	private Sensor gpsSensor;
	private Sensor sleepSensor;
	private Sensor glucoseSensor;
	private Sensor bloodPressureSensor;
	private final SensorManager sensorManager;
	private HashMap<Integer, List<PatternRecognizer>> patternRecognizerMap = new HashMap<Integer, List<PatternRecognizer>>();
	private SensorEvent previousSensorEvent;

	private List<SensorEvent> predictions = new ArrayList<SensorEvent>();

	private static final double GPS_DISTANCE_THRESHOLD = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("sensor.gps.distance.threshold.meters"));

	private static final Logger logger = Logger
			.getLogger(EmerigenSensorEventListener.class);

	private final int minDelayBetweenReadingsMillis = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"));

	public EmerigenSensorEventListener() {
		sensorManager = SensorManager.getInstance();

		// Create all sensors
		CreateAllSensors();

		// Subscribe to all sensors
		subscribeToAllSensors();

	}

	private void CreateAllSensors() {
		bloodPressureSensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_BLOOD_PRESSURE, Sensor.LOCATION_MACHINE);
		glucoseSensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_GLUCOSE,
				Sensor.LOCATION_MACHINE);
		heartRateSensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);
		accelerometerSensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_ACCELEROMETER, Sensor.LOCATION_PHONE);
		temperatureSensor = sensorManager.getDefaultSensorForLocation(
				Sensor.TYPE_TEMPERATURE, Sensor.LOCATION_PHONE);
		gpsSensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		sleepSensor = sensorManager.getDefaultSensorForLocation(Sensor.TYPE_SLEEP,
				Sensor.LOCATION_MACHINE);
	}

	private void subscribeToAllSensors() {
		sensorManager.registerListenerForSensorWithFrequency(this, heartRateSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, temperatureSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, gpsSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, sleepSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, bloodPressureSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, glucoseSensor,
				SensorManager.SENSOR_DELAY_NORMAL);

		// TODO retrieve and register all cycles for each sensor
	}

	/**
	 * By default assume that a significant change has not occurred. All subclasses
	 * should override this method.
	 * 
	 * @param previousSensorEvent
	 * @param currentSensorEvent
	 * @return
	 */
	protected boolean significantChangeHasOccurred(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		return false;
	}

	/**
	 * Our main processing has completed. Enable subclasses to do their processing.
	 * 
	 * @param previousSensorEvent
	 * @param currentSensorEvent
	 * @return
	 */
	protected List<Prediction> processSensorChange(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		return null;
	}

	/**
	 * Update all dynamic cycles with the current sensor event
	 * 
	 * @param previousSensorEvent
	 * @param currentSensorEvent
	 */
	protected void updateCycles(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		// TODO implement cycle detection code soon
		return;
	}

	@Override
	public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {

		List<Prediction> result = new ArrayList<Prediction>();

		// Always log the new event
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent);

		// Required elapse time has passed since last event?
		if (minimumDelayBetweenReadingsIsSatisfied(previousSensorEvent, sensorEvent,
				minDelayBetweenReadingsMillis)) {

			// Data has significantly changed?
			if (significantChangeHasOccurred(previousSensorEvent, sensorEvent)) {
				// TODO let cycle-based pattern recognizers have all events so durations
				// merged

				// Return prediction list that culls individual PR predictions
				List<SensorEvent> predictions = getPredictionsForCurrentSensorEvent(
						previousSensorEvent, sensorEvent);

				// Provide all cycle learning routines access to the latest sensor event
				updateCycles(previousSensorEvent, sensorEvent);

				// Do any event-listener-specific processing
				result = processSensorChange(previousSensorEvent, sensorEvent);
			}
		}
		previousSensorEvent = sensorEvent;
		return result;
	}

	/**
	 * 
	 * @param previousSensorEvent
	 * @param currentSensorEvent
	 * @param minDelayBetweenReadingsMillis
	 * @return true if elapse time between events exceeds the required minimum
	 */
	protected boolean minimumDelayBetweenReadingsIsSatisfied(
			SensorEvent previousSensorEvent, SensorEvent currentSensorEvent,
			int minDelayBetweenReadingsMillis) {
		if (previousSensorEvent != null) {

			long currentTime = currentSensorEvent.getTimestamp();
			long previousTime = previousSensorEvent.getTimestamp();
			long elapsedTime = currentTime - previousTime;
			return elapsedTime >= minDelayBetweenReadingsMillis;
		} else
			return true;
	}

	/**
	 * Locate any predictions from the current sensor event, creating a transition
	 * if necessary.
	 * 
	 * @param previousSensorEvent
	 * @param currentSensorEvent
	 */
	protected List<SensorEvent> getPredictionsForCurrentSensorEvent(
			SensorEvent previousSensorEvent, SensorEvent currentSensorEvent) {

		List<SensorEvent> predictions = new ArrayList<SensorEvent>();

		if (isNewEvent(currentSensorEvent)) {
			logger.info("sensorEvent IS new");

			// Create new Transition from previous event, unless no previous event
			if (null != previousSensorEvent) {
				logger.info(
						"Creating new transition from sensorEvent: " + previousSensorEvent
								+ ", to sensorEvent: " + currentSensorEvent);
				KnowledgeRepository.getInstance().newTransition(previousSensorEvent,
						currentSensorEvent);
			}

			// Not predicting now
			predictions = new ArrayList<SensorEvent>();

		} else if (eventHasPredictions(currentSensorEvent)
				|| predictions.contains(currentSensorEvent)) {
			logger.info("sensorEvent has predictions");

			// Save the current predictions
			predictions = KnowledgeRepository.getInstance()
					.getPredictionsForSensorEvent(currentSensorEvent);
			logger.info("Predictions from current sensorEvent: " + predictions);

			// TODO make predictions to whom??
		}
		return predictions;

	}

	/**
	 * 
	 * @param sensorEvent
	 * @return true if predictions exist for the given event
	 */
	protected boolean eventHasPredictions(SensorEvent sensorEvent) {

		// Retrieve the count of predictions for this sensor event
		int predictionCount = KnowledgeRepository.getInstance()
				.getPredictionCountForSensorTypeAndLocation(sensorEvent.getSensorType(),
						sensorEvent.getSensorLocation());
		return predictionCount > 0;
	}

	/**
	 * TODO the sensor event has just been logged, so this should return false???
	 * 
	 * @param sensorEvent
	 * @return true if the sensorEvent is not in the repository
	 */
	private boolean isNewEvent(SensorEvent sensorEvent) {

		SensorEvent event = KnowledgeRepository.getInstance()
				.getSensorEvent(sensorEvent.getKey());
		return null == event;
	}

	@Override
	public void onPause() {

		// TODO Unregister from all sensors during a pause
		// sensorManager.unregisterListenerFromAllSensors(this);
	}

	@Override
	public void onResume() {

		// Restore registrations on resumption
		subscribeToAllSensors();
	}

}
