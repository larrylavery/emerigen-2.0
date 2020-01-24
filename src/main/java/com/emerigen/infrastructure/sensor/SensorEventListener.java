package com.emerigen.infrastructure.sensor;

public interface SensorEventListener {

	public void onCreate();

	public boolean onSensorChanged(SensorEvent sensorEvent);

	public void onAccuracyChanged();

	public void onPause();

	public void onResume();

}
