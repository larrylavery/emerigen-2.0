package com.emerigen.infrastructure.evaporation;

public interface InformationWithRelevanceHolder {

	/**
	 * Return the relevant information if its current relevance is at least the
	 * value of minimumRelevance
	 * 
	 * @param key
	 * @param minRelevance The minimum acceptable relevance
	 * @return The information if its relevance exceeds minimumRelevance otherwise
	 *         return null
	 */
	default public Object getInformationWithMinimumRelevance(String key,
			double minimumRelevance) {
		return (Object) null;
	}

	/**
	 * 
	 * @param key
	 * @return the relevant information if its relevance is greater than the
	 *         absolute minimum (configurable)
	 */
	default public Object getInformationWithRelevance(String key) {
		return (Object) null;
	}

	default public void setInformationWithRelevance(String key, Object information) {
		return;
	}

}
