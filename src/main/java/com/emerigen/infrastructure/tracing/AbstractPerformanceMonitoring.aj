package com.emerigen.infrastructure.tracing;

//import ...
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

public abstract aspect AbstractPerformanceMonitoring {
    private Logger logger = Logger.getLogger(AbstractPerformanceMonitoring.class);

 	abstract pointcut monitoredOperations();

	abstract pointcut ignoredClassesAndMethods();

	// Capture the performance measurements for the specified classes
	Object around() : monitoredOperations()  {
        long start = System.nanoTime();
        try {
            return proceed();
        } finally {
            long complete = System.nanoTime();
            NDC.push("        ::: ");
            logger.info("Operation "
                    + thisJoinPointStaticPart.getSignature().toShortString() + " took "
                    + (complete - start) + " nanoseconds");
            NDC.pop();
        }
	}
}