
package com.emerigen.infrastructure.learning.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.emerigen.infrastructure.learning.Prediction;
import com.emerigen.infrastructure.learning.PredictionService;
import com.emerigen.infrastructure.sensor.EmerigenSensorEventListener;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.sensor.SensorEvent;
import com.emerigen.infrastructure.sensor.SensorEventListener;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class CPR_GeoFenceTest {

	final private long secondsPerYear = 31556952L;
	final private int daysPerWeek = 7;
	final private long hoursPerDay = 24;
	final private long minutesPerHour = 60;
	final private long secondsPerMinute = 60;
	final private long milliSecondsPerSecond = 1000;
	final private long nanoSecondsPerMillisecond = 1000000;

	private static long defaultCycleDurationNano = Long.parseLong(EmerigenProperties
			.getInstance().getValue("cycle.default.data.point.duration.nano"));

	private long minimumDelayBetweenReadings = Long
			.parseLong(EmerigenProperties.getInstance()
					.getValue("sensor.default.minimum.delay.between.readings.millis"))
			* 1000000;

	private long defaultDataPointDurationNano = Long.parseLong(EmerigenProperties
			.getInstance().getValue("cycle.default.data.point.duration.nano"));

	private double allowablePercentDifferenceForEquality = Double
			.parseDouble(EmerigenProperties.getInstance()
					.getValue("cycle.allowable.percent.difference.for.equality"));

	@Mock
	PredictionService mockPredictionService;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	public static Cycle createCycle(String cycleType, int sensorType,
			int sensorLocation) {
		Cycle cycle;

		if ("Daily".equals(cycleType)) {
			cycle = new DailyCycle(sensorType, sensorLocation);
		} else if ("Weekly".equals(cycleType)) {
			cycle = new WeeklyCycle(sensorType, sensorLocation);
		} else if ("Monthly".equals(cycleType)) {
			cycle = new MonthlyCycle(sensorType, sensorLocation);
		} else if ("Yearly".equals(cycleType)) {
			cycle = new YearlyCycle(sensorType, sensorLocation);
		} else {
			throw new IllegalArgumentException(
					"cycle type must be valid, but was (" + cycleType + ")");
		}
		cycle.setCycleType(cycleType);

		return cycle;
	}

	@Test
	public final void givenNewCycle_whenCreated_thenCycleEventReferencesHaveDefaultCycleStartValues() {
		/**
		 * default values: permenent Event, TempEvent
		 * 
		 */
		Cycle cycle = createCycle("Daily", 2, 2);
		assertThat(cycle.getTemporarySensorEvent()).isNotNull();
		assertThat(cycle.getTemporarySensorEvent().getTimestamp())
				.isEqualTo(cycle.getCycleStartTimeNano());
		assertThat(cycle.getPermanentSensorEvent()).isNotNull();
		assertThat(cycle.getPermanentSensorEvent().getTimestamp())
				.isEqualTo(cycle.getCycleStartTimeNano());
	}

	@Test
	public final void givenNewEventToExistingDestablizedCycle_whenCycleStablized_thenEventReferencesAreAdjusted() {
		fail("Not yet implemented");
		/**
		 * When a cycle has remained at a specific data point for a 
		 * configurable amount of time, Adjust permEvent to temporary event,
		 * set temp to current.
		 * Attach route to permanent event 
		 */

		Cycle cycle = createCycle("daily", 2, 2);
		
		//Construct and send insignificant event changes via onSensorChanged
		
		//Verify the appropriate reference adjustments
		SensorEvent permanentEvent = cycle.getCurrentEvent();
		SensorEvent temporaryEvent = cycle.getCurrentEvent();
		assertThat(permanentEvent.equals(cycle.getCurrentEvent()).isTrue();
		assertThat(temporaryEvent.equals(cycle.getCurrentEvent()).isTrue();
		
	}

	@Test
	public final void givenBuiltCycle_whenEventAccumulatedThatDiffersFromNextDestination_thenAddNewCycleDestinationAndRoute() {
		fail("Not yet implemented");

		// Given a previuosly built geo fence

		// When an Event accumulated with significantly different destination

		// then add new destination
	}

	@Test
	public final void givenBuiltCycle_whenEventAccumulatedThatRouteDiffersFromExisting_thenMergeRoutes() {
		fail("Not yet implemented");

		// Given a previuosly built geo fence

		// When an Event accumulated with significantly different route

		// then merge routes
	}

	@Test
	public final void givenRoute__whenInvalidDestinationsAdded_thenIllegalArgumentinvxceptionThrown() {
		fail("Not yet implemented");

		// Given a route

		// When Valid destinations are added (invalid gps locations)

		// then IllegalArgumentExcepption thrown
	}

	@Test
	public final void givenRoute__whenValidDestinationsAdded_thenNoExceptionThrown() {
		fail("Not yet implemented");

		// Given a route

		// When Valid destinations are added

		// then No exceptions thrown
	}

	@Test
	public final void givenNewRoute__whenDestinationsIterated_thenNoneReturned() {
		fail("Not yet implemented");

		// Given an empty route

		// When its destinations are iterated

		// then No destinations are retrieved
	}

	@Test
	public final void givenBuiltRoute__whenDestinationsIterated_thenDestinationsAreRetrievedInOrder() {
		fail("Not yet implemented");

		// Given a previuosly built route

		// When its destinations are iterated

		// then the destinations are retrieved in travel order
	}

	@Test
	public final void givenNewRoute__whenDestinationAdded_thenRetrievedAndValidatedOK() {
		fail("Not yet implemented");

		// Given a previuosly built route

		// When a GPS location is added to the route

		// then it can be retrieved and validated OK
	}

	@Test
	public final void givenBuiltCycle_whenEventAfterLastDestinationOfCycle_thenSetTempAndMerge() {
		fail("Not yet implemented");

		// Given a previuosly built geo fence

		// When an significantly different event arrives after 5am (last event of cycle)

		// then set as temporaryEvent ans start merging until stabilized
	}

	@Test
	public final void givenBuiltCycle_whenAtWorkAndSignificantlyDifferentDataPpoint_thenPredictNextDestinationAndSuggestRoute() {
		fail("Not yet implemented");

		// Given a previuosly built geo fence

		// When an significantly different event arrives at 10amm

		// then predict lunch datapoints and suggest route
	}

	@Test
	public final void givenBuiltCycle_whenEventBetweenMidnightAnd6amConsumed_thenTheFollowingEventAndRoutePredicted() {
		fail("Not yet implemented");

		// Given a previuosly built geo fence

		// When an event arrives for 4am (between yesterday at 5pm and 6am today

		// then the 6am event and route is returned as a pridection
	}

	@Test
	public final void givenNewEventToExistingDestablizedCycle_whenCycleStablized_thenRouteHasBeenAddedToCycleEvent() {
		fail("Not yet implemented");
		/**
		 * When a cycle has remained at a specific data point for a configurable amount
		 * of time, a route from the last major destination is added to the cycle.
		 * Verify its contents
		 */

		Cycle cycle = createCycle("daily", 2, 2);

		// Save current permanent event
		SensorEvent permanentEvent = cycle.getPermanentSensorEvent();

		// Construct and send insignificant event changes via onSensorChanged

		// Verify the appropriate reference adjustments
		assertThat(cycle.getPermanentSensorEvent().hasRoute()).isTrue();

		// Verify route data
		Route route = permanentEvent.getRoute();
		assertThat(route.size()).isEqual(xxx);

	}

	@Test
	public final void givenFirstSignificantDataPointChange_whenOccurs_thenEventReferencesAreAdjusted() {
		fail("Not yet implemented");
		/**
		 * When a significant data point change occurs, update references,
		 * update permEvent duration. update tempEvent, 
		 */

		Cycle cycle = createCycle("daily", 2, 2);
		
		//Construct and send significant change event via onSensorChanged
		
		//Verify the appropriate reference adjustments
		SensorEvent permanentEvent = cycle.getPermanentSensorEvent();
		SensorEvent temporaryEvent = cycle.getTemporarySensorEvent();
		assertThat(!permanentEvent.equals(temporaryEvent)).isTrue();
		assertThat(temporaryEvent.equals(cycle.getCurrentEvent()).isTrue();
		
		//Verify that permanent Event ref duration has been updated
		long expectedDuration = cycle.getCurrentEvent().getTimestamp()- permanentEvent.getTimestamp();
		assertThat(permanentEvent.getDataPointDuration()).isEqualTo(expectedDuration);
		
	}

	@Test
	public final void givenFiveEventsWithEqualDataPoints_whenConsumed_thenPermEventDurationAccumulatesDuration() {

		/**
		 * When we stay at the same data point, then temp sensorEvent reference
		 * accumulates duration
		 * 
		 */
		Cycle cycle = createCycle("Daily", 2, 2);
		SensorEventListener listener = new EmerigenSensorEventListener();
		long initialDuration = cycle.getPermanentSensorEvent().getDataPointDurationNano();

		// Construct and send 5 insignificant change events via onSensorChanged
		Sensor hrSensor = SensorManager.getInstance().getDefaultSensorForLocation(
				Sensor.TYPE_HEART_RATE, Sensor.LOCATION_PHONE);

		Random rd = new Random(); // creating Random object
		float[] values = new float[] { rd.nextFloat(), rd.nextFloat() };
		SensorEvent event1 = new SensorEvent(hrSensor, values);
		SensorEvent event2 = new SensorEvent(hrSensor, values);
		event2.setTimestamp(event1.getTimestamp() + minimumDelayBetweenReadings);
		SensorEvent event3 = new SensorEvent(hrSensor, values);
		event3.setTimestamp(event2.getTimestamp() + minimumDelayBetweenReadings);
		SensorEvent event4 = new SensorEvent(hrSensor, values);
		event4.setTimestamp(event3.getTimestamp() + minimumDelayBetweenReadings);
		SensorEvent event5 = new SensorEvent(hrSensor, values);
		event5.setTimestamp(event4.getTimestamp() + minimumDelayBetweenReadings);

		List<Prediction> predictions = listener.onSensorChanged(event1);
		predictions = listener.onSensorChanged(event2);
		predictions = listener.onSensorChanged(event3);
		predictions = listener.onSensorChanged(event4);
		predictions = listener.onSensorChanged(event5);

		// Calculate what the final duration should be
		long expectedDuration = initialDuration + (5 * defaultDataPointDurationNano);

		// Verify that temp event duration has accumulated durations
		assertThat(cycle.getPermanentSensorEvent().getDataPointDurationNano())
				.isEqualTo(expectedDuration);

	}

	@Test
	public final void givenFirstEventOfCurrentCycle_whenCreated_thenEventHasDefaultValues() {
		fail("Not yet implemented");

		/**
		 * default values: default dataPointDuration
		 * 
		 */
		Cycle cycle = createCycle("Daily", 2, 2);

		// Send new event to onSensorChanged

		// Validate results
		assertThat(cycle.getCycleDurationTimeNano()).isEqualTo(xxx);
		assertThat(cycle.getCycleStartTimeNano()).isEqualTo(xxx);
		assertThat(cycle.getTemporarySensorEvent()).isEqualTo(cycle.getCurrentEvent());
		assertThat(cycle.getPermanentSensorEvent()).isEqualTo(cycle.getCurrentEvent());

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
