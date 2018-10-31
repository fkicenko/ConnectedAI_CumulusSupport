package com.cisco.tme.REST;

/**
 * Created by frank.kicenko on 2018-02-04.
 */
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.cisco.tme.Config;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 *
 * This is the object responsible for communicating with a REST API.
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes:
 *   https://github.com/scribejava/scribejava/tree/master/scribejava-apis/src/main/java/com/github/scribejava/apis
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 *
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 *
 */
public class RestClient extends Activity {

    // Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
    public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";

    // See https://developer.chrome.com/multidevice/android/intents
    public static final String REST_CALLBACK_URL_TEMPLATE = "intent://%s#Intent;action=android.intent.action.VIEW;scheme=%s;package=%s;S.browser_fallback_url=%s;end";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private static final String BASE_URL = Config.REST_URL;
    private static final String ECE_BASE_URL = Config.CHAT_REST_URL;
    private String mURL;
    private String mPayload;
    private Context mContext;
    private String stringUrl;
    private String payload;
    private Context context;

    public RestClient() {
    }
    public RestClient(String URL, String payLoad, Context context) {
        super();
        this.mURL = URL;
        this.mPayload = payLoad;
        this.mContext = context;
        //doInBackground();
    }
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        System.out.println("** Connecting to: " + url);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(ECE_BASE_URL, params, responseHandler);
    }
    public static void postJSON(JSONObject url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(ECE_BASE_URL, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL; // + relativeUrl;
    }

    public static String makePostRequest(String stringUrl, String payload, Context context) throws IOException {
        URL url = new URL(ECE_BASE_URL);
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        String line;
        StringBuffer jsonString = new StringBuffer();

        uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        uc.setRequestMethod("POST");
        uc.setDoInput(true);
        uc.setInstanceFollowRedirects(false);
        uc.connect();
        OutputStreamWriter writer = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
        writer.write(payload);
        writer.close();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            while((line = br.readLine()) != null){
                jsonString.append(line);
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        uc.disconnect();
        return jsonString.toString();
    }
    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                makePostRequest(mURL, mPayload, mContext);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            return;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
