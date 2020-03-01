package com.emerigen.infrastructure.sensor;

import org.apache.log4j.Logger;

import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonIgnore;
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
	final public static int REPORTING_MODE_CONTINUOUS = 1;
	public final static int REPORTING_MODE_ON_CHANGE = 2;

	// Sensor Types
	public static final int TYPE_ACCELEROMETER = 1;
	public static final int TYPE_HEART_RATE = 2;
	public static final int TYPE_TEMPERATURE = 4;
	public static final int TYPE_GPS = 8;
	public static final int TYPE_SLEEP = 16;
	public static final int TYPE_BLOOD_PRESSURE = 32;
	public static final int TYPE_GLUCOSE = 64;

	// Sensor locations
	public final static int LOCATION_PHONE = 1;
	public final static int LOCATION_WATCH = 2;
	public final static int LOCATION_BODY = 4;
	public static final int LOCATION_CAR = 8;
	public static final int LOCATION_MACHINE = 16;

	public static final int DELAY_NORMAL = 0;
	public static final long nanoSecondsPerMilliSeconds = 1000000;

	private long minimumDelayBetweenReadings = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"))
			* nanoSecondsPerMilliSeconds;

	private int reportingMode;
	String type = "sensor";
	private boolean wakeUpSensor;
	@JsonIgnore
	private boolean activated = false;
//	@JsonIgnore
	private int sensorType;
