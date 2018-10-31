/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cisco.tme;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;

import ai.api.util.BluetoothController;

public class SettingsManager extends Fragment{

    private static final String SETTINGS_PREFS_NAME = "com.cisco.tme.APP_SETTINGS";
    private static final String PREF_USE_BLUETOOTH = "USE_BLUETOOTH";
    private static final String PREF_USE_VOICE = "USE_VOICE";

    private Context context;
    private SharedPreferences prefs;

    private boolean useBluetooth;
    private boolean useVoice;

    public SettingsManager(final Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);

        useBluetooth = prefs.getBoolean(PREF_USE_BLUETOOTH, true);
        useVoice = prefs.getBoolean(PREF_USE_VOICE, true);
    }

    public void setUseBluetooth(final boolean useBluetooth) {
        this.useBluetooth = useBluetooth;

        prefs.edit().putBoolean(PREF_USE_BLUETOOTH, useBluetooth).commit();
        final BluetoothController controller = ((MainActivity)context).getBluetoothController();
        if (useBluetooth) {
            controller.start();
        } else {
            controller.stop();
        }
    }
    public void setUseVoice(final boolean useVoice) {
        this.useVoice = useVoice;

        prefs.edit().putBoolean(PREF_USE_VOICE, useVoice).commit();
        final BluetoothController controller = ((MainActivity)context).getBluetoothController();
        if (useVoice) {
            controller.start();
        } else {
            controller.stop();
        }
    }

    public boolean isUseBluetooth() {
        return useBluetooth;
    }
    public boolean isUseVoice() {
        return useVoice;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
