package com.emerigen.infrastructure.repository.couchbase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CouchbaseEntityRepositoryTest.class,
		CouchbaseSensorEventRepositoryTest.class, CouchbaseRepositoryLifecycleTest.class,
		CouchbaseCycleRepositoryTest.class, CouchbaseRepositoryQueryTest.class })
public class AllTests {

}
