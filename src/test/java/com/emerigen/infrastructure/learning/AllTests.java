package com.emerigen.infrastructure.learning;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CircularListTest.class, CycleConstraintsTest.class, CyclePredictionTest.class,
		PredictionTest.class, TransitionPatternRecognizerTest.class, TransitionPredictionTest.class,
		CPR_ConstraintsTest.class, CPR_InsertionsTest.class, CPR_LearningTest.class, CPR_PredictionTest.class,
		CPR_RolloverTest.class })
public class AllTests {

}
