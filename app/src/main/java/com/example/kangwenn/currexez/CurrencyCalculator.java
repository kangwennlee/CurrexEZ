package com.example.kangwenn.currexez;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

public class CurrencyCalculator extends AppCompatActivity {
    EditText editTextAmount;
    EditText editTextAmount2;
    Spinner spinnerCurrency;
    TextView textViewTotal;
    SharedPreferences sharedPref;
    String[] currencyName = {"USD", "AUD", "CNY", "THB", "JPY", "GBP", "KRW", "HKD", "SGD"};
    String[] currName = new String[currencyName.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_calculator);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextAmount2 = findViewById(R.id.editTextAmount2);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        textViewTotal = findViewById(R.id.textViewTotal);
        sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
        for (int i = 0; i < currencyName.length; i++) {
            String string = getResources().getString(getResources().getIdentifier(currencyName[i], "string", getApplicationContext().getPackageName()));
            currName[i] = string;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);
        Intent intent = getIntent();
        int position = intent.getIntExtra("currency", 0);
        spinnerCurrency.setSelection(position);
        try {
            Double amount = Double.parseDouble(editTextAmount.getText().toString());
            float currRate = sharedPref.getFloat(currencyName[spinnerCurrency.getSelectedItemPosition()], 0);
            Double total = amount * currRate;
            editTextAmount2.setText(String.format(Locale.US, "%.2f", total));
        } catch (NumberFormatException e) {

        }
        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextAmount.hasFocus()) {
                    try {
                        Double amount = Double.parseDouble(editTextAmount.getText().toString());
                        float currRate = sharedPref.getFloat(currencyName[spinnerCurrency.getSelectedItemPosition()], 0);
                        Double total = amount * currRate;
                        editTextAmount2.setText(String.format(Locale.US, "%.2f", total));
                    } catch (NumberFormatException e) {

                    }
                }
            }
        });
        editTextAmount2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextAmount2.hasFocus()) {
                    try {
                        Double amount = Double.parseDouble(editTextAmount2.getText().toString());
                        float currRate = sharedPref.getFloat(currencyName[spinnerCurrency.getSelectedItemPosition()], 0);
                        Double total = amount / currRate;
                        editTextAmount.setText(String.format(Locale.US, "%.2f", total));
                    } catch (NumberFormatException e) {

                    }
                }
            }
        });
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Double amount = Double.parseDouble(editTextAmount.getText().toString());
                    float currRate = sharedPref.getFloat(currencyName[position], 0);
                    Double total = amount * currRate;
                    editTextAmount2.setText(String.format(Locale.US, "%.2f", total));
                } catch (NumberFormatException e) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
