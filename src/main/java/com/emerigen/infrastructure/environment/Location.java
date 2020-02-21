package com.emerigen.infrastructure.environment;

public class Location {

	final private int xCoordinate;
	final private int yCoordinate;

	/**
	 * Assumed that all coordinates are non-negative with origin at 0,0.
	 * 
	 * @param xCoordinate
	 * @param yCoordinate
	 */
	public Location(int xCoordinate, int yCoordinate) {
		if (xCoordinate < 0)
			throw new IllegalArgumentException("x coordinate must be zero or more");
		if (yCoordinate < 0)
			throw new IllegalArgumentException("y coordinate must be zero or more");

		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}

	/**
	 * @return the xCoordinate
	 */
	public int getxCoordinate() {
		return xCoordinate;
	}

	/**
	 * @return the yCoordinate
	 */
	public int getyCoordinate() {
		return yCoordinate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + xCoordinate;
		result = prime * result + yCoordinate;
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
		Location other = (Location) obj;
		if (xCoordinate != other.xCoordinate)
			return false;
		if (yCoordinate != other.yCoordinate)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Location [xCoordinate=" + xCoordinate + ", yCoordinate=" + yCoordinate
				+ "]";
	}

}
