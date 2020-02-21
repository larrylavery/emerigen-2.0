package com.emerigen.infrastructure.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ KnowledgePropertiesTest.class, ScheduledMethodTaskTest.class,
		ScheduledMethodAspectTest.class, LeakyBucketTest.class, UtilsTest.class,
		DynamicLoggingAspectTest.class })
public class AllTests {

}
