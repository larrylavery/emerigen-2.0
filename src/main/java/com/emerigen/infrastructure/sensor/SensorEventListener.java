package com.emerigen.infrastructure.sensor;

import java.util.List;

import com.emerigen.infrastructure.learning.Prediction;

public interface SensorEventListener {

	public List<Prediction> onSensorChanged(SensorEvent sensorEvent);

	public default void onPause() {
	}

	public default void onResume() {
	}

}
