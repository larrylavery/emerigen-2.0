/**
 * 
 */
package com.emerigen.infrastructure.repository.couchbase;

import java.util.List;
import org.json.JSONObject;


/**
 * @author Larry
 * 
 * Represents the service that handles IO to/from a specific data storage type,
 * as specified by the JSONObject type. A repository handles one 
 * JSON Object type only. The dataStorage "name" represents the JSONObject type. 
 * 
 * Although the underlying implementation may be any number of data 
 * implementations (CouchDB, Relational, File storage, memory, etc), 
 * the user of this interface remains unaware of the specific implementation.
 * The interface must be implemented fully for each specific dataStorage 
 * type (CouchDB, Relational, file storage, etc.) with no implementation
 * type leakage.
 * 
 * Implementations must verify that the JSONObject type field matches 
 * the dataStorage type (usually the dataStorage name) before inserting 
 * the JSONObject.
 * 
 * This is a "functional" interface so instance variables should be final.
 * This means that each connection to the data storage returns a new 
 * instance of the Repository implementation. The "connect" is done in the
 * constructor. The "disconnect" should be explicitly called or memory 
 * may leak.
 * 
 * log() requests do not return an indicator of success or failure. This is 
 * because millions of immutable facts (ie records), effectively infinite,
 * may be logged and a certain percentage of failures is expected with
 * minimal effect on the intended usage (ie machine learning, pattern 
 * recognition, etc). The frequency of Exceptions from all repositories
 * is monitored. When that frequency reaches a configurable threshold, the
 * repository will be restarted (or recreated) as necessary. Continued failures
 * after a configurable number of restarts/recreates result in more drastic
 * recovery processes until finally the repository is shut down permanently
 * until a sys admin fixes the underlying issues. 
 * 
 * query() takes an input string that represents the query language for the
 * specific dataStorage type. Although the queryType must match the dataStorage 
 * "type", There is no restriction on the contents of the query string as long 
 * as it is executable by the underlying dataStorage type. 
 *  
 *
 */
public interface Repository {

	public void log(JSONObject document);
	
	public List<JSONObject> simpleQuery(final String queryString);
		
	public void disconnect();
	
	public Repository reconnect();
	
	public CouchbaseRepositoryConfig getConfig();
}
