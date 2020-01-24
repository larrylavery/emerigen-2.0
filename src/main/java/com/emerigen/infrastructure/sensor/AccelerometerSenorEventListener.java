package com.emerigen.infrastructure.sensor;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.utils.EmerigenProperties;

public class AccelerometerSenorEventListener implements SensorEventListener {

	private final SensorManager sensorManager;
	private AccelerometerSensor accelerometerSensor;

	private long currentTime;
	private long lastUpdateTime;

	private float last_x, last_y, last_z;
	private static final Logger logger = Logger.getLogger(AccelerometerSensor.class);

	private static final float SHAKE_THRESHOLD = Float
			.parseFloat(EmerigenProperties.getInstance().getValue("sensor.accelerometer.shake.threshold.millis"));

	private final int minDelayBetweenReadingsMillis = Integer.parseInt(
			EmerigenProperties.getInstance().getValue("sensor.default.minimum.delay.between.readings.millis"));

	public AccelerometerSenorEventListener() {
		sensorManager = SensorManager.getInstance();
		accelerometerSensor = (AccelerometerSensor) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	
	}
	
	@Override
	public void onCreate() {
		sensorManager.registerListenerForSensorWithFrequency(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public boolean onSensorChanged(SensorEvent sensorEvent) {

		// If the minimum delay has occurred then process the event
		Sensor sensor = sensorEvent.getSensor();

		if (sensor.getClass().isInstance(accelerometerSensor)) {

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

					//Update for next change event
					last_x = x;
					last_y = y;
					last_z = z;
					return true;
				}
				logger.info("The speed did not exceed the 'shake threshold' of " + SHAKE_THRESHOLD);
			}
		}

		// Minimum time lapse has not occurred, do nothing
		return false;
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
		sensorManager.registerListenerForSensorWithFrequency(this, accelerometerSensor,
				sensorManager.SENSOR_DELAY_NORMAL);
	}

}
