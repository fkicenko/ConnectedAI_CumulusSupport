package com.cisco.tme.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.cisco.tme.AIApplication;
import com.cisco.tme.MainActivity;
import com.cisco.tme.R;
import com.cisco.tme.SettingsManager;

import ai.api.util.BluetoothController;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String SETTINGS_PREFS_NAME = "com.cisco.tme.APP_SETTINGS";
    private static final String PREF_USE_BLUETOOTH = "USE_BLUETOOTH";
    private static final String PREF_USE_VOICE = "USE_VOICE";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private Context myContext;
    private boolean useBluetooth;
    private boolean useVoice;
    private CheckBox bluetoothSwitch;
    private CheckBox voiceSwitch;
    private SettingsManager settingsManager;
    private SharedPreferences prefs;
    View v;

    public SettingsFragment()  {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = v.getContext().getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);

        useBluetooth = prefs.getBoolean(PREF_USE_BLUETOOTH, true);
        useVoice = prefs.getBoolean(PREF_USE_VOICE, true);
        settingsManager = ((MainActivity)myContext).getSettingsManager();

        ViewGroup bluetoothSection = (ViewGroup) v.findViewById(R.id.activity_settings_bluetooth_section);
        bluetoothSection.setOnClickListener(this);

        bluetoothSwitch = (CheckBox) v.findViewById(R.id.activity_settings_bluetooth_swith);
        bluetoothSwitch.setChecked(settingsManager.isUseBluetooth());
        bluetoothSwitch.setOnCheckedChangeListener(this);

        ViewGroup voiceSection = (ViewGroup) v.findViewById(R.id.activity_settings_voice_section);
        voiceSection.setOnClickListener(this);

        voiceSwitch = (CheckBox) v.findViewById(R.id.activity_settings_voice);
        voiceSwitch.setChecked(settingsManager.isUseVoice());
        voiceSwitch.setOnCheckedChangeListener(this);


        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.activity_settings_bluetooth_swith:
                settingsManager.setUseBluetooth(isChecked);
                Toast.makeText(myContext, "Bluetooth is " + isChecked, Toast.LENGTH_SHORT).show();
                break;
            case R.id.activity_settings_voice:
                settingsManager.setUseVoice(isChecked);
                Toast.makeText(myContext, "Voice is " + isChecked, Toast.LENGTH_SHORT).show();
                break;
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_settings_bluetooth_section:
                bluetoothSwitch.performClick();
                break;
            case R.id.activity_settings_voice_section:
                voiceSwitch.performClick();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
