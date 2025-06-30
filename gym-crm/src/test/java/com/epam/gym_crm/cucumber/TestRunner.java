package com.epam.gym_crm.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features") // Points to src/test/resources/features
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.epam.gym_crm.cucumber.steps") // Points to your step definitions
public class TestRunner {
}