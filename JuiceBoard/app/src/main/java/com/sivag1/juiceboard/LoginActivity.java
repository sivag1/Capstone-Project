package com.sivag1.juiceboard;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;
import com.parse.ParseUser;

import java.io.IOException;


public class LoginActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View
        .OnClickListener {

    private int result = 0;
    public static final int RESULT_DISCONNECT = 999;
    public static final int RESULT_LOGOFF = 998;
    /* Track whether the sign-in button has been clicked so that we know to resolve
     * all issues preventing sign-in without waiting.
     */
    private boolean mSignInClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can
     * resolve them when the user clicks sign-in.
     */
    private ConnectionResult mConnectionResult;
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 1;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;

    SignInButton auth;
    public static String USER_DATA = "USER_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = (SignInButton) findViewById(R.id.sign_in_button);
        auth.setOnClickListener(this);
        auth.setSize(SignInButton.SIZE_WIDE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            result = bundle.getInt("RESULT", 0);
        }
    }

    private void logoff() {
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        logoffUser(false);
        goSplash();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void skipLogin(View v) {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void doLogin(View view) {
        if (view.getId() == R.id.sign_in_button
                && !mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            goSplash();
        } else if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    private void goSplash() {
        Intent i = new Intent(this, SplashActivity.class);
        startActivity(i);
        this.finish();
    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (result == RESULT_DISCONNECT) {
            disconnect();
        } else if (result == RESULT_LOGOFF) {
            logoff();
        } else {
            final String personName;
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                personName = currentPerson.getDisplayName();
                //String id = currentPerson.getId();
            } else
                personName = null;

            AsyncTask<Void, Void, User> task = new AsyncTask<Void, Void, User>() {
                @Override
                protected User doInBackground(Void... params) {
                    User user = null;
                    String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
                    String accountID = null;
                    try {
                        accountID = GoogleAuthUtil.getAccountId(LoginActivity.this, accountName);
                        user = new User();
                        user.setId(accountID);
                        user.setEmail(accountName);
                        user.setDisplayName(personName);
                    } catch (GoogleAuthException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return user;
                }

                @Override
                protected void onPostExecute(User user) {
                    super.onPostExecute(user);

                    if (user != null) {
                        String error = BackendDataFacade.maintainUser(user);
                        if (error == null) {
                            createLocalAccount(user);
                            goSplash();
                        } else {
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_error_toast), Toast.LENGTH_SHORT).show();

                    }
                }
            };

            task.execute();
        }
    }

    private void createLocalAccount(User user) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        editor.putString(USER_DATA, gson.toJson(user));
        editor.commit();
    }

    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {
        //result =0;
        //mGoogleApiClient.connect();
        doLogin(v);
    }

    private void disconnect() {
        // Prior to disconnecting, run clearDefaultAccount().
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        // mGoogleApiClient is now disconnected and access has been revoked.
                        // Trigger app logic to comply with the developer policies
                        if (status.isSuccess()) {
                            logoffUser(true);
                            goSplash();
                        }
                    }
                });

    }

    private void logoffUser(boolean isAllUsers) {
        String error = BackendDataFacade.delete(this, isAllUsers);
        if (error == null) {
            if (isAllUsers) {
                ParseUser parseUser = ParseUser.getCurrentUser();
                String userDeleteError = BackendDataFacade.deleteUser(parseUser);
                if (userDeleteError == null) {
                    ParseUser.logOut();
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.commit();
                }
            }
        } else {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        }
    }

}
