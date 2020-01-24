package com.emerigen.infrastructure.utils;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emerigen.infrastructure.repository.RepositoryException;
import com.emerigen.infrastructure.tracing.DynamicLoggingAspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;


public class DynamicLoggingAspectTest {

	private static Logger logger = Logger.getLogger(DynamicLoggingAspectTest.class);

	@Test
	public final void givenLoggingLevelSetToWarn_whenChangedToDebug_thenLoggingLevelshouldChange() {
		//given
		//DynamicLoggingAspect logAspect = new DynamicLoggingAspect();
		//assertThat(logger.isDebugEnabled()).isFalse();
		
		DynamicLoggingAspect.setLoggingLevel("DEBUG");
		assertThat(logger.isDebugEnabled()).isTrue();
		
		DynamicLoggingAspect.setLoggingLevel("WARN");
		assertThat(logger.isDebugEnabled()).isFalse();		
	}

	public static final void exceptionThrower() throws Exception {
		throw new RepositoryException("No text");		
	}

	@Test
	public final void givenLoggingAspectCreated_whenExceptionFrequencyExceeded_thenLoggingLevelChangedToDebug() {
		//given
		for (int i = 0; i < 5; i++) {
			try {
				DynamicLoggingAspectTest.exceptionThrower();
			} catch (Exception e) {	}			
		}
		
		//Then
		assertThat(logger.isDebugEnabled()).isTrue();	
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertThat(logger.isDebugEnabled()).isFalse();	
	}

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
