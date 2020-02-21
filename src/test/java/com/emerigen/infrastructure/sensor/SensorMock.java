package com.emerigen.infrastructure.sensor;

import static org.junit.Assert.fail;

import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.opencsv.CSVReader;

public class SensorMock {

	private final Sensor sensor;
	private SensorEventListener listener;
	private int minimumDelayBetweenReadings;
	private int pauseResumeInterval;
	private CSVReader reader;
	private String[] line;

	private static Logger logger = Logger.getLogger(SensorMock.class);

	public SensorMock(Sensor sensor, SensorEventListener listener,
			int minimumDelayBetweenReadings, int pauseResumeInterval) {
		this.sensor = sensor;
		this.listener = listener;
		this.minimumDelayBetweenReadings = minimumDelayBetweenReadings;
		this.pauseResumeInterval = pauseResumeInterval;

		prepareSensorEventStream(sensor);

		// startGeneratingSensorEvents(minimumDelayBetweenReadings);
	}

	public void prepareSensorEventStream(Sensor sensor) {

		// filename pattern example: "/Phone-HeartRate-sensor-events.csv"
		String sensorEventFile = sensor.getLocationName() + "-" + sensor.getTypeName()
				+ "-" + "sensor-events.csv";
		try {
			reader = new CSVReader(new FileReader(sensorEventFile));

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Unable to initialize sensor event stream with filename ("
							+ sensorEventFile + "), ex: " + e + ". exiting.");
		}
	}

	public void startGeneratingSensorEvents() {

		SensorEvent newSensorEvent = getNextSensorEvent();
		while (newSensorEvent != null) {
			logger.info("newSensorEvent: " + newSensorEvent);

			// Push event to my sensor
			listener.onSensorChanged(newSensorEvent);

			// sleep for the specified minimum sensor reading delay
			try {
				Thread.sleep(minimumDelayBetweenReadings);
			} catch (InterruptedException e) {
				// Ignore exceptions but print the stack trace
				e.printStackTrace();
			}

			// Get the next sensorEvent
			newSensorEvent = getNextSensorEvent();
		}
		// End of sensor events
		logger.info("End of sensor events");

	}

	/**
	 * @return the next sensory event for the listener or null if done
	 */
	public SensorEvent getNextSensorEvent() {

		float[] values;
		SensorEvent sensorEvent = null;

		try {

			// If there is another sensory event
			if ((line = reader.readNext()) != null) {

				// Convert it to an array of floats
				values = new float[line.length];
				for (int i = 0; i < line.length; i++) {
					values[i] = Float.parseFloat(line[i]);
				}

				// Create the SensorEvent
				sensorEvent = new SensorEvent(sensor, values);
				logger.info("Next sensor event: " + sensorEvent);
				return sensorEvent;
			}
			logger.info("End of sensor events for Sensor : " + sensor);
			return sensorEvent;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Unexpected error reading sensor event file. ex: " + e.getMessage());
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// Would remove all sensor related records, but indexer rollback occurs
//		CouchbaseRepository.getInstance().removeAllDocuments("sensor-event");
//		CouchbaseRepository.getInstance().removeAllDocuments("transition");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testIsActivated() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testSetActivated() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testActivate() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testDeactivate() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetMinimumDelay() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testIsWakUpSensor() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetType() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetLocation() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetMinDelayBetweenReadingsMillis() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetSensorType() {
		fail("Not yet implemented"); // TODO
	}

}
