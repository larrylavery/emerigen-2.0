/**
 * 
 */
package com.emerigen.infrastructure.repository.couchbase;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.couchbase.client.core.env.KeyValueServiceConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.ParameterizedN1qlQuery;
import com.couchbase.client.java.query.Statement;
import com.emerigen.infrastructure.repository.RepositoryException;
import com.emerigen.infrastructure.utils.EmerigenProperties;

/**
 * @author Larry
 * 
 *         Represents the service that handles IO to/from a specific data
 *         storage type, as specified by the JSONObject type. A repository
 *         handles one JSON Object type only. The dataStorage "name" represents
 *         the JSONObject type.
 * 
 *         Although the underlying implementation may be any number of data
 *         implementations (CouchDB, Relational, File storage, memory, etc), the
 *         user of this interface remains unaware of the specific
 *         implementation. The interface must be implemented fully for each
 *         specific dataStorage type (CouchDB, Relational, file storage, etc.)
 *         with no implementation type leakage.
 * 
 *         Implementations must verify that the JSONObject type field matches
 *         the dataStorage type (usually the dataStorage name) before inserting
 *         the JSONObject.
 * 
 *         This is a "functional" interface so instance variables should be
 *         final. This means that each connection to the data storage returns a
 *         new instance of the Repository implementation. The "connect" is done
 *         in the constructor. The "disconnect" should be explicitly called or
 *         memory may leak.
 * 
 *         log() requests do not return an indicator of success or failure. This
 *         is because millions of immutable facts (ie records), effectively
 *         infinite, may be logged and a certain percentage of failures is
 *         expected with minimal effect on the intended usage (ie machine
 *         learning, pattern recognition, etc). The frequency of Exceptions from
 *         all repositories is monitored. When that frequency reaches a
 *         configurable threshold, the repository will be restarted (or
 *         recreated) as necessary. Continued failures after a configurable
 *         number of restarts/recreates result in more drastic recovery
 *         processes until finally the repository is shut down permanently until
 *         a sys admin fixes the underlying issues.
 * 
 *         query() takes an input string that represents the query language for
 *         the specific dataStorage type. Although the queryType must match the
 *         dataStorage "type", There is no restriction on the contents of the
 *         query string as long as it is executable by the underlying
 *         dataStorage type.
 * 
 *
 */
public class CouchbaseRepository {

	public static final String SENSOR_EVENT = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.sensor.event");
	public static final String PREDICTION = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.prediction");
	public static final String TRANSITION = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.transition");
	public static final String ENTITY = EmerigenProperties.getInstance()
			.getValue("couchbase.bucket.entity");
	public static final String PERSON = "person-sample";
	private static final String PASSWORD = EmerigenProperties.getInstance()
			.getValue("couchbase.server.password");
	private static final String LOCALHOST = EmerigenProperties.getInstance()
			.getValue("couchbase.server.localhost");
	private static final String ADMINISTRATOR = EmerigenProperties.getInstance()
			.getValue("couchbase.server.userid");
	private static final int NUM_KEY_VALUE_SERVICES = Integer.parseInt(EmerigenProperties
			.getInstance().getValue("couchbase.server.number.key.value.services"));
	private static final CouchbaseRepositoryConfig couchbaseRepositoryConfig = new CouchbaseRepositoryConfig(
			LOCALHOST, ADMINISTRATOR, PASSWORD);
	static private final KeyValueServiceConfig keyValueServiceConfig = KeyValueServiceConfig
			.create(NUM_KEY_VALUE_SERVICES);
	static private Cluster cluster = null;

	static private CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
			.continuousKeepAliveEnabled(Boolean.valueOf(EmerigenProperties.getInstance()
					.getValue("couchbase.server.continuous.keep.alive.enabled")))
			.keyValueServiceConfig(keyValueServiceConfig)
			.connectTimeout(Integer.parseInt(EmerigenProperties.getInstance()
					.getValue("couchbase.server.connect.timeout")))
			.socketConnectTimeout(Integer.parseInt(EmerigenProperties.getInstance()
					.getValue("couchbase.server.socket.connect.timeout")))
			.disconnectTimeout(Integer.parseInt(EmerigenProperties.getInstance()
					.getValue("couchbase.server.disconnect.timeout")))
			.kvTimeout(Integer.parseInt(EmerigenProperties.getInstance()
					.getValue("couchbase.server.kv.timeout")))
			.keepAliveTimeout(Integer.parseInt(EmerigenProperties.getInstance()
					.getValue("couchbase.server.keep.alive.timeout")))
			.build();

