package com.emerigen.infrastructure.utils;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface HighEventFrequencyHandler {

	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

	int threshold() default 1;

	int minimumTimeAboveThreshold() default 1;

	String frequencyEventName();
}
