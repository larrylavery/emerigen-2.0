package com.emerigen.knowledge;

import java.time.Instant;
import java.util.List;

public class Entity {

	private String entityID = null;
	private String timestamp = Instant.now().toString();
	private List<ChannelType> channels = null;

	public Entity() {
	}

	public Entity(String entityID, List<ChannelType> channels) {

		// Validate parms
		if (entityID == null || entityID.isEmpty()) {
			throw new IllegalArgumentException("entityID must not be null or empty");
		}
		if (channels == null || channels.isEmpty()) {
			throw new IllegalArgumentException("channels must not be null or empty");
		}
		this.entityID = entityID;
		this.channels = channels;
	}

	/**
	 * @return the entityID
	 */
	public String getEntityID() {
		return entityID;
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the channels
	 */
	public List<ChannelType> getChannels() {
		return channels;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channels == null) ? 0 : channels.hashCode());
		result = prime * result + ((entityID == null) ? 0 : entityID.hashCode());
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
		Entity other = (Entity) obj;
		if (channels == null) {
			if (other.channels != null)
				return false;
		} else if (!channels.equals(other.channels))
			return false;
		if (entityID == null) {
			if (other.entityID != null)
				return false;
		} else if (!entityID.equals(other.entityID))
			return false;
		return true;
	}

}
