package com.example.kangwenn.currexez;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, HomepageFragment.OnFragmentInteractionListener {
    TextView userName, userEmail;
    ImageView userProfilePic;
    FirebaseUser currentFirebaseUser;
    String apiKey;
    private DatabaseReference mReference;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PurchaseCurrency.class);
                startActivity(i);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        String date = sdf.format(new Date());
        if (!date.equals(sharedPref.getString("date", null))) {
            retrieveCurrencyRates();
        }
    }

    protected void retrieveCurrencyRates() {
        String url = "http://data.fixer.io/api/latest?access_key=b248b26a99c6e0ba6ed327d7a59cbcd1";
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final String[] currencyName = {"AUD", "MYR", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP", "HKD", "HRK", "HUF", "IDR", "ILS", "INR", "ISK", "JPY", "KRW", "MXN", "NOK", "NZD", "PHP", "PLN", "RON", "RUB", "SEK", "SGD", "THB", "TRY", "TWD", "USD", "ZAR"};
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject rates = response.getJSONObject("rates");
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    for (int i = 0; i < currencyName.length; i++) {
                        Double msiaRate = rates.getDouble("MYR");
                        Double currRate = rates.getDouble(currencyName[i]);
                        editor.putFloat(currencyName[i], currRate.floatValue() / msiaRate.floatValue());
                    }
                    editor.putString("date", response.getString("date"));
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirebaseAnalytics.logEvent("app_destroy", null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.logEvent("click_home", null);
        mReference = FirebaseDatabase.getInstance().getReference("User");
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = mReference.child(currentFirebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Intent i = new Intent(getApplicationContext(), EditProfile.class);
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Fragment fragment = new HomepageFragment();
        this.getFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "homepage").commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        userName = findViewById(R.id.textUserName);
        userEmail = findViewById(R.id.textUserEmail);
        userProfilePic = findViewById(R.id.imageViewUserPic);
        String name = "", email = "";
        try {
            name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } catch (NullPointerException e) {

        }
        userName.setText(name);
        userEmail.setText(email);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), AboutUs.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_rate) {
            Intent i = new Intent(getApplicationContext(), CurrencyRates.class);
            startActivity(i);
        } else if (id == R.id.nav_calculator) {
            Intent i = new Intent(getApplicationContext(), CurrencyCalculator.class);
            startActivity(i);
        } else if (id == R.id.nav_purchase) {
            Intent i = new Intent(getApplicationContext(), PurchaseCurrency.class);
            startActivity(i);
        } else if (id == R.id.nav_history) {
            Intent i = new Intent(getApplicationContext(), PurchaseHistory2.class);
            startActivity(i);
        } else if (id == R.id.nav_profile) {
            Intent i = new Intent(getApplicationContext(), UserProfile.class);
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Bundle bundle = new Bundle();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
                                String date = sdf.format(new Date());
                                bundle.putString("logout_time", date);
                                mFirebaseAnalytics.logEvent("logout", bundle);
                                Intent i = new Intent(getApplicationContext(), LauncherActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                showToast(R.string.sign_out_failed);
                            }
                        }
                    });
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @MainThread
    private void showToast(@StringRes int errorMessageRes) {
        Toast.makeText(getApplicationContext(), errorMessageRes, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