	private static HashMap<String, Bucket> buckets = new HashMap<String, Bucket>();
	private static Logger logger = Logger.getLogger(CouchbaseRepository.class);
	private static CouchbaseRepository instance;

	public static CouchbaseRepository getInstance() {

		if (instance == null) {
			synchronized (CouchbaseRepository.class) {
				if (instance == null) {
					instance = new CouchbaseRepository();
				}
			}

		}
		// Return singleton CouchbaseRepository
		return instance;
	}

	private CouchbaseRepository() {
		cluster = CouchbaseCluster.create(env,
				couchbaseRepositoryConfig.getConnectionString());
		cluster.authenticate(couchbaseRepositoryConfig.getUserID(),
				couchbaseRepositoryConfig.getPassword());
	}

	private void openBucketIfNecessary(String bucketName) {

		if (buckets.get(bucketName) == null) {
			Bucket bucket = cluster.openBucket(bucketName);
			logger.info(" - bucket opened, bucket name: " + bucketName);

			// TODO make functional
			buckets.put(bucketName, bucket);
		}
	}

	public void log(final String bucketName, final String primaryKey,
			final JsonObject jsonObject) {

		openBucketIfNecessary(bucketName);

		Bucket bucket = buckets.get(bucketName);
		if (bucket != null) {
			// insert the jsonObject into my bucket
			bucket.insert(JsonDocument.create(primaryKey, jsonObject));
		} else {
			throw new BucketNotFoundException(
					"log(bucketName() failed because the bucket was not found");
		}
	}

	public N1qlQueryResult query(final String bucketName, final Statement statement,
			final JsonArray placeholderValues) {

		openBucketIfNecessary(bucketName);
		Bucket bucket = buckets.get(bucketName);
		if (bucket != null) {
			ParameterizedN1qlQuery n1qlQuery = ParameterizedN1qlQuery
					.parameterized(statement, placeholderValues);

			// The actual Repository query() call
			N1qlQueryResult result = bucket.query(n1qlQuery);
			if (!result.finalSuccess()) {
				throw new RepositoryException("N1qlQuery failure: " + result.errors()
						+ "/n statement: " + statement);
			}
			return result;
		} else {
			throw new BucketNotFoundException(
					"query(...) failed because the bucket was not found");
		}

	}

	public N1qlQueryResult query(final String bucketName, final N1qlQuery n1qlQuery) {

		openBucketIfNecessary(bucketName);
		Bucket bucket = buckets.get(bucketName);
		if (bucket != null) {

			// The actual Repository query() call
			N1qlQueryResult result = bucket.query(n1qlQuery);
			if (!result.finalSuccess()) {
				throw new RepositoryException("N1qlQuery failure: " + result.errors()
						+ "/n statement: " + n1qlQuery);
			}
			return result;
		} else {
			throw new BucketNotFoundException(
					"query(...) failed because the bucket was not found");
		}

	}

	public JsonObject extractJsonResult(N1qlQueryResult result, String enclosingType,
			int index) {
		return result.allRows().get(index).value().getObject(enclosingType);
	}

	public void removeAllDocuments(final String bucketName) {

		openBucketIfNecessary(bucketName);
		Bucket bucket = buckets.get(bucketName);
		if (bucket != null) {

			if (!bucket.bucketManager().flush()) {
				throw new RepositoryException(
						"Error occurred while deleting all documents in bucket ("
								+ bucket.name() + ")");
			}
		}
	}

	public boolean createPrimaryIndex(String bucketName) {

		openBucketIfNecessary(bucketName);
		if (buckets.size() > 0) {
			Bucket bucket = buckets.get(bucketName);

			if (bucket != null) {
				return bucket.bucketManager().createN1qlPrimaryIndex(true, true);
			}
			return false;
		}
		return false;
	}

	public JsonDocument get(final String bucketName, final String docID) {

		openBucketIfNecessary(bucketName);

		Bucket bucket = buckets.get(bucketName);
		if (bucket != null) {
			JsonDocument jsonDocument = bucket.get(docID);
			return jsonDocument;
		} else {
			throw new BucketNotFoundException(
					"query(...) failed because the bucket was not found");
		}
	}

	public boolean isConnected() {
		if (instance == null) {
			return false;
		}
		return true;
	}

	public CouchbaseRepository reconnect() {
		disconnect();
		return CouchbaseRepository.getInstance();

	}

	public void disconnect() {
		if (instance != null) {
			cluster.disconnect();
			cluster = null;
			instance = null;
			buckets = new HashMap<String, Bucket>();
		}
	}

	public CouchbaseRepositoryConfig getConfig() {
		return couchbaseRepositoryConfig;
	}
}
