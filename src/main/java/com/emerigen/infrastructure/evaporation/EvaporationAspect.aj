package com.emerigen.infrastructure.evaporation;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.emerigen.infrastructure.environment.Environment;
//import com.emerigen.infrastructure.environment.RelevantInformationHolder;
import com.emerigen.infrastructure.utils.EmerigenProperties;

public aspect EvaporationAspect {
	
	//declare parents: Environment implements InformationWithRelevanceHolder;

	private static EmerigenProperties props = EmerigenProperties.getInstance();

	private static final int MILLIS_PER_SECOND = 1000;
	private static final int SECONDS_PER_MINUTE = 60;
	private  static final int FREQUENCY_PER_MINUTE = Integer.parseInt(props.getValue("evaporation.frequency.per.minute"));
	private  static final double EVAPORATION_REDUCTION_FACTOR = Double
			.parseDouble(props.getValue("evaporation.reduction.factor"));
//	private  static final double MINIMUM_RELEVANCE = Double.parseDouble(props.getValue("evaporation.minimum.relevance"));
	private  static final double MAXIMUM_RELEVANCE = Double.parseDouble(props.getValue("evaporation.maximum.relevance"));
	private  static final double LOWER_THRESHOLD = Double.parseDouble(props.getValue("evaporation.lower.relevance.threshold"));
	private  static final double EVAPORATION_INCREASE_FACTOR = Double
			.parseDouble(props.getValue("evaporation.information.access.increase"));

	private static HashMap<String, RelevantInformation> relevantInformationMap;
	private static Timer timer;

	/**
	 * InformationWithRelevanceHolder interface methods to implement.
	 */
	pointcut holdsInformationWithRelevanceSetMethod():
		execution (* com.emerigen..InformationWithRelevanceHolder+.setInformationWithRelevance(String, Object));

	pointcut holdsInformationWithRelevanceGetMethod():
		execution (* com.emerigen..InformationWithRelevanceHolder+.getInformationWithRelevance(String));

	pointcut holdsInformationWithMinimumRelevanceGetMethod():
		execution (* com.emerigen..InformationWithRelevanceHolder+.getInformationWithMinimumRelevance(String, double));

	// Select Environment initialization
	pointcut environmentCreation(Environment env) :
		initialization(Environment.new(..)) && this(env);

	// Initialize evaporation apperatus only once
	after(Environment env) returning : environmentCreation(env) {
		relevantInformationMap = new HashMap<String, RelevantInformation>();
		initializeEvaporationTimer();
	}

	/**
	 * 
	 * @return The info from the environment if its relevance is above a
	 *         configurable threshold.
	 * 
	 */
	Object around() : holdsInformationWithRelevanceGetMethod() {

		Object[] args = thisJoinPoint.getArgs();
		Object relevantInformation = getInformationWithRelevance((String) args[0]);
		return relevantInformation;
	}

	Object around() : holdsInformationWithMinimumRelevanceGetMethod() {

		Object[] args = thisJoinPoint.getArgs();
		Object relevantInformation = getInformationWithMinimumRelevance((String) args[0],
				(double) args[1]);
		return relevantInformation;
	}

	/**
	 * Store information with the supplied relevance
	 * 
	 */
	void around() : holdsInformationWithRelevanceSetMethod() {

		Object[] args = thisJoinPoint.getArgs();
		setInformationWithRelevance((String) args[0], args[1]);
	}

	private void initializeEvaporationTimer() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				evaporateRelevancies();
			}
		}, (SECONDS_PER_MINUTE * MILLIS_PER_SECOND) / FREQUENCY_PER_MINUTE,
				(SECONDS_PER_MINUTE * MILLIS_PER_SECOND) / FREQUENCY_PER_MINUTE);
	}

	private void evaporateRelevancies() {
		//System.out.println("evaporateRelevances() entry");
		for (RelevantInformation entry : relevantInformationMap.values()) {
			synchronized (this) {
				//System.out.println("evaporateRelevances() looping - new relevance: " + (entry.getRelevance() - EVAPORATION_REDUCTION_FACTOR));
				entry.setRelevance(entry.getRelevance() - EVAPORATION_REDUCTION_FACTOR);
			}
		}
	}
	
	public Object getInformationWithMinimumRelevance(String key, double minimumRelevance) {
		RelevantInformation entry = (RelevantInformation) relevantInformationMap.get(key);

		if (entry == null) {
			return null;
		} else if (entry.getRelevance() < minimumRelevance) {
			return null;
		} else {
			return entry.getInformation();
		}
	}

	public Object getInformationWithRelevance(String key) {

		if (key == null || key.isEmpty())
			throw new IllegalArgumentException("Key may not be null or empty");

		RelevantInformation entry = relevantInformationMap.get(key);
		if (entry == null) {
			return null;
		} else if (entry.getRelevance() < LOWER_THRESHOLD) {
			return null;
		} else {

			entry.setRelevance(entry.getRelevance() + EVAPORATION_INCREASE_FACTOR);
			relevantInformationMap.put(key, entry);
			return entry.getInformation();
		}
	}

	public void setInformationWithRelevance(String key, Object object) {

		if (key == null || key.isEmpty())
			throw new IllegalArgumentException("Key may not be null or empty");

		RelevantInformation relInfo = new RelevantInformation(object, MAXIMUM_RELEVANCE);
		relevantInformationMap.put(key, relInfo);
	}


}
