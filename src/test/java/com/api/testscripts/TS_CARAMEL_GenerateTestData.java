package com.api.testscripts;

import com.api.common.CaramelApiEndPoints;
import com.api.common.CommonUtil;
import com.api.restassured.ApiParamGenerator;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TS_CARAMEL_GenerateTestData extends TS_CARAMEL_BaseTest{


    private String signInUrl;
    private String getOffersUrl;


    @BeforeClass
    public void setSignInUrl() {

        signInUrl = envUrl + CaramelApiEndPoints.CAR_POST_SIGNIN;
        getOffersUrl = envUrl + CaramelApiEndPoints.CAR_GET_OFFERS;
    }


    @Test(enabled = false,dataProvider = "getTestData")
    public void readTestData(JSONObject testData) throws JSONException {

        JSONObject jsonObject = (JSONObject) testData;
        JSONObject body = ApiParamGenerator.getSignInParams(jsonObject.getString("email"), jsonObject.getString("password"));
        Response signInResponse = requestUtil.postRequestWithHeadersAndJsonBody(signInUrl, appHeaders, body, true);
        String token = signInResponse.jsonPath().getString("token");
        logger.info(token);
        System.out.println(token);
    }


    @Test(dataProvider = "getDummyTestData")
    public void checkUserOffers(JSONObject testData) throws Exception {
        JSONObject data = (JSONObject) testData;
        currentTestCase.info(CommonUtil.getStringForReport("Data For this test"));
        currentTestCase.info(CommonUtil.getStringForReport( CommonUtil.getFormattedJSON(testData.toString())));
        int offerCount = data.getInt("offerCount");
        String initiatedBy = data.getString("initiatedBy");
        JSONArray verifiedOffersArr=null;
        //for offers , check if user(buyer/seller) has req no of offers , if more delete , if less create it
        //for Active Txn , check if user(buyer/seller) has req no of Active Txn , if more delete , if less create it(By creating offer and accept)
        //for Prev Txn , check if user(buyer/seller) has req no of Pre Txn , if more delete , if less create it (By Creating active Txn and mark it as Completed)
        switch (initiatedBy.toLowerCase()) {
            case "buyer":
                currentTestCase.info(CommonUtil.getStringForReport("checking offers for buyer <b>"+data.getString("buyerEmail")+"</b>"));
                String buyerToken = caramelUtil.signIn(data.getString("buyerEmail"), data.getString("buyerPassword"), true);
                checkOffers(testData, buyerToken);
                verifiedOffersArr = caramelUtil.getOffersForUser(buyerToken, false);
                Assert.assertEquals(verifiedOffersArr.length(),offerCount,"Offer Requirement for Buyer not matched with the test data");
                break;
            case "seller":
                currentTestCase.info(CommonUtil.getStringForReport("checking offers for seller <b>"+data.getString("sellerEmail")+"</b>"));
                String sellerToken = caramelUtil.signIn(data.getString("sellerEmail"), data.getString("sellerPassword"), true);
                checkOffers(testData, sellerToken);
                 verifiedOffersArr = caramelUtil.getOffersForUser(sellerToken, false);
                Assert.assertEquals(verifiedOffersArr.length(),offerCount,"Offer Requirement for seller not matched with the test data");
                break;
            default:
                throw new Exception("InitiatedBy Field Required in the test data with values buyer/seller.");
        }

    }

    @Test(dataProvider = "getDummyTestData")
    public void checkUserActiveTxn(JSONObject testData) throws Exception {
        //"vin": "5YJ3E1EA3JF012877",
        currentTestCase.info(CommonUtil.getStringForReport("Data For this test"));
        currentTestCase.info(CommonUtil.getStringForReport( CommonUtil.getFormattedJSON(testData.toString())));
        JSONObject data = (JSONObject) testData;
        JSONArray verifiedActiveTxnArr = null;
        int activeTxnCount = data.getInt("activeTxnCount");
        String initiatedBy = data.getString("initiatedBy");
        //for offers , check if user(buyer/seller) has req no of offers , if more delete , if less create it
        //for Active Txn , check if user(buyer/seller) has req no of Active Txn , if more delete , if less create it(By creating offer and accept)
        //for Prev Txn , check if user(buyer/seller) has req no of Pre Txn , if more delete , if less create it (By Creating active Txn and mark it as Completed)
        switch (initiatedBy.toLowerCase()) {
            case "buyer":
                currentTestCase.info(CommonUtil.getStringForReport("checking Active Txns for buyer <b>"+data.getString("buyerEmail")+"</b>"));
                String buyerToken = caramelUtil.signIn(data.getString("buyerEmail"), data.getString("buyerPassword"), true);
                checkActiveTxn(testData, buyerToken);
                 verifiedActiveTxnArr = caramelUtil.getActiveTxnsForUser(buyerToken, false);
                Assert.assertEquals(verifiedActiveTxnArr.length(),activeTxnCount,"Active Txn Requirement for Buyer not matched with the test data");
                 break;
            case "seller":
                currentTestCase.info(CommonUtil.getStringForReport("checking Active Txns for seller <b>"+data.getString("sellerEmail")+"</b>"));
                String sellerToken = caramelUtil.signIn(data.getString("sellerEmail"), data.getString("sellerPassword"), true);
                checkActiveTxn(testData, sellerToken);
                verifiedActiveTxnArr = caramelUtil.getActiveTxnsForUser(sellerToken, false);
                Assert.assertEquals(verifiedActiveTxnArr.length(),activeTxnCount,"Active Txn Requirement for seller not matched with the test data");
                break;
            default:
                throw new Exception("InitiatedBy Field Required in the test data with values buyer/seller.");
        }
        checkTxnState(testData);
    }

    @Test(dataProvider = "getDummyTestData")
    public void checkUserPreviousTxn(JSONObject testData) throws Exception {
        //"vin": "5YJ3E1EA3JF012877",
        currentTestCase.info(CommonUtil.getStringForReport("Data For this test"));
        currentTestCase.info(CommonUtil.getStringForReport( CommonUtil.getFormattedJSON(testData.toString())));
        JSONObject data = (JSONObject) testData;
        JSONArray verifiedPreviousTxnArr=null;
        currentTestCase.info(CommonUtil.getStringForReport("Test Data Requirement -<b>" + data.getString("description") + "</b>"));
        int prevTxnCount = data.getInt("prevTxnCount");
        String initiatedBy = data.getString("initiatedBy");
        //for offers , check if user(buyer/seller) has req no of offers , if more delete , if less create it
        //for Active Txn , check if user(buyer/seller) has req no of Active Txn , if more delete , if less create it(By creating offer and accept)
        //for Prev Txn , check if user(buyer/seller) has req no of Pre Txn , if more delete , if less create it (By Creating active Txn and mark it as Completed)
        switch (initiatedBy.toLowerCase()) {
            case "buyer":
                currentTestCase.info(CommonUtil.getStringForReport("checking Previous Txns for buyer <b>"+data.getString("buyerEmail")+"</b>"));
                String buyerToken = caramelUtil.signIn(data.getString("buyerEmail"), data.getString("buyerPassword"), true);
                checkPrevTxn(testData, buyerToken);
                verifiedPreviousTxnArr = caramelUtil.getPreviousTxnForUser(buyerToken, false);
                Assert.assertEquals(verifiedPreviousTxnArr.length(),prevTxnCount,"Previous Txn Requirement for Buyer not matched with the test data");
                 break;
            case "seller":
                currentTestCase.info(CommonUtil.getStringForReport("checking Previous Txns for Seller <b>"+data.getString("sellerEmail")+"</b>"));
                String sellerToken = caramelUtil.signIn(data.getString("sellerEmail"), data.getString("sellerPassword"), true);
                checkPrevTxn(testData, sellerToken);
                verifiedPreviousTxnArr = caramelUtil.getPreviousTxnForUser(sellerToken, false);
                Assert.assertEquals(verifiedPreviousTxnArr.length(),prevTxnCount,"Previous Txn Requirement for Seller not matched with the test data");
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


    void checkOffers(JSONObject testData, String token) throws JSONException {
        int diff = 0;
        int reqOfferCount = testData.getInt("offerCount");
        String initiatedBy = testData.getString("initiatedBy");
        JSONArray offersArr = caramelUtil.getOffersForUser(token, false);
        if (offersArr.length() == reqOfferCount) {
            this.currentTestCase.info("user already has required No of offers");
            return;
        } else
            diff = reqOfferCount - offersArr.length();
        int loopLength = Math.abs(diff);
        switch (initiatedBy.toLowerCase()) {
            case "buyer":
                for (int i = 0; i < loopLength; i++) {
                    if (diff > 0)
                        caramelUtil.createOfferAsBuyer(testData, token, true);
                    else
                        caramelUtil.deleteOffer(token, offersArr.getJSONObject(i));

                    offersArr = caramelUtil.getOffersForUser(token, false);
                    if (offersArr.length()==reqOfferCount)
                        break;
                }
                break;
            case "seller":
                for (int i = 0; i < loopLength; i++) {
                    if (diff > 0)
                        caramelUtil.createOfferAsSeller(testData, token, true);
                    else
                        caramelUtil.deleteOffer(token, offersArr.getJSONObject(i));

                    offersArr = caramelUtil.getOffersForUser(token, false);
                    if (offersArr.length()==reqOfferCount)
                        break;
                }
                break;
        }

    }


    void checkActiveTxn(JSONObject testData, String token) throws Exception {
        int diff = 0;
        int reqActiveTxnCount = testData.getInt("activeTxnCount");
        String initiatedBy = testData.getString("initiatedBy");
        JSONArray activeTxnArr = caramelUtil.getActiveTxnsForUser(token, false);
        if (activeTxnArr.length() == reqActiveTxnCount) {
            this.currentTestCase.info("user already has required No of Active Txn");
            return;
        }
        diff = reqActiveTxnCount - activeTxnArr.length();

        int loopLength = Math.abs(diff);
        switch (initiatedBy.toLowerCase()) {
            case "buyer":
                for (int i = 0; i < loopLength; i++) {
                    activeTxnArr = caramelUtil.getActiveTxnsForUser(token, false);
                    if (diff > 0)
                        caramelUtil.createOfferAsBuyerAndAccept(testData, true);
                    else
                        caramelUtil.cancelTransaction(token, activeTxnArr.getJSONObject(0).getString("transactionId"), false);

                    activeTxnArr = caramelUtil.getActiveTxnsForUser(token, false);
                    if (activeTxnArr.length()==reqActiveTxnCount)
                        break;
                }
                break;
            case "seller":
                for (int i = 0; i < loopLength; i++) {
                    if (diff > 0)
                        caramelUtil.createDealAsSellerAndAccept(testData, true);
                    else
                        caramelUtil.cancelTransaction(token, activeTxnArr.getJSONObject(i).getString("transactionId"), false);

                    activeTxnArr = caramelUtil.getActiveTxnsForUser(token, false);
                    if (activeTxnArr.length()==reqActiveTxnCount)
                        break;
                }
                break;
        }

    }


    void checkPrevTxn(JSONObject testData, String token) throws Exception {

        int diff = 0;
        int reqPrevTxnCount = testData.getInt("prevTxnCount");
        String initiatedBy = testData.getString("initiatedBy");
        JSONArray prevTxnArr = caramelUtil.getPreviousTxnForUser(token, false);
        if (prevTxnArr.length() == reqPrevTxnCount) {
            this.currentTestCase.info("user already has required No of Previous Txn");
            return;
        }
        diff = reqPrevTxnCount - prevTxnArr.length();
        int loopLength = Math.abs(diff);
        switch (initiatedBy.toLowerCase()) {
            case "buyer":
                for (int i = 0; i < loopLength; i++) {
                    if (diff > 0) {
                        JSONObject response = caramelUtil.createOfferAsBuyerAndAccept(testData, true);
                        caramelUtil.changeActiveToPreviousTxn(response.getString("transactionId"), token, false);
                    } else
                        caramelUtil.cancelTransaction(token, prevTxnArr.getJSONObject(0).getString("transactionId"), false);

                    prevTxnArr = caramelUtil.getPreviousTxnForUser(token, false);
                    if (prevTxnArr.length()==reqPrevTxnCount)
                        break;
                }
                break;
            case "seller":
                for (int i = 0; i < loopLength; i++) {
                    if (diff > 0) {
                        JSONObject response = caramelUtil.createDealAsSellerAndAccept(testData, true);
                        caramelUtil.changeActiveToPreviousTxn(response.getString("transactionId"), token, false);
                    } else
                        caramelUtil.cancelTransaction(token, prevTxnArr.getJSONObject(0).getString("transactionId"), false);
                    prevTxnArr = caramelUtil.getPreviousTxnForUser(token, false);
                    if (prevTxnArr.length()==reqPrevTxnCount)
                        break;
                }
                break;
        }
    }

    private void checkTxnState(JSONObject testData){


        JSONArray activeTxnArr = null;
        try {
            int activeTxnCount = testData.getInt("activeTxnCount");
            if (activeTxnCount ==0){
                this.currentTestCase.info(CommonUtil.getStringForReport("Not checking Txn State.Required Active Txn Count is 0."));
            }
            if (testData.has("buyerState")) {
                this.currentTestCase.info(CommonUtil.getStringForReport("Checking Transaction State for Buyer"));
                String token=caramelUtil.signIn(testData.getString("buyerEmail"),testData.getString("buyerPassword"),false);
                activeTxnArr = caramelUtil.getActiveTxnsForUser(token, false);
                if (activeTxnArr.length() ==0) {
                    return;
                }
                caramelUtil.changeTxnStateForBuyer(token, activeTxnArr.getJSONObject(0).getString("transactionId"), testData.getString("buyerState"), true);
                this.currentTestCase.info("Buyer State Changed to "+testData.getString("buyerState"));
            }
            else if (testData.has("sellerState")){
                this.currentTestCase.info(CommonUtil.getStringForReport("Checking Transaction State for Seller"));
                String token=caramelUtil.signIn(testData.getString("sellerEmail"),testData.getString("sellerPassword"),false);
                activeTxnArr = caramelUtil.getActiveTxnsForUser(token, false);
                if (activeTxnArr.length() ==0) {
                    return;
                }
                caramelUtil.changeTxnStateForBuyer(token, activeTxnArr.getJSONObject(0).getString("transactionId"), testData.getString("buyerState"), true);
                this.currentTestCase.info("Seller State Changed to "+testData.getString("sellerState"));
            }
            else {
                this.currentTestCase.info("No Test Data for Buyer and seller Txn State");
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
