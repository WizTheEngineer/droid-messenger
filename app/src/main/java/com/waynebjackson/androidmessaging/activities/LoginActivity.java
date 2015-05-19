package com.waynebjackson.androidmessaging.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.waynebjackson.androidmessaging.MessagingApp;
import com.waynebjackson.androidmessaging.R;
import com.waynebjackson.androidmessaging.models.MessagingUser;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private EditText usernameField, passwordField;
    private Button btnSignUp, btnLogin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ParseUser.getCurrentUser() != null) {
            startMessagingActivity();
        }

        usernameField = (EditText) findViewById(R.id.user_name_field);
        passwordField = (EditText) findViewById(R.id.password_field);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        btnLogin.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_sign_up:
                signUp();
                break;
        }
    }

    private void login() {
        showProgressDialog("Logging In", "One moment please...");
        String userName = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString();

        ParseUser.logInInBackground(userName, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    startMessagingActivity();
                } else {
                    hideProgressDialog();
                    Toast.makeText(LoginActivity.this, "Error logging in, please try again", Toast.LENGTH_LONG).show();
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        });
    }

    private void signUp() {
        showProgressDialog("Signing Up", "One moment please...");
        String userName = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString();

        MessagingUser newUser = new MessagingUser();
        newUser.setUsername(userName);
        newUser.setPassword(password);

        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    startMessagingActivity();
                } else {
                    hideProgressDialog();
                    Toast.makeText(LoginActivity.this, "Error signing up, please try again", Toast.LENGTH_LONG).show();
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        });
    }

    private void startMessagingActivity() {
        Intent messagingActivity = new Intent(this, MessagingActivity.class);
        startActivity(messagingActivity);

        // Subscribe to channel to receive messages
        ((MessagingApp) getApplication()).subscribeToMessagingChannel();
    }

    private void showProgressDialog(String title, String message) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        progressDialog.hide();
    }
}
