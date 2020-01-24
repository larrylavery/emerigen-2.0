package com.emerigen.infrastructure.environment;

import java.util.function.BiFunction;

import com.emerigen.infrastructure.utils.EmerigenProperties;

public class MessageToSpread {

	final private int messageHops;
	final private int maxHops;
	final private String message;
	final private BiFunction<Object, Object, Object> contentUpdateFunction;

	public MessageToSpread(String message, int messageHops, BiFunction<Object, Object, Object> function) {
		this(message, messageHops,
				Integer.parseInt(EmerigenProperties.getInstance().getValue("spreading.max.broadcast.hops")), function);

	}

	public MessageToSpread(String message, int messageHops, int maxHops, BiFunction<Object, Object, Object> function) {

		if (message == null || message.isEmpty())
			throw new IllegalArgumentException("Message must not be null or empty.");
		if (messageHops <= 0)
			throw new IllegalArgumentException("messageHops must be positive.");
		if (maxHops <= 0)
			throw new IllegalArgumentException("maxHops must be positive.");
		if (function == null)
			throw new IllegalArgumentException("content update function must not be null.");

		this.message = message;
		this.messageHops = messageHops;
		this.contentUpdateFunction = function;
		this.maxHops = maxHops;
	}

	/**
	 * @return the messageHops
	 */
	public int getMessageHops() {
		return messageHops;
	}

	/**
	 * 
	 * @return new MessageToSpread with the messageHops incremented. Preserves
	 *         immutability.
	 */
	public MessageToSpread incrementHops() {
		return new MessageToSpread(getMessage(), messageHops + 1, getMaxHops(), getContentUpdateFunction());
	}

	public boolean hopsExceeded() {
		return messageHops > maxHops;
	}

	/**
	 * @return the maxHops
	 */
	public int getMaxHops() {
		return maxHops;
	}

	/**
	 * @return the message
	 */
	public BiFunction<Object, Object, Object> getContentUpdateFunction() {
		return contentUpdateFunction;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contentUpdateFunction == null) ? 0 : contentUpdateFunction.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		MessageToSpread other = (MessageToSpread) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MessageToSpread [messageHops=" + messageHops + ", maxHops=" + maxHops + ", message=" + message + "]";
	}

}
