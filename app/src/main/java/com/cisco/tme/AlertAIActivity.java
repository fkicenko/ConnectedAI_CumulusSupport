package com.cisco.tme;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.cisco.tme.other.ChatAdapter;
import com.google.gson.JsonElement;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
import ai.api.ui.AIDialog;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class AlertAIActivity extends Activity implements AIDialog.AIDialogListener{

    private static final String TAG = AlertAIActivity.class.getSimpleName();
    private AIConfiguration config;
    private Context myContext;
    private AIDialog aiDialog;
    EmojiconEditText emojiconEditText;
    private ChatAdapter chatAdapter;
    private AIDataService aiDataService;
    private boolean side = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize DialogFlow for Speech to Text
        config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        myContext = this;
        aiDialog = new AIDialog(myContext, config, R.layout.aidialog);
        aiDialog.setResultsListener(this);
        TTS.init(myContext);

        sendRequest();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onCancelled() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emojiconEditText.setText("");
            }
        });
    }
    @Override
    public void onError(final AIError error) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emojiconEditText.setText(error.toString());
            }
        });
    }
    /*
     * AIRequest should have query OR event
     */
    private void sendRequest() {

        final String queryString = "e125";
        final String contextString = "";
        final String eventString = "";

        if (queryString.isEmpty()) {
            onError(new AIError(getString(R.string.non_empty_query)));
            return;
        }
        //getSentiment(emojiconEditText.getText().toString());

        final AsyncTask<String, Void, AIResponse> task = new AsyncTask<String, Void, AIResponse>() {

            private AIError aiError;

            @Override
            protected AIResponse doInBackground(final String... params) {
                final AIRequest request = new AIRequest();
                String query = params[0];
                String event = params[1];
                if (!query.isEmpty())
                    request.setQuery(query);
                if (!event.isEmpty())
                    request.setEvent(new AIEvent(event));
                final String contextString = params[2];
                RequestExtras requestExtras = null;
                if (!contextString.isEmpty()) {
                    final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
                    requestExtras = new RequestExtras(contexts, null);
                }

                try {
                    return aiDataService.request(request, requestExtras);
                } catch (final AIServiceException e) {
                    aiError = new AIError(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final AIResponse response) {
                if (response != null) {
                    onResult(response);
                } else {
                    onError(aiError);
                }
            }
        };

        task.execute(queryString, eventString, contextString);
    }
    @Override
    public void onResult(final AIResponse response) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onResult");
                //resultTextView.setText(gson.toJson(response));
                Log.i(TAG, "Received success response");
                emojiconEditText.setText("");
                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());
                // Display spoken words in Chat
                chatAdapter.add(new ChatMessage(side,  result.getResolvedQuery()));
                Log.i(TAG, "Action: " + result.getAction());
                final String speech = result.getFulfillment().getSpeech();
                chatAdapter.add(new ChatMessage(!side, speech));
                Log.i(TAG, "Speech: " + speech);
                if(((MainActivity)myContext).getSettingsManager().isUseVoice()) {
                    // Speak our results and insert into the chat dialog
                    TTS.speak(speech);
                    // We should have waited for speech to end and now listen
                    aiDialog.showAndListen();
                }

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                    }
                }
            }

        });
        onResume();
        /*if(((MainActivity)myContext).getSettingsManager().isUseVoice()) {
            aiDialog.resume();
        }*/
    }
}
