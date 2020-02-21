package com.emerigen.infrastructure.sensor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SensorEventListenerTest.class, SensorEventTest.class, SensorTest.class,
		EmerigenSensorEventListenerTest.class, GpsSensorTest.class,
		SensorManagerTest.class })
public class AllTests {

}
