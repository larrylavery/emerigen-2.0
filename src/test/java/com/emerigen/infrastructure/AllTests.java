package com.emerigen.infrastructure;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ com.emerigen.infrastructure.environment.AllTests.class,
		com.emerigen.infrastructure.evaporation.AllTests.class,
		com.emerigen.infrastructure.repository.AllTests.class,
		com.emerigen.infrastructure.repository.couchbase.AllTests.class,
		com.emerigen.infrastructure.utils.AllTests.class, com.emerigen.knowledge.AllTests.class,
		com.emerigen.infrastructure.sensor.AllTests.class,
		com.emerigen.infrastructure.learning.AllTests.class })
public class AllTests {

}
