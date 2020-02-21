package com.emerigen.infrastructure.environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.everit.json.schema.ValidationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.log4j.Logger;

import com.emerigen.infrastructure.environment.Environment;
import com.emerigen.infrastructure.evaporation.InformationWithRelevanceHolder;

public class EnvironmentTest {

	private Environment env = Environment.getInstance();
	private static Logger logger = Logger.getLogger(EnvironmentTest.class);

	@Test
	public final void givenOneAgentCreated_whenGetNeighbors_thenNullReturned() {
		List<Agent> objs = Environment.getInstance().createAgents(1);
		List<Agent> newAgents = Environment.getInstance().getNeighbors(objs.get(0));

		then(newAgents).isNotNull().isEmpty();
	}

	@Test
	public final void givenManyAgentNeighborsInitialized_whenGetNeighbors_thenAllNeighborsAreRetrieved() {
		// Given
		Agent a1 = Environment.getInstance().createAgentAtLocation(new Location(4, 4));
		Environment.getInstance().createAgentAtLocation(new Location(3, 5));
		Environment.getInstance().createAgentAtLocation(new Location(3, 4));
		Environment.getInstance().createAgentAtLocation(new Location(3, 3));
		Environment.getInstance().createAgentAtLocation(new Location(5, 3));
		Environment.getInstance().createAgentAtLocation(new Location(5, 4));
		Environment.getInstance().createAgentAtLocation(new Location(5, 5));
		// List<Agent> agents = neighborhood.createAgents(2);

		// When neighbors requested
		List<Agent> neighbors = Environment.getInstance().getNeighbors(a1, 7);

		then(neighbors).isNotNull().isNotEmpty();
		then(neighbors.size()).isEqualTo(6);
	}

	@Test
	public final void givenAgentCreatedAndMoved_whenQueried_thenRetrieved() {

		// Given
		List<Agent> objs = Environment.getInstance().createAgents(1);

		// When moveTo() requested
		List<Agent> returnedAgents = Environment.getInstance().allAgents();
		Location newLocation = new Location(1, 0);
		Location oldLocation = (Location) objs.get(0).getLocation();
		Environment.getInstance().moveTo(objs.get(0), newLocation);

		then(returnedAgents).isNotNull().isNotEmpty().contains(objs.get(0));
	}

	@Test
	public final void givenMultipleAgentsCreated_whenListed_thenRetrieved() {

		Environment env = Environment.getInstance();
		env.initializeData();

		// Given
		List<Agent> agents = env.createAgents(2);
		List<Agent> agentsAfterRetrieving = env.allAgents();

		assertThat(agentsAfterRetrieving).isNotNull().contains(agents.get(0))
				.contains(agents.get(1));
		assertThat(agentsAfterRetrieving.size()).isEqualTo(2);

	}

