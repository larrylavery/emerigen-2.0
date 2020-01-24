package com.emerigen.infrastructure.evaporation;

public class RelevantInformation {
	
	final static double DEFAULT_RELEVANCE = 1.0;
	private Object information;
	private double relevance;

	public RelevantInformation(Object info) {
		this.information = info;
		this.relevance = DEFAULT_RELEVANCE;
	}
	public RelevantInformation(Object info, double relevance) {
		this.information = info;
		this.relevance = relevance;
	}

	public Object getInformation() {
		return information;
	}

	public double getRelevance() {
		return relevance;
	}

	public void setRelevance(double d) {
		this.relevance = d;
	}
}
