/**
 *
 */
package com.api.dependencyInjector;


import com.api.common.CaramelUtil;
import com.api.common.CommonUtil;
import com.api.reporting.ExtentManager;
import com.api.restassured.RequestUtil;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConfigurationModule extends AbstractModule {


    private ExtentReports extent;
    private ExtentTest test;

    @Inject
    private Logger logger;

    @Override
    protected void configure() {

        extent=ExtentManager.getInstance();
        bind(ExtentReports.class).toInstance(extent);
        Names.bindProperties(binder(), getTestEnvironmentProperties());
       // Names.bindProperties(binder(), getTestDataProperties());
        bind(RequestUtil.class).toInstance(new RequestUtil());
        bind(CommonUtil.class).toInstance(new CommonUtil());
        bind(CaramelUtil.class).toInstance(new CaramelUtil());
       // bind(User.class).toProvider(UserProvider.class);
    }


    private Map<String, String> getTestEnvironmentProperties() {


        test=extent.createTest("Reading Test Data and Config file");
        logger.info("Reading config file...");
        String runMode = null;
        Properties prop = new Properties();
        InputStream input = null;
        Map<String, String> propertiesMap = new HashMap<String, String>();
        try {
            input = new FileInputStream(CommonUtil.getProjectDir() + "//config//config.properties");
            // load a properties file
            prop.load(input);
            runMode = prop.getProperty("RunMode");
            propertiesMap.put("RunMode", prop.getProperty("RunMode"));
            switch (runMode.toLowerCase()) {
                case "prod":
                    propertiesMap.put("envUrl", prop.getProperty("HOST.CARAMEL.PROD"));
                    break;
                case "qa":
                    propertiesMap.put("envUrl", prop.getProperty("HOST.CARAMEL.QA"));
                    break;
                case "stage":
                    propertiesMap.put("envUrl", prop.getProperty("HOST.CARAMEL.STAGE"));
                    //put preprod properties;
                    break;
                default:
                    break;
            }
            // get the property value and print it out

            logger.info("Reading config file successful");
            System.out.println("*******************Test  Environment is " + runMode + "  *******************");
            extent.setSystemInfo("Test Environment",runMode);
            extent.setSystemInfo("Test Environment Url",propertiesMap.get("envUrl"));
            test.pass(CommonUtil.getStringForReport("Test  Environment is <b>" + runMode+"</b>"));
            test.pass(CommonUtil.getStringForReport("Test  Environment Properties <b>" + propertiesMap+"</b>"));
            logger.info("Test Environment Properties.."+propertiesMap);

        } catch (IOException ex) {
            logger.log(Level.SEVERE,"Reading config file failed..");
            logger.log(Level.SEVERE,ex.getLocalizedMessage());
            test.error(ex.getLocalizedMessage());
            test.error(ex.getStackTrace().toString());
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    test.error(ex.getLocalizedMessage());
                    test.error(ex.getStackTrace().toString());
                    ex.printStackTrace();
                }
            }
        }
        return propertiesMap;
    }

    private Map<String, String> getTestDataProperties() {


        String runMode = null;
        Properties prop = new Properties();
        InputStream input = null;
        Map<String, String> dataPropsMap = new HashMap<String, String>();
        try {
            logger.info("Reading Test Data file");
            input = new FileInputStream(CommonUtil.getProjectDir() + "//config//data.properties");
            // load a properties file
            prop.load(input);

              // get the property value and print it out
          //  System.out.println("*******************Test  Data is " + runMode + "  *******************");
           // System.out.println("Test Data Properties " + dataPropsMap);

            logger.info("Test Data Properties.."+dataPropsMap);
            test.pass(CommonUtil.getStringForReport("Test Data Properties <b>" + dataPropsMap+"</b>"));

        } catch (IOException ex) {
            logger.log(Level.SEVERE,"Reading Test Data file failed..");
            logger.log(Level.SEVERE,ex.getLocalizedMessage());
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    test.error(ex.getLocalizedMessage());
                    test.error(ex.getStackTrace().toString());
                    ex.printStackTrace();
                }
            }
        }
        return dataPropsMap;
    }


}
