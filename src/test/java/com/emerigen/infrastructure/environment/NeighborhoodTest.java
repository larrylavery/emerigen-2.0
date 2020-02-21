package com.emerigen.infrastructure.environment;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.log4j.Logger;

public class NeighborhoodTest {

	private static Logger logger = Logger.getLogger(NeighborhoodTest.class);

	@Test
	public final void givenNeighborhoodWithMultipleObjects_whenMoveToMostConcentratedArea_thenNewLaocationShouldHaveTheMostNeighbors() {
		// This test case is NOT needed now! if some client of the NeighborhoodImpl
		// requires
		// it, then it will be implemented at that time.
		logger.warn(
				"NOT NEEDED AT THIS TIME: givenNeighborhoodWithMultipleObjects_whenMoveToMostConcentratedArea_thenNewLaocationShouldHaveTheMostNeighbors");
		return;
	}

	@Test
	public final void givenOneAgent_whenMovedToValidLocation_thenNewLocationSetInAgent() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent agent = new Agent();

		// When move requested
		Location newLocation = neighborhood.moveAgentToRandomLocation(agent);

		// When moveTo() requested
		then(agent.getLocation()).isEqualTo(newLocation);
	}

	@Test
	public final void givenAgentWithNoNeighbors_whenFiveNeighborsRequested_thenNoneReturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 4));

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1, 5);

		then(neighbors).isNotNull();
		then(neighbors.size()).isEqualTo(0);
	}

	@Test
	public final void givenAgentWithSixNeighbors_whenSevenNeighborsRequested_thenSixReturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		Agent a5 = new Agent();
		Agent a6 = new Agent();
		Agent a7 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 4));
		neighborhood.moveAgentToLocation(a2, new Location(3, 5));
		neighborhood.moveAgentToLocation(a3, new Location(3, 4));
		neighborhood.moveAgentToLocation(a4, new Location(3, 3));
		neighborhood.moveAgentToLocation(a5, new Location(5, 3));
		neighborhood.moveAgentToLocation(a6, new Location(5, 4));
		neighborhood.moveAgentToLocation(a7, new Location(5, 5));
		// List<Agent> agents = neighborhood.createAgents(2);

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1, 7);

		then(neighbors).isNotNull().isNotEmpty();
		then(neighbors.size()).isEqualTo(6);
	}

	@Test
	public final void givenAgentWithSixNeighbors_whenFiveNeighborsRequested_thenFiveReturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		Agent a5 = new Agent();
		Agent a6 = new Agent();
		Agent a7 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 4));
		neighborhood.moveAgentToLocation(a2, new Location(3, 5));
		neighborhood.moveAgentToLocation(a3, new Location(3, 4));
		neighborhood.moveAgentToLocation(a4, new Location(3, 3));
		neighborhood.moveAgentToLocation(a5, new Location(5, 3));
		neighborhood.moveAgentToLocation(a6, new Location(5, 4));
		neighborhood.moveAgentToLocation(a7, new Location(5, 5));
		// List<Agent> agents = neighborhood.createAgents(2);

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1, 5);

		then(neighbors).isNotNull().isNotEmpty();
		then(neighbors.size()).isEqualTo(5);
	}

	@Test
	public final void givenAgentWithOneLeftSideNeighbor_whenNeighborsRequested_thenListWithOneReturned() {
		// public List<T> getNeighborsOf(T object) {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 4));
		neighborhood.moveAgentToLocation(a2, new Location(3, 4));
		// List<Agent> agents = neighborhood.createAgents(2);

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1);

		then(neighbors).isNotNull().isNotEmpty().contains(a2);
		then(neighbors.size()).isEqualTo(1);
	}

	@Test
	public final void givenAgentOnTopEdgeWithThreeBottomSideNeighborsOnWrapAroundEdge_whenNeighborsRequested_thenThreeArereturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 9));
		neighborhood.moveAgentToLocation(a2, new Location(3, 0));
		neighborhood.moveAgentToLocation(a3, new Location(4, 0));
		neighborhood.moveAgentToLocation(a4, new Location(5, 0));
		// List<Agent> agents = neighborhood.createAgents(2);

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1);

		then(neighbors).isNotNull().isNotEmpty().contains(a2).contains(a3).contains(a4);
		then(neighbors.size()).isEqualTo(3);
	}

	@Test
	public final void givenAgentOnBottomEdgeWithThreeTopSideNeighborsOnWrapAroundEdge_whenNeighborsRequested_thenThreeArereturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 0));
		neighborhood.moveAgentToLocation(a2, new Location(3, 9));
		neighborhood.moveAgentToLocation(a3, new Location(4, 9));
		neighborhood.moveAgentToLocation(a4, new Location(5, 9));
		// List<Agent> agents = neighborhood.createAgents(2);

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1);

		then(neighbors).isNotNull().isNotEmpty().contains(a2).contains(a3).contains(a4);
		then(neighbors.size()).isEqualTo(3);
	}

	@Test
	public final void givenAgentOnRightEdgeWithThreeRightSideNeighborsOnWrapAroundEdge_whenNeighborsRequested_thenThreeArereturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(9, 4));
		neighborhood.moveAgentToLocation(a2, new Location(0, 5));
		neighborhood.moveAgentToLocation(a3, new Location(0, 4));
		neighborhood.moveAgentToLocation(a4, new Location(0, 3));
		// List<Agent> agents = neighborhood.createAgents(2);

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1);

		then(neighbors).isNotNull().isNotEmpty().contains(a2).contains(a3).contains(a4);
		then(neighbors.size()).isEqualTo(3);
	}

	@Test
	public final void givenAgentOnLeftEdgeWithThreeLeftSideNeighborsOnWrapAroundEdge_whenNeighborsRequested_thenThreeArereturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(0, 4));
		neighborhood.moveAgentToLocation(a2, new Location(9, 5));
		neighborhood.moveAgentToLocation(a3, new Location(9, 4));
		neighborhood.moveAgentToLocation(a4, new Location(9, 3));
		// List<Agent> agents = neighborhood.createAgents(2);

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1);

		then(neighbors).isNotNull().isNotEmpty().contains(a2).contains(a3).contains(a4);
		then(neighbors.size()).isEqualTo(3);
	}

	@Test
	public final void givenAgentWithThreeLeftSideNeighbors_whenNeighborsRequested_thenThreeArereturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 4));
		neighborhood.moveAgentToLocation(a2, new Location(3, 5));
		neighborhood.moveAgentToLocation(a3, new Location(3, 4));
		neighborhood.moveAgentToLocation(a4, new Location(3, 3));
		// List<Agent> agents = neighborhood.createAgents(2);

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1);

		then(neighbors).isNotNull().isNotEmpty().contains(a2).contains(a3).contains(a4);
		then(neighbors.size()).isEqualTo(3);
	}

	@Test
	public final void givenAgentWithThreeRightSideNeighbors_whenNeighborsRequested_thenThreeArereturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 4));
		neighborhood.moveAgentToLocation(a2, new Location(5, 5));
		neighborhood.moveAgentToLocation(a3, new Location(5, 4));
		neighborhood.moveAgentToLocation(a4, new Location(5, 3));

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1);

		then(neighbors).isNotNull().isNotEmpty().contains(a2).contains(a3).contains(a4);
		then(neighbors.size()).isEqualTo(3);
	}

	@Test
	public final void givenAgentWithThreeTopSideNeighbors_whenNeighborsRequested_thenThreeArereturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 4));
		neighborhood.moveAgentToLocation(a2, new Location(3, 5));
		neighborhood.moveAgentToLocation(a3, new Location(4, 5));
		neighborhood.moveAgentToLocation(a4, new Location(5, 5));

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1);

		then(neighbors).isNotNull().isNotEmpty().contains(a2).contains(a3).contains(a4);
		then(neighbors.size()).isEqualTo(3);
	}

	@Test
	public final void givenAgentWithThreeBottomSideNeighbors_whenNeighborsRequested_thenThreeArereturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent a1 = new Agent();
		Agent a2 = new Agent();
		Agent a3 = new Agent();
		Agent a4 = new Agent();
		neighborhood.moveAgentToLocation(a1, new Location(4, 4));
		neighborhood.moveAgentToLocation(a2, new Location(3, 3));
		neighborhood.moveAgentToLocation(a3, new Location(4, 3));
		neighborhood.moveAgentToLocation(a4, new Location(5, 3));

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(a1);

		then(neighbors).isNotNull().isNotEmpty().contains(a2).contains(a3).contains(a4);
		then(neighbors.size()).isEqualTo(3);
	}

	@Test
	public final void givenAgentWithNoNeighbors_whenNeighborsRequested_thenEmptyListReturned() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);
		Agent agent = new Agent();

		// When neighbors requested
		List<Agent> neighbors = neighborhood.getNeighborsOfAgent(agent);

		then(neighbors).isNotNull().isEmpty();

	}

	@Test
	public final void givenInputAgentIsNull_whenNeighborsRequested_thenIlegalArgumentException() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);

		// When neighbors requested
		final Throwable throwable = catchThrowable(
				() -> neighborhood.getNeighborsOfAgent(null));

		then(throwable).as("A null agent should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNullInputAgent_whenCountNeighborsRequested_thenIlegalArgumentException() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);

		// When neighbors requested
		final Throwable throwable = catchThrowable(
				() -> neighborhood.getNeighborsOfAgent(null, 1));

		then(throwable).as("A null object should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNonPositiveMaxNeighborCount_whenCountNeighborsRequested_thenIlegalArgumentException() {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);

		// When neighbors requested
		final Throwable throwable = catchThrowable(
				() -> neighborhood.getNeighborsOfAgent(new Agent(), -1));

		then(throwable)
				.as("A non positive percentage should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenNonPositiveCount_whenCountNeighborsRequested_thenIlegalArgumentException() {
		// public List<T> getNeighborsOf(T object) {

		// Given
		Neighborhood neighborhood = new NeighborhoodImpl(10);

		// When neighbors requested
		final Throwable throwable = catchThrowable(
				() -> neighborhood.getNeighborsOfAgent(new Agent(), -1));

		then(throwable).as("A non positive count should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);

	}

	@Test
	public final void givenSizeIsZero_whenCreated_thenIllegalArgumentException() {

		// Given

		// When
		final Throwable throwable = catchThrowable(() -> new NeighborhoodImpl(0));

		then(throwable)
				.as("A neighborhood size zero should throw a IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

}
