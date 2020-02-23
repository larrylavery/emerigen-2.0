/*
 This class specifies the classes, methods, and constructors that will be enabled
 for entry and exit tracing.  
*/

package com.emerigen.infrastructure.tracing;







import com.emerigen.infrastructure.repository.KnowledgeRepository;
import com.emerigen.infrastructure.repository.RepositoryException;
import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepository;
import com.emerigen.infrastructure.repository.couchbase.CouchbaseRepositoryConfig;
import com.emerigen.infrastructure.repository.couchbase.BucketNotFoundException;
import com.emerigen.infrastructure.environment.Agent;
import com.emerigen.infrastructure.environment.Environment;
import com.emerigen.infrastructure.environment.NeighborhoodImpl;
import com.emerigen.infrastructure.environment.MessageToSpread;
import com.emerigen.infrastructure.environment.Location;
import com.emerigen.knowledge.Entity;
import com.emerigen.infrastructure.evaporation.EvaporationAspect;
import com.emerigen.infrastructure.evaporation.InformationWithRelevanceHolder;
import com.emerigen.infrastructure.evaporation.RelevantInformation;
import com.emerigen.infrastructure.learning.Transition;
import com.emerigen.infrastructure.learning.cycle.Cycle;
import com.emerigen.infrastructure.utils.EmerigenProperties;
import com.emerigen.infrastructure.utils.ScheduledMethodTaskTest;
import com.emerigen.infrastructure.utils.ScheduledMethodAspect;
import com.emerigen.infrastructure.utils.LeakyBucket;
import com.emerigen.infrastructure.sensor.AccelerometerSensor;
import com.emerigen.infrastructure.sensor.AccelerometerSensorEventListener;
import com.emerigen.infrastructure.sensor.HeartRateSensorEventListener;
//import com.emerigen.infrastructure.sensor.TemperatureSensorEventListener;
import com.emerigen.infrastructure.sensor.GpsSensorEventListener;
//import com.emerigen.infrastructure.sensor.SleepSensorEventListener;
//import com.emerigen.infrastructure.sensor.GlucoseSensorEventListener;
import com.emerigen.infrastructure.sensor.GlucoseSensor;
import com.emerigen.infrastructure.sensor.BloodPressureSensor;
import com.emerigen.infrastructure.sensor.SleepSensor;
import com.emerigen.infrastructure.sensor.HeartRateSensor;
import com.emerigen.infrastructure.sensor.GpsSensor;
import com.emerigen.infrastructure.sensor.TemperatureSensor;
import com.emerigen.infrastructure.sensor.EmerigenSensorEventListener;
import com.emerigen.infrastructure.sensor.SensorEventListener;
import com.emerigen.infrastructure.sensor.SensorManager;
import com.emerigen.infrastructure.sensor.Sensor;
import com.emerigen.infrastructure.utils.Utils;
import com.emerigen.infrastructure.sensor.SensorEvent;

aspect Trace extends AbstractTrace {

	/**
	 * The application classes
	 */

	pointcut ignoredClassesAndMethods():
		within(Trace) 
		|| execution(* Object.*(..)) 
		|| execution(* *Test.*(..))
		|| within(AbstractTrace); 
		
	pointcut environmentClasses(): 
		(within(Agent)
		|| within(Environment)
		|| within(Location) 
		|| within(RelevantInformation) 
		|| within(MessageToSpread) 
		|| within(InformationWithRelevanceHolder) 
		|| within(EvaporationAspect) 
		|| within(AccelerometerSensor) 
		|| within(AccelerometerSensorEventListener) 
		|| within(HeartRateSensor) 
		|| within(HeartRateSensorEventListener) 
		|| within(TemperatureSensor) 
//		|| within(TemperatureSensorEventListener) 
		|| within(GpsSensor) 
		|| within(GpsSensorEventListener) 
		|| within(BloodPressureSensor) 
//		|| within(BloodPressureSensorEventListener) 
		|| within(SleepSensor) 
//		|| within(SleepSensorEventListener) 
		|| within(GlucoseSensor) 
//		|| within(GlucoseSensorEventListener) 
		|| within(EmerigenSensorEventListener) 
		|| within(SensorEventListener) 
		|| within(SensorManager) 
		|| within(SensorEvent) 
		|| within(Sensor) 
		|| within(NeighborhoodImpl));

	pointcut repositoryClasses():
		
		(within(CouchbaseRepository)
		|| within(KnowledgeRepository));

	pointcut javaBeanClasses(): 
		(within(Entity) 
		|| within(Transition) 
		|| within(SensorEvent));

	pointcut supportingClasses():
		(within(EmerigenProperties)
		|| within(LeakyBucket) 
		|| within(RepositoryException) 
		|| within(BucketNotFoundException) 
		|| within(Utils) 
		|| within(Cycle) 
		|| within(CycleNode) 
		|| within(CouchbaseRepositoryConfig));

	pointcut infrastructureClasses():
		(within(RelevantInformation)
		|| within(EvaporationAspect) 
		|| within(ScheduledMethodAspect) 
		|| within(ScheduledMethodTaskTest)
		|| within(ScheduledMethodAspect)
		|| within(InformationWithRelevanceHolder));

	pointcut classes(): 
		(repositoryClasses()
		|| javaBeanClasses() 
		|| supportingClasses()
		|| infrastructureClasses()
		|| environmentClasses());

	/**
	 * The constructors in those classes - but only the ones with 3 arguments.
	 */
	pointcut constructors() : 
		execution(*.new(..));

	/**
	 * This specifies all the method executions.
	 */
	pointcut repositoryMethods(): 
		!execution(public static * getInstance(..))
		&& (execution(* KnowledgeRepository.*(..)) 
		|| execution(* CouchbaseRepository(..)));

//	pointcut javaBeanMethods(): ;
//		!ignoredClassesAndMethods();

	pointcut infrastructureMethods(): 
		(execution(* getInformationWithMinimumRelevance(..)) 
		|| execution(* getInformationWithRelevance(..)) 
		|| execution(* setInformationWithRelevance(..)) 
		|| execution(* evaporateRelevancies(..))
		|| execution(* com.emerigen..LeakyBuctet.*(..))
		|| execution(* getPrediction*(..)));

	pointcut environmentMethods():
		(execution(* com.emerigen..Environment.*(..)) 
		|| execution(* com.emerigen..Location.*(..)) 
		|| execution(* com.emerigen..MessageToSpread(..)) 
		|| execution(* com.emerigen..NeighborhoodImpl.*(..)) 
		|| execution(* com.emerigen..Neighborhood.*(..)) 
		|| execution(* com.emerigen..Agent.*(..)));

	pointcut methods():
		(repositoryMethods() 
//		|| javaBeanMethods()
		|| infrastructureMethods()
		|| environmentMethods());

	/**
	 * This specifies all the methods to be performance tested. comment out these
	 * lines to disable performance tracing. Add more classes and methods as
	 * required
	 */
	pointcut performanceTraceMethods():
		!ignoredClassesAndMethods()
		&& (execution(public * com.emerigen..KnowledgeRepository.newSensorEvent(..))
	    	|| execution(public * com.emerigen..KnowledgeRepository.newTransition(..))
			|| execution(public * com.emerigen..KnowledgeRepository.get*Count*(..))
			|| execution(public * com.emerigen..KnowledgeRepository.getPredictions*(..))
	    	|| execution(public * com.emerigen..CouchbaseRepository.log(..))
	    	|| execution(public void evaporateRelevancies())
	    	|| execution(public * com.emerigen..query(..)));

}
