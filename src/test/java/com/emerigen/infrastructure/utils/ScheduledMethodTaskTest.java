package com.emerigen.infrastructure.utils;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

public class ScheduledMethodTaskTest {


	@Test
	public final void givenStart2AndCurrentStepOne_whenCreated_thenShouldExecuteIsFalse() {
		Set<Method> methodsAnnotatedWith = new Reflections("com.emerigen", new MethodAnnotationsScanner())
				.getMethodsAnnotatedWith(ScheduledMethod.class);
		Method m = methodsAnnotatedWith.iterator().next();
		ScheduledMethodTask task = new ScheduledMethodTask(m, 2, 1, ExecutionPriority.HIGH, "any comments");

		assertThat(task.shouldExecute(1)).isFalse();
		assertThat(task.shouldExecute(2)).isTrue();
		assertThat(task.shouldExecute(3)).isTrue();
	}

	
	//@Test
	//TODO this test is better tested from the ScheduledMethodaspect since it requires
	//all plumbing to be in place which makes for a huge test.. Comment out for now
/**	public final void givenFourValidTasksCreated_whenStepsExecutedAtVariousIntervals_thenRunCountsAreValid() {

		Set<Method> methodsAnnotatedWith = new Reflections("com.emerigen", new MethodAnnotationsScanner())
				.getMethodsAnnotatedWith(ScheduledMethod.class);
		AnnotationExample annoEx = new AnnotationExample();
	
		//List<ScheduledMethodTask> scheduledMethodTasks = getAllScheduledMethodTasks();

		//List<ScheduledMethodTask> tasksToExecuteInThisStep = scheduledMethodTasks.stream()
		//		.filter(task -> task.shouldExecute(currentStep)).collect(Collectors.toList());


		// Do first step
		for (Method method : methodsAnnotatedWith) {
			try {
				method.setAccessible(true);
				method.invoke(annoEx, (Object[]) null);
				System.out.println("iterating methods: " + method);
			} catch (Exception x) {
				System.out.println(x.getMessage());
			}
		}
		assertThat(annoEx.getDoSomething1Count()).isEqualTo(1);
		assertThat(annoEx.getDoSomething2Count()).isEqualTo(1);
  		assertThat(annoEx.getDoSomething3Count()).isEqualTo(0);
		assertThat(annoEx.getDoSomething4Count()).isEqualTo(1);
		assertThat(annoEx.getDoSomethingElseCount()).isEqualTo(0);
//		@ScheduledMethod(start=1, interval=1, priority=ExecutionPriority.LOW)
//		public void doSomething1() {doSomething1Count++;}
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething2() {doSomething2Count++;}		
//		@ScheduledMethod(start=2, interval=3, priority=ExecutionPriority.MEDIUM)
//		public void doSomething3() {doSomething3Count++;}		
//		@ScheduledMethod(start=1, interval=2, priority=ExecutionPriority.HIGH)
//		public void doSomething4() {doSomething4Count++;}
//		void doSomethingElse() {doSomethingElseCount++;}


		// Do 2nd step
		for (Method method : methodsAnnotatedWith) {
			try {
				method.setAccessible(true);
				method.invoke(annoEx, (Object[]) null);
			} catch (Exception x) {
				System.out.println(x.getMessage());
			}
		}
		assertThat(annoEx.getDoSomething1Count() == 2);
		assertThat(annoEx.getDoSomething2Count()).isEqualTo(1);
		assertThat(annoEx.getDoSomething3Count() == 1);
		assertThat(annoEx.getDoSomething4Count() == 1);
		assertThat(annoEx.getDoSomethingElseCount() == 0);

		// Do 3rd step
		for (Method method : methodsAnnotatedWith) {
			try {
				method.setAccessible(true);
				method.invoke(annoEx, (Object[]) null);
			} catch (Exception x) {
				System.out.println(x.getMessage());
			}
		}
		assertThat(annoEx.getDoSomething1Count() == 3);
		assertThat(annoEx.getDoSomething2Count() == 2);
		assertThat(annoEx.getDoSomething3Count() == 1);
		assertThat(annoEx.getDoSomething4Count() == 2);
		assertThat(annoEx.getDoSomethingElseCount() == 0);

		// Do 4th step
		for (Method method : methodsAnnotatedWith) {
			try {
				method.setAccessible(true);
				method.invoke(annoEx, (Object[]) null);
			} catch (Exception x) {
				System.out.println(x.getMessage());
			}
		}
		assertThat(annoEx.getDoSomething1Count() == 4);
		assertThat(annoEx.getDoSomething2Count() == 2);
		assertThat(annoEx.getDoSomething3Count() == 1);
		assertThat(annoEx.getDoSomething4Count() == 2);
		assertThat(annoEx.getDoSomethingElseCount() == 0);

	}
*/
	@Test
	public final void givenStartThreeAndCurrentStepOne_whenCreated_thenShouldExecuteIsFalse() {
		Set<Method> methodsAnnotatedWith = new Reflections("com.emerigen", new MethodAnnotationsScanner())
				.getMethodsAnnotatedWith(ScheduledMethod.class);
		Method m = methodsAnnotatedWith.iterator().next();
		ScheduledMethodTask task = new ScheduledMethodTask(m, 3, 1, ExecutionPriority.HIGH, "any comments");

		assertThat(task.shouldExecute(1)).isFalse();
		assertThat(task.shouldExecute(2)).isFalse();
		assertThat(task.shouldExecute(3)).isTrue();
	}

