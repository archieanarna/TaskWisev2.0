package com.example.taskwiserebirth.conversational;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequest {

    private static final String SERVER_ADDRESS = "https://taskwise.michacaldaira.com/";
    private static String GET_TASK_DETAIL = "task_detail";


    // add boolean isTurnBased if true change server address
    public static void sendRequest(String userMessage, String aiName, String userId, boolean inTurnBasedInteraction, final HttpRequestCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject requestBodyJson = new JSONObject();
        try {
            requestBodyJson.put("user_prompt", userMessage);
            requestBodyJson.put("ai_name", aiName);
            requestBodyJson.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(requestBodyJson.toString(), JSON);

        String url;
        if (inTurnBasedInteraction) {
            url = SERVER_ADDRESS + GET_TASK_DETAIL;
        } else {
            url = SERVER_ADDRESS;
        }

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String errorMessage;
                try {
                    throw e;
                } catch (SocketTimeoutException timeoutException) {
                    errorMessage = "Request timed out";
                } catch (UnknownHostException unknownHostException) {
                    errorMessage = "Host not found";
                } catch (IOException ioException) {
                    errorMessage = "Request failed: " + e.getMessage();
                }

                Log.e("HttpRequest", errorMessage);
                callback.onFailure(errorMessage);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        if (response.code() == 404) {
                            callback.onFailure("Page not found");
                        } else if (response.code() == 502) {
                            callback.onFailure("Sorry, server is not available at the moment");
                        } else {
                            callback.onFailure("Unexpected code: " + response.code());
                        }

                        Log.e("HttpRequest", "Unexpected code: " + response);
                        response.close();

                        return;
                    }

                    // Get the response body as a string
                    String responseBody = response.body().string();
                    response.close();

                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String responseText = jsonResponse.getString("response");
                    String intent = jsonResponse.getString("intent");

                    callback.onSuccess(intent, responseText);

                } catch (IOException e) {
                    Log.e("HttpRequest", "Error reading response body", e);
                    callback.onFailure("Error reading response body" + e.getMessage());
                } catch (JSONException e) {
                    Log.e("HttpRequest", "JSON error: ", e);
                    callback.onFailure("JSON error: " + e.getMessage());

                }
            }
        });
    }

    public interface HttpRequestCallback {
        void onSuccess(String intent, String responseText);
        void onFailure(String errorMessage);
    }
}
