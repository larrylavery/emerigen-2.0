/**
 * 
 */
package com.emerigen.infrastructure.utils;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * @author Larry
 *
 */
public @interface ScheduledMethod {

	int start() default 1;

	int interval() default 1;

	ExecutionPriority priority() default ExecutionPriority.MEDIUM;

	String comments() default "No comments";
}
