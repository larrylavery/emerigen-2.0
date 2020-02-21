package com.emerigen.infrastructure.utils;

public class AnnotationExample {

	public static int doSomething1Count = 0, doSomething2Count = 0, doSomething3Count = 0,
			doSomethingElseCount = 0, doSomething4Count = 0;

	public AnnotationExample() {
	}

	@ScheduledMethod(start = 1, interval = 1, priority = ExecutionPriority.LOW)
	public void doSomething1() {
		++doSomething1Count;
	}

	@ScheduledMethod(start = 1, interval = 2, priority = ExecutionPriority.HIGH)
	public void doSomething2() {
		++doSomething2Count;
	}

	@ScheduledMethod(start = 2, interval = 3, priority = ExecutionPriority.MEDIUM)
	public void doSomething3() {
		++doSomething3Count;
	}

	@ScheduledMethod(start = 1, interval = 2, priority = ExecutionPriority.HIGH)
	public void doSomething4() {
		++doSomething4Count;
	}

	void doSomethingElse() {
		++doSomethingElseCount;
	}

	/**
	 * @return the doSomething1Count
	 */
	public static int getDoSomethingelseCount() {
		return doSomething1Count;
	}

	public static int getDoSomething1Count() {
		return doSomething1Count;
	}

	/**
	 * @return the doSomething2Count
	 */
	public static int getDoSomething2Count() {
		return doSomething2Count;
	}

	/**
	 * @return the doSomething5Count
	 */
	public static int getDoSomething3Count() {
		return doSomething3Count;
	}

	/**
	 * @return the doSomething4Count
	 */
	public static int getDoSomething4Count() {
		return doSomething4Count;
	}

	public static int getDoSomethingElseCount() {
		return doSomethingElseCount;
	}
}
