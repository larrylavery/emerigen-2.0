package com.emerigen.infrastructure.tracing;



import com.emerigen.infrastructure.environment.Environment;
import com.emerigen.infrastructure.environment.Agent;
import com.emerigen.infrastructure.environment.NeighborhoodImpl;
import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.learning.cycle.CyclePatternRecognizer;
import com.emerigen.infrastructure.learning.TransitionPatternRecognizer;
import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepository;

public aspect PerformanceMonitoring extends AbstractPerformanceMonitoring {
	
	
	pointcut ignoredClassesAndMethods():
		within(PerformanceMonitoring) 
		|| execution(* Object.*(..)) 
		|| within(AbstractTrace)
		|| within(Trace)
		|| within(AbstractPerformanceMonitoring); 

	pointcut monitoredOperations(): 
		!ignoredClassesAndMethods()
		&& (execution(* com.emerigen..*.*(..)) 
		&& (within(Agent)
		|| within(Environment)
		|| within(KnowledgeRepository) 
		|| within(Cycle) 
		|| within(CyclePatternRecognizer) 
		|| within(TransitionPatternRecognizer) 
		|| within(PredictionService) 
		|| within(CustomSensorEventSerializer) 
		|| within(CustomSensorEventDeserializer) 
		|| within(CustomTransitionSerializer) 
		|| within(CustomTransitionDeserializer) 
		|| within(CustomCycleSerializer) 
		|| within(CustomCycleDeserializer) 
		|| within(CouchbaseRepository) 
		|| within(NeighborhoodImpl)));	

}