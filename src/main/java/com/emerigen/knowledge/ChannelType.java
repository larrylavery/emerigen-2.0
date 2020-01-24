package com.emerigen.knowledge;

import java.time.Instant;

public class ChannelType {
	
	//Identifies the type of channel (eg visual, touch, xyz, etc). Unique per entity
	 String channelType = null;
	
	//The uri of the file containing the sensory events for this channel type
	 String sensoryEventsUri = null;

	//An indication of whether learning for this channel has completed (ie all sensory events have been processed
	 boolean learningComplete = true;

	 private String timestamp = String.valueOf(System.nanoTime());

	
	 public ChannelType() {}
	 
	 public ChannelType(String channelType, String sensoryEventsUri, boolean learningComplete) {
		
		//Validate parms
		if (channelType == null || channelType.isEmpty()) {
			throw new IllegalArgumentException("ChannelType must not be null or empty");
		} if (sensoryEventsUri == null || sensoryEventsUri.isEmpty()) {
			throw new IllegalArgumentException("sensoryEventsUri must not be null or empty");			
		} 
		
		this.channelType = channelType;
		this.sensoryEventsUri = sensoryEventsUri;
		this.learningComplete = learningComplete;
	}

	/**
	 * @return the channelType
	 */
	public String getChannelType() {
		return channelType;
	}

	/**
	 * @return the sensoryEventsURI
	 */
	public String getSensoryEventsUri() {
		return sensoryEventsUri;
	}

	/**
	 * @return the learningComplete
	 */
	public boolean isLearningComplete() {
		return learningComplete;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channelType == null) ? 0 : channelType.hashCode());
		result = prime * result + ((sensoryEventsUri == null) ? 0 : sensoryEventsUri.hashCode());
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
		ChannelType other = (ChannelType) obj;
		if (channelType == null) {
			if (other.channelType != null)
				return false;
		} else if (!channelType.equals(other.channelType))
			return false;
		if (sensoryEventsUri == null) {
			if (other.sensoryEventsUri != null)
				return false;
		} else if (!sensoryEventsUri.equals(other.sensoryEventsUri))
			return false;
		return true;
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param channelType the channelType to set
	 */
	void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	/**
	 * @param sensoryEventsUri the sensoryEventsUri to set
	 */
	void setSensoryEventsUri(String sensoryEventsUri) {
		this.sensoryEventsUri = sensoryEventsUri;
	}

	/**
	 * @param learningComplete the learningComplete to set
	 */
	void setLearningComplete(boolean learningComplete) {
		this.learningComplete = learningComplete;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