//	@JsonIgnore
	private int sensorLocation;
	@JsonIgnore
	private String locationName;
	@JsonIgnore
	private String typeName;

	private static final Logger logger = Logger.getLogger(Sensor.class);

	public Sensor() {

	}

	public Sensor(int sensorType, int sensorLocation, int reportingMode,
			int minimumDelayBetweenReadings, boolean isWakeUpSensor) {
		if (sensorType <= 0)
			throw new IllegalArgumentException("sensorType must be positive");
		if (sensorLocation <= 0)
			throw new IllegalArgumentException("sensorLocation must be positive");
		if ((reportingMode != REPORTING_MODE_CONTINUOUS)
				&& (reportingMode != REPORTING_MODE_ON_CHANGE))
			throw new IllegalArgumentException(
					"Reporting mode (" + reportingMode + ") is not valid");
		if (minimumDelayBetweenReadings < 0)
			throw new IllegalArgumentException(
					"MinimumDelayBetweenReadings must not be negative");

		this.sensorType = sensorType;
		this.sensorLocation = sensorLocation;
		this.typeName = getTypeName(sensorType);
		this.locationName = getLocationName(sensorLocation);
		this.minimumDelayBetweenReadings = minimumDelayBetweenReadings;
		this.reportingMode = reportingMode;
		this.wakeUpSensor = isWakeUpSensor;

		// Enable this sensor to start producing events
		activate();
	}

	public Sensor(int sensorType, int sensorLocation, int reportingMode,
			boolean isWakeUpSensor) {
		this(sensorType, sensorLocation, reportingMode,
				Integer.parseInt(EmerigenProperties.getInstance().getValue(
						"sensor.default.minimum.delay.between.readings.millis")),
				isWakeUpSensor);
	}

	/**
	 * By default assume that a significant change has occurred. Subclasses should
	 * override this method as required.
	 * 
	 * @param previousSensorEvent
	 * @param currentSensorEvent
	 * @return
	 */
	public boolean significantChangeHasOccurred(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		return true;
	}

	/**
	 * 
	 * @param previousSensorEvent
	 * @param currentSensorEvent
	 * @param minDelayBetweenReadingsMillis
	 * @return true if elapse time between events exceeds the required minimum
	 */
	public boolean minimumDelayBetweenReadingsIsSatisfied(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		if (previousSensorEvent != null) {

			long currentTime = currentSensorEvent.getTimestamp();
			long previousTime = previousSensorEvent.getTimestamp();
			long elapsedTime = Math.abs(currentTime - previousTime);
			return elapsedTime >= minimumDelayBetweenReadings;
		} else
			return true;
	}

	/**
	 * Calculate the distance between the previous and current readings. Must be
	 * overriden by subclasses
	 * 
	 * @param previousGpsCoordinates
	 * @param currentGpsCoordinates
	 * @return
	 */
	public double getDifferenceBetweenReadings(SensorEvent previousSensorEvent,
			SensorEvent currentSensorEvent) {
		return 0.0;
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
		result = prime * result + (int) (minimumDelayBetweenReadings
				^ (minimumDelayBetweenReadings >>> 32));
		result = prime * result + reportingMode;
		result = prime * result + sensorLocation;
		result = prime * result + sensorType;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (wakeUpSensor ? 1231 : 1237);
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
		if (minimumDelayBetweenReadings != other.minimumDelayBetweenReadings)
			return false;
		if (reportingMode != other.reportingMode)
			return false;
		if (sensorLocation != other.sensorLocation)
			return false;
		if (sensorType != other.sensorType)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (wakeUpSensor != other.wakeUpSensor)
			return false;
		return true;
	}

	public int getSensorType() {
		return sensorType;
	}

	public int getSensorLocation() {
		return sensorLocation;
	}

	/**
	 * @return the minimumDelayBetweenReadings
	 */
	public long getMinimumDelayBetweenReadings() {
		return minimumDelayBetweenReadings;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getTypeName(int type) {

		switch (type) {
		case TYPE_SLEEP:
			return "Sleep";

		case TYPE_BLOOD_PRESSURE:
			return "BloodPressure";

		case TYPE_GLUCOSE:
			return "Glucose";

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
		return locationName;
	}

	public String getLocationName(int location) {

		switch (location) {
		case LOCATION_BODY:
			return "Body";

		case LOCATION_PHONE:
			return "Phone";

		case LOCATION_WATCH:
			return "Watch";

		case LOCATION_CAR:
			return "Car";

		case LOCATION_MACHINE: // CPAP, Glucose monitor, Blood pressure monitor
			return "Machine";

		default:
			return "No_SENSOR_LOCATION";
		}
	}

	@Override
	public String toString() {
		return "Sensor [minimumDelayBetweenReadings=" + minimumDelayBetweenReadings
				+ ", reportingMode=" + reportingMode + ", type=" + type
				+ ", wakeUpSensor=" + wakeUpSensor + ", activated=" + activated
				+ ", sensorType=" + sensorType + ", sensorLocation=" + sensorLocation
				+ ", locationName=" + locationName + ", typeName=" + typeName + "]";
	}

	/**
	 * Subclasses should override for sensor-specific equality
	 * 
	 * @param firstSensorEvent
	 * @param secondSensorEvent
	 * @return
	 */
	public boolean equals(SensorEvent firstSensorEvent, SensorEvent secondSensorEvent) {
		// TODO add code w Utils for fields we are interested in
		return firstSensorEvent.equals(secondSensorEvent);
	}

	/**
	 * @param reportingMode the reportingMode to set
	 */
	public void setReportingMode(int reportingMode) {
		this.reportingMode = reportingMode;
	}

	/**
	 * @param wakeUpSensor the wakeUpSensor to set
	 */
	public void setWakeUpSensor(boolean wakeUpSensor) {
		this.wakeUpSensor = wakeUpSensor;
	}

	/**
	 * @param locationName the locationName to set
	 */
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param minimumDelayBetweenReadings the minimumDelayBetweenReadings to set
	 */
	public void setMinimumDelayBetweenReadings(long minimumDelayBetweenReadings) {
		this.minimumDelayBetweenReadings = minimumDelayBetweenReadings;
	}

	/**
	 * @param activated the activated to set
	 */
	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	/**
	 * @param sensorType the sensorType to set
	 */
	public void setSensorType(int sensorType) {
		this.sensorType = sensorType;
	}

	/**
	 * @param sensorLocation the sensorLocation to set
	 */
	public void setSensorLocation(int sensorLocation) {
		this.sensorLocation = sensorLocation;
	}
}
