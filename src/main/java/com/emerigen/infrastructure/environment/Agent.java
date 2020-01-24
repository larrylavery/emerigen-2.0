package com.emerigen.infrastructure.environment;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

//import com.emerigen.infrastructure.environment.MessageToSpread;

public class Agent implements Comparable<Agent> {
	// public class Agent implements Comparator<Agent>, Comparable<Agent> {

	private Object content = "";

	private List<MessageToSpread> priorContent = new ArrayList();
	private Location location;

	private static Logger logger = Logger.getLogger(Agent.class);
	

	public Agent(Location location) {
		this.location = location;
	}

	public Agent() {
		this(new Location(0,0));
	}

	public void broadcastMessage(MessageToSpread message, List<Agent> recipients) {

		if (message == null)
			throw new IllegalArgumentException("Message must not be null");
		if (recipients == null || recipients.size() == 0)
			throw new IllegalArgumentException("Recipients must not be null or empty");

		// Broadcast the message asynchronously in a new thread
		new Thread(() -> {
			for (Agent agent : recipients) {
				logger.info(" sending message to recipient: " + agent);
				agent.spreadMessage(message);
			}
		}).start();

	}

	public void spreadMessage(MessageToSpread message) {
		if (message == null)
			throw new IllegalArgumentException("Message must not be null");

		// Return if content has been broadcast by me in the past
		if (priorContent.contains(message))
			return;
		logger.info(" Message was not contained in prior messages, continuing");

		priorContent.add(message);
		logger.info(" updating priorContent with new content");

		// Return if maxHops exceeded
		if (message.hopsExceeded())
			return;
		logger.info(" Max hops not exceeded");

		// Apply the content update function to my content
		setContent(message.getContentUpdateFunction().apply(getContent(), message.getMessage()));
		logger.info(" content updated: " + content);

		// Increment to number of hops
		MessageToSpread newMessage = message.incrementHops();
		logger.info(" hops incremented: " + message.getMessageHops());

		// Broadcast the message to my neighbors
		List<Agent> neighbors = Environment.getInstance().getNeighbors(this);
		if (neighbors.size() > 0)
			broadcastMessage(newMessage, neighbors);
	}

	public Object getContent() {
		// Content specific to this Agent
		return content;
	}

	void setContent(Object obj) {
		this.content = obj;
	}

	@Override
	public String toString() {
		return "Agent [content=" + content + ", priorContent=" + priorContent + ", location=" + location + "]";
	}

	/**
	 * @return the priorContent
	 */
	public List<MessageToSpread> getPriorContent() {
		return priorContent;
	}

	/**
	 * @param priorContent the priorContent to set
	 */
	public void setPriorContent(List<MessageToSpread> priorContent) {
		this.priorContent = priorContent;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((priorContent == null) ? 0 : priorContent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Agent other = (Agent) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (priorContent == null) {
			if (other.priorContent != null)
				return false;
		} else if (!priorContent.equals(other.priorContent))
			return false;
		return true;
	}

	@Override
	public int compareTo(Agent other) {
		return Integer.compare(this.hashCode(), other.hashCode());
	}
}
