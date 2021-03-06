package com.emerigen.infrastructure.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.learning.TransitionPatternRecognizer;
import com.emerigen.infrastructure.learning.cycle.CyclePatternRecognizer;
import com.emerigen.infrastructure.learning.cycle.DailyCycle;
import com.emerigen.infrastructure.learning.cycle.MonthlyCycle;
import com.emerigen.infrastructure.learning.cycle.WeeklyCycle;
import com.emerigen.infrastructure.learning.cycle.YearlyCycle;
import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class SensorManager {

	public static final int SENSOR_DELAY_NORMAL = Integer
			.parseInt(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"));
	private static Logger logger = Logger.getLogger(SensorManager.class);
	private List<Sensor> allSensors = new ArrayList<Sensor>();
	private PredictionService predictionService = new PredictionService();

	// Enable retrieving all event listeners for a sensor
	private HashMap<Sensor, List<SensorEventListener>> eventListenersPerSensor = new HashMap<Sensor, List<SensorEventListener>>();
	private HashMap<Sensor, List<SensorEventListener>> disabledEventListeners;

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

	public SensorManager(PredictionService predictionService) {
		if (predictionService == null)
			throw new IllegalArgumentException("predictionService must not be null");

		this.predictionService = new PredictionService();
		eventListenersPerSensor = new HashMap<Sensor, List<SensorEventListener>>();
		disabledEventListeners = null;
		allSensors = new ArrayList<Sensor>();
	}

	public SensorManager() {
		this(new PredictionService());
	}

	/**
	 * Register a listener for the given sensor
	 * 
	 * @param listener   The listener to invoke when sensor publishes a new event
	 * @param sensorType the classname of the sensor to register for
	 * @return true if the listener has been registered, otherwise false
	 */
	public boolean registerListenerForSensor(SensorEventListener listener,
			Sensor sensor) {
		if (listener == null)
			throw new IllegalArgumentException("Listener must not be null");
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");

		List<SensorEventListener> eventListeners = eventListenersPerSensor.get(sensor);
		if (eventListeners == null) {

			// The listener is not registered to this sensor. Add them
			eventListeners = new ArrayList<SensorEventListener>();
			eventListeners.add(listener);
			eventListenersPerSensor.put(sensor, eventListeners);
			return true;
		} else if (!eventListeners.contains(listener)) {

			// Listener has registration but not for this sensor. Add it
			eventListeners.add(listener);
			eventListenersPerSensor.put(sensor, eventListeners);
			return true;
		} else {

			// Registration already exists
			return true;
		}
//		return true;
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
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		if (listener == null)
			throw new IllegalArgumentException("listener must not be null");

		List<SensorEventListener> eventListeners = eventListenersPerSensor.get(sensor);
		if (!eventListeners.contains(listener)) {

			// Listener was not registered to the given sensor
			return true;
		} else {

			// Remove registration for given sensor
			eventListeners.remove(listener);
			eventListenersPerSensor.put(sensor, eventListeners);
			return true;
		}
	}

	/**
	 * Load and create all pattern recognizers for the given sensor
	 * 
	 * @param sensor
	 * @return a list of sensor event listeners applicable for the given sensor
	 */
	public List<SensorEventListener> createPatternRecognizersForSensor(Sensor sensor) {
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");

		List<SensorEventListener> patternRecognizers = KnowledgeRepository.getInstance()
				.getPatternRecognizersForSensor(sensor);

		/**
		 * Add all cycle pattern recognizer types for each sensor
		 */
		// Daily
		DailyCycle dailyCycle = new DailyCycle(sensor.getSensorType(), sensor.getSensorLocation());
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(dailyCycle, sensor,
				new PredictionService(sensor));
		patternRecognizers.add(cpr);

		// Weekly
		WeeklyCycle weeklyCycle = new WeeklyCycle(sensor.getSensorType(), sensor.getSensorLocation());
		cpr = new CyclePatternRecognizer(weeklyCycle, sensor,
				new PredictionService(sensor));
		patternRecognizers.add(cpr);

		// Monthly
		MonthlyCycle monthlyCycle = new MonthlyCycle(sensor.getSensorType(),
				sensor.getSensorLocation());
		cpr = new CyclePatternRecognizer(monthlyCycle, sensor,
				new PredictionService(sensor));
		patternRecognizers.add(cpr);

		// Yearly
		YearlyCycle yearlyCycle = new YearlyCycle(sensor.getSensorType(), sensor.getSensorLocation());
		cpr = new CyclePatternRecognizer(yearlyCycle, sensor,
				new PredictionService(sensor));
		patternRecognizers.add(cpr);

		// Add a Transition pattern recognizer to the list for this sensor
		patternRecognizers.add(
				new TransitionPatternRecognizer(sensor, new PredictionService(sensor)));
		return patternRecognizers;
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
		List<SensorEventListener> listeners = eventListenersPerSensor.get(sensor);
		if (listeners == null) {

			// Listener is not registered for any sensor
			return false;
		} else if (listeners.contains(listener)) {

			// listener is registered to the given sensor
			return true;
		} else {

			// Not registered to the given sensor
			return false;
		}
	}

	/**
	 * This is a "factory method" design pattern based on the given sensor type and
	 * location. TODO We may want to return one selected from a list of the same
	 * type.
	 * 
	 * @param sensorType     is the type of sensor to create
	 * @param sensorLocation the location of the sensor to be created
	 * @return the default sensor of the requested type
	 */
	public Sensor getDefaultSensorForLocation(int sensorType, int sensorLocation) {

		Sensor sensor;
		if (sensorType <= 0)
			throw new IllegalArgumentException("sensorType must be positive");
		if (sensorLocation <= 0)
			throw new IllegalArgumentException("sensorLocataion must be positive");

		List<SensorEventListener> listeners;

		// Retrieve existing sensors matching type and location
		List<Sensor> sensors = allSensors.stream().filter(
				(s) -> (s.getSensorType() == sensorType) && (s.getSensorLocation() == sensorLocation))
				.collect(Collectors.toList());

		// Return the first located sensor
		if (!sensors.isEmpty()) {
			logger.info("Returning existing sensor: " + sensors.get(0));
			return sensors.get(0);
		}

		switch (sensorType) {

		case Sensor.TYPE_HEART_RATE:
			sensor = new HeartRateSensor(sensorLocation, Sensor.REPORTING_MODE_CONTINUOUS,
					false);
			allSensors.add(sensor);
			listeners = createPatternRecognizersForSensor(sensor);
			registerEventListenersForSensor(listeners, sensor);
			logger.info("Created new Heart Rate sensor");
			return sensor;

		case Sensor.TYPE_ACCELEROMETER:
			sensor = new AccelerometerSensor(sensorLocation,
					Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			listeners = createPatternRecognizersForSensor(sensor);
			registerEventListenersForSensor(listeners, sensor);
			logger.info("Creating Accelerometer sensor");
			return sensor;

		case Sensor.TYPE_SLEEP:
			sensor = new SleepSensor(sensorLocation, Sensor.REPORTING_MODE_CONTINUOUS,
					SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			listeners = createPatternRecognizersForSensor(sensor);
			registerEventListenersForSensor(listeners, sensor);
			logger.info("Creating Sleep monitoring sensor");
			return sensor;

		case Sensor.TYPE_BLOOD_PRESSURE:
			sensor = new BloodPressureSensor(sensorLocation,
					Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			listeners = createPatternRecognizersForSensor(sensor);
			registerEventListenersForSensor(listeners, sensor);
			logger.info("Creating blood pressure monitoring sensor");
			return sensor;

		case Sensor.TYPE_GLUCOSE:
			sensor = new GlucoseSensor(sensorLocation, Sensor.REPORTING_MODE_CONTINUOUS,
					SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			listeners = createPatternRecognizersForSensor(sensor);
			registerEventListenersForSensor(listeners, sensor);
			logger.info("Creating glucose monitoring sensor");
			return sensor;

		case Sensor.TYPE_TEMPERATURE:
			sensor = new TemperatureSensor(sensorLocation,
					Sensor.REPORTING_MODE_CONTINUOUS, SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			listeners = createPatternRecognizersForSensor(sensor);
			registerEventListenersForSensor(listeners, sensor);
			logger.info("Creating Temperature sensor");
			return sensor;

		case Sensor.TYPE_GPS:
			sensor = new GpsSensor(sensorLocation, Sensor.REPORTING_MODE_CONTINUOUS,
					SENSOR_DELAY_NORMAL, false);
			allSensors.add(sensor);
			listeners = createPatternRecognizersForSensor(sensor);
			registerEventListenersForSensor(listeners, sensor);
			logger.info("Creating GPS sensor");
			return sensor;
		default:
			throw new IllegalArgumentException(
					"Sensor type must be equal to one of the defined constants");
		}
	}

	private void registerEventListenersForSensor(List<SensorEventListener> listeners,
			Sensor sensor) {
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");
		if (listeners == null)
			throw new IllegalArgumentException("listeners must not be null");

		for (SensorEventListener sensorEventListener : listeners) {
			registerListenerForSensor(sensorEventListener, sensor);
		}
	}

	/**
	 * @return the eventListenersPerSensor
	 */
	public final List<SensorEventListener> getRegistrationsForSensor(Sensor sensor) {
		if (sensor == null)
			throw new IllegalArgumentException("sensor must not be null");

		List<SensorEventListener> listeners = eventListenersPerSensor.get(sensor);
		if (listeners != null)
			return listeners;
		else
			return new ArrayList<SensorEventListener>();
	}

	public void disableListenerRegistrations() {
		disabledEventListeners = eventListenersPerSensor;
		eventListenersPerSensor = new HashMap<Sensor, List<SensorEventListener>>();
	}

	public void enableListenerRegistrations() {
		eventListenersPerSensor = disabledEventListeners;
	}

	/**
	 * @return the predictionService
	 */
	public PredictionService getPredictionService() {
		return predictionService;
	}

	/**
	 * @param predictionService the predictionService to set
	 */
	public void setPredictionService(PredictionService predictionService) {
		if (predictionService == null)
			throw new IllegalArgumentException("predictionService must not be null");

		this.predictionService = predictionService;
	}

	/**
	 * @return the eventListenersPerSensor
	 */
	public HashMap<Sensor, List<SensorEventListener>> getEventListenersPerSensor() {
		return eventListenersPerSensor;
	}

	/**
	 * @return the disabledEventListeners
	 */
	HashMap<Sensor, List<SensorEventListener>> getDisabledEventListeners() {
		return disabledEventListeners;
	}

}