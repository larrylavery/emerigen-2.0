/**
 * 
 */
package com.emerigen.infrastructure.repository.couchbase;

import java.time.Duration;

import org.apache.log4j.Logger;

import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.TimeoutConfig;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.InsertOptions;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.UpsertOptions;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
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

	private static final String PASSWORD = EmerigenProperties.getInstance()
			.getValue("couchbase.server.password");
	private static final String LOCALHOST = EmerigenProperties.getInstance()
			.getValue("couchbase.server.localhost");
	private static final String ADMINISTRATOR = EmerigenProperties.getInstance()
			.getValue("couchbase.server.userid");
	private static Logger logger = Logger.getLogger(CouchbaseRepository.class);
	private static CouchbaseRepository instance;
	private ClusterEnvironment env;
	private Cluster cluster;
	private Bucket bucket;
	private final static int MAX_HTTP_CONNECTIONS = 2;
	private static final String KNOWLEDGE_DB = "knowledge";
	private Collection knowledgeCollection = null;

	public static CouchbaseRepository getInstance() {

		if (instance == null) {
			synchronized (CouchbaseRepository.class) {
				if (instance == null)
					instance = new CouchbaseRepository();
			}
		}
		// Return singleton CouchbaseRepository
		return instance;
	}

	private CouchbaseRepository() {

		// Create connection to the cluster and retrieve collection
		connect();
	}

	private void connect() {

		try {
			env = ClusterEnvironment.builder()
					.timeoutConfig(TimeoutConfig.kvTimeout(Duration.ofSeconds(5)))
					.ioConfig(IoConfig.maxHttpConnections(MAX_HTTP_CONNECTIONS)).build();

			// Create a cluster using those client settings.
			cluster = Cluster.connect(LOCALHOST, ClusterOptions
					.clusterOptions(ADMINISTRATOR, PASSWORD).environment(env));

			// Open a Collection connected to our knowledge db
			bucket = cluster.bucket(KNOWLEDGE_DB);
			knowledgeCollection = bucket.defaultCollection();
		} catch (Exception e) {
			throw new RepositoryException("query exception, cause: " + e);

		}

	}

	@Override
	protected void finalize() {
//TODO uncomment finalize later
		try {
			// Shut down gracefully.
			Thread.sleep(1000);
			cluster.disconnect();
			env.shutdown();
		} catch (Exception e) {
			throw new RepositoryException("query exception, cause: " + e);

		}

	}

	public void log(final String key, final JsonObject jsonObject, boolean synchronous) {
		MutationResult insertResult;

		try {
			// Wait for completion if synchronous
			if (synchronous) {
				insertResult = knowledgeCollection.insert(key, jsonObject, InsertOptions
						.insertOptions().durability(DurabilityLevel.PERSIST_TO_MAJORITY));
			} else {
				insertResult = knowledgeCollection.insert(key, jsonObject);
			}
		} catch (Exception e) {
			throw new RepositoryException("query exception, cause: " + e);

		}

	}

	public void replace(final String key, final JsonObject jsonObject,
			boolean synchronous) {
		MutationResult upsertResult;

		try {
			// Wait for completion if synchronous
			if (synchronous) {
				upsertResult = knowledgeCollection.upsert(key, jsonObject, UpsertOptions
						.upsertOptions().durability(DurabilityLevel.PERSIST_TO_MAJORITY));
			} else {
				// insert the jsonObject into my bucket
				upsertResult = knowledgeCollection.upsert(key, jsonObject);
			}
		} catch (Exception e) {
			throw new RepositoryException("query exception, cause: " + e);

		}

	}

	/**
	 * Query with parameters
	 * 
	 * @param parameterizedStatement Holds the string to be parameterized
	 * @param placeholderValues      Holds the parameters
	 * @return
	 */
	public QueryResult query(final String parameterizedStatement,
			final QueryOptions placeholderValues) {

		try {
			return cluster.query(parameterizedStatement, placeholderValues);
		} catch (Exception e) {
			throw new RepositoryException("query exception, cause: " + e);
		}

	}

	/**
	 * Query using a string that may contain embedded parameters
	 * 
	 * @param statement The statement containing embeded parms
	 * @return
	 */
	public QueryResult query(final String statement) {

		try {
			QueryResult queryResult = cluster.query(statement);
			return queryResult;
		} catch (Exception e) {
			throw new RepositoryException("query exception, cause: " + e);

		}

	}

	public void removeAllDocuments() {
		cluster.buckets().flushBucket(KNOWLEDGE_DB);
	}

	public JsonObject get(final String docID) {
		try {
			GetResult getResult = knowledgeCollection.get(docID);
			return getResult.contentAsObject();
		} catch (DocumentNotFoundException e) {
			logger.info("Document not found", e);
			return null;
		}

	}

	public JsonObject get(final String docID, String docType) {

		try {
			GetResult getResult = knowledgeCollection.get(docID);
			return getResult.contentAsObject();
		} catch (Exception e) {
			throw new RepositoryException("query exception, cause: " + e);

		}

	}

	public boolean isConnected() {
		if (instance == null || cluster == null) {
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
			env.shutdown();
			cluster = null;
			instance = null;
		}
	}

	/**
	 * Remove the document with the given key
	 * 
	 * @param key the key of the document to remove
	 */
	public void remove(String key) {
		if (key == null | key.isEmpty())
			throw new IllegalArgumentException("key must not be null or empty");

		try {
			knowledgeCollection.remove(key);
		} catch (Exception e) {
			throw new RepositoryException("query exception, cause: " + e);

		}

	}
}
