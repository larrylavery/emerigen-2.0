package com.emerigen.infrastructure.evaporation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.emerigen.infrastructure.environment.EnvironmentTest;

@RunWith(Suite.class)
@SuiteClasses({ EnvironmentTest.class, EvaporationTest.class })
public class AllTests {

}
