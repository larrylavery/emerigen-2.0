package com.emerigen.infrastructure.sensor;

import com.emerigen.infrastructure.utils.EmerigenProperties;

/**
 * This is the base class for all sensors. There are generally three types.
 * 
 * Motion sensors measure acceleration and rotational forces along three axes.
 * Some of the motion sensors are accelerometer for measuring shake, tilt, etc.,
 * proximity sensor for measuring proximity of an object relative to the screen
 * of the device, gyroscope for measuring spin, turn, etc., , gravity sensor for
 * measuring force of gravity and rotational vector for measuring device
 * orientation.
 * 
 * Environmental sensors measure environmental properties such as temperature,
 * pressure, light, humidity. Examples of environmental sensors are thermometer,
 * barometer, photometer used for controlling brightness, and humidity senor.
 * 
 * Position sensors help in calculating device position, for example
 * accelerometer and magneto meters are used to calculate device position.
 * Proximity sensor helps in finding closeness of the device to an object, hand
 * or face.
 * 
 * 
 * @author Larry
 *
 */
public class Sensor {

	/**
	 * Report events at a constant rate as defined by the sampling period
	 */
	// Sensor Modes
	final static int REPORTING_MODE_CONTINUOUS = 1;
	final static int REPORTING_MODE_ON_CHANGE = 2;

	// Sensor Types
	public static final int TYPE_ACCELEROMETER = 1;
	public static final int TYPE_HEART_RATE = 2;
	public static final int TYPE_TEMPERATURE = 4;

	// Sensor locations
	public final static int LOCATION_PHONE = 1;
	public final static int LOCATION_WATCH = 2;
	public final static int LOCATION_BODY = 4;
	public static final int LOCATION_CAR = 8;

	public static final int DELAY_NORMAL = 1;

	private static int minimumDelayBetweenReadings = Integer.parseInt(
			EmerigenProperties.getInstance().getValue("sensor.default.minimum.delay.between.readings.millis"));

	private int reportingMode;
	private boolean isWakUpSensor;
	private boolean activated = false;
	private int sensorType;
	private int sensorLocation;

	public Sensor(int sensorType, int sensorLocation, int reportingMode, int minimumDelayBetweenReadings,
			boolean isWakeUpSensor) {
		if (sensorType <= 0)
			throw new IllegalArgumentException("sensorType must be positive");
		if (sensorLocation <= 0)
			throw new IllegalArgumentException("sensorLocation must be positive");
		if ((reportingMode != REPORTING_MODE_CONTINUOUS) && (reportingMode != REPORTING_MODE_ON_CHANGE))
			throw new IllegalArgumentException("Reporting mode (" + reportingMode + ") is not valid");
		if (minimumDelayBetweenReadings < 0)
			throw new IllegalArgumentException("MinimumDelayBetweenReadings must not be negative");

		this.sensorType = sensorType;
		this.sensorLocation = sensorLocation;
		this.minimumDelayBetweenReadings = minimumDelayBetweenReadings;
		this.reportingMode = reportingMode;
		this.isWakUpSensor = isWakeUpSensor;

		// Enable this sensor to start producing events
		activate();
	}

	public Sensor(int sensorType, int sensorLocation, int reportingMode, boolean isWakeUpSensor) {
		this(sensorType, sensorLocation, reportingMode, minimumDelayBetweenReadings, isWakeUpSensor);
	}

	public int getReportingMode() {
		return reportingMode;
	}

	public boolean isActivated() {
		return activated;
	}

	/**
	 * Initialize the sensor. A Sensor starts publishing events after activation.
	 * 
	 * @return true if the sensor was successfully activated, otherwise false.
	 */
	public boolean activate() {
		// TODO figure what should happen for sensor activation
		this.activated = true;
		return true;
	}

	/**
	 * Quiesce this sensor. No events are published until the sensor has been
	 * activated.
	 */
	public boolean deactivate() {
		// TODO figure what should happen for sensor deactivation
		this.activated = false;
		return true;
	}

	/**
	 * @return the isWakUpSensor
	 */
	public boolean isWakUpSensor() {
		return isWakUpSensor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (activated ? 1231 : 1237);
		result = prime * result + (isWakUpSensor ? 1231 : 1237);
		result = prime * result + reportingMode;
		result = prime * result + sensorLocation;
		result = prime * result + sensorType;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sensor other = (Sensor) obj;
		if (activated != other.activated)
			return false;
		if (isWakUpSensor != other.isWakUpSensor)
			return false;
		if (reportingMode != other.reportingMode)
			return false;
		if (sensorLocation != other.sensorLocation)
			return false;
		if (sensorType != other.sensorType)
			return false;
		return true;
	}

	public int getType() {
		return sensorType;
	}

	public int getLocation() {
		return sensorLocation;
	}

	/**
	 * @return the minimumDelayBetweenReadings
	 */
	public int getMinimumDelayBetweenReadings() {
		return minimumDelayBetweenReadings;
	}

	/**
	 * @return the sensorType
	 */
	public int getSensorType() {
		return sensorType;
	}

	public String getTypeName() {

		switch (sensorType) {
		case TYPE_ACCELEROMETER:
			return "Accelerometer";

		case TYPE_HEART_RATE:
			return "HeartRate";

		case TYPE_TEMPERATURE:
			return "Temperature";

		default:
			return "No_SENSOR_TYPE";
		}

	}

	public String getLocationName() {

		switch (sensorLocation) {
		case LOCATION_BODY:
			return "Body";

		case LOCATION_PHONE:
			return "Phone";

		case LOCATION_WATCH:
			return "Watch";

		case LOCATION_CAR:
			return "Car";

		default:
			return "No_SENSOR_LOCATION";
		}
	}

	@Override
	public String toString() {
		return "Sensor [reportingMode=" + reportingMode + ", isWakUpSensor=" + isWakUpSensor + ", activated="
				+ activated + ", sensorType=" + sensorType + ", sensorLocation=" + sensorLocation + "]";
	}
}
