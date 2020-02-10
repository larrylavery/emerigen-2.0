package com.emerigen.infrastructure.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.learning.PatternRecognizer;
import com.emerigen.infrastructure.learning.TransitionPatternRecognizer;
import com.emerigen.infrastructure.repository.KnowledgeRepository;

public class SensorManager {

	public static final int SENSOR_DELAY_NORMAL = 0;

	private static Logger logger = Logger.getLogger(SensorManager.class);

	private List<Sensor> allSensors = new ArrayList<Sensor>();

	// Enable retrieving all sensors for a listener
	private HashMap<SensorEventListener, List<Sensor>> registeredSensorsPerListener;

	// Enable retrieving all event listeners for a sensor
	private HashMap<Sensor, List<SensorEventListener>> listenersPerSensor = new HashMap<Sensor, List<SensorEventListener>>();

	// Enable retrieving all pattern recognizers for a sensor type
	private HashMap<Sensor, List<PatternRecognizer>> registeredPatternRecognizersPerSensor = new HashMap<Sensor, List<PatternRecognizer>>();

	// Singleton infrastructure
	private static SensorManager instance;

	public static SensorManager getInstance() {
		if (instance == null) {
			synchronized (SensorManager.class) {
				if (instance == null) {
					instance = new SensorManager();
				}
			}
		}
		// Return the singleton SensorManager
		return instance;
	}

	public static void reset() {
		instance = null;
	}

	public SensorManager() {
		registeredSensorsPerListener = new HashMap<SensorEventListener, List<Sensor>>();
	}

	/**
	 * Register a listener for the given sensor type at the given sampling frequency
	 * 
	 * @param listener          The listener to invoke when sensor publishes a new
	 *                          event
	 * @param sensorType        the classname of the sensor to register for
	 * @param samplingFrequency the frequency at which this listener should receive
	 *                          new event notifications. The desired delay between
	 *                          two consecutive events in microseconds
	 * @return true if the listener has been registered, otherwise false
	 */
	public boolean registerListenerForSensorWithFrequency(SensorEventListener listener,
			Sensor sensor, int samplingFrequencyMillis) {
		if (listener == null)
			throw new IllegalArgumentException("Listener must not be null");
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		if (samplingFrequencyMillis < 0)
			throw new IllegalArgumentException(
					"samplingFrequencyMillis must be zero or more");

		List<Sensor> sensors = registeredSensorsPerListener.get(listener);

		if (sensors == null) {

			// The listener is not registered to any sensor. Initialize data
			sensors = new ArrayList<Sensor>();
			sensors.add(sensor);
			registeredSensorsPerListener.put(listener, sensors);
			return true;
		} else if (!sensors.contains(sensor)) {

			// Listener has registration but not for this sensor. Add it
			sensors.add(sensor);
			registeredSensorsPerListener.put(listener, sensors);
			return true;
		} else {

			// Registration already exists
			return true;
		}

	}

	/**
	 * Register all pattern recognizers for the given sensor at the given sampling
	 * frequency
	 * 
	 * @param patternRecognizer The pattern recognizer to invoke when sensor
	 *                          publishes a new event
	 * @param sensor            the sensor to register for
	 * @param samplingFrequency the frequency at which this pattern recognizer
	 *                          should receive new event notifications. The desired
	 *                          delay between two consecutive events in nanoseconds
	 * @return true if the pattern recognizer was successfully registered, otherwise
	 *         false
	 */
	public boolean registerPatternRecognizersForSensorWithFrequency(Sensor sensor,
			int samplingFrequencyMillis) {
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		if (samplingFrequencyMillis < 0)
			throw new IllegalArgumentException(
					"samplingFrequencyMillis must be zero or more");

		// Retrieve all cycle pattern recognizers for this sensor type
		List<PatternRecognizer> patternRecognizers = KnowledgeRepository.getInstance()
				.getPatternRecognizersForSensorType(sensor);

		// Add a Transition pattern recognizer to the list for this sensor
		patternRecognizers.add(new TransitionPatternRecognizer(sensor));

		if (patternRecognizers == null || patternRecognizers.isEmpty()) {

			// There are no pattern recognizers to register
			return true;
		} else if (!patternRecognizers.isEmpty()) {
			/**
			 * This will overwrite existing registered pattern recognizers
			 */
			for (PatternRecognizer patternRecognizer : patternRecognizers) {
				registerPatternRecognizerForSensorWithFrequency(patternRecognizer, sensor,
						samplingFrequencyMillis);
			}
			return true;
		} else {

			// Registration already exists
			return true;
		}
	}

