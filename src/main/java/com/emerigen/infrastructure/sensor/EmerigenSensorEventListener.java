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
	List<Sensor> sensors = new ArrayList<Sensor>();
	List<SensorEventListener> listeners = new ArrayList<SensorEventListener>();

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

		// TODO design to handle whatever sensors/listeners available
		// TODO for each sensor, load cycles & transition PRs like sm.getDefaultSensor()
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
		return true;
	}

	@Override
	public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {

		SensorManager sm = SensorManager.getInstance();
		List<Prediction> result = new ArrayList<Prediction>();

		// Required elapse time has passed since last event?
		if (minimumDelayBetweenReadingsIsSatisfied(previousSensorEvent, sensorEvent,
				minDelayBetweenReadingsMillis)) {

			// Data has significantly changed?
			if (significantChangeHasOccurred(previousSensorEvent, sensorEvent)) {

				/**
				 * Send the event to each registered Listener for processing and
				 * accumulate the predictions.
				 */
				List<SensorEventListener> listeners = sm
						.getRegistrationsForSensor(sensorEvent.getSensor());
				for (SensorEventListener sensorEventListener : listeners) {
					if (!(sensorEventListener instanceof EmerigenSensorEventListener)) {
						result.addAll(sensorEventListener.onSensorChanged(sensorEvent));
					}
				}
			}
		}
		previousSensorEvent = sensorEvent;

		// Always log the new event
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent);
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
		return true;
//		if (previousSensorEvent != null) {
//
//			long currentTime = currentSensorEvent.getTimestamp();
//			long previousTime = previousSensorEvent.getTimestamp();
//			long elapsedTime = currentTime - previousTime;
//			return elapsedTime >= minDelayBetweenReadingsMillis;
//		} else
//			return true;
	}

	@Override
	public void onPause() {

		// Disable, but save, all current registrations
		sensorManager.disableListenerRegistrations();
	}

	@Override
	public void onResume() {

		// Restore registrations on resumption
		sensorManager.enableListenerRegistrations();
	}

}
