package samples;

import org.json.JSONObject;

import com.couchbase.client.core.env.KeyValueServiceConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

public class KeyedJsonObject {
	final private JsonObject jsonObject;
	final private String objectKey;

	public KeyedJsonObject(JsonObject jsonObject, String objectKey) {
		this.jsonObject = jsonObject;
		this.objectKey = objectKey;
	}

	/**
	 * @return the jsonObject
	 */
	public JsonObject getJsonObject() {
		return jsonObject;
	}

	/**
	 * @return the objectKey
	 */
	public String getObjectKey() {
		return objectKey;
	}

}
