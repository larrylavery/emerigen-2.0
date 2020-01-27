package com.emerigen.infrastructure.sensor;

public interface SensorEventListener {

	public boolean onSensorChanged(SensorEvent sensorEvent);

	public void onPause();

	public void onResume();

}
