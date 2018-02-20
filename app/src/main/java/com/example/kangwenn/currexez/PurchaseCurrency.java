package com.example.kangwenn.currexez;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class PurchaseCurrency extends AppCompatActivity {
    SharedPreferences sharedPref;
    String[] currencyName = {"USD", "AUD", "CNY", "THB", "JPY", "GBP", "KRW", "HKD", "SGD"};
    String[] currName = new String[currencyName.length];
    Spinner spinnerSelectCurr;
    EditText editTextPurAmount;
    TextView textViewTotal;
    TextView textViewName;
    Button buttonProceed;
    RadioButton radioButtonCredit;
    RadioButton radioButtonOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_currency);
        spinnerSelectCurr = findViewById(R.id.spinnerSelectCurr);
        editTextPurAmount = findViewById(R.id.editTextPurAmount);
        textViewTotal = findViewById(R.id.textViewPrice);
        textViewName = findViewById(R.id.textViewCurrency);
        buttonProceed = findViewById(R.id.buttonProceed);
        radioButtonCredit = findViewById(R.id.radioButtonCredit);
        radioButtonOnline = findViewById(R.id.radioButtonOnline);
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
        buttonProceed.setEnabled(false);
        editTextPurAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextPurAmount.getText().toString().isEmpty()) {
                    textViewTotal.setText("");
                    buttonProceed.setEnabled(false);
                } else {
                    try {
                        Double amount = Double.parseDouble(editTextPurAmount.getText().toString());
                        float currRate = sharedPref.getFloat(currencyName[spinnerSelectCurr.getSelectedItemPosition()], 0);
                        Double total = amount / currRate;
                        textViewTotal.setText("This will cost you: RM " + String.format(Locale.US, "%.2f", total));
                        buttonProceed.setEnabled(true);
                    } catch (NumberFormatException e) {

                    }
                }
            }
        });
        spinnerSelectCurr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textViewName.setText(currName[position]);
                try {
                    Double amount = Double.parseDouble(editTextPurAmount.getText().toString());
                    float currRate = sharedPref.getFloat(currencyName[position], 0);
                    Double total = amount / currRate;
                    textViewTotal.setText("This will cost you: RM " + String.format(Locale.US, "%.2f", total));
                } catch (NumberFormatException e) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioButtonCredit.isChecked()) {

                } else if (radioButtonOnline.isChecked()) {

                }
                Toast.makeText(getBaseContext(), "Successfully Purchased!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
