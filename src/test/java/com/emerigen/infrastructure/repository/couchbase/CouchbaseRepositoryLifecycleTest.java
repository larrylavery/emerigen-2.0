/**
 * 
 */
package com.emerigen.infrastructure.repository.couchbase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;

import org.junit.Test;

import com.couchbase.client.java.json.JsonObject;
import com.emerigen.infrastructure.repository.RepositoryException;

/**
 * @author Larry
 *
 */
public class CouchbaseRepositoryLifecycleTest {

	CouchbaseRepository myRepos;
	static private ArrayList<String> sensorEventIDs = new ArrayList<>();

	// @Test
	public final void givenValidConnectionWhenReconnectedThenIsConnectedShouldRetunTrue() {

		// Given
		myRepos = CouchbaseRepository.getInstance();
		assertThat(myRepos.isConnected()).isNotNull().isTrue();

		// When
		myRepos = myRepos.reconnect();

		// Then
		then(myRepos.isConnected()).as(
				"Valid connection, reconnected, should return a new Repository that is connected")
				.isNotNull().isTrue();
	}

	@Test
	public final void givenValidConnectionWhenDisconnectedThenIsConnectedShouldRetunFalse() {

		// Given
		myRepos = CouchbaseRepository.getInstance();
		assertThat(myRepos.isConnected()).isNotNull().isTrue();

		// When
		myRepos.disconnect();

		// Then
		then(myRepos.isConnected()).as(
				"Valid connection, disconnected, should return false for isConnected()")
				.isNotNull().isFalse();
	}

	@Test
	public final void testThatCouchbaseConnectionCreationWithValidParametersIsSuccessful() {
		myRepos = CouchbaseRepository.getInstance();

		assertThat(myRepos.isConnected())
				.as("Valid parms should create a successful connection").isNotNull()
				.isTrue();

	}

	// @Test
	public final void testThatCouchbaseConnectionCreationWithInvalidConnectionStringThrowsConfigurationException() {

		// When
		final Throwable throwable = catchThrowable(
				() -> CouchbaseRepository.getInstance());

		// Then
		then(throwable).as(
				"A ConfigurationException or TimeoutException should be thrown if a bad connectionString is passed")
				.isInstanceOf(RuntimeException.class);
	}

	// @Test
	public final void testThatCouchbaseConnectionCreationWithInvalidUserIdThrowsRepositoryException() {

		// When
		final Throwable throwable = catchThrowable(
				() -> CouchbaseRepository.getInstance());

		// Then
		then(throwable).as(
				"An InvalidPasRepositoryExceptionswordException should be thrown if a bad password is passed")
				.isInstanceOf(RepositoryException.class);
	}

	// @Test
	public final void testThatCouchbaseConnectionCreationWithInvalidPasswordThrowsRepositoryException() {

		// When
		final Throwable throwable = catchThrowable(
				() -> CouchbaseRepository.getInstance());

		// Then
		then(throwable)
				.as("An RepositoryException should be thrown if a bad password is passed")
				.isInstanceOf(RepositoryException.class);
	}

	@Test
	public final void testThatCouchbaseConnectionCreationWithInvalidBucketNameThrowsBucketDoesNotExistException() {

		JsonObject myObj = JsonObject.create();
		// When
		final Throwable throwable = catchThrowable(() -> CouchbaseRepository.getInstance()
				.log("any-primary-key", myObj, false));

		// Then
		then(throwable).as(
				"A RepositoryException should be thrown if the bucket name is invalid")
				.isInstanceOf(RepositoryException.class);
	}

}