	/**
	 * Register a pattern recognizer for the given sensor at the given sampling
	 * frequency
	 * 
	 * @param patternRecognizer The pattern recognizer to invoke when sensor
	 *                          publishes a new event
	 * @param sensor            the sensor to register for
	 * @param samplingFrequency the frequency at which this pattern recognizer
	 *                          should receive new event notifications. The desired
	 *                          delay between two consecutive events in nanoseconds
	 * @return true if the pattern recognizer was successfully registered, otherwise
	 *         false
	 */
	public boolean registerPatternRecognizerForSensorWithFrequency(
			PatternRecognizer patternRecognizer, Sensor sensor,
			int samplingFrequencyMillis) {
		if (patternRecognizer == null)
			throw new IllegalArgumentException("patternRecognizer must not be null");
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		if (samplingFrequencyMillis < 0)
			throw new IllegalArgumentException(
					"samplingFrequencyMillis must be zero or more");

		List<Sensor> sensors = registeredSensorsPerListener.get(patternRecognizer);

		if (sensors == null) {

			// pattern recognizer is not registered to the sensor type. Initialize data
			List<Sensor> newSensors = new ArrayList<Sensor>();
			newSensors.add(sensor);
			registeredSensorsPerListener.put(patternRecognizer, newSensors);
			return true;
		} else if (!sensors.contains(sensor)) {

			// Pattern recognizer is not registered for this sensor. Add it
			sensors.add(sensor);
			registeredSensorsPerListener.put(patternRecognizer, sensors);
			return true;
		} else {

			// Registration already exists
			return true;
		}

	}

	/**
	 * Unregister the listener for the given sensor type
	 * 
	 * @param listener        the listener to unregister
	 * @param sensorClassName the sensor type to unregister from
	 * @return true if the unregister was successful, otherwise false
	 */
	public boolean unregisterListenerFromSensor(SensorEventListener listener,
			Sensor sensor) {
		List<Sensor> sensors = registeredSensorsPerListener.get(listener);
		if (!sensors.contains(sensor)) {

			// Listener was not registered to the given sensor
			return true;
		} else {

			// Remove registration for given sensor
			sensors.remove(sensor);
			registeredSensorsPerListener.put(listener, sensors);
			return true;
		}
	}

	/**
	 * Unregister the patternRecognizer for the given sensor and remove it from the
	 * repository
	 * 
	 * @param patternRecognizer
	 * @param sensor
	 * @return
	 */
	public boolean unregisterPatternRecognizerFromSensor(
			PatternRecognizer patternRecognizer, Sensor sensor) {
		List<PatternRecognizer> patternRecognizers = registeredPatternRecognizersPerSensor
				.get(patternRecognizer);
		if (!patternRecognizers.contains(patternRecognizer)) {

			// pattern recognizer was not registered to the given sensor
			return true;
		} else {

			// Remove registration for given pattern recognizer
			patternRecognizers.remove(patternRecognizer);
			registeredPatternRecognizersPerSensor.put(sensor, patternRecognizers);
			return true;
		}
	}

	/**
	 * Unregister the listener for all sensor types *
	 * 
	 * @param listener the listener to unregister
	 * @return true if the unregister was successful, otherwise false
	 */
	public boolean unregisterListenerFromAllSensors(SensorEventListener listener) {
		List<Sensor> sensors = registeredSensorsPerListener.get(listener);
		if (sensors.size() > 0) {

			// Remove Listener registrations

			sensors = new ArrayList<Sensor>();
			registeredSensorsPerListener.remove(listener);
			registeredSensorsPerListener.put(listener, sensors);
			return true;
		} else {

			// Listener was not registered to any sensor
			return true;
		}
	}

	public List<Sensor> getAllSensors() {
		return allSensors;

	}

