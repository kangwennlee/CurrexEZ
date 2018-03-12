package com.example.kangwenn.currexez;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;

public class CurrencyRates extends AppCompatActivity {
    SwipeRefreshLayout swipeRefreshLayout;
    ListView listView;
    TextView textDate, textHeading;
    String[] currencyName = {"USD", "EUR", "AUD", "GBP", "SGD", "CNY", "THB", "JPY", "KRW", "HKD", "TWD"};
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_rates);
        setTitle("Today's Rate");
        listView = findViewById(R.id.currencyList);
        textDate = findViewById(R.id.textDate);
        textHeading = findViewById(R.id.textHeading);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        retrieveRates();
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrieveRates();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), CurrencyCalculator.class);
                i.putExtra("currency", position);
                Bundle bundle = new Bundle();
                bundle.putString("currency_name",currencyName[position]);
                mFirebaseAnalytics.logEvent("currency_rate_selected",bundle);
                startActivity(i);
            }
        });
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.logEvent("click_rate",null);
    }

    protected void retrieveRates() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
        Date currentDate = new Date(System.currentTimeMillis());
        textHeading.setText("Rate for " + sharedPref.getString("date", null));
        String text = "";
        textDate.setText("Last refreshed: " + currentDate.toString());
        String[] currencyN = new String[currencyName.length];
        try {
            for (int i = 0; i < currencyName.length; i++) {
                Float currRate;
                String string = getResources().getString(getResources().getIdentifier(currencyName[i], "string", getApplicationContext().getPackageName()));
                if (sharedPref.getFloat(currencyName[i], 0) < 1) {
                    currRate = 1 / sharedPref.getFloat(currencyName[i], 0);
                    text = "1 " + string + "\t\t\tRM " + currRate;
                } else {
                    currRate = 1 / sharedPref.getFloat(currencyName[i], 0) * 100;
                    text = "100 " + string + "\t\t\tRM " + currRate;
                }

                currencyN[i] = text;
            }
            swipeRefreshLayout.setRefreshing(false);
        } catch (RuntimeException e) {

        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currencyN);
        listView.setAdapter(adapter);
    }
}
