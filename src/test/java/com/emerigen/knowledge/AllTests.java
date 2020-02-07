package com.emerigen.knowledge;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TransitionTest.class, CycleTest.class, EntityTest.class, })
public class AllTests {

}
