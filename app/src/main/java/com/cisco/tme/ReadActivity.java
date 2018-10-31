package com.cisco.tme;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Frank Kicenko, Cisco Systems Inc.
 *
 */
public class ReadActivity extends Activity {
	
//	private static final String TAG = "ReadActivity";
	
	private TextView tv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read);
        
        tv = (TextView) findViewById(R.id.textView1);
        tv.setTextSize(CumulusSupport.getFontSize());
        
        String id = getIntent().getStringExtra("id");
		String[] projection = {"_id", "address", "date", "body"};
		String selection = "_id = ?";
		String[] selectionArgs = {id};
        Cursor c = getContentResolver().query(CumulusSupport.inboxUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
        	setTitle(c.getString(c.getColumnIndex("address")));
        	tv.setText(c.getString(c.getColumnIndex("body")));
        }
    }
    
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageButton1:
			if (tv.getTextSize() > 1) tv.setTextSize(tv.getTextSize()-1);
			CumulusSupport.sp.edit().putString(CumulusSupport.FONT_SIZE, String.valueOf(tv.getTextSize())).commit();
			break;
			
		case R.id.imageButton2:
			tv.setTextSize(tv.getTextSize()+1);
			CumulusSupport.sp.edit().putString(CumulusSupport.FONT_SIZE, String.valueOf(tv.getTextSize())).commit();
			break;
			
		case R.id.imageButton3:
			CumulusSupport.setClipboardData(tv.getText().toString());
			Toast.makeText(this, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
			break;
			
		case R.id.imageButton4:
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.setType("text/*");// text/plain
			shareIntent.putExtra(Intent.EXTRA_TEXT, tv.getText().toString());
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, "subject"); // TODO
			startActivity(Intent.createChooser(shareIntent, "Share via"));
			break;			
		}
	}    

}
