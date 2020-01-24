package com.emerigen.infrastructure.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

public class ScheduledMethodTask implements Runnable, Comparable<ScheduledMethodTask> {

	private final int start;
	private final int interval;
	private final ExecutionPriority priority;
	private  String comments;
	private final Method methodToExecute;
	private static Logger logger = Logger.getLogger(ScheduledMethodTask.class);

	public ScheduledMethodTask(Method methodToExecute, 
			int start, int interval, 
			ExecutionPriority priority,
			String comments) {
		this(methodToExecute, start, interval, priority);
		if (comments == null)
			throw new IllegalArgumentException("Comments must not be null");

		this.comments = comments;
	}

	public ScheduledMethodTask(Method methodToExecute, 
			int start, int frequency, 
			ExecutionPriority priority) {
		if (start <= 0)
			throw new IllegalArgumentException("Start must be a positive number");
		if (frequency <= 0)
			throw new IllegalArgumentException("interval must be a positive number");
		if (methodToExecute == null)
			throw new IllegalArgumentException("method must not be null");
		
		this.methodToExecute = methodToExecute;
		this.interval = frequency;
		this.priority = priority;
		this.start = start;
	}


	@Override
	public void run() {
		// Execute the method annotated with @ScheduledMethod

		try {

			Class<?> clazz = methodToExecute.getDeclaringClass();
			Constructor<?> ctor = clazz.getConstructor();
			Object object = ctor.newInstance(new Object[] { });
			
			methodToExecute.setAccessible(true);
			methodToExecute.invoke(object, (Object[]) null);

			// Handle any exceptions thrown by method to be invoked.
		} catch (InvocationTargetException x) {
			Throwable cause = x.getCause();
			logger.error("InvocationTargetException thrown, message: " + x.getMessage(), cause);

		} catch (IllegalAccessException x) {
			Throwable cause = x.getCause();
			logger.error("IllegalAccessException thrown, message: " + x.getMessage(), cause);

		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException thrown, message: " + e.getMessage());
			e.printStackTrace();

		} catch (SecurityException e) {
			logger.error("SecurityException thrown, message: " + e.getMessage());
			e.printStackTrace();

		} catch (InstantiationException e) {
			logger.error("InstantiationException thrown, message: " + e.getMessage());
			e.printStackTrace();

		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException thrown, message: " + e.getMessage());
			e.printStackTrace();
		}

	}

	
	public boolean shouldExecute(int currentStep) {

		// If we should execute at later step return false
		if (getStart() > currentStep)
			return false;

		// If we should execute at later step return false
		if (getStart() == currentStep)
			return true;
		
		// If the interval since the last execution has not been reached
		if ((currentStep-getStart()) % interval == 0)
			return true;

		return false;

	}

	public int getStart() {
		return start;
	}

	public int getInterval() {
		return interval;
	}

	public ExecutionPriority getPriority() {
		return priority;
	}
	
	public int compareTo(ScheduledMethodTask other) {
		return this.getPriority().compareTo(other.getPriority());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + interval;
		result = prime * result + ((methodToExecute == null) ? 0 : methodToExecute.hashCode());
		result = prime * result + start;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScheduledMethodTask other = (ScheduledMethodTask) obj;
		if (interval != other.interval)
			return false;
		if (methodToExecute == null) {
			if (other.methodToExecute != null)
				return false;
		} else if (!methodToExecute.equals(other.methodToExecute))
			return false;
		if (priority != other.priority)
			return false;
		if (start != other.start)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ScheduledMethodTask [start=" + start + ", interval=" + interval + ", priority=" + priority
				+ ", comments=" + comments + ", methodToExecute=" + methodToExecute + "]";
	}
}
