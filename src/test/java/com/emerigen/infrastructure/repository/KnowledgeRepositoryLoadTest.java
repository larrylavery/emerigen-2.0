package com.emerigen.infrastructure.repository;


import org.jsmart.zerocode.core.domain.LoadWith;
import org.jsmart.zerocode.core.domain.TestMapping;
import org.jsmart.zerocode.core.runner.parallel.ZeroCodeLoadRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@LoadWith("load_config.properties")
@TestMapping(testClass = KnowledgeRepositoryTest.class, testMethod = "givenValidSensorEvent_whenTranslatedAndLogged_thenItshouldBeTheSameWhenRetrieved")
@RunWith(ZeroCodeLoadRunner.class)
public class KnowledgeRepositoryLoadTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
