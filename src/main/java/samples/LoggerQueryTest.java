package samples;
/**
 * @Test public final void
 *       givenValidPatternWithParmsDocLogged_WhenQueriedByEntityIdAndChannelType_ThenDocShouldBeFound()
 *       {
 * 
 *       // Given - A Connection to the repository has been established dbInfo =
 *       new CouchbaseRepositoryConfig(LOCALHOST_IP, ADMINISTRATOR, PASSWORD,
 *       "pattern", PATTERN_ID_FIELD); CouchbaseRepository patternRepository =
 *       new CouchbaseRepository(env, dbInfo);
 * 
 *       SoftAssertions softly = new SoftAssertions();
 * 
 *       // Create a JSON Document String patternID =
 *       UUID.randomUUID().toString(); JsonObject pattern = JsonObject.create()
 *       .put("patternID", patternID) .put("timestamp", "time1")
 *       .put("entityID", "entityID1") .put("channelType", "channelType11")
 *       .put("sensoryPattern", JsonArray.from("11.11", "22.22"));
 * 
 *       // Store the Document patternRepository.log("pattern11", pattern);
 * 
 *       // Load the Document and print it System.out.println("\n\nRight after
 *       insert" + patternRepository.get("pattern11"));
 * 
 *       // Create a N1QL Primary Index (but ignore if it exists) //
 *       bucket.bucketManager().createN1qlPrimaryIndex(true, false);
 * 
 *       // Perform a N1QL Query N1qlQueryResult result =
 *       patternRepository.query(N1qlQuery .parameterized("SELECT * FROM
 *       `pattern` WHERE channelType = $1", JsonArray.from("channelType11")));
 * 
 *       // Print each found Row result.forEach(System.out::println);
 * 
 *       // Then the document should be retrieved successfully
 *       assertThat(result).isNotNull().isNotEmpty();
 *       assertThat(result.info().resultCount()).isEqualTo(1);
 * 
 *       //Retrieve the "pattern" sub-document JsonObject patternJsonObject =
 *       ((N1qlQueryRow)result.allRows().get(0)) .value().getObject("pattern");
 * 
 *       softly.assertThat(patternJsonObject.getString("patternID")).isEqualTo(patternID);
 *       softly.assertThat(patternJsonObject.getString("timestamp")).isEqualTo("time1");
 *       softly.assertThat(patternJsonObject.getString("entityID")).isEqualTo("entityID1");
 *       softly.assertThat(patternJsonObject.getString("channelType")).isEqualTo("channelType11");
 *       softly.assertThat(patternJsonObject.getArray("sensoryPattern")).isEqualTo(JsonArray.from("11.11",
 *       "22.22")); softly.assertAll();
 * 
 *       }
 * 
 * @Test public final void sampleTest() {
 * 
 *       // Given - A Connection to the repository has been established dbInfo =
 *       new CouchbaseRepositoryConfig(LOCALHOST_IP, ADMINISTRATOR, PASSWORD,
 *       "person-sample", PATTERN_ID_FIELD); CouchbaseRepository
 *       personRepository = new CouchbaseRepository(env, dbInfo);
 * 
 *       // Create a JSON Document JsonObject arthur =
 *       JsonObject.create().put("name", "Arthur Jr").put("email",
 *       "kingarthur@couchbase.com") .put("interests", JsonArray.from("Movies",
 *       "Guns"));
 * 
 *       // Store the Document personRepository.log("u:larry_arthur", arthur);
 * 
 *       // Load the Document and print it System.out.println("\n\n" +
 *       personRepository.get("u:larry_arthur"));
 * 
 *       // Create a N1QL Primary Index (but ignore if it exists) //
 *       bucket.bucketManager().createN1qlPrimaryIndex(true, false);
 * 
 *       // Perform a N1QL Query N1qlQueryResult result =
 *       personRepository.query(N1qlQuery .parameterized("SELECT name FROM
 *       `person-sample` WHERE $1 IN interests", JsonArray.from("Movies")));
 * 
 *       // Print each found Row for (N1qlQueryRow row : result) { // Prints
 *       {"name":"Arthur"} System.out.println("\n\n" + row); }
 * 
 *       }
 * 
 * @Test public final void
 *       givenValidPatternDocLogged_WhenQueriedByPrimaryKey_ThenDocShouldBeFound()
 *       {
 * 
 *       // Given - A Connection to the repository has been established dbInfo =
 *       new CouchbaseRepositoryConfig(LOCALHOST_IP, ADMINISTRATOR, PASSWORD,
 *       PATTERN, PATTERN_ID_FIELD); CouchbaseRepository patternRepository = new
 *       CouchbaseRepository(env, dbInfo);
 * 
 *       SoftAssertions softly = new SoftAssertions();
 * 
 *       // add the document String patternID = UUID.randomUUID().toString();
 *       JsonObject patternJsonDoc = JsonObject.create();
 *       patternJsonDoc.put(PATTERN_ID_FIELD, patternID).put("channelType",
 *       "channelType1").put("entityID", "entity11") .put("timestamp",
 *       "200").put("sensoryPattern", JsonArray.from("1.1", "2.2", "3.3"));
 * 
 *       // Log using our repository under test patternRepository.log(patternID,
 *       patternJsonDoc);
 * 
 *       // Retrieve by primary using the repository under test JsonDocument
 *       getDoc = patternRepository.get(patternID);
 * 
 *       assertThat(getDoc).isNotNull();
 *       softly.assertThat(getDoc.content().get("patternID")).isEqualTo(patternID);
 *       softly.assertThat(getDoc.content().get("channelType")).isEqualTo("channelType1");
 *       softly.assertThat(getDoc.content().get("entityID")).isEqualTo("entity11");
 *       softly.assertThat(getDoc.content().get("timestamp")).isEqualTo("200");
 *       softly.assertThat(getDoc.content().get("sensoryPattern").equals(JsonArray.from("1.1",
 *       "2.2", "3.3")));
 * 
 *       JsonDocument getDoc2 = patternRepository.get("invalid-pattern-id");
 *       softly.assertThat(getDoc2).isNull(); softly.assertAll();
 * 
 *       }
 * 
 * @Test public final void
 *       givenValidPatternDocLogged_WhenQueriedBySimpleStatement_ThenDocShouldBeFound()
 *       {
 * 
 *       // Given - A Connection to the repository has been established dbInfo =
 *       new CouchbaseRepositoryConfig(LOCALHOST_IP, ADMINISTRATOR, PASSWORD,
 *       PATTERN, PATTERN_ID_FIELD); CouchbaseRepository patternRepository = new
 *       CouchbaseRepository(env, dbInfo);
 * 
 *       SoftAssertions softly = new SoftAssertions();
 * 
 *       // Create a JSON Document String patternID =
 *       UUID.randomUUID().toString(); JsonObject patternJsonDoc =
 *       JsonObject.create() .put(PATTERN_ID_FIELD, patternID)
 *       .put("channelType", "channelType11") .put("entityID","entity111")
 *       .put("timestamp", "2000") .put("sensoryPattern",JsonArray.from("1.1",
 *       "2.2", "3.3"));
 * 
 *       // Log using our repository under test patternRepository.log(patternID,
 *       patternJsonDoc);
 * 
 *       // Retrieve all patterns N1qlQueryResult result =
 *       patternRepository.query(N1qlQuery.simple("SELECT * FROM `pattern`"));
 *       result.forEach(System.out::println);
 * 
 *       assertThat(result).isNotNull().isNotEmpty();
 *       assertThat(result.info().resultCount()).isEqualTo("1"); N1qlQueryRow
 *       row = result.allRows().get(0);
 *       softly.assertThat(row.value().getString("patternID")).isEqualTo(patternID);
 *       softly.assertThat(row.value().getString("pattern.timestamp")).isEqualTo("200");
 *       softly.assertThat(row.value().getString("pattern.entityID")).isEqualTo("100");
 *       softly.assertAll();
 * 
 *       }
 */