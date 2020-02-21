package com.emerigen.infrastructure.environment;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AgentTest.class, EnvironmentTest.class, MessageSpreadingTest.class,
		NeighborhoodTest.class })
public class AllTests {

}
