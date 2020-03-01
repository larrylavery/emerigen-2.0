package samples;

import java.util.UUID;

import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.query.core.*;
import com.couchbase.client.java.query.*;
import com.couchbase.client.java.query.util.*;
import com.couchbase.client.java.search.facet.*;
import com.couchbase.client.java.search.queries.*;
import com.couchbase.client.java.search.result.SearchQueryResult;
import com.couchbase.client.java.search.result.SearchQueryRow;

public class CouchDBSampleSearch {

	public static void simpleTextQuery(Bucket bucket) {
		String indexName = "travel-sample-index-unstored";
		MatchQuery query = SearchQuery.match("swanky");

		SearchQueryResult result = bucket
				.query(new SearchQuery(indexName, query).limit(10));

		printResult("Simple Text Query", result);
	}

	public static void simpleTextQueryOnStoredField(Bucket bucket) {
//        String indexName = "travel-sample-index-stored";
		String indexName = "by_destination_airport";
		MatchQuery query = SearchQuery.match("MLA").field("destinationairport");

		SearchQueryResult result = bucket
				.query(new SearchQuery(indexName, query).limit(10).clearHighlight());

		printResult("Simple Text Query on Stored Field", result);
	}

	public static void simpleTextQueryOnNonDefaultIndex(Bucket bucket) {
		String indexName = "travel-sample-index-hotel-description";
		MatchQuery query = SearchQuery.match("swanky");

		SearchQueryResult result = bucket
				.query(new SearchQuery(indexName, query).limit(10));

		printResult("Simple Text Query on Non-Default Index", result);
	}

	public static void textQueryOnStoredFieldWithFacet(Bucket bucket) {
		String indexName = "travel-sample-index-stored";
		MatchQuery query = SearchQuery.match("La Rue Saint Denis!!")
				.field("reviews.content");

		SearchQueryResult result = bucket.query(
				new SearchQuery(indexName, query).limit(10).clearHighlight().addFacet(
						"Countries Referenced", SearchFacet.term("country", 5)));

		printResult("Match Query with Facet, Result by Row", result);

		System.out.println();
		System.out.println("Match Query with Facet, Result by hits:");
		System.out.println(result.hits());

		System.out.println();
		System.out.println("Match Query with Facet, Result by facet: ");
		System.out.println(result.facets());
	}

	public static void docIdQueryMethod(Bucket bucket) {
		String indexName = "travel-sample-index-unstored";
		DocIdQuery query = SearchQuery.docId("hotel_26223", "hotel_28960");

		SearchQueryResult result = bucket.query(new SearchQuery(indexName, query));

		printResult("DocId Query", result);
	}

	public static void unAnalyzedTermQuery(Bucket bucket, int fuzzinessLevel) {
		String indexName = "travel-sample-index-stored";
		TermQuery query = SearchQuery.term("sushi").field("reviews.content")
				.fuzziness(fuzzinessLevel);

		SearchQueryResult result = bucket
				.query(new SearchQuery(indexName, query).limit(50).clearHighlight());

		printResult(
				"Unanalyzed Term Query with Fuzziness Level of " + fuzzinessLevel + ":",
				result);
	}

	public static void matchPhraseQueryOnStoredField(Bucket bucket) {
		String indexName = "travel-sample-index-stored";
		MatchPhraseQuery query = SearchQuery.matchPhrase("Eiffel Tower")
				.field("description");

		SearchQueryResult result = bucket
				.query(new SearchQuery(indexName, query).limit(10).clearHighlight());

		printResult("Match Phrase Query, using Analysis", result);
	}

	public static void unAnalyzedPhraseQuery(Bucket bucket) {
		String indexName = "travel-sample-index-stored";
		PhraseQuery query = SearchQuery.phrase("dorm", "rooms").field("description");

		SearchQueryResult result = bucket
				.query(new SearchQuery(indexName, query).limit(10).clearHighlight());

		printResult("Phrase Query, without Analysis", result);
	}

	public static void conjunctionQueryMethod(Bucket bucket) {
		String indexName = "travel-sample-index-stored";
		MatchQuery firstQuery = SearchQuery.match("La Rue Saint Denis!!")
				.field("reviews.content");
		MatchQuery secondQuery = SearchQuery.match("boutique").field("description");

		ConjunctionQuery conjunctionQuery = SearchQuery.conjuncts(firstQuery,
				secondQuery);

		SearchQueryResult result = bucket.query(
				new SearchQuery(indexName, conjunctionQuery).limit(10).clearHighlight());

		printResult("Conjunction Query", result);
	}

