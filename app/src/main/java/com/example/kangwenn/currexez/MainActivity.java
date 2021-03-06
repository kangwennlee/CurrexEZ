package com.example.kangwenn.currexez;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.kangwenn.currexez.Entity.Purchase;
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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, HomepageFragment.OnFragmentInteractionListener {
    TextView textUserName, textUserEmail;
    ImageView userProfilePic;
    FirebaseUser currentFirebaseUser;
    String apiKey;
    private DatabaseReference mReference;
    private FirebaseAnalytics mFirebaseAnalytics;

    String todaydate;
    Date dateToday;
    Date collectionDate;
    Activity activity;

    String nationality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activity = this;
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PurchaseCurrency.class);
                startActivity(i);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });*/

        FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.floatingBtnMenu);

        final com.getbase.floatingactionbutton.FloatingActionButton purchase =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_purchase);
        purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PurchaseCurrency.class);
                startActivity(i);
            }
        });

        final com.getbase.floatingactionbutton.FloatingActionButton sell =
                (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_sell);
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SellCurrency.class);
                startActivity(i);
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
        final SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        String date = sdf.format(new Date());
        if (!date.equals(sharedPref.getString("date", null))) {
            retrieveCurrencyRates();
        }

        nationality = sharedPref.getString("nationality", null);

        setNotificationAlarm();
    }

    protected void retrieveCurrencyRates() {
        String url = "http://data.fixer.io/api/latest?access_key=b248b26a99c6e0ba6ed327d7a59cbcd1";
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        final String[] currencyName = {"AUD", "MYR", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP", "HKD", "HRK", "HUF", "IDR", "ILS", "INR", "ISK", "JPY", "KRW", "MXN", "NOK", "NZD", "PHP", "PLN", "RON", "RUB", "SAR", "SEK", "SGD", "THB", "TRY", "TWD", "USD", "ZAR"};
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
                } else {
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
                    if (sharedPref.getString("nationality", null) == null) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("nationality", dataSnapshot.child("nation").getValue().toString());
                        editor.apply();
                        Fragment fragment = new HomepageFragment();
                        getFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "homepage").commit();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Fragment fragment = new HomepageFragment();
        getFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "homepage").commit();
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
        textUserName = findViewById(R.id.textUserName);
        textUserEmail = findViewById(R.id.textUserEmail);
        userProfilePic = findViewById(R.id.imageViewUserPic);
        try {
            textUserName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            textUserEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        } catch (NullPointerException e) {
            Log.d("d", e.getMessage());
        }
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
            if (nationality.equals("Malaysia")) {
                Intent i = new Intent(getApplicationContext(), PurchaseCurrency.class);
                startActivity(i);
            } else {
                Intent i = new Intent(getApplicationContext(), PurchaseRinggit.class);
                startActivity(i);
            }
        } else if (id == R.id.nav_history) {
            Intent i = new Intent(getApplicationContext(), PurchaseHistory2.class);
            startActivity(i);
        } else if (id == R.id.nav_profile) {
            Intent i = new Intent(getApplicationContext(), UserProfile.class);
            startActivity(i);
        } else if (id == R.id.nav_changeLanguage) {
            showDialogForLanguage();
        }else if(id == R.id.nav_QR) {
            IntentIntegrator integrator = new IntentIntegrator(activity);
            integrator.setOrientationLocked(true);
            integrator.initiateScan();
        }else if (id == R.id.nav_logout) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                //Scan cancelled
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Scan successful
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), QRPage.class);
                intent.putExtra("product", result.getContents());
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showDialogForLanguage(){
        String defaule = LocaleHelper.getLanguage(MainActivity.this);
        int position;
        if(defaule.equals("en"))
            position = 0;
        else
            position = 1;
        String title = getString(R.string.dialogTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setSingleChoiceItems(R.array.languageChoose, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 1){
                    changeLanguage("zh");
                    recreate();
                }else{
                    changeLanguage("en");
                    recreate();
                }
            }
        });
        builder.show();
    }

    public void changeLanguage(String code){
        Context context = LocaleHelper.setLocale(this, code);
        Resources resources = context.getResources();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    public void setNotificationAlarm(){
        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("PurchaseHistory");
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userID = mCurrentUser.getUid();
        DatabaseReference currentUser = databaseReference.child(userID);
        final ArrayList<Purchase> arrayList = new ArrayList<>();
        final ArrayList<Date> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
        todaydate = sdf.format(new Date());

        try {
            dateToday = new SimpleDateFormat("dd/MM/yy").parse(todaydate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds1 : ds.getChildren()) {
                        int i = 0;
                        Purchase purchase = ds1.getValue(Purchase.class);
                        try {
                            collectionDate = new SimpleDateFormat("dd/MM/yy").parse(purchase.getCollectionDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        long t = System.currentTimeMillis();

                        if (dateToday.before(collectionDate) || dateToday.equals(collectionDate)) {
                            arrayList.add(purchase);
                            dateList.add(collectionDate);
                            long test = dateList.get(i).getTime();
                            if (t <= dateList.get(i).getTime()){ // to check whether the notification have display before.
                                Intent intent = new Intent(getApplicationContext(),notificationReceiver.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),100,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                                alarmManager.set(AlarmManager.RTC_WAKEUP,dateList.get(i).getTime(),pendingIntent);
                                i++;
                            }

                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
