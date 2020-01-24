package com.emerigen.infrastructure.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public class SensorManager {

	public static final int SENSOR_DELAY_NORMAL = 0;

	private static Logger logger = Logger.getLogger(SensorManager.class);

	private List<Sensor> allSensors = new ArrayList<Sensor>();

	// Enable retrieving all event listeners for a sensor
	private HashMap<SensorEventListener, List<Sensor>> registeredSensorsPerListener;

	// Enable retrieving all event listeners for a sensor
	private HashMap<Sensor, List<SensorEventListener>> listenersPerSensor = new HashMap<Sensor, List<SensorEventListener>>();

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
			throw new IllegalArgumentException("samplingFrequencyMillis must be zero or more");

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
	 * Unregister the listener for the given sensor type
	 * 
	 * @param listener        the listener to unregister
	 * @param sensorClassName the sensor type to unregister from
	 * @return true if the unregister was successful, otherwise false
	 */
	public boolean unregisterListenerFromSensor(SensorEventListener listener, Sensor sensor) {
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

	public boolean listenerIsRegisteredToSensor(SensorEventListener listener, Sensor sensor) {
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
	 * @param sensorType is the type of sensor to create
	 * @return the default sensor of the requested type
	 */
	public Sensor getDefaultSensor(int sensorType) {

		Sensor sensor;

		if (sensorType <= 0)
			throw new IllegalArgumentException("sensorType must be positive");

		switch (sensorType) {
		case Sensor.TYPE_HEART_RATE:
			sensor = new HeartRateSensor(Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL,
					false);
			allSensors.add(sensor);
			logger.info("Creating Heart Rate sensor");
			return sensor;

		case Sensor.TYPE_ACCELEROMETER:
			sensor = new AccelerometerSensor(Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL,
					false);
			allSensors.add(sensor);
			logger.info("Creating Accelerometer sensor");
			return sensor;

		case Sensor.TYPE_TEMPERATURE:
			sensor = new TemperatureSensor(sensorType, Sensor.REPORTING_MODE_CONTINUOUS,
					SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			logger.info("Creating Temperature sensor");
			return sensor;
		default:
			throw new IllegalArgumentException(
					"Sensor type must be equal to one of the defined constants");
		}
	}

	public Sensor getDefaultSensor(int sensorType, int sensorLocation) {

		Sensor sensor;

		if (sensorType <= 0)
			throw new IllegalArgumentException("sensorType must be positive");

		switch (sensorType) {
		case Sensor.TYPE_HEART_RATE:
			sensor = new HeartRateSensor(sensorType, sensorLocation,
					Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			logger.info("Creating Heart Rate sensor");
			return sensor;

		case Sensor.TYPE_ACCELEROMETER:
			sensor = new AccelerometerSensor(sensorType, sensorLocation,
					Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			logger.info("Creating Accelerometer sensor");
			return sensor;

		case Sensor.TYPE_TEMPERATURE:
			sensor = new TemperatureSensor(sensorType, sensorLocation,
					Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			logger.info("Creating Temperature sensor");
			return sensor;
		default:
			throw new IllegalArgumentException(
					"Sensor type must be equal to one of the defined constants");
		}
	}

	public boolean listenerIsRegisteredToSensors(SensorEventListener emerigenListener) {
		if (emerigenListener == null)
			throw new IllegalArgumentException("Listener must not be null");

		// Return false if the listener is not registered to any sensor.
		List<Sensor> sensors = registeredSensorsPerListener.get(emerigenListener);
		if (sensors == null) {

			// Listener is not registered for any sensor
			return false;
		} else if (!sensors.isEmpty()) {

			// listener is registered to at least one sensor
			return true;
		} else {

			// Not registered to any sensors
			return false;
		}
	}
}