	public static void queryStringMethod(Bucket bucket) {
		String indexName = "travel-sample-index-unstored";
		// StringQuery query = SearchQuery.string("description: Imperial");

		// SearchQueryResult result = bucket.query(
		// new SearchQuery(indexName, query).limit(10));

		// printResult("Query String Query", result);
	}

	public static void wildCardQueryMethod(Bucket bucket) {
		String indexName = "travel-sample-index-stored";
		WildcardQuery query = SearchQuery.wildcard("bouti*ue").field("description");

		SearchQueryResult result = bucket
				.query(new SearchQuery(indexName, query).limit(10).clearHighlight());

		printResult("Wild Card Query", result);
	}

	public static void numericRangeQueryMethod(Bucket bucket) {
		String indexName = "travel-sample-index-unstored";
		NumericRangeQuery query = SearchQuery.numericRange().min(10100).max(10200)
				.field("id");

		SearchQueryResult result = bucket
				.query(new SearchQuery(indexName, query).limit(10));

		printResult("Numeric Range Query", result);
	}

	public static void regexpQueryMethod(Bucket bucket) {
		String indexName = "travel-sample-index-stored";
		RegexpQuery query = SearchQuery.regexp("[a-z]").field("description");

		SearchQueryResult result = bucket
				.query(new SearchQuery(indexName, query).limit(10).clearHighlight());

		printResult("Regexp Query", result);
	}

	private static void printResult(String label, SearchQueryResult resultObject) {
		System.out.println();
		System.out.println("= = = = = = = = = = = = = = = = = = = = = = =");
		System.out.println("= = = = = = = = = = = = = = = = = = = = = = =");
		System.out.println();
		System.out.println(label);
		System.out.println();

		for (SearchQueryRow row : resultObject) {
			System.out.println(row);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Access the cluster that is running on the local host, authenticating with
		// the username and password of any user who has the "FTS Searcher" role
		// for the "travel-sample" bucket...
		//
		Cluster cluster = CouchbaseCluster.create("localhost");
		Bucket sensorReading = cluster.openBucket("Administrator", "12481632");

		// make several sensor readings
		SensorReading reading1 = new SensorReading(UUID.randomUUID().toString(), "", "5",
				1, (long) 1234, 0.0, 10.0, .5);
		SensorReading reading2 = new SensorReading(UUID.randomUUID().toString(), "", "5",
				1, (long) 1235, 0.0, 20.0, .7);
		SensorReading reading3 = new SensorReading(UUID.randomUUID().toString(), "", "5",
				1, (long) 1236, 1.0, 30.0, .4);
		SensorReading reading4 = new SensorReading(UUID.randomUUID().toString(), "", "5",
				1, (long) 1237, 1.0, 40.0, .9);

		// Add documents to the database with UUID
//		dbConnector.create(reading1.getId(), reading1);
//		dbConnector.create(reading2.getId(), reading2);
//		dbConnector.create(reading3.getId(), reading3);
//		dbConnector.create(reading4.getId(), reading4);

		sensorReading.upsert((Document<SensorReading>) reading1);

		// simpleTextQuery(sensorReading);

		/**
		 * simpleTextQueryOnStoredField(travelSample);
		 * 
		 * simpleTextQueryOnNonDefaultIndex(travelSample);
		 * 
		 * textQueryOnStoredFieldWithFacet(travelSample);
		 * 
		 * docIdQueryMethod(travelSample);
		 * 
		 * unAnalyzedTermQuery(travelSample, 0);
		 * 
		 * unAnalyzedTermQuery(travelSample, 2);
		 * 
		 * matchPhraseQueryOnStoredField(travelSample);
		 * 
		 * unAnalyzedPhraseQuery(travelSample);
		 * 
		 * conjunctionQueryMethod(travelSample);
		 * 
		 * queryStringMethod(travelSample);
		 * 
		 * wildCardQueryMethod(travelSample);
		 * 
		 * numericRangeQueryMethod(travelSample);
		 * 
		 * regexpQueryMethod(travelSample);
		 */
		cluster.disconnect();
	}
}