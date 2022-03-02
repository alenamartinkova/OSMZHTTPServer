package com.vsb.kru13.osmzhttpserver;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public abstract class LocationAndSensor {
    /**
     * Function that writes data to JSON file
     *
     * @param array
     * @param name
     * @param context
     *
     * @throws JSONException
     * @throws IOException
     */
    void writeData(JSONArray array, String name, Context context) throws JSONException, IOException {
        JSONObject jsonObject = this.readFileAndReturnJSON(context);

        Log.d("JSON-name", name);
        //Log.d("JSON-bef", jsonObject.toString());
        jsonObject.put(name, array);

        //Log.d("JSON-af", jsonObject.toString());
        // Convert JsonObject to String Format
        String userString = jsonObject.toString();
        // Define the File Path and its Name
        File file = new File(context.getFilesDir(), "sensorData");
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(userString);
        bufferedWriter.close();
    }

    /**
     * Function that reads and returns JSON file
     *
     * @param context
     *
     * @return
     * @throws IOException
     */
    private JSONObject readFileAndReturnJSON(Context context) throws IOException {
        File file = new File(context.getFilesDir(),"sensorData");
        JSONObject jsonObject = new JSONObject();

        if (file.exists()) {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();

            while (line != null){
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            String response = stringBuilder.toString();

            try {
                jsonObject = new JSONObject(response);
            } catch (JSONException err){
                Log.d("Error", err.toString());
            }
        }

        return jsonObject;
    }
}
