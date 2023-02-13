package com.api.common;


import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.google.gson.*;
import com.google.inject.Inject;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import static com.api.common.APIConstants.*;


public class CommonUtil {

    private ExtentTest currentTestCase;

    @Inject
    public Logger logger;

    public ExtentTest getCurrentTestInstance() {
        return currentTestCase;
    }

    public void setCurrentTestInstance(ExtentTest extentTest) {
        this.currentTestCase = extentTest;
    }



    public static Object jsonsEqual(Object obj1, Object obj2) throws JSONException {

        JSONObject diff = new JSONObject();

        if (!obj1.getClass().equals(obj2.getClass())) {
            return diff;
        }

        if (obj1 instanceof JSONObject && obj2 instanceof JSONObject) {
            JSONObject jsonObj1 = (JSONObject) obj1;

            JSONObject jsonObj2 = (JSONObject) obj2;

            List<String> names = new ArrayList(Arrays.asList(JSONObject.getNames(jsonObj1)));
            List<String> names2 = new ArrayList(Arrays.asList(JSONObject.getNames(jsonObj2)));
            if (!names.containsAll(names2) && names2.removeAll(names)) {
                for (String fieldName : names2) {
                    if (jsonObj1.has(fieldName))
                        diff.put(fieldName, jsonObj1.get(fieldName));
                    else if (jsonObj2.has(fieldName))
                        diff.put(fieldName, jsonObj2.get(fieldName));
                }
                names2 = Arrays.asList(JSONObject.getNames(jsonObj2));
            }

            if (names.containsAll(names2)) {
                for (String fieldName : names) {
                    Object obj1FieldValue = jsonObj1.get(fieldName);
                    Object obj2FieldValue = jsonObj2.get(fieldName);
                    Object obj = jsonsEqual(obj1FieldValue, obj2FieldValue);
                    if (obj != null && !checkObjectIsEmpty(obj))
                        diff.put(fieldName, obj);
                }
            }
            return diff;
        } else if (obj1 instanceof JSONArray && obj2 instanceof JSONArray) {

            JSONArray obj1Array = (JSONArray) obj1;
            JSONArray obj2Array = (JSONArray) obj2;
            if (!obj1Array.toString().equals(obj2Array.toString())) {
                JSONArray diffArray = new JSONArray();
                for (int i = 0; i < obj1Array.length(); i++) {
                    Object obj = null;
                    matchFound:
                    for (int j = 0; j < obj2Array.length(); j++) {
                        obj = jsonsEqual(obj1Array.get(i), obj2Array.get(j));
                        if (obj == null) {
                            break matchFound;
                        }
                    }
                    if (obj != null)
                        diffArray.put(obj);
                }
                if (diffArray.length() > 0)
                    return diffArray;
            }
        } else {
            if (!obj1.equals(obj2)) {
                return obj2;
            }
        }

        return null;
    }

    private static boolean checkObjectIsEmpty(Object obj) {
        if (obj == null)
            return true;
        String objData = obj.toString();
        if (objData.length() == 0)
            return true;
        if (objData.equalsIgnoreCase("{}"))
            return true;
        return false;
    }

    /**
     * This method formats a json object of type JSONObject and
     * return formatted json as a string
     *
     * @param jsonString
     * @return formattedJSON as String
     */
    public static String getFormattedJSON(String jsonString) {

        String formattedJSON = null;

        try {


            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(jsonString);
            formattedJSON = gson.toJson(je);

        } catch (Exception e) {
            e.printStackTrace();
            return formattedJSON;

        }

        return formattedJSON;
    }

    public static String getProjectDir() {
        String projectRootDir = null;
        String userDirectory = System.getProperty("user.dir");
        int index = userDirectory.indexOf("\\src\\");
        projectRootDir = (index == -1) ? userDirectory : userDirectory.substring(0, index);
        return projectRootDir;
    }


