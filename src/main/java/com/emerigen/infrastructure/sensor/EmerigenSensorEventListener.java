/**
 * 
 */
package com.emerigen.infrastructure.sensor;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
	private final SensorManager sensorManager;

	private long currentTime;
	private long lastUpdateTime;
	private SensorEvent lastSensorEvent;
	private List<SensorEvent> predictions = new ArrayList<SensorEvent>();
	private float last_x, last_y, last_z;
	private static final float SHAKE_THRESHOLD = Float.parseFloat(EmerigenProperties.getInstance()
			.getValue("sensor.accelerometer.shake.threshold.millis"));

	private static final Logger logger = Logger.getLogger(EmerigenSensorEventListener.class);

	private final int minDelayBetweenReadingsMillis = Integer.parseInt(EmerigenProperties
			.getInstance().getValue("sensor.default.minimum.delay.between.readings.millis"));

	public EmerigenSensorEventListener() {
		sensorManager = SensorManager.getInstance();

		// Create all sensors
		heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
		accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

		// Subscribe to all sensors
		sensorManager.registerListenerForSensorWithFrequency(this, heartRateSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, temperatureSensor,
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	@Override
	public void onCreate() {
		// TODO create these in the activate method??

	}

	@Override
	public boolean onSensorChanged(SensorEvent sensorEvent) {

		// If the minimum delay has occurred then process the event
		Sensor sensor = sensorEvent.getSensor();

		if (sensor.getClass().isInstance(heartRateSensor)) {
			return processHeartRateChanged(sensorEvent);
		}

		if (sensor.getClass().isInstance(accelerometerSensor)) {
			return processAccelerometerChange(sensorEvent);
		}

		// Not a sensor we are interested in
		return false;

	}

	private boolean processAccelerometerChange(SensorEvent sensorEvent) {

		// determine the elapse time since the last onChanged processed
		currentTime = System.currentTimeMillis();
		long elapseTime = currentTime - lastUpdateTime;
		if (elapseTime > minDelayBetweenReadingsMillis) {
			logger.info("The minimum elapse time millis (" + elapseTime
					+ ") since the last sensor change has occurred. Processing event: "
					+ sensorEvent);

			// minimum delay elapse time has occurred, extract coordinates
			float x = sensorEvent.getValues()[0];
			float y = sensorEvent.getValues()[1];
			float z = sensorEvent.getValues()[2];

			// Calculate the device's speed
			float speed = Math.abs(x + y + z - last_x - last_y - last_z) / elapseTime * 1000000;
//				float speed = Math.abs(x + y + z - last_x - last_y - last_z) / elapseTime * 1000;
			logger.info("The device speed is (" + speed + ")");
			if (speed > SHAKE_THRESHOLD) {
				logger.info("The speed exceeds the 'shake threshold' of " + SHAKE_THRESHOLD);

				// TODO sample code for processing a significant accelerometer move/shake

				// Update for next change event
				last_x = x;
				last_y = y;
				last_z = z;
				return true;
			} else {
				logger.info("The speed did not exceed the 'shake threshold' of " + SHAKE_THRESHOLD);
				return false;
			}
		} else {

			// Minimum delay has not occurred
			return false;
		}
	}

	private boolean processHeartRateChanged(SensorEvent sensorEvent) {

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
			processHeartRateEvent(sensorEvent);
			lastUpdateTime = currentTime;
			lastSensorEvent = sensorEvent;
			return true;
		} else {
			// Minimum time lapse has not occurred, don't process event
			return false;
		}
	}

	private void processHeartRateEvent(SensorEvent sensorEvent) {

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

	/**
	 * 
	 * @param sensorEvent
	 * @return true if predictions exist for the given event
	 */
	private boolean eventHasPredictions(SensorEvent sensorEvent) {

		// Retrieve the count of predictions for this sensor event
		int predictionCount = KnowledgeRepository.getInstance()
				.getPredictionCountForSensorTypeAndLocation(sensorEvent.getSensorType(),
						sensorEvent.getSensorLocation());
		return predictionCount > 0;
	}

	/**
	 * 
	 * @param sensorEvent
	 * @return true if the sensorEvent is not in the repository
	 */
	private boolean isNewEvent(SensorEvent sensorEvent) {

		SensorEvent event = KnowledgeRepository.getInstance().getSensorEvent(sensorEvent.getKey());
		return null == event;
	}

	@Override
	public void onAccuracyChanged() {
		// TODO code onAccuracyChaned if needed

	}

	@Override
	public void onPause() {

		// Unregister from all sensors during a pause
		sensorManager.unregisterListenerFromAllSensors(this);

	}

	@Override
	public void onResume() {

		// Restore registrations on resumption
		sensorManager.registerListenerForSensorWithFrequency(this, heartRateSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, temperatureSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

}
