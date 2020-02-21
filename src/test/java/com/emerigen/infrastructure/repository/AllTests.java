package com.emerigen.infrastructure.repository;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.emerigen.infrastructure.utils.KnowledgePropertiesTest;

@RunWith(Suite.class)
@SuiteClasses({ JsonSchemasTest.class,
		// KnowledgeRepositoryLoadTest.class,
		KnowledgeRepositoryTest.class })
public class AllTests {

}