	@Test
	public final void givenNonPositiveCount_whenGetNeighborsWithCountCalled_thenIllegalArgumentException() {

		// When the getNeighbors called
		final Throwable throwable = catchThrowable(
				() -> Environment.getInstance().getNeighbors(new Agent(), -4));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("A non-positive count should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullAgent_whenGetNeighborsWithCountCalled_thenIllegalArgumentException() {

		// When the getNeighbors called
		final Throwable throwable = catchThrowable(
				() -> Environment.getInstance().getNeighbors(null, 1));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("A null agent should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullAgent_whenGetNeighborsCalled_thenIllegalArgumentException() {

		// When the getNeighbors called
		final Throwable throwable = catchThrowable(
				() -> Environment.getInstance().getNeighbors(null, (obj1 -> {
					return null;
				})));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("A null agent should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullFunction_whenGetNeighborsCalled_thenIllegalArgumentException() {

		// When the getNeighbors called
		final Throwable throwable = catchThrowable(
				() -> Environment.getInstance().getNeighbors(new Agent(), null));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("A null function should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}
// TODO implement test when function support is added to the parameters
//	@Test
//	public final void givenNullFunction_whenGetNeighborsWithCountCalled_thenIllegalArgumentException() {
//		
//		// When the getNeighbors called
//		final Throwable throwable = catchThrowable(() -> Environment.getInstance()
//				.getNeighbors(new Agent() , 1, null));
//
//		// Then a IllegalArgumentException should be thrown
//		then(throwable).as("A null function should throw a IllegalArgumentException")
//		.isInstanceOf(IllegalArgumentException.class);
//
//	}

	@Test
	public final void givenMultipleAgentsCreated_whenGetNeighborsWithSequenceFunctionAndCount_thenNeighborsInOrderReturned() {
		// This test case is NOT needed now! if some client of the NeighborhoodImpl
		// requires
		// it, then it will be implemented at that time.
		logger.warn(
				"NOT NEEDED AT THIS TIME: givenMultipleAgentsCreated_whenGetNeighborsWithSequenceFunctionAndCount_thenNeighborsInOrderReturned");
		return;
	}

	@Test
	public final void givenObjWithRelevanceSet_whenGetWithNullKey_thenIllegalArgumentException() {
		// Given - objectWithRelevance
		String uuid = UUID.randomUUID().toString();
		env.setInformationWithRelevance(uuid, "info");

		// When - getInfo called with null or empty key
		final Throwable throwable = catchThrowable(
				() -> env.getInformationWithRelevance(null));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("An empty key should throw IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenObjWithRelevanceSet_whenGetWithEmptyKey_thenIllegalArgumentException() {
		// Given - objectWithRelevance
		String uuid = UUID.randomUUID().toString();
		env.setInformationWithRelevance(uuid, "info");

		// When - getInfo called with null or empty key
		final Throwable throwable = catchThrowable(
				() -> env.getInformationWithRelevance(""));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("An empty key should throw IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public void givenObjWithRelevance_whenEmptyKeySet_thenIllegalArgumentException() {

		// Given - objectWithRelevance
		String uuid = UUID.randomUUID().toString();
		env.setInformationWithRelevance(uuid, "info");

		// When - setInfo called with null object
		final Throwable throwable = catchThrowable(
				() -> env.setInformationWithRelevance("", "info"));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("An empty key should throw IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public void givenObjWithRelevance_whenNullKeySet_thenIllegalArgumentException() {

		// Given - objectWithRelevance

		// When - setInfo called with null object
		final Throwable throwable = catchThrowable(
				() -> env.setInformationWithRelevance(null, "info"));

		// Then a IllegalArgumentException should be thrown
		then(throwable).as("A null key should throw IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public void givenObjWithRelevanceSet_whenGetWithLessThanMinRelevance_thenNullIsReturned() {
		String uuid = UUID.randomUUID().toString();
		env.setInformationWithRelevance(uuid, "info");

		Object obj = env.getInformationWithMinimumRelevance(uuid, 0.4);
		assertThat(obj).isNotNull();
	}

	@Test
	public void givenObjWithRelevanceSet_whenGetWithGreaterThanMinRelevance_thenNullIsReturned() {
		String uuid = UUID.randomUUID().toString();
		env.setInformationWithRelevance(uuid, "info");
		Object obj = env.getInformationWithMinimumRelevance(uuid, 1.2);
		assertThat(obj).isNull();
	}

	@Test
	public void givenNoObjWithRelevanceSet_whenGet_thenNullIsReturned() {
		String uuid = UUID.randomUUID().toString();
		Object obj = env.getInformationWithRelevance(uuid);
		assertThat(obj).isNull();
	}

	@Test
	public void givenObjWithRelevance_whenSet_thenItshouldBeRetrievedOK() {
		String uuid = UUID.randomUUID().toString();
		env.setInformationWithRelevance(uuid, "info");
		Object obj = env.getInformationWithRelevance(uuid);
		assertThat(obj).isNotNull().isEqualTo("info");
	}

	@Test
	public final void givenNonPositiveNumberToCreate_whenCreateObjectCalled_thenIllegalArgumentException() {
		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(5);
		// When
		final Throwable throwable = catchThrowable(
				() -> Environment.getInstance().createAgentsOnRandomLocations(-1));

		then(throwable)
				.as("A non positive object Count should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		env.initializeData();
	}

	@After
	public void tearDown() throws Exception {
	}
}
