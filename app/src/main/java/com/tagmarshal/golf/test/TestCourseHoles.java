package com.tagmarshal.golf.test;

import android.util.Log;

import com.google.gson.Gson;
import com.tagmarshal.golf.R;
import com.tagmarshal.golf.application.GolfApplication;
import com.tagmarshal.golf.rest.model.RestHoleModel;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class TestCourseHoles {

    private static ArrayList<RestHoleModel> holes = new ArrayList<>();

    public static void init() {
        InputStream inputStream = GolfApplication.context.getResources().openRawResource(R.raw.test_holes);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonArray = new JSONArray(byteArrayOutputStream.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                RestHoleModel model = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), RestHoleModel.class);
                holes.add(model);
            }
        } catch (Exception e) {
            Log.e("TestCourseHoles", e.toString());
        }
    }

    public static ArrayList<RestHoleModel> getHoles() {
        return holes;
    }
}
