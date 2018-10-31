package com.cisco.tme.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;


import com.cisco.tme.ChatMessage;
import com.cisco.tme.Config;
import com.cisco.tme.ECE.Login;
import com.cisco.tme.MainActivity;
import com.cisco.tme.R;
import com.cisco.tme.REST.RestClient;

import com.cisco.tme.TTS;
import com.cisco.tme.nl.language.AccessTokenLoader;
import com.cisco.tme.nl.language.ApiFragment;
import com.cisco.tme.nl.language.model.EntityInfo;
import com.cisco.tme.nl.language.model.SentimentInfo;
import com.cisco.tme.nl.language.model.TokenInfo;
import com.cisco.tme.other.ChatAdapter;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
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
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment implements AIDialog.AIDialogListener {

    private static final String TAG = ChatFragment.class.getName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String messageText;
    private String messageUser;
    private long messageTime;
    private ChatAdapter chatAdapter;
    private ListView listView;
    private EditText chatText;
    private AIDataService aiDataService;
    private AIConfiguration config;
    private Context myContext;
    private boolean side = false;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ApiFragment apiFrag;

    private AIDialog aiDialog;

    private OnFragmentInteractionListener mListener;

    //Add Emojicon
    EmojiconEditText emojiconEditText;
    ImageView emojiButton,submitButton;
    EmojIconActions emojIconActions;
    View v;

    String payload = "{\n" +
            "  \"object\": \"mobile\",\n" +
            "  \"entry\": [{\n" +
            "    \"id\": \"1717527131834678\",\n" +
            "    \"time\": 1475942721780,\n" +
            "    \"messaging\": [{\n" +
            "      \"sender\": {\n" +
            "        \"id\": \"1256217357730577\"\n" +
            "      },\n" +
            "      \"recipient\": {\n" +
            "        \"id\": \"1717527131834678\"\n" +
            "      },\n" +
            "      \"timestamp\": 1475942721741,\n" +
            "      \"message\": {\n" +
            "        \"mid\": \"mid.1475942721728:3b9e3646712f9bed52\",\n" +
            "        \"seq\": 123,\n" +
            "        \"text\": \"Hello Chatbot\"\n" +
            "      }\n" +
            "    }]\n" +
            "  }]\n" +
            "}";

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Initialize DialogFlow for Speech to Text
        config = new AIConfiguration(Config.ACCESS_TOKEN,
                 AIConfiguration.SupportedLanguages.English,
                 AIConfiguration.RecognitionEngine.System);
        if(((MainActivity)myContext).getSettingsManager().isUseVoice()) {
            aiDialog = new AIDialog(getContext(), config, R.layout.aidialog);
            aiDialog.setResultsListener(this);
            TTS.init(myContext);
        }

        // Initialize the Sentiment API
        apiFrag = new ApiFragment();

        Login task = new Login();
        task.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_chat, container, false);
        // Let's disable the screen lock mode
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Add Emoji
        emojiButton = (ImageView)v.findViewById(R.id.emoji_button);
        submitButton = (ImageView)v.findViewById(R.id.submit_button);
        emojiconEditText = (EmojiconEditText)v.findViewById(R.id.emojicon_edit_text);
        emojIconActions = new EmojIconActions(v.getContext(), v, emojiButton, emojiconEditText);
        emojIconActions.ShowEmojicon();

        // Create the ListView
        ListView listOfMessage = (ListView)v.findViewById(R.id.list_of_message);

        // Add Chat Adapter and assign our text to the Right hand side
        chatAdapter = new ChatAdapter(v.getContext(), R.layout.right);
        listOfMessage.setAdapter(chatAdapter);
        aiDataService = new AIDataService(v.getContext(), config);
        emojiconEditText.setText("e125");
        sendRequest();
        // Send text via REST to ECE Server
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojiconEditText.requestFocus();
                sendRequest();
                apiFrag.analyzeSentiment(emojiconEditText.getText().toString());
                /*payload = payload.replaceAll("\n", "");
                payload = payload.replaceAll("\\s+","");
                System.out.println("JSON OUT: " + payload);
                System.out.println("JSON OUT2: " + getBodyTextAsJSON(payload).toString());

                // Initiate REST call to messaging server
                RestClient rc = new RestClient(Config.CHAT_REST_URL, payload, view.getContext());
                sendChatMessage();*/



            }
        });
        return v;
    }
    protected JSONObject getBodyTextAsJSON(String text) {
        String bodyText = text;
        if (bodyText != null && !TextUtils.isEmpty(bodyText)) {
            try {
                return new JSONObject(bodyText);
            } catch (JSONException e) {
                System.out.println("User's data is not a valid JSON object: " + e);
            }

        }
        return null;
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    /*
    * AIRequest should have query OR event
    */
    private void sendRequest() {

        final String queryString = String.valueOf(emojiconEditText.getText());
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
        getActivity().runOnUiThread(new Runnable() {
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
    @Override
    public void onCancelled() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emojiconEditText.setText("");
            }
        });
    }
    @Override
    public void onError(final AIError error) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emojiconEditText.setText(error.toString());
            }
        });
    }
    @Override
    public void onPause() {
        if (aiDialog != null) {
            aiDialog.pause();
        }
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (aiDialog != null) {
            aiDialog.resume();
        }
    }

    public void buttonListenOnClick(final View view) {
        aiDialog.showAndListen();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    private boolean sendChatMessage() {
        chatAdapter.add(new ChatMessage(side, emojiconEditText.getText().toString()));
        emojiconEditText.setText("");
        side = !side;
        return true;
    }
    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public String getMessageTime() {
        String messageTime = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        return messageTime;
    }

    // Google Sentiment Callbacks
    public void getSentiment(String text) {
        //apiFrag.analyzeEntities(text);
        apiFrag.analyzeSentiment(text);
        //apiFrag.analyzeSyntax(text);
    }
    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
