package com.api.restassured;

import com.api.common.CommonUtil;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.github.dzieciou.testing.curl.CurlLoggingRestAssuredConfigFactory;
import com.google.inject.Inject;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.restassured.config.EncoderConfig.encoderConfig;

/**
 *
 */
public class RequestUtil {


    //RestAssuredConfig config = CurlLoggingRestAssuredConfigFactory.createConfig();
    RequestSpecification request = RestAssured.given();
    RestAssuredConfig config = RestAssured.config().encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));


    @Inject
    private Logger logger;

    @Inject
    private CommonUtil commonUtil;

    /**
     * @param url
     * @param headers
     * @param parameters
     * @param isLoggingRequired
     * @return
     */
    public Response getRequestWithHeadersAndParam(String url, Map<String, Object> headers, Map<String, Object> parameters, boolean isLoggingRequired) {

        printRequestLogsToConsole("GET", url, headers, parameters,"");
        Response response = null;
        if (isLoggingRequired)
            printReqLogsToReport("GET", url, headers, parameters);
        try {
            response = RestAssured.given().headers(headers)
                    .and()
                    .params(parameters)
                    .when()
                    .get(url)
                    .then()
                    .extract().response();
            if (isLoggingRequired)
                printResponseLogsToReport(response);
        } catch (Exception e) {
            printExceptionLogsToConsole(e);
            printExceptionLogsToReport(e);
        }

        printResponseLogsToConsole(response);
        return response;

    }


    public Response postRequestWithHeadersAndParam(String url, Map<String, Object> headers, Map<String, Object> body, boolean isLoggingRequired) {

        printRequestLogsToConsole("POST", url, headers, null,body.toString());
        Response response = null;
        if (isLoggingRequired)
            printReqLogsToReport("POST", url, headers, body);
        try {
            response = request.given().headers(headers)
                    .and()
                    .body(body)
                    .when()
                    .post(url)
                    .then()
                    .extract().response();
            if (isLoggingRequired)
                printResponseLogsToReport(response);
            printResponseLogsToConsole(response);
        } catch (Exception e) {
            printExceptionLogsToConsole(e);
            printExceptionLogsToReport(e);
        }

        return response;

    }


    public Response postRequestWithHeadersAndBody(String url, Map<String, Object> headers, Object body, boolean isLoggingRequired) {

        Response response = null;
        if (isLoggingRequired)
            printReqLogsToReport("POST", url, headers, body);
        try {
            response = request.given().
                    headers(headers)
                    .and()
                    .body(body.toString())
                    .when()
                    .post(url)
                    .then()
                    .extract().response();
            if (isLoggingRequired)
                printResponseLogsToReport(response);
        } catch (Exception e) {
            printExceptionLogsToReport(e);
        }
        return response;

    }

    public Response postRequestWithHeadersAndJsonBody(String url, Map<String, Object> headers, JSONObject body, boolean isLoggingRequired) {

        Response response = null;
        if (isLoggingRequired)
            printReqLogsToReport("POST", url, headers, body);
        try {
            response = RestAssured.given().config(config).headers(headers)
                    .and()
                    .body(body.toString())
                    .when()
                    .post(url)
                    .then()
                    .extract().response();
            if (isLoggingRequired)
                printResponseLogsToReport(response);
        } catch (Exception e) {
            printExceptionLogsToReport(e);
        }
        return response;

    }

    public Response patchRequestWithHeadersAndJsonBody(String url, Map<String, Object> headers, JSONObject body, boolean isLoggingRequired) {

        Response response = null;
        if (isLoggingRequired)
            printReqLogsToReport("PATCH", url, headers, body);
        try {
            response = RestAssured.given().headers(headers)
                    .and()
                    .body(body.toString())
                    .when()
                    .patch(url)
                    .then()
                    .extract().response();
            if (isLoggingRequired)
                printResponseLogsToReport(response);
        } catch (Exception e) {
            printExceptionLogsToReport(e);
        }
        return response;

    }

    public Response deleteRequestWithHeadersAndJsonBody(String url, Map<String, Object> headers, boolean isLoggingRequired) {

        Response response = null;
        if (isLoggingRequired)
            printReqLogsToReport("DELETE", url, headers,new JSONObject());
        try {
            response = RestAssured.given().headers(headers)
                    .delete(url)
                    .then()
                    .extract().response();
            if (isLoggingRequired)
                printResponseLogsToReport(response);
        } catch (Exception e) {
            printExceptionLogsToReport(e);
        }
        return response;

    }


    public Response postRequestWithBasicAuth(String url, String username, String pwd, Map<String, Object> headers, Object body, boolean isLoggingRequired) {

        Response response = null;
        printRequestLogsToConsole("POST", url, headers, null,body);
        if (isLoggingRequired) {
            commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>LOGIN DETAILS - </b>" + " username : <b>" + username + "</b> , password: <b>" + pwd + "</b>"));
            printReqLogsToReport("POST", url, headers, body);
        }
        try {
            response = request.given().auth().preemptive().basic(username, pwd)
                    .headers(headers)
                    .contentType(ContentType.JSON)
                    .body(body.toString())
                    .post(url)
                    .then()
                    .extract().response();

            if (isLoggingRequired)
                printResponseLogsToReport(response);

            printResponseLogsToConsole(response);
        } catch (Exception e) {
            printExceptionLogsToConsole(e);
            printExceptionLogsToReport(e);
        }
        return response;

    }


    public String getResponseAsString(Response response) {

        return response.asString();
    }

    public void printReqLogsToReport(String httpMethod, String apiEndPoint, Map<String, Object> apiHeaders, Object apiRequest) {

        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>REQUEST METHOD </b>" + httpMethod));
        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>REQUEST URL</b> \n\n" + apiEndPoint));
        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>REQUEST HEADERS</b> \n\n" + CommonUtil.prettyPrintHeaders(apiHeaders)));
        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>REQUEST BODY</b>\n\n" + CommonUtil.getFormattedJSON(apiRequest.toString())));

    }

    public void printResponseLogsToReport(Response apiResponse) {

        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>RESPONSE CODE</b> " + apiResponse.getStatusCode()));
        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>RESPONSE HEADERS</b> \n\n" + apiResponse.getHeaders()));
        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>RESPONSE BODY </b>\n\n" + apiResponse.asPrettyString()));

    }

    public void printExceptionLogsToReport(Exception exception) {

        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("EXCEPTION OCCURED "));
        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>ERROR MESSAGE </b>\n" + exception.getMessage()));
        commonUtil.getCurrentTestInstance().info(CommonUtil.getStringForReport("<b>EXCEPTION STACK TRACE </b>"));
        commonUtil.getCurrentTestInstance().info(MarkupHelper.createCodeBlock(exception.toString()));

    }

    public void printRequestLogsToConsole(String reqMethod, String reqUrl, Map<String,Object> headers, Map<String,Object> parameters, Object body){

        logger.info("Making "+reqMethod+" Request to "+reqUrl);
        logger.info("Request Headers \n"+CommonUtil.prettyPrintHeaders(headers));
        logger.info("Request Parameters \n"+parameters);
        logger.info("Request Body \n"+body.toString());

 }

    public void printResponseLogsToConsole(Response response){

        logger.info("API Response:\n"+CommonUtil.getFormattedJSON(response.asString()));
        logger.info("Response Headers \n"+response.getHeaders());
      }


    public void printExceptionLogsToConsole(Exception e){

        logger.log(Level.SEVERE,"Request failed...");
        logger.log(Level.SEVERE," Failed Message ::"+e.getLocalizedMessage());
        e.printStackTrace();
    }

}
