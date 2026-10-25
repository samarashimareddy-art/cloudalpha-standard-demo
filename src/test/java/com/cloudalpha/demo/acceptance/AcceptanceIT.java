package com.cloudalpha.demo.acceptance;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Stage 11, acceptance test: the business scenarios in
 * src/test/resources/features, run against a deployed environment.
 *
 *   mvn -Pacceptance verify -Dbase.url=https://app-dev-123.example
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.cloudalpha.demo.acceptance")
public class AcceptanceIT {
}
