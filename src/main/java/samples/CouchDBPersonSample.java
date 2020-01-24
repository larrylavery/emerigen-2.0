package samples;

import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.query.*;

public class CouchDBPersonSample {

    public static void main(String... args) throws Exception {

        // Initialize the Connection
        Cluster cluster = CouchbaseCluster.create("localhost");
        cluster.authenticate("Administrator", "12481632");
        Bucket bucket = cluster.openBucket("person-sample");

        // Create a JSON Document
        JsonObject arthur = JsonObject.create()
            .put("name", "Arthur Jr")
            .put("email", "kingarthur@couchbase.com")
            .put("interests", JsonArray.from("Movies", "Guns"));

        // Store the Document
        bucket.upsert(JsonDocument.create("u:larry_arthur", arthur));

        // Load the Document and print it
        // Prints Content and Metadata of the stored Document
        System.out.println("\n\n" + bucket.get("u:larry_arthur"));

        // Create a N1QL Primary Index (but ignore if it exists)
        bucket.bucketManager().createN1qlPrimaryIndex(true, false);

        // Perform a N1QL Query
        N1qlQueryResult result = bucket.query(
            N1qlQuery.parameterized("SELECT name FROM `person-sample` WHERE $1 IN interests",
            JsonArray.from("Movies"))
        );

        // Print each found Row
        for (N1qlQueryRow row : result) {
            // Prints {"name":"Arthur"}
            System.out.println("\n\n" + row);
        }
    }
}