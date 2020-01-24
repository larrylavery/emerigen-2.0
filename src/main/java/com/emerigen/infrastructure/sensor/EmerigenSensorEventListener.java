/**
 * 
 */
package com.emerigen.infrastructure.sensor;

import org.apache.log4j.Logger;

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
	private float last_x, last_y, last_z;
	private static final float SHAKE_THRESHOLD = Float
			.parseFloat(EmerigenProperties.getInstance().getValue("sensor.accelerometer.shake.threshold.millis"));

	private static final Logger logger = Logger.getLogger(EmerigenSensorEventListener.class);

	private final int minDelayBetweenReadingsMillis = Integer.parseInt(
			EmerigenProperties.getInstance().getValue("sensor.default.minimum.delay.between.readings.millis"));

	public EmerigenSensorEventListener() {
		sensorManager = SensorManager.getInstance();
		heartRateSensor = (HeartRateSensor) sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
		accelerometerSensor = (AccelerometerSensor) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		temperatureSensor = (TemperatureSensor) sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
	}

	@Override
	public void onCreate() { //TODO create these in the activate method??
		sensorManager.registerListenerForSensorWithFrequency(this, heartRateSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, temperatureSensor,
				SensorManager.SENSOR_DELAY_NORMAL);

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
		
		//Not a sensor we are interested in
		return false;

	}

	private boolean processAccelerometerChange(SensorEvent sensorEvent) {

		// determine the elapse time since the last onChanged processed
		currentTime = System.currentTimeMillis();
		long elapseTime = currentTime - lastUpdateTime;
		if (elapseTime > minDelayBetweenReadingsMillis) {
			logger.info("The minimum elapse time millis (" + elapseTime
					+ ") since the last sensor change has occurred. Processing event: " + sensorEvent);

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
			
			//Minimum delay has not occurred
			return false;
		}
	}

	private boolean processHeartRateChanged(SensorEvent sensorEvent) {

		// determine the elapse time since the last onChanged processed
		currentTime = System.currentTimeMillis();
		long elapseTime = currentTime - lastUpdateTime;

		if (elapseTime > minDelayBetweenReadingsMillis) {
			logger.info("The minimum elapse time millis (" + elapseTime
					+ ") since the last sensor change has occurred. Processing event: " + sensorEvent);

			// Capture the current heart rate
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

			lastUpdateTime = currentTime;
			return true;
		} else {
			// Minimum time lapse has not occurred, do nothing
			return false;
		}
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
		sensorManager.registerListenerForSensorWithFrequency(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListenerForSensorWithFrequency(this, temperatureSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

}
