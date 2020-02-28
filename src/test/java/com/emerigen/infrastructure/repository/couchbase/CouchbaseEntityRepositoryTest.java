package com.emerigen.infrastructure.repository.couchbase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;

//import io.reactivex.Observable;

//@RunWith(MockitoJUnitRunner.class)
public class CouchbaseEntityRepositoryTest {

	@Test
	public final void givenEntityWithTwoChannelTypes_WhenChannelQueried_ThenItsSensoryEventSourcesShouldBeValid() {

//		"channels" : [
//		{
//			"channelType" : "ch-type-1",
//			"sensoryEventsUri" : "/Information/dev/logger/src/main/resources/channel-type-1.csv",
//			"learningComplete" : false
//	    }

		// Given - an Entity logged with two channelTypes pointing to a local
		// sensoryEvent file
		SoftAssertions softly = new SoftAssertions();
		JsonObject jsonObjectChannel = JsonObject.create()
				.put("channelType", "channelType1")
				.put("sensoryEventsUri",
						"/Information/dev/logger/src/main/resources/channel-type-1.csv")
				.put("learningComplete", false);

		JsonObject jsonObjectChannel2 = JsonObject.create()
				.put("channelType", "channelType2")
				.put("sensoryEventsUri",
						"/Information/dev/logger/src/main/resources/channel-type-1.csv")
				.put("learningComplete", true);

		String entityUuid1 = UUID.randomUUID().toString();
		JsonObject entityJsonDoc = JsonObject.create().put("entityID", entityUuid1)
				.put("timestamp", "1")
				.put("channels", JsonArray.from(jsonObjectChannel, jsonObjectChannel2));

		CouchbaseRepository.getInstance().log("entity", entityUuid1, entityJsonDoc,
				false);

		// Give the bucket a chance to catch up after the log
//		try {
//			Thread.sleep(500 + Long.parseLong(EmerigenProperties.getInstance()
//					.getValue("couchbase.server.logging.catchup.timer")));
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// When the document queried
		String queryString = "SELECT * FROM `entity` WHERE entityID = \"" + entityUuid1
				+ "\"";
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("entity",
				N1qlQuery.simple(queryString));

		// Then
		// Verify that sensory events can be retrieved from each sensory event source
		softly.assertThat(result).isNotNull().isNotEmpty();
		softly.assertThat(result.info().resultCount() == 1);

		JsonObject entityJsonObject = result.allRows().get(0).value().getObject("entity");

		softly.assertThat(entityJsonObject.getString("timestamp")).isEqualTo("1");
		softly.assertThat(entityJsonObject.getString("entityID")).isEqualTo(entityUuid1);
		softly.assertThat(entityJsonObject.getArray("channels")
				.equals(JsonArray.from(jsonObjectChannel, jsonObjectChannel2)));
		softly.assertAll();

		// And Then
		// Verify that learning-complete is marked on one channel (and not the other)
		// when the last sensoryEvent is consumed
	}

	@Test
	public final void givenEntityWithOneChannel_WhenChannelQueried_ThenItsSensoryEventSourceShouldBeValid() {

		// Given - Entity document with one channel logged
		SoftAssertions softly = new SoftAssertions();

		// Entity created with one channelType pointing to a local sensoryEvent file
		String entityUuid1 = UUID.randomUUID().toString();
		JsonObject channelType = JsonObject.create().put("channelType", "channelType1")
				.put("sensoryEventsURI",
						"/Information/dev/logger/src/main/resources/channel-type-1.csv")
				.put("learningComplete", true);

		JsonObject entityJsonDoc = JsonObject.create().put("entityID", entityUuid1)
				.put("timestamp", "1").put("channels", JsonArray.from(channelType));

		CouchbaseRepository.getInstance().log("entity", entityUuid1, entityJsonDoc,
				false);

		// Give the bucket a chance to catch up after the log
		// When the document queried
		String queryString = "SELECT * FROM `entity` WHERE entityID = \"" + entityUuid1
				+ "\"";
		N1qlQueryResult result = CouchbaseRepository.getInstance().query("entity",
				N1qlQuery.simple(queryString));

		// Then it should validate successfully
		assertThat(result).isNotNull().isNotEmpty();
		assertThat(result.info().resultCount() > 0);

		JsonObject entity2JsonObject = result.allRows().get(0).value()
				.getObject("entity");

		softly.assertThat(entity2JsonObject.getString("timestamp")).isEqualTo("1");
		softly.assertThat(entity2JsonObject.getString("entityID")).isEqualTo(entityUuid1);
		softly.assertThat(entity2JsonObject.getArray("channels"))
				.isEqualTo(JsonArray.from(channelType));

		softly.assertAll();

	}

}
