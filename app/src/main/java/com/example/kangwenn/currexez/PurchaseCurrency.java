package com.example.kangwenn.currexez;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class PurchaseCurrency extends AppCompatActivity {
    SharedPreferences sharedPref;
    String[] currencyName = {"USD", "AUD", "CNY", "THB", "JPY", "GBP", "KRW", "HKD", "SGD"};
    String[] currName = new String[currencyName.length];
    Spinner spinnerSelectCurr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_currency);
        spinnerSelectCurr = findViewById(R.id.spinnerSelectCurr);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
        for (int i = 0; i < currencyName.length; i++) {
            String string = getResources().getString(getResources().getIdentifier(currencyName[i], "string", getApplicationContext().getPackageName()));
            currName[i] = string;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectCurr.setAdapter(adapter);
    }
}
