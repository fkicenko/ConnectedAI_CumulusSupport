package com.cisco.tme.ECE;

import android.os.AsyncTask;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

/**
 * Created by frank.kicenko on 2018-03-05.
 */

public class Login extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... params) {
        // Create a connection to the jabber.org server.
        DomainBareJid serviceName = null;
        try {
            serviceName = JidCreate.domainBareFrom("example.org");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword("user1", "123")
                .setHost("ece.cc.com")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setXmppDomain(serviceName)
                .setPort(5222)
                .setDebuggerEnabled(true) // to view what's happening in detail
                .build();


        AbstractXMPPConnection conn1 = new XMPPTCPConnection(config);
        try {
            conn1.connect();
            if(conn1.isConnected()) {
                Log.w("app", "conn done");
            }
            conn1.login();

            if(conn1.isAuthenticated()) {
                Log.w("app", "Auth done");
            }
        }
        catch (Exception e) {
            Log.w("app", e.toString());
        }

        return "";
    }


    @Override
    protected void onPostExecute(String result) {
    }

}

