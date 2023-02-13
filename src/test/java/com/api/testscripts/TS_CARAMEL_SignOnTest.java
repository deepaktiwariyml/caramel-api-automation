package com.api.testscripts;

import com.api.common.CaramelApiEndPoints;
import com.api.common.CommonUtil;
import com.api.common.TransactionStatus;
import com.api.restassured.ApiParamGenerator;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TS_CARAMEL_SignOnTest extends TS_CARAMEL_BaseTest {

    private String signInUrl;
    private String getOffersUrl;


    @BeforeClass
    public void setSignInUrl() {

        signInUrl = envUrl + CaramelApiEndPoints.CAR_POST_SIGNIN;
        getOffersUrl = envUrl + CaramelApiEndPoints.CAR_GET_OFFERS;
    }

    @Test
    public void tc001_SignInSuccess() throws Exception {

        commonUtil.logExecutionStart();
        currentTestCase.assignAuthor("Deepak Tiwari");
        JSONObject body = ApiParamGenerator.getSignInParams("test_user_automation_12+autoverify@ymedialabs.com", "Testing@123");
        Response signInResponse = requestUtil.postRequestWithHeadersAndJsonBody(signInUrl, appHeaders, body, true);
        String token = signInResponse.jsonPath().getString("token");
        logger.info(token);
        System.out.println(token);
        //Assert.assertNotNull(signInResponse.jsonPath().getJsonObject("token"));
        // signInResponse.then().assertThat().body(matchesJsonSchemaInClasspath("response-schema-rules//signInResponseSchema.json"));
        commonUtil.logExecutionEnd();
    }

    @Test(dataProvider = "getTestData")
    public void readTestData(JSONObject testData) throws JSONException {

        JSONObject jsonObject = (JSONObject) testData;
        JSONObject body = ApiParamGenerator.getSignInParams(jsonObject.getString("email"), jsonObject.getString("password"));
        Response signInResponse = requestUtil.postRequestWithHeadersAndJsonBody(signInUrl, appHeaders, body, true);
        String token = signInResponse.jsonPath().getString("token");
        logger.info(token);
        System.out.println(token);
    }

    @Test(dataProvider = "getDummyTestData")
    public void readDummyData(JSONObject testData) throws Exception {
        //"vin": "5YJ3E1EA3JF012877",
        JSONObject data = (JSONObject) testData;
        int offerCount=data.getInt("offerCount");
        int activeTxnCount=data.getInt("activeTxnCount");
        int prevTxnCount=data.getInt("prevTxnCount");
        String initiatedBy=data.getString("initiatedBy");
        //for offers , check if user(buyer/seller) has req no of offers , if more delete , if less create it
        //for Active Txn , check if user(buyer/seller) has req no of Active Txn , if more delete , if less create it(By creating offer and accept)
        //for Prev Txn , check if user(buyer/seller) has req no of Pre Txn , if more delete , if less create it (By Creating active Txn and mark it as Completed)
         switch (initiatedBy.toLowerCase()){
             case "buyer":
                 String buyerToken=caramelUtil.signIn(data.getString("buyerEmail"),data.getString("buyerPassword"),true);
                  if (offerCount !=0){
                      checkOffers(testData,buyerToken);
                  }
                 if (activeTxnCount !=0){
                     checkActiveTxn(testData,buyerToken);


                 }
                 if (prevTxnCount !=0){
                     checkPrevTxn(testData,buyerToken);
                 }


                 break;
             case "seller":
//                 String sellerToken=caramelUtil.signIn(data.getString("sellerEmail"),data.getString("sellerPassword"),true);
//                 if (offerCount !=0){
//                     checkOffers(testData,sellerToken);
//                 }
//                 if (activeTxnCount !=0){
//                     checkActiveTxn(testData,sellerToken);
//                 }
//                 if (prevTxnCount !=0){
//                     checkPrevTxn(testData,sellerToken);
//                 }
                 break;
             default:
                 throw new Exception("InitiatedBy Field Required in the test data with values buyer/seller.");
         }



    }

    @DataProvider
    public Object[][] getTestData() throws JSONException {
        String filePath = CommonUtil.getProjectDir() + "/src/main/resources/test-data/testdata.json";
        JSONArray array = commonUtil.readFileAsJSONArray(filePath);
        Object[][] testdata = new Object[array.length()][1];
        for (int i = 0; i < array.length(); i++) {
            testdata[i][0] = array.getJSONObject(i);
        }
        return testdata;
    }


    @DataProvider
    public Object[][] getDummyTestData() throws JSONException {
        String filePath = CommonUtil.getProjectDir() + "/src/main/resources/test-data/dummydata.json";
        JSONArray array = commonUtil.readFileAsJSONArray(filePath);
        Object[][] testdata = new Object[array.length()][1];
        for (int i = 0; i < array.length(); i++) {
            testdata[i][0] = array.getJSONObject(i);
        }


        return testdata;
    }


    void checkOffers(JSONObject testData,String token) throws JSONException {
        int diff=0;
        int reqOfferCount=testData.getInt("offerCount");
        String initiatedBy=testData.getString("initiatedBy");
        JSONArray offersArr=caramelUtil.getOffersForUser(token,false);
        if (offersArr.length() == reqOfferCount){
            this.currentTestCase.info("user already has required No of offers");
            return;
        }
        else
         diff=reqOfferCount-offersArr.length();
         int loopLength=Math.abs(diff);
        switch (initiatedBy.toLowerCase()) {
            case "buyer":
                for (int i=0;i<loopLength;i++){
                    if (diff>0)
                        caramelUtil.createOfferAsBuyer(testData,token,true);
                }
                break;
            case "seller":
                for (int i=0;i<loopLength;i++){
                    if (diff>0)
                    caramelUtil.createOfferAsSeller(testData,token,true);
                }
                break;
        }

    }



    void checkActiveTxn(JSONObject testData,String token) throws Exception {
        int diff=0;
        int reqActiveTxnCount=testData.getInt("activeTxnCount");
        String initiatedBy=testData.getString("initiatedBy");
        String buyerState=testData.getString("buyerState");
        JSONArray activeTxnArr=caramelUtil.getActiveTxnsForUser(token,false);
        if (activeTxnArr.length() == reqActiveTxnCount){
            this.currentTestCase.info("user already has required No of Active Txn");
            caramelUtil.changeTxnStateForBuyer(token,activeTxnArr.getJSONObject(0).getString("transactionId"), buyerState,true);
            return;
        }
        diff=reqActiveTxnCount-activeTxnArr.length();

        int loopLength=Math.abs(diff);
        switch (initiatedBy.toLowerCase()) {
            case "buyer":
                for (int i=0;i<loopLength;i++){
                    activeTxnArr=caramelUtil.getActiveTxnsForUser(token,false);
                    if (diff>0)
                        caramelUtil.createOfferAsBuyerAndAccept(testData,true);
                    else
                        caramelUtil.cancelTransaction(token,activeTxnArr.getJSONObject(0).getString("transactionId"),false);
                }
                activeTxnArr=caramelUtil.getActiveTxnsForUser(token,true);
                caramelUtil.changeTxnStateForBuyer(token,activeTxnArr.getJSONObject(0).getString("transactionId"), buyerState,true);
                break;
            case "seller":
                for (int i=0;i<loopLength;i++){
                    activeTxnArr=caramelUtil.getActiveTxnsForUser(token,false);
                    if (diff>0)
                        caramelUtil.createDealAsSellerAndAccept(testData,true);
                    else
                        caramelUtil.cancelTransaction(token,activeTxnArr.getJSONObject(i).getString("transactionId"),false);
                }
                break;
        }

    }


    void checkPrevTxn(JSONObject testData,String token) throws Exception {

        int diff=0;
        int reqPrevTxnCount=testData.getInt("prevTxnCount");
        String initiatedBy=testData.getString("initiatedBy");
        JSONArray prevTxnArr=caramelUtil.getPreviousTxnForUser(token,false);
        JSONArray activeTxnArr=caramelUtil.getActiveTxnsForUser(token,false);
        if (prevTxnArr.length() == reqPrevTxnCount){
            this.currentTestCase.info("user already has required No of Previous Txn");
            return;
        }
         diff=reqPrevTxnCount-prevTxnArr.length();
         int loopLength=Math.abs(diff);
        switch (initiatedBy.toLowerCase()) {
            case "buyer":
                for (int i=0;i<loopLength;i++){
                    prevTxnArr=caramelUtil.getPreviousTxnForUser(token,false);
                    if (diff>0) {
                        JSONObject response=caramelUtil.createOfferAsBuyerAndAccept(testData, true);
                        caramelUtil.changeActiveToPreviousTxn(response.getString("transactionId"),token,false);
                     }
                    else
                        caramelUtil.cancelTransaction(token,prevTxnArr.getJSONObject(0).getString("transactionId"),false);
                }
                break;
            case "seller":
                for (int i=0;i<loopLength;i++){
                    prevTxnArr=caramelUtil.getPreviousTxnForUser(token,false);
                    if (diff>0) {
                        JSONObject response = caramelUtil.createDealAsSellerAndAccept(testData, true);
                        caramelUtil.changeActiveToPreviousTxn(response.getString("transactionId"),token,false);
                    }
                    else
                        caramelUtil.cancelTransaction(token,activeTxnArr.getJSONObject(0).getString("transactionId"),false);
                }
                break;
        }

    }
}
