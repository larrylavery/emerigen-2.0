package com.emerigen.infrastructure.sensor;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class HeartRateSensorEventListenerOld implements SensorEventListener {

	private final SensorManager sensorManager;
	private HeartRateSensor heartRateSensor;

	private long currentTime;
	private long lastUpdateTime;

	private boolean predicting = false;

	private static final Logger logger = Logger.getLogger(HeartRateSensor.class);

	private final int minDelayBetweenReadingsMillis = Integer.parseInt(EmerigenProperties
			.getInstance().getValue("sensor.default.minimum.delay.between.readings.millis"));
	private SensorEvent lastSensorEvent;
	private List<SensorEvent> predictions = new ArrayList<SensorEvent>();

	public HeartRateSensorEventListenerOld(int sensorLocation) {
		sensorManager = SensorManager.getInstance();
		heartRateSensor = (HeartRateSensor) sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE,
				sensorLocation);

		sensorManager.registerListenerForSensorWithFrequency(this, heartRateSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public boolean onSensorChanged(SensorEvent sensorEvent) {

		// If the minimum delay has occurred then process the event
		Sensor sensor = sensorEvent.getSensor();

		if (sensor.getClass().isInstance(heartRateSensor)) {

			// Allways log the sensor event
			KnowledgeRepository.getInstance().newSensorEvent(sensorEvent);

			// determine the elapse time since the last onChanged processed
			currentTime = System.currentTimeMillis();
			long elapseTime = currentTime - lastUpdateTime;
			if (elapseTime > minDelayBetweenReadingsMillis) {
				logger.info("The minimum elapse time millis (" + elapseTime
						+ ") since the last sensor change has occurred. Processing event: "
						+ sensorEvent);

				// Retrieve the current heart rate
				int currentHeartRate = (int) sensorEvent.getValues()[0];

				/**
				 * TODO Add heart rate learning/prediction code here.
				 * 
				 * Possibilities: checking for patterns on prior days near the same time
				 * periord; locate existing Transitions and make predictions about potential
				 * next heart rate range; make important predictions about heart rate moving to
				 * danger zone (tachycardia, SVT, etc). The same learning/prediction that would
				 * happen for most other sensors exception these may be more important for
				 * certain types of individuals with heart disease or potential heart disease.
				 */
				processEvent(sensorEvent);
				lastUpdateTime = currentTime;
				lastSensorEvent = sensorEvent;
				return true;
			} else {
				// Minimum time lapse has not occurred, don't process event
				return false;
			}
		}
		return false;

	}

	private void processEvent(SensorEvent sensorEvent) {

		if (isNewEvent(sensorEvent)) {
			logger.info("sensorEvent IS new");

			// Create new Transition from previous event, unless no previous event
			if (null != lastSensorEvent) {
				logger.info("Creating new transition from sensorEvent: " + lastSensorEvent
						+ ", to sensorEvent: " + sensorEvent);
				KnowledgeRepository.getInstance().newTransition(lastSensorEvent, sensorEvent);
			}

			// Not predicting now
			predictions = new ArrayList<SensorEvent>();

		} else if (eventHasPredictions(sensorEvent) || predictions.contains(sensorEvent)) {
			logger.info("sensorEvent has predictions");

			// Save the current predictions
			predictions = KnowledgeRepository.getInstance()
					.getPredictionsForSensorEvent(sensorEvent);
			logger.info("Predictions from current sensorEvent: " + predictions);

			// TODO make predictions to whom??
		}
	}

	private boolean eventHasPredictions(SensorEvent sensorEvent) {

		// Retrieve the count of predictions for this sensor event
		int predictionCount = KnowledgeRepository.getInstance()
				.getPredictionCountForSensorTypeAndLocation(sensorEvent.getSensorType(),
						sensorEvent.getSensorLocation());
		return predictionCount > 0;
	}

	private boolean isNewEvent(SensorEvent sensorEvent) {

		SensorEvent event = KnowledgeRepository.getInstance().getSensorEvent(sensorEvent.getKey());
		return null == event;
	}

	@Override
	public void onAccuracyChanged() {
		// TODO Implement onAccuracyChnged only if required

	}

	@Override
	public void onPause() {

		// Unregister during a pause
		sensorManager.unregisterListenerFromAllSensors(this);
	}

	@Override
	public void onResume() {

		// Restore the registration on resumption
		sensorManager.registerListenerForSensorWithFrequency(this, heartRateSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

}
