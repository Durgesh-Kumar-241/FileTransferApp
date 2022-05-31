package com.dktechhub.shareit.filetransferapp.ui.main;

import com.dktechhub.shareit.filetransferapp.SharedItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FileConverter {
    public static JSONObject getJson(ArrayList<SharedItem> sharedItems) throws JSONException {
        JSONObject main = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(SharedItem sharedItem:sharedItems)
        {
            JSONObject js = new JSONObject();
            js.put("name",sharedItem.name);
            js.put("size",sharedItem.size);
            js.put("type",sharedItem.type);
            js.put("id",sharedItem.id);
            jsonArray.put(js);
        }
        main.put("list",jsonArray);
        return main;
    }

    public static ArrayList<SharedItem> fromJson(JSONObject jsonObject) throws JSONException {   ArrayList<SharedItem> arrayList = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        for(int i=0;i< jsonArray.length();i++)
        {   JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            arrayList.add(new SharedItem(jsonObject1.getString("name"),jsonObject1.getLong("size"),jsonObject1.getString("type"),jsonObject1.getString("id")));
        }
        return arrayList;
    }
}
