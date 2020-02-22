package com.emerigen.infrastructure.learning;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CircularListTest.class, CycleConstraintsTest.class, PredictionTest.class,
		CyclePatternRecognizerTest.class, TransitionPatternRecognizerTest.class,
		TransitionPredictionTest.class, CPR_ConstraintsTest.class,
		CPR_InsertionsTest.class, CPR_LearningTest.class, CPR_PredictionTest.class,
		TransitionTest.class, CycleTest.class, CPR_RolloverTest.class })
public class AllTests {

}
