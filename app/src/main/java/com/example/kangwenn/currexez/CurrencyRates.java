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

import java.util.Date;

public class CurrencyRates extends AppCompatActivity {
    SwipeRefreshLayout swipeRefreshLayout;
    ListView listView;
    TextView textDate;
    String[] currencyName = {"USD", "AUD", "CNY", "THB", "JPY", "GBP", "KRW", "HKD", "SGD"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_rates);
        listView = findViewById(R.id.currencyList);
        textDate = findViewById(R.id.textDate);
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
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void retrieveRates() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
        Date currentDate = new Date(System.currentTimeMillis());
        String text = "";
        textDate.setText("Last updated: " + currentDate.toString());
        String[] currencyN = new String[currencyName.length];
        try {
            for (int i = 0; i < currencyName.length; i++) {
                float currRate = sharedPref.getFloat(currencyName[i], 0);
                String string = getResources().getString(getResources().getIdentifier(currencyName[i], "string", getApplicationContext().getPackageName()));
                //text += currencyName[i] + " : "+currRate + "\n";
                text = currRate + " " + string;
                currencyN[i] = text;
            }
            swipeRefreshLayout.setRefreshing(false);
        } catch (RuntimeException e) {

        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currencyN);
        listView.setAdapter(adapter);
    }
}
