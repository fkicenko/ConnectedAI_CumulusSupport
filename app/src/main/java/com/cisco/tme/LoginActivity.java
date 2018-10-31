package com.cisco.tme;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewManager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button login;
    private TextView loginLockedTV;
    private TextView attemptsLeftTV;
    private TextView numberOfRemainingLoginAttemptsTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setup the logo on the Login view
        CardView cardView = (CardView) findViewById(R.id.cv);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("", "CardView clicked");
            }
        });
        // Create the Image for the login screen
        ImageView imageView = (ImageView) findViewById(R.id.iv);
        Picasso.with(imageView.getContext())
//                .load("http://o.aolcdn.com/hss/storage/midas/19dcdabec46a02182add1b78b897392/202720874/google-pixel-c-1200.jpg")
                .load(R.drawable.blogpost2)
                .resize(dp2px(220), 0)
                .into(imageView);
        // Setup the hints for the login credentials
        final TextInputLayout usernameWrapper = (TextInputLayout) findViewById(R.id.usernameWrapper);
        final TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);
        usernameWrapper.setHint("michael.littlefoot");
        passwordWrapper.setHint("**********");
        login = findViewById(R.id.loginButton);
        setupVariables(usernameWrapper, passwordWrapper);
    }
    public int dp2px(int dp) {
        WindowManager wm = (WindowManager) this.getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);
        return (int) (dp * displaymetrics.density + 0.5f);
    }
    public void authenticateLogin(View view) {
        // Just evaluate blank credentials for demo purposes
        if (username.getText().toString().equals("") && password.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Hello Michael" + username.getText().toString() + "!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Could not log you in! Setting up Guest access", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void setupVariables(TextInputLayout Username, TextInputLayout Password ) {
        username = (EditText) Username.getEditText();
        username.requestFocus();
        password = (EditText) Password.getEditText();

    }

}