package com.emerigen.infrastructure.environment;

import java.util.List;

	
public interface Neighborhood {
		
	/**
	 * Returns the list of the Agent's nearest neighbors. If the Agent is
	 * near an edge, then it's neighbors will include its neighbors on
	 * the wrap around border side.
	 * 
	 * @param Agent The Agent whose neighbors we are locating
	 * @return The list of neighbors
	 */
	public List<Agent> getNeighborsOfAgent(Agent agent);

	/**
	 * Returns the list of the agent's nearest neighbors up to a maximum
	 * number as specified by maxCount. If the Agent is
	 * near an edge, then it's neighbors may also include its neighbors on
	 * the wrap around border side.
	 * 
	 * @param agent The agent whose neighbors we are locating
	 * @return The list of neighbors
	 */
	public List<Agent> getNeighborsOfAgent(Agent agent, int maxCount);
		
	/**
	 * Moves an Agent to the specified Location.
	 * 
	 * @param Agent The Agent being moved
	 * @parm newLocation
	 */
	public void moveAgentToLocation(Agent agent, Location newLocation);
	
	/**
	 * Moves an Agent to a location near the middle of the
	 * most concentrated area in the space.
	 * @param Agent The Agent being moved
	 * @return The location that the Agent was moved to.
	 */
	public Location moveAgentToHighestConcentrationLocation(Agent agent);

	/**
	 * Add the agent to a random location in my space.
	 * @param agent
	 */
	public Location moveAgentToRandomLocation(Agent agent);
	
	public boolean locationIsOccupied(Location location);
	
	
	public List<Agent> getAllAgents();
	
	/**
	 * Calculate the distance between two Locations
	 * 
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	default public int getDistance(Location loc1, Location loc2) {

		// Calculate the x and y differences
		int x = loc1.getxCoordinate() - loc2.getxCoordinate();
		int y = loc1.getyCoordinate() - loc2.getyCoordinate();

		// Calculate the hypotenuse length as the distance
		return (int) Math.hypot(x, y);
	}

}
