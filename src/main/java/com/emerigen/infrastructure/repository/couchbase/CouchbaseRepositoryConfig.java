/**
 * 
 */
package com.emerigen.infrastructure.repository.couchbase;

import org.apache.log4j.Logger;

import com.emerigen.infrastructure.environment.Environment;

/**
 * @author Larry
 *
 */
public class CouchbaseRepositoryConfig {

	final String connectionString;
	final String userID;
	final String password;
	private static Logger logger = Logger.getLogger(CouchbaseRepositoryConfig.class);

	public CouchbaseRepositoryConfig(final String connectionString, final String userID,
			final String password) {
		this.connectionString = connectionString;
		this.userID = userID;
		this.password = password;
	}

	/**
	 * @return the connectionString
	 */
	public String getConnectionString() {
		return connectionString;
	}

	/**
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

}