	public boolean listenerIsRegisteredToSensor(SensorEventListener listener,
			Sensor sensor) {
		if (listener == null)
			throw new IllegalArgumentException("Listener must not be null");
		if (sensor == null)
			throw new IllegalArgumentException("Sensor must not be null");

		// Return false if the listener is not registered to any sensor.
		List<Sensor> sensors = registeredSensorsPerListener.get(listener);
		if (sensors == null) {

			// Listener is not registered for any sensor
			return false;
		} else if (sensors.contains(sensor)) {

			// listener is registered to the given sensor
			return true;
		} else {

			// Not registered to the given sensor
			return false;
		}
	}

	/**
	 * This is a "factory method" design pattern based on the given sensor type.
	 * TODO We may want to return one selected from a list of the same type.
	 * 
	 * @param sensorType     is the type of sensor to create
	 * @param sensorLocation TODO
	 * @return the default sensor of the requested type
	 */
	public Sensor getDefaultSensorForLocation(int sensorType, int sensorLocation) {

		Sensor sensor;
		if (sensorType <= 0)
			throw new IllegalArgumentException("sensorType must be positive");
		if (sensorLocation <= 0)
			throw new IllegalArgumentException("sensorLocataion must be positive");

		// Retrieve existing sensors matching type and location
		List<Sensor> sensors = allSensors.stream().filter(
				(s) -> (s.getType() == sensorType) && (s.getLocation() == sensorLocation))
				.collect(Collectors.toList());

		// Return the first located sensor
		if (!sensors.isEmpty()) {
			logger.info("Returning existing sensor: " + sensors.get(0));
//			registerPatternRecognizersForSensorWithFrequency(sensors.get(0),
//					SENSOR_DELAY_NORMAL);
			return sensors.get(0);
		}

		switch (sensorType) {

		case Sensor.TYPE_HEART_RATE:
			sensor = new HeartRateSensor(sensorLocation, Sensor.REPORTING_MODE_CONTINUOUS,
					SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			registerPatternRecognizersForSensorWithFrequency(sensor, SENSOR_DELAY_NORMAL);
			logger.info("Created new Heart Rate sensor");
			return sensor;

		case Sensor.TYPE_ACCELEROMETER:
			sensor = new AccelerometerSensor(sensorLocation,
					Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			registerPatternRecognizersForSensorWithFrequency(sensor, SENSOR_DELAY_NORMAL);
			logger.info("Creating Accelerometer sensor");
			return sensor;

		case Sensor.TYPE_SLEEP:
			sensor = new SleepSensor(sensorLocation, Sensor.REPORTING_MODE_CONTINUOUS,
					SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			registerPatternRecognizersForSensorWithFrequency(sensor, SENSOR_DELAY_NORMAL);
			logger.info("Creating Sleep monitoring sensor");
			return sensor;

		case Sensor.TYPE_BLOOD_PRESSURE:
			sensor = new BloodPressureSensor(sensorLocation,
					Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			registerPatternRecognizersForSensorWithFrequency(sensor, SENSOR_DELAY_NORMAL);
			logger.info("Creating blood pressure monitoring sensor");
			return sensor;

		case Sensor.TYPE_GLUCOSE:
			sensor = new GlucoseSensor(sensorLocation, Sensor.REPORTING_MODE_CONTINUOUS,
					SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			registerPatternRecognizersForSensorWithFrequency(sensor, SENSOR_DELAY_NORMAL);
			logger.info("Creating glucose monitoring sensor");
			return sensor;

		case Sensor.TYPE_TEMPERATURE:
			sensor = new TemperatureSensor(sensorLocation,
					Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			registerPatternRecognizersForSensorWithFrequency(sensor, SENSOR_DELAY_NORMAL);
			logger.info("Creating Temperature sensor");
			return sensor;

		case Sensor.TYPE_GPS:
			sensor = new GpsSensor(sensorLocation, Sensor.REPORTING_MODE_CONTINUOUS,
					SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			registerPatternRecognizersForSensorWithFrequency(sensor, SENSOR_DELAY_NORMAL);
			logger.info("Creating GPS sensor");
			return sensor;
		default:
			throw new IllegalArgumentException(
					"Sensor type must be equal to one of the defined constants");
		}
	}

	public boolean listenerIsRegisteredToAnySensor(SensorEventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("Listener must not be null");

		// Return false if the listener is not registered to any sensor.
		List<Sensor> sensors = registeredSensorsPerListener.get(listener);
		if (sensors == null || sensors.isEmpty()) {

			// Listener is not registered for any sensor
			return false;
		} else {

			// listener is registered to at least one sensor
			return true;
		}
	}
}