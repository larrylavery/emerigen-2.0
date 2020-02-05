package com.emerigen.infrastructure.learning;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CircularListTest.class, CycleConstraintsTest.class,
		CyclePatternRecognizerTest.class, CyclePredictionTest.class, PredictionTest.class,
		TransitionPatternRecognizerTest.class, TransitionPredictionTest.class })
public class AllTests {

}
