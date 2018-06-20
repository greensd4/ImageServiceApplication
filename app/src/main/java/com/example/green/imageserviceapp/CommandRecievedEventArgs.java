package com.example.green.imageserviceapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommandRecievedEventArgs {
    private int CommandID;
    private String[] Args;
    private String RequestDirPath;

    public CommandRecievedEventArgs(int id,byte[] args, String reqDirPath) {
        this.CommandID = id;
        String s = new String(args);
        this.Args = new String[1];
        this.Args[0] = s;
        this.RequestDirPath = reqDirPath;
    }

    public String toJson() {
        JSONObject j = new JSONObject();
        try {
            JSONArray jA = new JSONArray();
            j.put("CommandID", CommandID);
            jA.put(Args[0]);
            j.put("Args", jA);
            j.put("RequestDirPath", RequestDirPath);
        } catch (JSONException je) {
            Log.e("JSon","C:Error", je);
            return null;
        }
        return j.toString();
    }

}
