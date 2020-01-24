package com.emerigen.infrastructure.utils;

import org.junit.*;

import com.emerigen.infrastructure.utils.EmerigenProperties;

import static org.junit.Assert.assertEquals;

public class KnowledgePropertiesTest {



    
    @Test
    public void GivenPropertiesAreDefined_WhenPropertiesQueried_ThenValuesAreValidated() {   	
    	
        EmerigenProperties myProps = EmerigenProperties.getInstance();
    assertEquals("entity", myProps.getValue("couchbase.bucket.entity"));
    assertEquals("Administrator", myProps.getValue("couchbase.server.userid"));
    assertEquals("entity", myProps.getValue("couchbase.bucket.entity"));
    }
 

}
