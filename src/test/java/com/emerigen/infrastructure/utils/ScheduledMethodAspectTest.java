package com.emerigen.infrastructure.utils;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScheduledMethodAspectTest {


	private String originalProp = null;
	

	@Test
	public final void givenFourMethodsAnnotated_whenExecutedTwice_thenRunCountsAreCorrect() {
		//ScheduledMethodAspect aspect = new ScheduledMethodAspect();
		AnnotationExample annoEx = new AnnotationExample();
		List<ScheduledMethodTask> tasks = ScheduledMethodAspect.getAllScheduledMethodTasks();

		ScheduledMethodAspect.executeSteps(tasks,2);
		assertThat(AnnotationExample.getDoSomething1Count()).isEqualTo(2);
		assertThat(AnnotationExample.getDoSomething2Count()).isEqualTo(1);
  		assertThat(AnnotationExample.getDoSomething3Count()).isEqualTo(1);
		assertThat(AnnotationExample.getDoSomething4Count()).isEqualTo(1);
		assertThat(AnnotationExample.getDoSomethingElseCount()).isEqualTo(0);
//		@ScheduledMethod(start=1, interval=1, priority=ExecutionPriority.LOW)
//		public void doSomething1() {doSomething1Count++;}
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething2() {doSomething2Count++;}		
//		@ScheduledMethod(start=2, interval=3, priority=ExecutionPriority.MEDIUM)
//		public void doSomething3() {doSomething3Count++;}		
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething4() {doSomething4Count++;}
//		void doSomethingElse() {doSomethingElseCount++;}
		
		assertThat(tasks.size()).isEqualTo(4);

	}

	@Test
	public final void givenFourMethodsAnnotated_whenExecutedThreeTimes_thenRunCountsAreCorrect() {
		//ScheduledMethodAspect aspect = new ScheduledMethodAspect();
		AnnotationExample annoEx = new AnnotationExample();
		List<ScheduledMethodTask> tasks = ScheduledMethodAspect.getAllScheduledMethodTasks();

		ScheduledMethodAspect.executeSteps(tasks,3);
		assertThat(AnnotationExample.getDoSomething1Count()).isEqualTo(3);
		assertThat(AnnotationExample.getDoSomething2Count()).isEqualTo(2);
  		assertThat(AnnotationExample.getDoSomething3Count()).isEqualTo(1);
		assertThat(AnnotationExample.getDoSomething4Count()).isEqualTo(2);
		assertThat(AnnotationExample.getDoSomethingElseCount()).isEqualTo(0);
//		@ScheduledMethod(start=1, interval=1, priority=ExecutionPriority.LOW)
//		public void doSomething1() {doSomething1Count++;}
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething2() {doSomething2Count++;}		
//		@ScheduledMethod(start=2, interval=3, priority=ExecutionPriority.MEDIUM)
//		public void doSomething3() {doSomething3Count++;}		
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething4() {doSomething4Count++;}
//		void doSomethingElse() {doSomethingElseCount++;}
		
		assertThat(tasks.size()).isEqualTo(4);

	}

	@Test
	public final void givenFourMethodsAnnotated_whenExecutedFiveTimes_thenRunCountsAreCorrect() {
		//ScheduledMethodAspect aspect = new ScheduledMethodAspect();
		AnnotationExample annoEx = new AnnotationExample();
		List<ScheduledMethodTask> tasks = ScheduledMethodAspect.getAllScheduledMethodTasks();

		ScheduledMethodAspect.executeSteps(tasks,5);
		assertThat(AnnotationExample.getDoSomething1Count()).isEqualTo(5);
		assertThat(AnnotationExample.getDoSomething2Count()).isEqualTo(3);
  		assertThat(AnnotationExample.getDoSomething3Count()).isEqualTo(2);
		assertThat(AnnotationExample.getDoSomething4Count()).isEqualTo(3);
		assertThat(AnnotationExample.getDoSomethingElseCount()).isEqualTo(0);
//		@ScheduledMethod(start=1, interval=1, priority=ExecutionPriority.LOW)
//		public void doSomething1() {doSomething1Count++;}
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething2() {doSomething2Count++;}		
//		@ScheduledMethod(start=2, interval=3, priority=ExecutionPriority.MEDIUM)
//		public void doSomething3() {doSomething3Count++;}		
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething4() {doSomething4Count++;}
//		void doSomethingElse() {doSomethingElseCount++;}
		
		assertThat(tasks.size()).isEqualTo(4);

	}
	@Test
	public final void givenFourMethodsAnnotated_whenExecutedOnce_thenRunCountsAreCorrect() {
		//ScheduledMethodAspect aspect = new ScheduledMethodAspect();
		AnnotationExample annoEx = new AnnotationExample();
		List<ScheduledMethodTask> tasks = ScheduledMethodAspect.getAllScheduledMethodTasks();

		ScheduledMethodAspect.executeSteps(tasks,1);
		assertThat(AnnotationExample.getDoSomething1Count()).isEqualTo(1);
		assertThat(AnnotationExample.getDoSomething2Count()).isEqualTo(1);
  		assertThat(AnnotationExample.getDoSomething3Count()).isEqualTo(0);
		assertThat(AnnotationExample.getDoSomething4Count()).isEqualTo(1);
		assertThat(AnnotationExample.getDoSomethingElseCount()).isEqualTo(0);
//		@ScheduledMethod(start=1, interval=1, priority=ExecutionPriority.LOW)
//		public void doSomething1() {doSomething1Count++;}
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething2() {doSomething2Count++;}		
//		@ScheduledMethod(start=2, interval=3, priority=ExecutionPriority.MEDIUM)
//		public void doSomething3() {doSomething3Count++;}		
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething4() {doSomething4Count++;}
//		void doSomethingElse() {doSomethingElseCount++;}
		
		assertThat(tasks.size()).isEqualTo(4);

	}

	@Test
	public final void givenFourMethodsAnnotated_whenGetAllScheduledMethodTasks_thenFourReturned() {
		//ScheduledMethodAspect aspect = new ScheduledMethodAspect();
		List<ScheduledMethodTask> tasks = ScheduledMethodAspect.getAllScheduledMethodTasks();
		
		assertThat(tasks.size()).isEqualTo(4);

	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		originalProp = EmerigenProperties.getInstance().getValue("scheduled.methods.package");
		AnnotationExample.doSomething1Count = 0;
		AnnotationExample.doSomething2Count = 0;
		AnnotationExample.doSomething3Count = 0;
		AnnotationExample.doSomething4Count = 0;
		EmerigenProperties.getInstance().setValue("scheduled.methods.package",
				"com.emerigen.infrastructure.utils");

	}

	@After
	public void tearDown() throws Exception {
		EmerigenProperties.getInstance().setValue("scheduled.methods.package",
				originalProp);
	}

}
