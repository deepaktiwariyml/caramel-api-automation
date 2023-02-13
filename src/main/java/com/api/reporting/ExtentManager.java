package com.api.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.api.common.CommonUtil;


public class ExtentManager {
    
	public static String runMode;
	
    private static ExtentReports extent;
    private static ExtentTest test;
    public static ExtentReports getInstance() {
    	if (extent == null)
    		createInstance(CommonUtil.getProjectDir()+"//reports//api-automation-report.html");
    	return extent;
    }
    
    public static ExtentReports createInstance(String fileName) {
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(fileName);
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setDocumentTitle("Caramel Automation Report");
        htmlReporter.config().setEncoding("utf-8");
        htmlReporter.config().setReportName("Caramel Automation Report");
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Host Name", "CARAMEL-YML");

        return extent;
    }
    
    
    public static ExtentTest createTest(String name, String description){
		test = extent.createTest(name, description);
		return test;
	}
    
}