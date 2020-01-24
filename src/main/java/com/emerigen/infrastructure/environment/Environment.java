package com.emerigen.infrastructure.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.evaporation.InformationWithRelevanceHolder;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public class Environment implements InformationWithRelevanceHolder {

	EmerigenProperties props = EmerigenProperties.getInstance();
	private static Environment instance;
	private Neighborhood neighborhood;
	private static Logger logger = Logger.getLogger(Environment.class);
	private final int SIZE = Integer.parseInt(
			EmerigenProperties.getInstance().getValue("environment.gridspace.size"));

	public static Environment getInstance() {

		if (instance == null) {
			synchronized (Environment.class) {
				if (instance == null) {
					instance = new Environment();
				}
			}
		}

		// Return singleton
		return instance;
	}

	public Environment() {

		initializeData();
	}

	public void initializeData() {

		// Initialize AgentSpace
		this.neighborhood = new NeighborhoodImpl(SIZE);
	}

	/**
	 * Creates the specified number of Objects and places them at random positions
	 * in the space.
	 * 
	 * @param numberToCreate
	 * @return The list of created objects where each contains it's location
	 */

	public List<Agent> createAgents(int numberToCreate) {

		if (numberToCreate < 1)
			throw new IllegalArgumentException("numberToCreate must be positive.");

		List<Agent> agents = new ArrayList<Agent>();
		Agent agent;

		// Create the agents and store them randomly in AgentSpace
		for (int i = 0; i < numberToCreate; i++) {
			agent = new Agent();
			agents.add(agent);
			neighborhood.moveAgentToRandomLocation(agent);
		}
		return agents;
	}

	public Agent createAgentAtLocation(Location location) {
		Agent agent = new Agent();
		neighborhood.moveAgentToLocation(agent, location);
		return agent;
	}

	public List<Agent> createAgentsOnRandomLocations(int numberToCreate) {
		if (numberToCreate < 1)
			throw new IllegalArgumentException("numberToCreate must be positive.");

		List<Agent> newAgents = new ArrayList<>();
		Agent newAgent;

		for (int i = 0; i < numberToCreate; i++) {
			newAgent = new Agent();
			neighborhood.moveAgentToRandomLocation(newAgent);
			newAgents.add(newAgent);
		}
		return newAgents;
	}

	public List<Agent> getNeighbors(Agent agent,
			Function<Object, Object> sequencingFunction) {

		if (agent == null)
			throw new IllegalArgumentException("Agent must not be null.");
		if (sequencingFunction == null)
			throw new IllegalArgumentException("Sequencing functionmust not be null.");

		// TODO implement sequencing function when required
		return neighborhood.getNeighborsOfAgent(agent);
	}

	public List<Agent> getNeighbors(Agent agent, int neighborCount) {

		if (agent == null)
			throw new IllegalArgumentException("Agent must not be null.");
		if (neighborCount <= 0)
			throw new IllegalArgumentException("NeighborCount must be positive.");
//		if (sequencingFunction == null) 
//			throw new IllegalArgumentException("Sequencing function must not be null.");

		// TODO implement sequencing function when needed
		return neighborhood.getNeighborsOfAgent(agent, neighborCount);
	}

	public List<Agent> getNeighbors(Agent agent) {

		if (agent == null)
			throw new IllegalArgumentException("Agent must not be null.");
//		if (sequencingFunction == null) 
//			throw new IllegalArgumentException("Sequencing function must not be null.");

		// TODO implement sequencing function when needed
		return neighborhood.getNeighborsOfAgent(agent);
	}

	public List<Agent> allAgents() {
		return neighborhood.getAllAgents();
	}

	public void moveTo(Agent agent, Location newLocation) {
		neighborhood.moveAgentToLocation(agent, newLocation);
	}

}
