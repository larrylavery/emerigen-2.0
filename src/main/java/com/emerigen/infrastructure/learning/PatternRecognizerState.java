package com.emerigen.infrastructure.learning;

import java.util.List;

import com.emerigen.infrastructure.sensor.SensorEvent;

public interface PatternRecognizerState {

	public List<Prediction> onSensorChanged(SensorEvent sensorEvent);

}
