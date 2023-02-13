package com.api.restassured;

import com.api.common.APIConstants;
import com.api.common.CaramelUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiParamGenerator {


    public static JSONObject getSignInParams(String email, String password) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("password", password);
        return jsonObject;
    }

    public static JSONObject getInitiateOfferAsBuyerParams(String sellerEmail, Long amount, boolean isAuction, JSONObject vehicleDetailsTestData) throws JSONException {
        JSONObject offerAsBuyerParam = new JSONObject();
        JSONObject vehicleInfo = new JSONObject();
        vehicleInfo.put("year",vehicleDetailsTestData.getInt("year"));
        vehicleInfo.put("make",vehicleDetailsTestData.getString("make"));
        vehicleInfo.put("model",vehicleDetailsTestData.getString("model"));
        offerAsBuyerParam.put("amount", amount);
        offerAsBuyerParam.put("initiatedBy", APIConstants.buyerInitiatedStr);
        offerAsBuyerParam.put("isAuction", isAuction);
        offerAsBuyerParam.put("sellerEmail", sellerEmail);
        offerAsBuyerParam.put("vehicle", vehicleInfo);
        return offerAsBuyerParam;
    }

    public static JSONObject getInitiateOfferAsSellerParams(String buyerEmail, Long amount,boolean isAuction,String zipCode,JSONObject vehicleDetails) throws JSONException {
        JSONObject offerAsSellerParam = new JSONObject();
        vehicleDetails.put("vin", CaramelUtil.generateVin());
        List<String> createList = new ArrayList<>();
        createList.add("deal");
        createList.add("vehicle");
        JSONObject dataObject = new JSONObject();
        //With Buyer Details
        if (buyerEmail != null && !buyerEmail.equalsIgnoreCase("")) {
            createList.add("offer");
            dataObject.put("buyerEmail", buyerEmail);
        }
        dataObject.put("askingPrice", amount);
        dataObject.put("isAuction",isAuction);
        dataObject.put("zipCode",zipCode);
        dataObject.put("vehicle", vehicleDetails);
        offerAsSellerParam.put("create", createList);
        offerAsSellerParam.put("data", dataObject);
        return offerAsSellerParam;
    }


    public static JSONObject createDealParams(String offerId, String zipCode, JSONObject vehicleDetails) throws JSONException {
        JSONObject createDealParams = new JSONObject();
        List<String> createList = new ArrayList<>();
        List<String> linkList = new ArrayList<>();
        createList.add("deal");
        createList.add("vehicle");
        linkList.add("offer");
        JSONObject dataObject = new JSONObject();
        dataObject.put("offerId", offerId);
        dataObject.put("zipCode", zipCode);
        dataObject.put("vehicle", vehicleDetails);
        createDealParams.put("create", createList);
        createDealParams.put("link", linkList);
        createDealParams.put("data", dataObject);
        return createDealParams;
    }

    public static JSONObject updateStatusParams(String status) throws JSONException {
        JSONObject updateStatusParams = new JSONObject();
        updateStatusParams.put("status", status);
        return updateStatusParams;
    }

    public static JSONObject updateBuyerStateParams(String status) throws JSONException {
        JSONObject updateStateParams = new JSONObject();
        updateStateParams.put("buyerStatus", status);
        return updateStateParams;
    }

    public static JSONObject updateSellerStateParams(String status) throws JSONException {
        JSONObject updateStateParams = new JSONObject();
        updateStateParams.put("sellerStatus", status);
        return updateStateParams;
    }

}