    public static JsonObject getJsonObjectFromString(String json) {
        JsonElement jsonElement = new JsonParser().parse(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject;

    }

    /**
     * This Method converts the provided input to Html formatted text
     * So that it can be printed in the Extent Report
     *
     * @param text
     * @return
     */
    public static String getStringForReport(String text) {

        return "<pre>" + text + "</pre>";
    }


    public static JsonNode getJsonNode(Object object) throws Exception {
        String jsonString = null;

        ObjectMapper mapper = new ObjectMapper();

        if (object instanceof JSONObject)
            jsonString = ((JSONObject) object).toString();
        if (object instanceof JsonObject)
            jsonString = ((JsonObject) object).toString();
        if (object instanceof String)
            jsonString = object.toString();
        if (object instanceof Response)
            jsonString = ((Response) object).asString();


        JsonNode node = mapper.readTree(jsonString);
        return node;
    }

    public JsonNode getJsonDiff(Object source, Object target) throws Exception {
        JsonNode diff = null;
        diff = JsonDiff.asJson(CommonUtil.getJsonNode(source), CommonUtil.getJsonNode(target));
        printJsonDifference(diff);
        return diff;

    }

    public void printJsonDifference(JsonNode diff) {
        List<String> properties_missing_from_right = new ArrayList<>();
        List<String> properties_missing_from_left = new ArrayList<>();
        List<String> properties_with_diff_value = new ArrayList<>();
        StringBuffer propertiesMissingFromRight = new StringBuffer();
        StringBuffer propertiesMissingFromleft = new StringBuffer();
        StringBuffer propertiesWithDiffValue = new StringBuffer();


        for (int i = 0; i < diff.size(); i++) {
            JsonNode node = diff.get(i);
            String op = node.get("op").asText().trim();

            switch (op) {

                case "add":
                    properties_missing_from_left.add(node.get("path").toString());
                    propertiesMissingFromRight.append(node.get("path").toString() + "\n");
                    break;

                case "remove":
                    properties_missing_from_right.add(node.get("path").toString());
                    propertiesMissingFromleft.append(node.get("path").toString() + "\n");
                    break;

                case "replace":
                    properties_with_diff_value.add(node.get("path").toString());
                    propertiesWithDiffValue.append(node.get("path").toString() + "\n");
                    break;
            }
        }
        ;
        int totalDiffCount = properties_missing_from_right.size() + properties_missing_from_left.size() + properties_with_diff_value.size();
        int max = Math.max(properties_missing_from_left.size(), properties_missing_from_right.size());
        int rowSize = Math.max(max, properties_with_diff_value.size());
        String[][] tableData = new String[rowSize + 1][3];

        tableData[0][0] = "PROPERTIES MISSING FROM LEFT (" + properties_missing_from_left.size() + ")";
        tableData[0][1] = "PROPERTIES MISSING FROM RIGHT (" + properties_missing_from_right.size() + ")";
        tableData[0][2] = "PROPERTIES WITH DIFF VALUES (" + properties_with_diff_value.size() + ")";

        for (int i = 1; i < tableData.length; i++) {
            tableData[i][0] = (i < properties_missing_from_left.size()) ? properties_missing_from_left.get(i) : "NA";
            tableData[i][1] = (i < properties_missing_from_right.size()) ? properties_missing_from_right.get(i) : "NA";
            tableData[i][2] = (i < properties_with_diff_value.size()) ? properties_with_diff_value.get(i) : "NA";

        }
        currentTestCase.info(getStringForReport("Total " + totalDiffCount + " differences found."));
        Markup m = MarkupHelper.createTable(tableData);
        currentTestCase.info(m);

    }


    public JSONArray readFileAsJSONArray(String resourceNameWithPath){

        JSONArray fileContent=null;
        File file = new File(resourceNameWithPath);
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            fileContent = new JSONArray(content);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileContent;
    }

    public JSONObject readFileAsJSONObject(String resourceNameWithPath){

        JSONObject fileContent=null;
        File file = new File(resourceNameWithPath);
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            fileContent = new JSONObject(content);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileContent;
    }


    public static String prettyPrintHeaders(Map<String ,Object> headerMap){

        String prettyHeaders="";
        if (headerMap == null){
            return null;
        }
        for (String key:headerMap.keySet()){
            prettyHeaders=prettyHeaders.concat(key+":"+headerMap.get(key)+"\n");
        }
        return prettyHeaders;
    }

    public  void logExecutionStart(){

       String methodName =Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info("\n\n************************* Execution Starts for:: "+methodName+" ***************\n\n");
    }

    public  void logExecutionEnd(){

        String methodName =Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info("\n\n************************* Execution Finished for:: "+methodName+" ***************\n\n");
    }

}
