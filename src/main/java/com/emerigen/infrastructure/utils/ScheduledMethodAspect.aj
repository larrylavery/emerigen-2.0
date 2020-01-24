package com.emerigen.infrastructure.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import com.emerigen.infrastructure.environment.Environment;

public aspect ScheduledMethodAspect {

	private static Logger logger = Logger.getLogger(ScheduledMethod.class);

	private static int numberOfStepsToExecute = Integer
			.parseInt(EmerigenProperties.getInstance().getValue("steps.number.to.execute"));
	private static int currentStep = 1;
	private static List<ScheduledMethodTask> scheduledMethodTasks;

	// Select Environment initialization join point
	pointcut environmentCreation(Environment env) :
		initialization(Environment.new(..)) && this(env);

	// Initialize ScheduledMethod aspect once during Environment initialization
	after(Environment env) returning : environmentCreation(env) {

		// get sorted (on priority) method list with @ScheduledMethod annotation
		scheduledMethodTasks = getAllScheduledMethodTasks();

		executeSteps(scheduledMethodTasks, numberOfStepsToExecute);
	}

	public static void executeSteps(List<ScheduledMethodTask> scheduledMethodTasks, int numberOfStepsToExecute) {

		for (int step = 0; step < numberOfStepsToExecute; step++) {
			currentStep = step + 1;
			// Determine which tasks should be executed in this step
			List<ScheduledMethodTask> tasksToExecuteInThisStep = scheduledMethodTasks.stream()
					.filter(task -> task.shouldExecute(currentStep)).collect(Collectors.toList());

			// Execute all selected tasks in this step
			ExecuteStep(tasksToExecuteInThisStep);
		}
	}

	/**
	 * Execute all ScheduledMethods for this time step
	 * 
	 * @param scheduledMethodTasks
	 */
	public static void ExecuteStep(List<ScheduledMethodTask> scheduledMethodTasks) {
		int threadPoolSze = Integer.parseInt(EmerigenProperties.getInstance().getValue("steps.thread.count"));
		int threadWaitTime = Integer.parseInt(EmerigenProperties.getInstance().getValue("steps.thread.wait.time"));

		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSze);
		for (ScheduledMethodTask scheduledMethodTask : scheduledMethodTasks) {
			executor.execute(scheduledMethodTask);
		}
		
		//Shutdown for this step after each thread is given a configurable amount of time to execute
		shutdownExecutorService(executor, threadWaitTime * scheduledMethodTasks.size());
	}

	/**
	 * Shuts down the threadpool in an orderly fashion.
	 * 
	 * @param executor
	 */
	private static void shutdownExecutorService(ExecutorService executor, int waitTimeInMillis) {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(waitTimeInMillis, TimeUnit.MILLISECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException thrown during shutdown: " + e.getMessage());
			executor.shutdownNow();
		}
	}

	/**
	 * 
	 * @return the sorted (on priority) list of annotated methods
	 */
	public static List<ScheduledMethodTask> getAllScheduledMethodTasks() {
		List<ScheduledMethodTask> scheduledMethodTasks = new ArrayList<ScheduledMethodTask>();

		// Locate all methods in the project with the @ScheduledMethod annotation
		String packagesToScan = EmerigenProperties.getInstance().getValue("scheduled.methods.package");
		Set<Method> methodsAnnotatedWith = new Reflections(packagesToScan, new MethodAnnotationsScanner())
				.getMethodsAnnotatedWith(ScheduledMethod.class);
		logger.info("List of methods in project annotated with @ScheduledMethodst: " + methodsAnnotatedWith);

		// Create ScheduledMethodTasks for each annotated method
		int start, interval;
		ExecutionPriority priority;
		String comments;
		ScheduledMethodTask task;

		for (Method method : methodsAnnotatedWith) {

			// Retrieve annotation parameters
			ScheduledMethod[] scheduledMethodAnno = method.getAnnotationsByType(ScheduledMethod.class);
			start = scheduledMethodAnno[0].start();
			interval = scheduledMethodAnno[0].interval();
			priority = scheduledMethodAnno[0].priority();
			comments = scheduledMethodAnno[0].comments();
			task = new ScheduledMethodTask(method, start, interval, priority, comments);

			logger.info("Annotated task added to task list: " + task.toString());
			scheduledMethodTasks.add(task);
		}

		Collections.sort(scheduledMethodTasks, Collections.reverseOrder());
		return scheduledMethodTasks;
	}
}
