package com.instalert.emergencies.bdd;
import org.junit.platform.suite.api.*;
import static io.cucumber.junit.platform.engine.Constants.*;
@Suite @IncludeEngines("cucumber") @SelectClasspathResource("features")
@ConfigurationParameter(key=GLUE_PROPERTY_NAME,value="com.instalert.emergencies.bdd")
@ConfigurationParameter(key=PLUGIN_PROPERTY_NAME,value="pretty,json:target/cucumber.json")
public class EmergenciesAcceptanceTest {}
