package com.example.kangwenn.currexez;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/**
 * Created by kangw on 22/1/2018.
 */

public class LauncherActivity extends AppCompatActivity{
    private static final int RC_SIGN_IN = 100;
    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";
    private static final String GOOGLE_PRIVACY_POLICY_URL = "https://www.google.com/policies/privacy/";
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setTheme(R.style.AppTheme);
        setContentView(R.layout.login);
        MobileAds.initialize(this,"ca-app-pub-2148688310360459~5245440096");
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String date = sdf.format(new Date());
        bundle.putString("open_time", date);
        mFirebaseAnalytics.logEvent("app_open", bundle);
        Answers.getInstance().logCustom(new CustomEvent("App Opened").putCustomAttribute("time", date));
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //if the user is signed in, launch homepage, else launch the sign in (AuthUI) Activity
        if (auth.getCurrentUser() != null) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        } else {
            signIn();
        }
    }

    public void signIn() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(R.style.BlackBackground)
                        .setLogo(R.drawable.logo)
                        .setAvailableProviders(
                                Arrays.asList(
                                        new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                        new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()
                                )
                        )
                        .setTosUrl(GOOGLE_TOS_URL)
                        .setPrivacyPolicyUrl(GOOGLE_PRIVACY_POLICY_URL)
                        .setIsSmartLockEnabled(false, true)
                        .setAllowNewEmailAccounts(true)
                        .build(),
                RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == RESULT_OK) {
                Bundle bundle = new Bundle();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
                String date = sdf.format(new Date());
                bundle.putString("login_time", date);
                mFirebaseAnalytics.logEvent("login", bundle);
                Answers.getInstance().logLogin(new LoginEvent()
                        .putSuccess(true));
                //updateUserDetails();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showToast(R.string.sign_in_cancelled);
                    super.onBackPressed();
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showToast(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showToast(R.string.unknown_error);
                    return;
                }
            }

            showToast(R.string.unknown_sign_in_response);
        }
    }
    @MainThread
    private void showToast(@StringRes int errorMessageRes) {
        Toast.makeText(getApplicationContext(), errorMessageRes, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }
}
