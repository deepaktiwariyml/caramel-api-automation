package com.api.common;

import com.api.restassured.ApiParamGenerator;
import com.api.restassured.RequestUtil;
import com.aventstack.extentreports.ExtentTest;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CaramelUtil {



    @Inject
    private RequestUtil requestUtil;

    @Inject
    @Named("envUrl")
    private String baseUrl;

    private ExtentTest currentTestCase;

    public CaramelUtil() {

    }


    public ExtentTest getCurrentTestInstance() {

        return currentTestCase;
    }

    public void setCurrentTestInstance(ExtentTest extentTest) {
        this.currentTestCase = extentTest;
    }


    public  LinkedHashMap<String, Object> getHeaders() {
        LinkedHashMap<String, Object> headerInfo = new LinkedHashMap<>();
        headerInfo.put("Content-Type","application/json");
        return headerInfo;
    }

    public String signIn(String email, String password,boolean isLoggingRequired) throws JSONException {

        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Signing in..User Details."+email+" | "+password));
        String signInUrl = this.baseUrl.concat(CaramelApiEndPoints.CAR_POST_SIGNIN);
        LinkedHashMap<String, Object> appHeaders = getHeaders();
        JSONObject body = ApiParamGenerator.getSignInParams(email,password);
        Response signInResponse = requestUtil.postRequestWithHeadersAndJsonBody(signInUrl, appHeaders, body, true);
        if (signInResponse !=null && signInResponse.jsonPath().getString("token")!=null) {
            this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Login Successful"));
            return signInResponse.jsonPath().getString("token");
        }
        else
            return null;
    }


    public JSONObject createOfferAsBuyer(JSONObject testData, String buyerAuthToken,boolean isLoggingRequired) throws JSONException {

        if (!testData.getString("initiatedBy").equalsIgnoreCase("buyer")){
            return new JSONObject().put("message","InitiatedBy is not Buyer . Please Check Test Data");
        }
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Creating Offer As A Buyer.."));
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",buyerAuthToken);
        JSONObject createOfferBody=new JSONObject();
        createOfferBody=ApiParamGenerator.getInitiateOfferAsBuyerParams(testData.getString("sellerEmail"),testData.getLong("price"),testData.getBoolean("isAuction"),testData.getJSONObject("vehicleDetails"));
        String createOfferUrl = this.baseUrl.concat(CaramelApiEndPoints.CAR_POST_CREATE_OFFER_BUYER);
        Response createOfferResponse = requestUtil.postRequestWithHeadersAndJsonBody(createOfferUrl,apiHeaders, createOfferBody, true);
        String offerId=createOfferResponse.jsonPath().getString("uid");
        if (createOfferResponse !=null && offerId!=null) {
            this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Offer Creating as Buyer Successful.Offer uid is <b>"+offerId+ "</b>"));
            return new JSONObject(createOfferResponse.asString());
        }
        else
            return new JSONObject();
    }


    public JSONObject createOfferAsSeller(JSONObject testData, String sellerAuthToken,boolean isLoggingRequired) throws JSONException {

        if (!testData.getString("initiatedBy").equalsIgnoreCase("seller")){
            return new JSONObject().put("message","InitiatedBy is not Seller . Please Check Test Data");
        }
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Creating Offer/Deal As A Seller.."));
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",sellerAuthToken);
        JSONObject createOfferBody=new JSONObject();
        createOfferBody=ApiParamGenerator.getInitiateOfferAsSellerParams(testData.getString("buyerEmail"),testData.getLong("price"),testData.getBoolean("isAuction"),testData.getString("zipCode"),testData.getJSONObject("vehicleDetails"));
        String createOfferUrl = this.baseUrl.concat(CaramelApiEndPoints.CAR_POST_CREATE_OFFER_SELLER);
        Response createOfferResponse = requestUtil.postRequestWithHeadersAndJsonBody(createOfferUrl,apiHeaders, createOfferBody, true);
        String dealId=createOfferResponse.jsonPath().getString("deal.dealId");
        String  offerId=createOfferResponse.jsonPath().getJsonObject("offer.offerId");
        if (createOfferResponse !=null && offerId!=null) {
            this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Offer/Deal Creation as Seller Successful.Offer uid is <b>"+offerId+ "</b>"+" and Deal uid is <b>"+dealId+ "</b>"));
            return new JSONObject(createOfferResponse.asString());
        }
        else
            return new JSONObject();
    }

    public JSONObject createOfferAsBuyerAndAccept(JSONObject testData,boolean isLoggingRequired) throws JSONException, InterruptedException {


        if (!testData.getString("initiatedBy").equalsIgnoreCase("buyer")){
            return new JSONObject().put("message","Initiated By is not Buyer . Please Check Test Data");
        }
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Buyer LogginIn.."));
        String buyerToken=signIn(testData.getString("buyerEmail"),testData.getString("buyerPassword"),true);
        JSONObject createOfferResponse=createOfferAsBuyer(testData,buyerToken,isLoggingRequired);
        String offerId=createOfferResponse.getString("uid");
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Seller LogginIn.."));
        String sellerToken=signIn(testData.getString("sellerEmail"),testData.getString("sellerPassword"),true);
        //String sellerToken="eyJraWQiOiIyRFRNYWtFU0tUXC8yeUFwMkUzaUp1RGVFMWRMQ2xUOUxEaXVWMHhXbEg1MD0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzZmU5YTY4OC04YmQzLTQyMDMtODRiYy0xOTIzODAyYTcyMWQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMi5hbWF6b25hd3MuY29tXC91cy13ZXN0LTJfeXVqa1NlZHF3IiwicGhvbmVfbnVtYmVyX3ZlcmlmaWVkIjp0cnVlLCJjb2duaXRvOnVzZXJuYW1lIjoiM2ZlOWE2ODgtOGJkMy00MjAzLTg0YmMtMTkyMzgwMmE3MjFkIiwiZ2l2ZW5fbmFtZSI6ImRlZXBhayIsIm9yaWdpbl9qdGkiOiJkZDhhOGFmNS0yZDEzLTRmM2MtODk2OS0xNWJiNzAwZDQ2YjgiLCJhdWQiOiI1OG9ubDV1a2N2djE3cTg3ZjNnNTUzb2o5YiIsImV2ZW50X2lkIjoiZTI2ZDRhY2YtZGZiYy00YmZjLTk5YmMtMjIxYjVmMzAwOWJkIiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2NzU3NzAxODgsInBob25lX251bWJlciI6IisxOTgzMzk5MjA4NSIsImV4cCI6MTY3NTc3Mzc4OCwiaWF0IjoxNjc1NzcwMTg4LCJmYW1pbHlfbmFtZSI6InRpd2FyaSIsImp0aSI6ImY0ZGU0MDkyLTlhZjYtNGNjOS1hOTUzLTQwZjNhZjM0ZTk2OSIsImVtYWlsIjoiZGVlcGFrLnRpd2FyaSt0ZXN0MkB5bWVkaWFsYWJzLmNvbSJ9.C6gKB6Lh7wCYgBwFitq7xTJgxukPpwUTSaCNAt0aA8iS0mxd4ROqSB6zKOQZMgOqpFz3byntnT6ddKK7Ukg7N07WeQPYM-46rZnwF0F9CB3KPon54iNhZpq__xcTjK3qPKHvMeMMQT_MYcfRSz7VMh_SP3pSCrMDD_l-uoy5jultPmP6EgM36eqg3pIV0NKvKxhzNHYGEGf2d-pne6wWra3FdzzHzJZCcYHoUs6VsSzkSVY1rpqR5waAVukGxO2b4Q3_ZmBKsRQcwjJLH2XUWpjPxw3Gdw_cxotDX-0kCySnA5hu52ly7cBWd0fNj5lwwTXcW7E0aS-sR2Sctswgmg";
        LinkedHashMap<String,Object> apiHeadersWithSellerToken=getHeaders();
        LinkedHashMap<String,Object> apiHeadersWithBuyerToken=getHeaders();
        apiHeadersWithSellerToken.put("Authorization",sellerToken);
        apiHeadersWithBuyerToken.put("Authorization",buyerToken);
        JSONObject vehicleInfoTestData=testData.getJSONObject("vehicleDetails");
        JSONObject vehicleDetailsWithVIN=new JSONObject();
        vehicleDetailsWithVIN.put("color",vehicleInfoTestData.getString("color"));
        vehicleDetailsWithVIN.put("year",vehicleInfoTestData.getInt("year"));
        vehicleDetailsWithVIN.put("make",vehicleInfoTestData.getString("make"));
        vehicleDetailsWithVIN.put("model",vehicleInfoTestData.getString("model"));
        vehicleDetailsWithVIN.put("mileage",vehicleInfoTestData.getString("mileage"));
        vehicleDetailsWithVIN.put("vin",generateVin());
        JSONObject createDealParams=ApiParamGenerator.createDealParams(offerId,testData.getString("zipCode"),vehicleDetailsWithVIN);
        String createDealUrl = this.baseUrl.concat(CaramelApiEndPoints.CAR_POST_CREATE_DEAL);
        TimeUnit.SECONDS.sleep(5);
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Seller Accepts the Offer."));
        Response createDealResponse = requestUtil.postRequestWithHeadersAndJsonBody(createDealUrl,apiHeadersWithSellerToken, createDealParams, true);
        if (createDealResponse !=null) {
            String acceptOfferUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_OFFER).replace(":offerId",offerId);
            JSONObject updateOfferAsSellerParams=ApiParamGenerator.updateStatusParams(APIConstants.sellerDealAcceptAck);
            this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Seller Update the Offer."));
            Response updateOfferAsSellerResponse = requestUtil.patchRequestWithHeadersAndJsonBody(acceptOfferUrl,apiHeadersWithSellerToken, updateOfferAsSellerParams, true);
            String txnId =updateOfferAsSellerResponse.jsonPath().getString("transactionId");
            this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Txn Created.Transaction Id is <b>"+txnId+"</b>"));
            return new JSONObject().put("transactionId",txnId);
        }
        else
            return new JSONObject();
    }

    public JSONObject createDealAsSellerAndAccept(JSONObject testData,boolean isLoggingRequired) throws JSONException, InterruptedException {


        if (!testData.getString("initiatedBy").equalsIgnoreCase("seller")){
            return new JSONObject().put("message","Initiated By is not Seller . Please Check Test Data");
        }
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Seller Signing In.."));
        String sellerToken=signIn(testData.getString("sellerEmail"),testData.getString("sellerPassword"),true);
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Creating Deal as Seller.."));
        JSONObject dealResponseAsSeller=createOfferAsSeller(testData,sellerToken,true);
        String offerId=dealResponseAsSeller.getJSONObject("offer").getString("offerId");

        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Buyer Signing In.."));
        String buyerToken=signIn(testData.getString("buyerEmail"),testData.getString("buyerPassword"),true);

        //String sellerToken="eyJraWQiOiIyRFRNYWtFU0tUXC8yeUFwMkUzaUp1RGVFMWRMQ2xUOUxEaXVWMHhXbEg1MD0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzZmU5YTY4OC04YmQzLTQyMDMtODRiYy0xOTIzODAyYTcyMWQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMi5hbWF6b25hd3MuY29tXC91cy13ZXN0LTJfeXVqa1NlZHF3IiwicGhvbmVfbnVtYmVyX3ZlcmlmaWVkIjp0cnVlLCJjb2duaXRvOnVzZXJuYW1lIjoiM2ZlOWE2ODgtOGJkMy00MjAzLTg0YmMtMTkyMzgwMmE3MjFkIiwiZ2l2ZW5fbmFtZSI6ImRlZXBhayIsIm9yaWdpbl9qdGkiOiJkZDhhOGFmNS0yZDEzLTRmM2MtODk2OS0xNWJiNzAwZDQ2YjgiLCJhdWQiOiI1OG9ubDV1a2N2djE3cTg3ZjNnNTUzb2o5YiIsImV2ZW50X2lkIjoiZTI2ZDRhY2YtZGZiYy00YmZjLTk5YmMtMjIxYjVmMzAwOWJkIiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2NzU3NzAxODgsInBob25lX251bWJlciI6IisxOTgzMzk5MjA4NSIsImV4cCI6MTY3NTc3Mzc4OCwiaWF0IjoxNjc1NzcwMTg4LCJmYW1pbHlfbmFtZSI6InRpd2FyaSIsImp0aSI6ImY0ZGU0MDkyLTlhZjYtNGNjOS1hOTUzLTQwZjNhZjM0ZTk2OSIsImVtYWlsIjoiZGVlcGFrLnRpd2FyaSt0ZXN0MkB5bWVkaWFsYWJzLmNvbSJ9.C6gKB6Lh7wCYgBwFitq7xTJgxukPpwUTSaCNAt0aA8iS0mxd4ROqSB6zKOQZMgOqpFz3byntnT6ddKK7Ukg7N07WeQPYM-46rZnwF0F9CB3KPon54iNhZpq__xcTjK3qPKHvMeMMQT_MYcfRSz7VMh_SP3pSCrMDD_l-uoy5jultPmP6EgM36eqg3pIV0NKvKxhzNHYGEGf2d-pne6wWra3FdzzHzJZCcYHoUs6VsSzkSVY1rpqR5waAVukGxO2b4Q3_ZmBKsRQcwjJLH2XUWpjPxw3Gdw_cxotDX-0kCySnA5hu52ly7cBWd0fNj5lwwTXcW7E0aS-sR2Sctswgmg";
        LinkedHashMap<String,Object> apiHeadersWithSellerToken=getHeaders();
        LinkedHashMap<String,Object> apiHeadersWithBuyerToken=getHeaders();
        apiHeadersWithSellerToken.put("Authorization",sellerToken);
        apiHeadersWithBuyerToken.put("Authorization",buyerToken);

        if (dealResponseAsSeller !=null) {
            String acceptOfferUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_OFFER).replace(":offerId",offerId);
            JSONObject updateOfferAsSellerParams=ApiParamGenerator.updateStatusParams(APIConstants.sellerDealAcceptAck);
            JSONObject updateOfferAsBuyerParams=ApiParamGenerator.updateStatusParams(APIConstants.buyerDealAcceptAck);
            this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Buyer Update the Offer."));
            Response updateOfferAsBuyerResponse = requestUtil.patchRequestWithHeadersAndJsonBody(acceptOfferUrl,apiHeadersWithBuyerToken, updateOfferAsBuyerParams, true);
            String txnId =updateOfferAsBuyerResponse.jsonPath().getString("transactionId");
            //this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Seller Update the Offer."));
            //Response updateOfferAsSellerResponse = requestUtil.patchRequestWithHeadersAndJsonBody(acceptOfferUrl,apiHeadersWithSellerToken, updateOfferAsSellerParams, true);
            this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Txn Created. Transaction Id is <b>"+txnId+"</b>"));
            return new JSONObject().put("transactionId",txnId);
        }
        else
            return new JSONObject();
    }

    public JSONObject acceptOffer(JSONObject createOfferBody, Map<String,Object> headers, boolean isLoggingRequired) throws JSONException {

        String createOfferUrl = this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_OFFER);
        Response createOfferResponse = requestUtil.postRequestWithHeadersAndJsonBody(createOfferUrl,headers, createOfferBody, true);
        if (createOfferResponse !=null && createOfferResponse.jsonPath().getString("uid")!=null)
            return new JSONObject(createOfferResponse.asString());
        else
            return new JSONObject();
    }

    public static String generateVin(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        Random rnd = new Random();
        int number = rnd.nextInt(999999);


        return generatedString.toUpperCase()+String.format("%06d", number);
    }


    public JSONObject changeActiveToPreviousTxn(String txnId,String authToken,boolean isLoggingRequired) throws JSONException {

        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Update Transaction To Previous.Txn Id is "+txnId));
        String updateTxnUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_TXN).replace(":txnId",txnId);
        JSONObject updateStatusParams=ApiParamGenerator.updateStatusParams(APIConstants.prevTxnStatus);
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",authToken);
        Response updateTxnResponse = requestUtil.patchRequestWithHeadersAndJsonBody(updateTxnUrl,apiHeaders,updateStatusParams, isLoggingRequired);
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Txn marked as Previous."));
        return new JSONObject(updateTxnResponse.asString());
    }

    public boolean isOfferExists(JSONArray offers, String initiatedBy) throws JSONException {
        boolean isExist = false;
        for (int i = 0; i < offers.length(); i++) {
            if (offers.getJSONObject(i).getString("initiatedBy").equals(initiatedBy)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    public JSONArray getPreviousTxnForUser(String authToken,boolean isLoggingRequired) throws JSONException {
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Fetching Prev Transation"));
        boolean isExist = false;
        JSONArray prevTxnArray=new JSONArray();
        String prevTxnUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_GET_PREV_TXNS);
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",authToken);
        Response response=requestUtil.getRequestWithHeadersAndParam(prevTxnUrl,apiHeaders,new HashMap<>(),isLoggingRequired);
        if (response !=null)
            prevTxnArray=new JSONArray(response.asString());

        return prevTxnArray;

    }

    public JSONObject cancelTransaction(String authToken,String txnId,boolean isLoggingRequired) throws JSONException {
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Trying to Cancel Transation with id "+txnId));
        boolean isExist = false;
        JSONObject cancelTxnResponse=new JSONObject();
        String cancelTxnUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_DEL_CANCEL_TXN).replace(":txnId",txnId);
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",authToken);
        Response response=requestUtil.deleteRequestWithHeadersAndJsonBody(cancelTxnUrl,apiHeaders,isLoggingRequired);
        if (response !=null)
               cancelTxnResponse=new JSONObject(response.asString());

        return cancelTxnResponse;

    }

    public JSONArray getActiveTxnsForUser(String authToken,boolean isLoggingRequired) throws JSONException {
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Fetching Active Transations"));
        boolean isExist = false;
        JSONArray activeTxnArray=new JSONArray();
        String activeTxnUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_GET_ACTIVE_TXNS);
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",authToken);
        Response response=requestUtil.getRequestWithHeadersAndParam(activeTxnUrl,apiHeaders,new HashMap<>(),isLoggingRequired);
        if (response !=null)
            activeTxnArray=new JSONArray(response.asString());

        return activeTxnArray;

    }


    public JSONArray getOffersForUser(String authToken,boolean isLoggingRequired) throws JSONException {
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Fetching Offers"));
        boolean isExist = false;
        JSONArray offersArray=new JSONArray();
        String getOffersUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_GET_OFFERS);
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",authToken);
        Response response=requestUtil.getRequestWithHeadersAndParam(getOffersUrl,apiHeaders,new HashMap<>(),isLoggingRequired);
        if (response !=null)
            offersArray=new JSONArray(response.asString());

        return offersArray;

    }

    public JSONObject changeTxnStateForBuyer(String authToken,String txnId,String status,boolean isLoggingRequired) throws JSONException {
        JSONObject responseUpdate=new JSONObject();
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Changing Status For Buyer"));
        String updateTxnUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_TXN).replace(":txnId",txnId);
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",authToken);
        JSONObject updateStateBody=ApiParamGenerator.updateBuyerStateParams(status);
        Response response=requestUtil.patchRequestWithHeadersAndJsonBody(updateTxnUrl,apiHeaders,updateStateBody,isLoggingRequired);
        if (response !=null)
            responseUpdate=new JSONObject(response.asString());

        return responseUpdate;

    }


    public JSONObject updateDealAskingPrice(String sellerAuthToken,String dealId,Long askingPrice,boolean isLoggingRequired) throws JSONException {
        JSONObject responseUpdate=new JSONObject();
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Updating Asking Price of the Deal . Deal Id "+dealId + " Asking Price is "+askingPrice));
        String updateDealUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_DEAL).replace(":dealId",dealId);
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",sellerAuthToken);
        JSONObject updateDealBody=ApiParamGenerator.updateDealParams(askingPrice,false);
        Response response=requestUtil.patchRequestWithHeadersAndJsonBody(updateDealUrl,apiHeaders,updateDealBody,isLoggingRequired);
        if (response !=null)
            responseUpdate=new JSONObject(response.asString());

        return responseUpdate;
    }


    public JSONObject deleteDeal(String sellerAuthToken,String dealId,boolean isLoggingRequired) throws JSONException {
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Trying to Delete Deal with id "+dealId));
        boolean isExist = false;
        JSONObject cancelTxnResponse=new JSONObject();
        String cancelDealUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_DEL_CANCEL_DEAL).replace(":dealId",dealId);
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",sellerAuthToken);
        Response response=requestUtil.deleteRequestWithHeadersAndJsonBody(cancelDealUrl,apiHeaders,isLoggingRequired);
        if (response !=null)
            cancelTxnResponse=new JSONObject(response.asString());

        return cancelTxnResponse;

    }


    public JSONObject changeTxnStateForSeller(String authToken,String txnId,String status,boolean isLoggingRequired) throws JSONException {
        JSONObject responseUpdate=new JSONObject();
        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Changing Status For Seller"));
        String updateTxnUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_TXN).replace(":txnId",txnId);
        LinkedHashMap<String,Object> apiHeaders=getHeaders();
        apiHeaders.put("Authorization",authToken);
        JSONObject updateStateBody=ApiParamGenerator.updateSellerStateParams(status);
        Response response=requestUtil.patchRequestWithHeadersAndJsonBody(updateTxnUrl,apiHeaders,updateStateBody,isLoggingRequired);
        if (response !=null)
            responseUpdate=new JSONObject(response.asString());

        return responseUpdate;

    }


    public String acceptOfferAndCreateTxn(JSONObject offer) throws JSONException {
        String txnId=null;
        String buyerToken =null;
        String sellerToken =null;
        String offerId=offer.getString("uid");
        String initiatedBy=offer.getString("initiatedBy");
        String type=offer.getString("type");
        LinkedHashMap<String,Object> apiHeadersWithSellerToken=getHeaders();
        LinkedHashMap<String,Object> apiHeadersWithBuyerToken=getHeaders();
            switch (initiatedBy) {
                case "buyerInitiated":
                    buyerToken = signIn(offer.getString("buyerEmail"), APIConstants.universalPassword, false);
                    this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Offer Initiated By Buyer. Seller LogginIn.."));
                    sellerToken = signIn(offer.getString("sellerEmail"), APIConstants.universalPassword, true);
                    apiHeadersWithSellerToken = getHeaders();
                    apiHeadersWithBuyerToken = getHeaders();
                    apiHeadersWithSellerToken.put("Authorization", sellerToken);
                    apiHeadersWithBuyerToken.put("Authorization", buyerToken);
                    JSONObject vehicleInfoTestData = offer.getJSONObject("buyerInitiatedVehicle");
                    JSONObject vehicleDetailsWithVIN = new JSONObject();
                    vehicleDetailsWithVIN.put("year", vehicleInfoTestData.getInt("year"));
                    vehicleDetailsWithVIN.put("make", vehicleInfoTestData.getString("make"));
                    vehicleDetailsWithVIN.put("model", vehicleInfoTestData.getString("model"));
                    vehicleDetailsWithVIN.put("mileage", APIConstants.universalMilege);
                    vehicleDetailsWithVIN.put("vin", generateVin());
                    JSONObject createDealParams = ApiParamGenerator.createDealParams(offer.getString("uid"), APIConstants.universalZipCode, vehicleDetailsWithVIN);
                    String createDealUrl = this.baseUrl.concat(CaramelApiEndPoints.CAR_POST_CREATE_DEAL);
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Seller Accepts the Offer."));
                    Response createDealResponse = requestUtil.postRequestWithHeadersAndJsonBody(createDealUrl, apiHeadersWithSellerToken, createDealParams, true);
                    if (createDealResponse != null) {
                        String acceptOfferUrl = this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_OFFER).replace(":offerId", offerId);
                        JSONObject updateOfferAsSellerParams = ApiParamGenerator.updateStatusParams(APIConstants.sellerDealAcceptAck);
                        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Seller Update the Offer."));
                        Response updateOfferAsSellerResponse = requestUtil.patchRequestWithHeadersAndJsonBody(acceptOfferUrl, apiHeadersWithSellerToken, updateOfferAsSellerParams, true);
                        txnId = updateOfferAsSellerResponse.jsonPath().getString("transactionId");
                        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Txn Created.Transaction Id is <b>" + txnId + "</b>"));

                    }
                    break;
                case "sellerInitiated":
                    sellerToken = signIn(offer.getString("sellerEmail"), APIConstants.universalPassword, false);
                    this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Offer Initiated By Seller.Buyer Signing In.."));
                    buyerToken = signIn(offer.getString("buyerEmail"), APIConstants.universalPassword, true);

                    //String sellerToken="eyJraWQiOiIyRFRNYWtFU0tUXC8yeUFwMkUzaUp1RGVFMWRMQ2xUOUxEaXVWMHhXbEg1MD0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzZmU5YTY4OC04YmQzLTQyMDMtODRiYy0xOTIzODAyYTcyMWQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMi5hbWF6b25hd3MuY29tXC91cy13ZXN0LTJfeXVqa1NlZHF3IiwicGhvbmVfbnVtYmVyX3ZlcmlmaWVkIjp0cnVlLCJjb2duaXRvOnVzZXJuYW1lIjoiM2ZlOWE2ODgtOGJkMy00MjAzLTg0YmMtMTkyMzgwMmE3MjFkIiwiZ2l2ZW5fbmFtZSI6ImRlZXBhayIsIm9yaWdpbl9qdGkiOiJkZDhhOGFmNS0yZDEzLTRmM2MtODk2OS0xNWJiNzAwZDQ2YjgiLCJhdWQiOiI1OG9ubDV1a2N2djE3cTg3ZjNnNTUzb2o5YiIsImV2ZW50X2lkIjoiZTI2ZDRhY2YtZGZiYy00YmZjLTk5YmMtMjIxYjVmMzAwOWJkIiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE2NzU3NzAxODgsInBob25lX251bWJlciI6IisxOTgzMzk5MjA4NSIsImV4cCI6MTY3NTc3Mzc4OCwiaWF0IjoxNjc1NzcwMTg4LCJmYW1pbHlfbmFtZSI6InRpd2FyaSIsImp0aSI6ImY0ZGU0MDkyLTlhZjYtNGNjOS1hOTUzLTQwZjNhZjM0ZTk2OSIsImVtYWlsIjoiZGVlcGFrLnRpd2FyaSt0ZXN0MkB5bWVkaWFsYWJzLmNvbSJ9.C6gKB6Lh7wCYgBwFitq7xTJgxukPpwUTSaCNAt0aA8iS0mxd4ROqSB6zKOQZMgOqpFz3byntnT6ddKK7Ukg7N07WeQPYM-46rZnwF0F9CB3KPon54iNhZpq__xcTjK3qPKHvMeMMQT_MYcfRSz7VMh_SP3pSCrMDD_l-uoy5jultPmP6EgM36eqg3pIV0NKvKxhzNHYGEGf2d-pne6wWra3FdzzHzJZCcYHoUs6VsSzkSVY1rpqR5waAVukGxO2b4Q3_ZmBKsRQcwjJLH2XUWpjPxw3Gdw_cxotDX-0kCySnA5hu52ly7cBWd0fNj5lwwTXcW7E0aS-sR2Sctswgmg";
                    apiHeadersWithSellerToken = getHeaders();
                    apiHeadersWithBuyerToken = getHeaders();
                    apiHeadersWithSellerToken.put("Authorization", sellerToken);
                    apiHeadersWithBuyerToken.put("Authorization", buyerToken);

                    if (offer != null) {
                        String acceptOfferUrl = this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_OFFER).replace(":offerId", offerId);
                        JSONObject updateOfferAsSellerParams = ApiParamGenerator.updateStatusParams(APIConstants.sellerDealAcceptAck);
                        JSONObject updateOfferAsBuyerParams = ApiParamGenerator.updateStatusParams(APIConstants.buyerDealAcceptAck);
                        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Buyer Update the Offer."));
                        Response updateOfferAsBuyerResponse = requestUtil.patchRequestWithHeadersAndJsonBody(acceptOfferUrl, apiHeadersWithBuyerToken, updateOfferAsBuyerParams, true);
                        txnId = updateOfferAsBuyerResponse.jsonPath().getString("transactionId");
                        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Txn Created. Transaction Id is <b>" + txnId + "</b>"));
                        break;
                    }
            }
                return txnId;
        }


        public JSONObject deleteOffer(String token,JSONObject offerObject) throws JSONException {
            JSONObject cancelTxnResponse=null;
            String type=offerObject.getString("type");
            this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Offer Type is <b>" + type + "</b>"));
            try {
                switch (type) {
                    case "buyer":
                        String txnId=acceptOfferAndCreateTxn(offerObject);
                        cancelTxnResponse=cancelTransaction(token,txnId,false);
                        this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Txn Created and Cancelled. Transaction Id is <b>" + txnId + "</b>"));
                        break;
                    case "seller":
                          JSONArray offersLinkedToDeal=offerObject.getJSONArray("offers");
                          String dealId=offerObject.getString("uid");

                          if (offersLinkedToDeal.length()==0){
                              this.getCurrentTestInstance().info(CommonUtil.getStringForReport("No Offer Linked to the Deal. Deal Id is <b>" + dealId + "</b>"));
                              //updateDealAskingPrice(token,dealId,0L,true);
                              //deleteDeal(token,dealId,true);
                             // this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Deal Cancelled. Deal Id is <b>" + dealId + "</b>"));
                          }
                          else {
                              for (int i=0;i<offersLinkedToDeal.length();i++){
                                  JSONObject offer=offersLinkedToDeal.getJSONObject(i);
                                   txnId=acceptOfferAndCreateTxn(offer);
                                   cancelTxnResponse=cancelTransaction(token,txnId,false);
                                   this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Txn Created and Cancelled. Transaction Id is <b>" + txnId + "</b>"));
//                                  String buyerToken=signIn(offer.getString("buyerEmail"),APIConstants.universalPassword,false);
//                                  LinkedHashMap<String,Object> apiHeadersWithBuyerToken = getHeaders();
//                                  apiHeadersWithBuyerToken.put("Authorization",buyerToken);
//                                  JSONObject updateStatusParams=ApiParamGenerator.updateStatusParams(APIConstants.buyerDealAcceptAck);
//                                  String updateOfferUrl= this.baseUrl.concat(CaramelApiEndPoints.CAR_PATCH_UPDATE_OFFER).replace(":offerId",offer.getString("uid"));
//                                  requestUtil.patchRequestWithHeadersAndJsonBody(updateOfferUrl,apiHeadersWithBuyerToken,updateStatusParams,true);
//                                  updateDealAskingPrice(token,dealId,0L,true);
//                                  deleteDeal(token,dealId,true);

                              }
                              //this.getCurrentTestInstance().info(CommonUtil.getStringForReport("Deal Cancelled. Deal Id is <b>" + dealId + "</b>"));
                          }

                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return cancelTxnResponse;
    }


}
