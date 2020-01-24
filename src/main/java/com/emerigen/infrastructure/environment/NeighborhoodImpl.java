/**
 * 
 */
package com.emerigen.infrastructure.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;


/**
 * @author Larry
 * @param <T>
 *
 */
public class NeighborhoodImpl implements Neighborhood {

	private static Logger logger = Logger.getLogger(Neighborhood.class);

	final private int size;

	// Holds all locations that have 1 or more agents occupying it.
	// If null there are no agents at that location
	private HashMap<Location, List<Agent>> occupiedLocations;

	/**
	 * Build a new Neighborhood of specified size.
	 * 
	 * @param size The size of each edge of the square neighborhood
	 * @return The created Neighborhood.
	 */
	public NeighborhoodImpl(int size) {

		if (size <= 4) // TODO define a property for gridspace.default.size
			throw new IllegalArgumentException("Neighborhood size must be at least 4.");
		this.size = size;

		/**
		 * Initialize the occupiedLocations.
		 */
		occupiedLocations = new HashMap<Location, List<Agent>>();
	}

	public List<Agent> getAllAgents() {
		List<Agent> allAgents = new ArrayList<>();
		List<Agent> agentsAtLocation;

		for (Location location : occupiedLocations.keySet()) {

			if (locationIsOccupied(location)) {
				agentsAtLocation = occupiedLocations.get(location);
				allAgents.addAll(agentsAtLocation);
			}
		}
		return allAgents;
	}

	@Override
	public List<Agent> getNeighborsOfAgent(Agent agent) {
		if (agent == null)
			throw new IllegalArgumentException("agent must not be null");

		List<Agent> neighbors = new ArrayList<>();
		int neighborRadius = 1;
		List<Agent> leftX = getLeftSideNeighbors(agent, neighborRadius);
		List<Agent> rightX = getRightSideNeighbors(agent, neighborRadius);
		List<Agent> aboveY = getTopSideNeighbors(agent, neighborRadius);
		List<Agent> belowY = getBottomSideNeighbors(agent, neighborRadius);

		// Get rid of duplicates
		Set<Agent> neighbors2 = new TreeSet<>();
		neighbors2.addAll(leftX);
		neighbors2.addAll(rightX);
		neighbors2.addAll(aboveY);
		neighbors2.addAll(belowY);
		neighbors.addAll(neighbors2);

		return neighbors;
	}

	@Override
	public List<Agent> getNeighborsOfAgent(Agent agent, int maxNeighborCount) {
		if (agent == null)
			throw new IllegalArgumentException("Agent must not be null");
		if (maxNeighborCount <= 0)
			throw new IllegalArgumentException("maxCount must be positive");

		// Get the Moore Neighbors
		List<Agent> neighbors = this.getNeighborsOfAgent(agent);

		List<Agent> maxCountNeighbors = new ArrayList<>();
		for (int i = 0; (i < maxNeighborCount && i < neighbors.size()); i++) {
			maxCountNeighbors.add(neighbors.get(i));
		}
		return maxCountNeighbors;
	}

	/**
	 * Move an agent to a random location in the neighborhood.
	 * 
	 * @param agent
	 */
	@Override
	public Location moveAgentToRandomLocation(Agent agent) {

		// get random x and y coordinates
		Random randomGenerator = new Random();
		int xCoordinate = randomGenerator.nextInt(size);
		int yCoordinate = randomGenerator.nextInt(size);

		// Add agent to location
		Location location = new Location(xCoordinate, yCoordinate);
		moveAgentToLocation(agent, location);
		return location;
	}

	/**
	 * Move an agent to a specified location in the neighborhood.
	 * 
	 * @param agent
	 */

	@Override
	public void moveAgentToLocation(Agent agent, Location newLocation) {
		if (agent == null)
			throw new IllegalArgumentException("Agent must not be null");
		if (newLocation == null)
			throw new IllegalArgumentException("newLocation must not be null");
		if ((newLocation.getxCoordinate() > size - 1) || (newLocation.getyCoordinate() > size - 1))
			throw new IllegalArgumentException("newLocation out of bounds: size=" 
					+ size + ", location: " + newLocation);

		// If there are agents at the new location then add to that list
		if (locationIsOccupied(newLocation)) {
			occupiedLocations.get(newLocation).add(agent);
		} else {
			// No agents at location. initialize location list & add agent
			List<Agent> agentList = new ArrayList<>();
			agentList.add(agent);
			occupiedLocations.put(newLocation, agentList);
		}
		agent.setLocation(newLocation);
	}

	@Override
	public boolean locationIsOccupied(Location location) {
		return (occupiedLocations.get(location) != null);
	}

	/**
	 * Move an agent to a location where the concentration of agents is high.
	 * 
	 * @param agent
	 */
	@Override
	public Location moveAgentToHighestConcentrationLocation(Agent agent) {
		if (agent == null)
			throw new IllegalArgumentException("Agent must not be null");
		// TODO implement moveAgentToHighestConcentrationLocation()
		return null;
	}

