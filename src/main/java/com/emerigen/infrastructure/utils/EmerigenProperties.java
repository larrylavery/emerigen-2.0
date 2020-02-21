package com.emerigen.infrastructure.utils;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.environment.Environment;

import java.io.*;
import java.util.Properties;

public class EmerigenProperties {

	private final static String EMERIGEN_PROPERTIES_FILENAME = "emerigen.properties";
	private static Logger logger = Logger.getLogger(EmerigenProperties.class);

	private static EmerigenProperties instance = null;
	private Properties props;

	public static EmerigenProperties getInstance() {

		if (instance == null) {
			synchronized (EmerigenProperties.class) {
				if (instance == null) {
					instance = new EmerigenProperties();
				}
			}

		}
		// Return singleton CouchbaseRepository
		return instance;
	}

	// TODO refactor properties to this
	// Create the agents based on parameters
//	Parameters params = RunEnvironment.getInstance().getParameters();
//	int eprCount = params.getInteger("epr_count");

	private EmerigenProperties() {
		props = new Properties();
		this.loadParams(EMERIGEN_PROPERTIES_FILENAME);
	}

	public String getValue(String propKey) {
		return this.props.getProperty(propKey);
	}

	public void loadParams(String filename) {
		InputStream is = null;

		// First, try loading from the current directory
		try {
			File f = new File("target/classes/" + filename);
			is = new FileInputStream(f);
			// System.out.println("loaded from current directory");

			props.load(is);
			return;
		} catch (Exception e) {
			is = null;
		}

		// Next try loading from classpath
		try {
			if (is == null) {
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				is = classLoader.getResourceAsStream(filename);
			}
			// System.out.println("loaded from classpath, 'is' = " + is);
		} catch (Exception e) {
			// Not found on classpath
			throw new RuntimeException(
					"The Emerigen properties file not found. filename = " + filename
							+ "..... " + e.getMessage());
		}
	}

	public String toString() {
		return props.toString();
	}

	public void saveParamChangesAsXML(String filename) {
		try {

			File f = new File("target/classes/" + filename);
			OutputStream out = new FileOutputStream(f);
			props.storeToXML(out, "");
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to save changes to Knowledge properties file. filename = "
							+ "target/classes/" + filename);
		}
	}

	public void setValue(String theKey, String theValue) {
		if ((theKey == null) || (theKey.length() == 0)) {
			throw new IllegalArgumentException(
					"The key value must not be null or have a length of zero.");
		} else if ((theValue == null) || (theValue.length() == 0)) {
			throw new IllegalArgumentException(
					"The value must not be null or have a length of zero.");
		}
		props.setProperty(theKey, theValue);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof EmerigenProperties))
			return false;

		EmerigenProperties that = (EmerigenProperties) o;

		return props != null ? props.equals(that.props) : that.props == null;

	}

	@Override
	public int hashCode() {
		return props != null ? props.hashCode() : 0;
	}
}
