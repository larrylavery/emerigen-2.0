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
	public static final int TYPE_GPS = 8;

	// Sensor locations
	public final static int LOCATION_PHONE = 1;
	public final static int LOCATION_WATCH = 2;
	public final static int LOCATION_BODY = 4;
	public static final int LOCATION_CAR = 8;

	public static final int DELAY_NORMAL = 1;

	private int minimumDelayBetweenReadings = Integer.parseInt(EmerigenProperties.getInstance()
			.getValue("sensor.default.minimum.delay.between.readings.millis"));

	private int reportingMode;
	private boolean wakeUpSensor;
	private boolean activated = false;
	private int type;
	private int location;
	private String locationName;
	private String typeName;

	public Sensor() {

	}

	public Sensor(int sensorType, int sensorLocation, int reportingMode,
			int minimumDelayBetweenReadings, boolean isWakeUpSensor) {
		if (sensorType <= 0)
			throw new IllegalArgumentException("type must be positive");
		if (sensorLocation <= 0)
			throw new IllegalArgumentException("location must be positive");
		if ((reportingMode != REPORTING_MODE_CONTINUOUS)
				&& (reportingMode != REPORTING_MODE_ON_CHANGE))
			throw new IllegalArgumentException(
					"Reporting mode (" + reportingMode + ") is not valid");
		if (minimumDelayBetweenReadings < 0)
			throw new IllegalArgumentException("MinimumDelayBetweenReadings must not be negative");

		this.type = sensorType;
		this.location = sensorLocation;

		this.minimumDelayBetweenReadings = minimumDelayBetweenReadings;
		this.reportingMode = reportingMode;
		this.wakeUpSensor = isWakeUpSensor;

		// Enable this sensor to start producing events
		activate();
	}

	public Sensor(int sensorType, int sensorLocation, int reportingMode, boolean isWakeUpSensor) {
		this(sensorType, sensorLocation, reportingMode,
				Integer.parseInt(EmerigenProperties.getInstance()
						.getValue("sensor.default.minimum.delay.between.readings.millis")),
				isWakeUpSensor);
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
	public boolean isWakeUpSensor() {
		return wakeUpSensor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (activated ? 1231 : 1237);
		result = prime * result + (wakeUpSensor ? 1231 : 1237);
		result = prime * result + reportingMode;
		result = prime * result + location;
		result = prime * result + type;
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
		if (wakeUpSensor != other.wakeUpSensor)
			return false;
		if (reportingMode != other.reportingMode)
			return false;
		if (location != other.location)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public int getType() {
		return type;
	}

	public int getLocation() {
		return location;
	}

	/**
	 * @return the minimumDelayBetweenReadings
	 */
	public int getMinimumDelayBetweenReadings() {
		return minimumDelayBetweenReadings;
	}

	public String getTypeName() {

		switch (type) {
		case TYPE_ACCELEROMETER:
			return "Accelerometer";

		case TYPE_HEART_RATE:
			return "HeartRate";

		case TYPE_TEMPERATURE:
			return "Temperature";

		case TYPE_GPS:
			return "GPS";

		default:
			return "No_SENSOR_TYPE";
		}

	}

	public String getLocationName() {

		switch (location) {
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
		return "Sensor [minimumDelayBetweenReadings=" + minimumDelayBetweenReadings
				+ ", reportingMode=" + reportingMode + ", wakeUpSensor=" + wakeUpSensor
				+ ", activated=" + activated + ", type=" + type + ", location=" + location
				+ ", locationName=" + locationName + ", typeName=" + typeName + "]";
	}
}
