package com.emerigen.knowledge;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.emerigen.infrastructure.learning.CycleTest;
import com.emerigen.infrastructure.learning.TransitionTest;

@RunWith(Suite.class)
@SuiteClasses({ TransitionTest.class, CycleTest.class, EntityTest.class, })
public class AllTests {

}