	/**
	 * Calculate and return the left hand side neighbors
	 * 
	 * @param agent
	 * @param neighborRadius
	 * @return
	 */
	private List<Agent> getLeftSideNeighbors(Agent agent, int neighborRadius) {
		List<Agent> agents = new ArrayList<>();
		Location loc = agent.getLocation();
		logger.info(" - Supplied agent location: "+ loc);

		// Calculate loop start and end variables
		int loopStart = loc.getyCoordinate() - neighborRadius; // TODO fix error if minus
		loopStart = loopStart < 0 ? 0 : loopStart; // Ensure within range
		int loopEnd = (loc.getyCoordinate() + neighborRadius + 1) % size;
		logger.info(" - loopStart: " + loopStart + ", loopEnd: " +loopEnd);

		int xCoordinate = (loc.getxCoordinate() - neighborRadius + size) % size;
		xCoordinate = xCoordinate < 0 ? 0 : xCoordinate;
		Location location;
		for (int y = loopStart; y < loopEnd; y++) {
			logger.info(" - looping, y: " + y);

			// If there are agents at this location
			location = new Location(xCoordinate, y);
			if (locationIsOccupied(location)) {
				logger.info("- Agents are at location: x - " 
			+ xCoordinate + "y - " + y);

				// Add them to the list to be returned
				agents.addAll(occupiedLocations.get(location));
				logger.info(" - Agents added to return list: " + agents);

			}
		}
		logger.info(" - returning agents " +  agents);
		return agents;
	}

	/**
	 * Calculate and return the right hand side neighbors
	 * 
	 * @param agent
	 * @param neighborRadius
	 * @return
	 */
	private List<Agent> getRightSideNeighbors(Agent agent, int neighborRadius) {
		List<Agent> agents = new ArrayList<>();
		Location loc = agent.getLocation();
		logger.info(" - Supplied agent location: " +  loc);

		// Calculate loop start and end variables
		int loopStart = loc.getyCoordinate() - neighborRadius;
		loopStart = loopStart < 0 ? 0 : loopStart;
		int loopEnd = (loc.getyCoordinate() + neighborRadius + 1) % size;
		logger.info(" - loopStart: "
				+ loopStart + ", loopend: " + loopEnd);

		int xCoordinate = (loc.getxCoordinate() + neighborRadius) % size;
		Location location;
		for (int y = loopStart; y < loopEnd; y++) {
			logger.info(" - looping, y: " + y);

			// If there are agents at this location
			location = new Location(xCoordinate, y);
			if (locationIsOccupied(location)) {
				logger.info(" - Agents are at location: x - " + xCoordinate
						+ ", y - " +  y);

				// Add them to the list to be returned
				agents.addAll(occupiedLocations.get(location));
				logger.info(" - Agents added to return list: "+ agents);

			}
		}
		logger.info(" - returning agents " + agents);
		return agents;
	}

	/**
	 * Calculate and return the top side neighbors
	 * 
	 * @param agent
	 * @param neighborRadius
	 * @return
	 */
	private List<Agent> getTopSideNeighbors(Agent agent, int neighborRadius) {
		List<Agent> agents = new ArrayList<>();
		Location loc = agent.getLocation();
		logger.info(" - Supplied agent location: " + loc);

		// Calculate loop start and end variables
		int loopStart = loc.getxCoordinate() - neighborRadius;
		loopStart = loopStart < 0 ? 0 : loopStart;
		int loopEnd = (loc.getxCoordinate() + neighborRadius + 1) % size;
		logger.info(" - loopStart: " + loopStart + ", loopEnd: " + loopEnd);

		int yCoordinate = (loc.getyCoordinate() + neighborRadius + size) % size;
		Location location;
		for (int x = loopStart; x < loopEnd; x++) {
			logger.info(" - looping, x: " + x);

			// If there are agents at this location
			location = new Location(x, yCoordinate);
			if (locationIsOccupied(location)) {
				logger.info(" - Agents are at location: x - " 
						+ x + ", y - " + yCoordinate);

				// Add them to the list to be returned
				agents.addAll(occupiedLocations.get(location));
				logger.info(" - Agents added to return list: " + agents);

			}
		}
		logger.info("getTopSideNeighbors() - returning agents " + agents);
		return agents;
	}

	/**
	 * Calculate and return the bottom side neighbors
	 * 
	 * @param agent
	 * @param neighborRadius
	 * @return
	 */
	private List<Agent> getBottomSideNeighbors(Agent agent, int neighborRadius) {
		List<Agent> agents = new ArrayList<>();
		Location loc = agent.getLocation();
		logger.info(" - Supplied agent location: "+ loc);

		// Calculate loop start and end variables
		int loopStart = loc.getxCoordinate() - neighborRadius;
		loopStart = loopStart < 0 ? 0 : loopStart;
		int loopEnd = (loc.getxCoordinate() + neighborRadius + 1) % size;
		logger.info(" - loopStart: " + loopStart + ", loopEnd: " +  loopEnd);

		int yCoordinate = loc.getyCoordinate() >= size - 1 ? 0 : (loc.getyCoordinate() - neighborRadius + size) % size;
		Location location;
		for (int x = loopStart; x < loopEnd; x++) {
			logger.info(" - looping, x: " + x);

			// If there are agents at this location
			location = new Location(x, yCoordinate);
			if (locationIsOccupied(location)) {
				logger.info(" - Agents are at location: x - " + x
						+ ", y - " + yCoordinate);

				// Add them to the list to be returned
				agents.addAll(occupiedLocations.get(location));
				logger.info(" - Agents added to return list: "+ agents);

			}
		}
		logger.info(" - returning agents " + agents);
		return agents;
	}
	


}
