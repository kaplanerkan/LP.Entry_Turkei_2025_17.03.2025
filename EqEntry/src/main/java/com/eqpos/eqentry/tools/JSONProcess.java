package com.eqpos.eqentry.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Created by dursu on 24.02.2018.
 */

public class JSONProcess {
    public static JsonObject getJSONHeader(int prCommand) {
        JsonObject jObj = new JsonObject();
        try {
            jObj.addProperty("command", prCommand);
        } catch (JsonParseException e) {
            return null;
        }
        try {
            jObj.addProperty("device", Variables.serialNumber);
        } catch (JsonParseException e) {
            return null;
        }

        try {
            jObj.addProperty("userid", Variables.userId);
        } catch (JsonParseException e) {
            return null;
        }

        return jObj;
    }

    public static String jsonPack(JsonObject jHead, JsonObject jData) {
        JsonArray lArr = new JsonArray();
        lArr.add(jHead);
        lArr.add(jData);
        return lArr.toString();
    }
}
