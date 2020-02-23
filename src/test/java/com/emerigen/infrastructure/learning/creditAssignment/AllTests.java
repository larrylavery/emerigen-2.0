package com.emerigen.infrastructure.learning.creditAssignment;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BidTest.class, CreditAssignmentModeratorTest.class,
		PredictionConsumerTest.class, PredictionSupplierTest.class })
public class AllTests {

}
