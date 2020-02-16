/**
 * 
 */
package com.emerigen.infrastructure.sensor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.repository.KnowledgeRepository;

/**
 * @author Larry
 *
 */
public class EmerigenSensorEventListener implements SensorEventListener {

	// My current sensors
	List<Sensor> sensors = new ArrayList<Sensor>();
	List<SensorEventListener> listeners = new ArrayList<SensorEventListener>();

	private final SensorManager sensorManager;

	private SensorEvent previousSensorEvent;

	private static final Logger logger = Logger.getLogger(EmerigenSensorEventListener.class);

	public EmerigenSensorEventListener() {
		sensorManager = SensorManager.getInstance();

	}

	@Override
	public List<Prediction> onSensorChanged(SensorEvent sensorEvent) {

		List<Prediction> predictions = new ArrayList<Prediction>();

		/**
		 * Send the event to each registered Listener for processing and accumulate the
		 * predictions.
		 */
		Set<Prediction> distinctPredictions = new HashSet<Prediction>();
		List<SensorEventListener> listeners = sensorManager
				.getRegistrationsForSensor(sensorEvent.getSensor());
		for (SensorEventListener sensorEventListener : listeners) {
			if (!(sensorEventListener instanceof EmerigenSensorEventListener)) {
				List<Prediction> newPredictions = sensorEventListener.onSensorChanged(sensorEvent);
				if (newPredictions != null)
					distinctPredictions.addAll(newPredictions);
//				distinctPredictions.addAll(sensorEventListener.onSensorChanged(sensorEvent));
			}
		}
		predictions.addAll(distinctPredictions);
		previousSensorEvent = sensorEvent;

		// Always log the new event
		KnowledgeRepository.getInstance().newSensorEvent(sensorEvent);
		return predictions;
	}

	@Override
	public void onPause() {

		// Disable, but save, all current registrations
		sensorManager.disableListenerRegistrations();
	}

	@Override
	public void onResume() {

		// Restore registrations on resumption
		sensorManager.enableListenerRegistrations();
	}

}
