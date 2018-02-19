package com.example.kangwenn.currexez;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class CurrencyRates extends AppCompatActivity {
    TextView textRates;
    TextView textDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_rates);
        textRates = findViewById(R.id.textRates);
        textDate = findViewById(R.id.textViewDate);
        String[] currencyName = {"USD", "AUD", "CNY", "THB", "JPY", "GBP", "KRW", "HKD", "SGD"};
        String text = "";
        try {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
            textDate.setText("Last updated: " + sharedPref.getString("date", null));
            for (int i = 0; i < currencyName.length; i++) {
                float currRate = sharedPref.getFloat(currencyName[i], 0);
                String string = getResources().getString(getResources().getIdentifier(currencyName[i], "string", getApplicationContext().getPackageName()));
                //text += currencyName[i] + " : "+currRate + "\n";
                text += string + " : " + currRate + "\n";
            }
            textRates.setText(text);
        } catch (RuntimeException e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
