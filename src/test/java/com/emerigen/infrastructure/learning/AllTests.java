package com.emerigen.infrastructure.learning;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CircularListTest.class, CycleConstraintsTest.class, CycleLearningTest.class })
public class AllTests {

}
