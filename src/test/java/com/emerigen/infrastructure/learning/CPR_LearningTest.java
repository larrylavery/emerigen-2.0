package com.emerigen.infrastructure.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Random;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorManager;

public class CPR_LearningTest {

	public static Cycle createCycle(String cycleType, int sensorType, int sensorLocation, int numberOfNodes) {
		Cycle cycle;

		if ("Daily".equals(cycleType))
			cycle = new DailyCycle(sensorType, sensorLocation);
		else if ("Weekly".equals(cycleType))
			cycle = new WeeklyCycle(sensorType, sensorLocation);
		else if ("Monthly".equals(cycleType))
			cycle = new MonthlyCycle(sensorType, sensorLocation);
		else if ("Yearly".equals(cycleType))
			cycle = new YearlyCycle(sensorType, sensorLocation);
		else
			throw new IllegalArgumentException("cycle type must be valid, but was (" + cycleType + ")");

		// Set attributes
		cycle.setPreviousCycleNodeIndex(0);
		CycleNode cycleNode;

		for (int i = 0; i < numberOfNodes; i++) {
			cycleNode = new CycleNode();
			int minimumDelayBetweenReadings;
			int reportingMode;
			boolean wakeUpSensor;
			SensorEvent sensorEvent = new SensorEvent();
			sensorEvent.setTimestamp(i * sensorEvent.getTimestamp());
			Sensor sensor;

			// Create SensorEvent
			sensorEvent.setSensorType(sensorType);
			sensorEvent.setSensorLocation(sensorLocation);

			// Set sensor event values
			float[] values = { 1.0f + (i + 1) * 100.0f, 2.0f + (i + 1) * 100.0f };
			sensorEvent.setValues(values);

			// create and set event sensor
			minimumDelayBetweenReadings = Sensor.DELAY_NORMAL;
			reportingMode = Sensor.REPORTING_MODE_ON_CHANGE;
			wakeUpSensor = false;
			sensor = SensorManager.getInstance().getDefaultSensorForLocation(sensorType, sensorLocation);
			sensor.setMinimumDelayBetweenReadings(minimumDelayBetweenReadings);
			sensor.setWakeUpSensor(wakeUpSensor);
			sensor.setReportingMode(reportingMode);
			sensorEvent.setSensor(sensor);

			// Set up the rest of the CycleNode fields
			cycleNode.setSensorEvent(sensorEvent);
			cycleNode.setStartTimeOffsetNano(100 * (i + 1));
			cycleNode.setDataPointDurationNano(1000 * (i + 1));
			cycleNode.setProbability(0.3);
			cycleNode.setMyCycle(cycle);
			cycle.addCycleNode(cycleNode);
		}
		return cycle;
	}

	@Test
	public final void givenExistingDefaultSensor_whenRetrieved_thenAllCyclePatternRecognizersAreRegistered() {
		Cycle cycle = createCycle("Daily", Sensor.TYPE_HEART_RATE, Sensor.LOCATION_WATCH, 1);

		KnowledgeRepository.getInstance().newCycle(UUID.randomUUID().toString(), cycle);
		SensorManager sm = SensorManager.getInstance();
		Sensor sensor = sm.getDefaultSensorForLocation(Sensor.TYPE_HEART_RATE, Sensor.LOCATION_WATCH);
		CyclePatternRecognizer PR = new CyclePatternRecognizer(cycle, new PredictionService(sensor));

		assertThat(sm.listenerIsRegisteredToSensor(PR, sensor)).isTrue();
	}

	@Test
	public final void givenValidCycleNodes_whenDurationsAdded_thenMustBeLessThanCycleDuration() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void givenNonEmptyCycle_whenSignificantlyDifferentEventDataPointArrives_thenNewNodeAddedToEndOfCycle() {
		Sensor gpsSensor = SensorManager.getInstance().getDefaultSensorForLocation(Sensor.TYPE_GPS,
				Sensor.LOCATION_PHONE);
		Cycle gpsCycle = new DailyCycle(Sensor.TYPE_GPS, Sensor.LOCATION_PHONE);
		CyclePatternRecognizer cpr = new CyclePatternRecognizer(gpsCycle, new PredictionService());
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(0);

		// When
		// gps sensors require lat and long floats
		Random rd = new Random();
		float[] values = { rd.nextFloat(), rd.nextFloat() };
		float[] values2 = { rd.nextFloat() + 10, rd.nextFloat() + 10 };
		SensorEvent event1 = new SensorEvent(gpsSensor, values);
		SensorEvent event2 = new SensorEvent(gpsSensor, values2);
		cpr.onSensorChanged(event1);
		cpr.onSensorChanged(event2);

		// Then
		assertThat(gpsCycle.getNodeList().size()).isEqualTo(2);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
