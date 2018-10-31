package com.cisco.tme.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by frank.kicenko on 2018-02-17.
 */

public class CumulusReceiver extends BroadcastReceiver {

    public CumulusReceiver() {

    }
    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "Action: " + intent.getAction(), Toast.LENGTH_SHORT).show();
    }

}