	@Test
	public final void givenIntervalFour_whenCreated_thenShouldExecuteActsCorrectly() {
		Set<Method> methodsAnnotatedWith = new Reflections("com.emerigen", new MethodAnnotationsScanner())
				.getMethodsAnnotatedWith(ScheduledMethod.class);
		Method m = methodsAnnotatedWith.iterator().next();
		ScheduledMethodTask task = new ScheduledMethodTask(m, 1, 4, ExecutionPriority.HIGH, "any comments");

		assertThat(task.shouldExecute(2)).isFalse();
		assertThat(task.shouldExecute(3)).isFalse();
		assertThat(task.shouldExecute(5)).isTrue();
		assertThat(task.shouldExecute(9)).isTrue();
		assertThat(task.shouldExecute(13)).isTrue();

	}

	@Test
	public final void givenThreePriorities_whenCreated_thenTheyShouldSortDescending() {
		Set<Method> methodsAnnotatedWith = new Reflections("com.emerigen", new MethodAnnotationsScanner())
				.getMethodsAnnotatedWith(ScheduledMethod.class);
		Method m = methodsAnnotatedWith.iterator().next();

		ScheduledMethodTask task1 = new ScheduledMethodTask(m, 1, 4, ExecutionPriority.LOW, "any comments");
		ScheduledMethodTask task2 = new ScheduledMethodTask(m, 1, 4, ExecutionPriority.HIGH, "any comments");
		ScheduledMethodTask task3 = new ScheduledMethodTask(m, 1, 4, ExecutionPriority.MEDIUM, "any comments");
		ScheduledMethodTask task4 = new ScheduledMethodTask(m, 1, 4, ExecutionPriority.MEDIUM, "any comments");

		List<ScheduledMethodTask> tasks = new ArrayList<ScheduledMethodTask>();
		tasks.add(task1);
		tasks.add(task2);
		tasks.add(task3);
		tasks.add(task4);
		Collections.sort(tasks, Collections.reverseOrder());

		assertThat(tasks.get(0).getPriority()).isEqualTo(ExecutionPriority.HIGH);
		assertThat(tasks.get(1).getPriority()).isEqualTo(ExecutionPriority.MEDIUM);
		assertThat(tasks.get(2).getPriority()).isEqualTo(ExecutionPriority.MEDIUM);
		assertThat(tasks.get(3).getPriority()).isEqualTo(ExecutionPriority.LOW);
	}

	@Test
	public final void givenNullMethod_whenTaskCreated_thenIllegalArgumentException() {

		// Given

		// When
		Method m = null;
		final Throwable throwable = catchThrowable(
				() -> new ScheduledMethodTask(m, 1, 1, ExecutionPriority.HIGH, "any comments"));

		// Then a ValidationException should not be thrown
		then(throwable).as("A non-positive start throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNonPositiveFrequency_whenTaskCreated_thenIllegalArgumentException() {

		// Given

		// When
		Method m = null;
		final Throwable throwable = catchThrowable(
				() -> new ScheduledMethodTask(m, 1, 0, ExecutionPriority.HIGH, "any comments"));

		// Then a ValidationException should not be thrown
		then(throwable).as("A non-positive start throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public final void givenNonPositiveStart_whenTaskCreated_thenIllegalArgumentException() {

		// Given

		// When
		Method m = null;
		final Throwable throwable = catchThrowable(
				() -> new ScheduledMethodTask(m, 0,
						1, ExecutionPriority.HIGH, "any comments"));

		// Then a ValidationException should not be thrown
		then(throwable).as("A non-positive start throws IllegalArgumentException")
				.isInstanceOf(IllegalArgumentException.class);
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
