package com.emerigen.infrastructure.learning.cycle;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CircularListTest.class, CPR_ConstraintsTest.class, CPR_GeoFenceTest.class,
		CPR_InsertionsTest.class, CPR_LearningTest.class, CPR_PredictionTest.class,
		CPR_RolloverTest.class, CycleConstraintsTest.class,
		CyclePatternRecognizerTest.class, CycleTest.class })
public class AllTests {

}
