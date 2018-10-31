package com.cisco.tme.BroadcastReceiver;

import android.content.Intent;
import android.util.Log;

import com.cisco.tme.Config;
import com.cisco.tme.REST.RestClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by frank.kicenko on 2018-02-17.
 */

public class TokenRefreshListenerService extends FirebaseInstanceIdService {
    private static final String TAG = TokenRefreshListenerService.class.getSimpleName();
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        System.out.println("*** TOKEN *** :" + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }
    public void sendRegistrationToServer(String refreshedToken) {
        // Initiate REST call to Context Service to obtain any Alerts from Context Service
		RestClient rc = new RestClient();
        RequestParams params = new RequestParams();
        params.put("token", refreshedToken);
		rc.post(Config.REST_URL_TOKEN, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// If the response is JSONObject instead of expected JSONArray
				System.out.println("Received JSONObject: " + response);

			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
				// Do something with the response
				System.out.println("Received JSONArray: " + timeline);

			}
		});
    }

}
