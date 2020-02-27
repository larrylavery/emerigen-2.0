package com.emerigen.infrastructure.learning;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.emerigen.infrastructure.learning.cycle.CPR_ConstraintsTest;
import com.emerigen.infrastructure.learning.cycle.CPR_InsertionsTest;
import com.emerigen.infrastructure.learning.cycle.CPR_LearningTest;
import com.emerigen.infrastructure.learning.cycle.CPR_PredictionTest;
import com.emerigen.infrastructure.learning.cycle.CPR_RolloverTest;
import com.emerigen.infrastructure.learning.cycle.CircularListTest;
import com.emerigen.infrastructure.learning.cycle.CycleConstraintsTest;
import com.emerigen.infrastructure.learning.cycle.CyclePatternRecognizerTest;
import com.emerigen.infrastructure.learning.cycle.CycleTest;

@RunWith(Suite.class)
@SuiteClasses({ CircularListTest.class, CycleConstraintsTest.class, PredictionTest.class,
		CyclePatternRecognizerTest.class, TransitionPatternRecognizerTest.class,
		TransitionPredictionTest.class, CPR_ConstraintsTest.class,
		CPR_InsertionsTest.class, CPR_LearningTest.class, CPR_PredictionTest.class,
		TransitionTest.class, CycleTest.class, Transition_MetadataTest.class,
		CPR_RolloverTest.class })
public class AllTests {

}
