package com.example.kangwenn.currexez;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.PurchaseEvent;
import com.example.kangwenn.currexez.Entity.Purchase;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

public class SellCurrency extends AppCompatActivity {

    SharedPreferences sharedPref;
    String[] currencyName = {"USD", "EUR", "AUD", "GBP", "SGD", "CNY", "THB", "JPY", "KRW", "HKD", "TWD"};
    String[] currName = new String[currencyName.length];
    Spinner spinnerSelectCurr;
    EditText editTextSellAmount, editTextDate, editTextLocation;
    TextView textViewTotal, textViewName;
    Button buttonProceed;
    RadioButton radioButtonCredit, radioButtonOnline;
    RadioGroup radioGroup;
    Double total;
    Calendar myCalendar;
    private ProgressDialog progressDialog;
    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference databaseUser;
    private FirebaseUser currentFirebaseUser;

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_currency);
        spinnerSelectCurr = findViewById(R.id.spinnerSelectSellCurr);
        editTextSellAmount = findViewById(R.id.editTextSellAmount);
        textViewTotal = findViewById(R.id.textViewPrice);
        textViewName = findViewById(R.id.textViewCurrency);
        buttonProceed = findViewById(R.id.buttonProceed);
        radioButtonCredit = findViewById(R.id.radioButtonCredit);
        radioButtonOnline = findViewById(R.id.radioButtonOnline);
        radioGroup = findViewById(R.id.radioGroupMethod);
        editTextDate = findViewById(R.id.editTextCollectionDate);
        editTextLocation = findViewById(R.id.editTextCollectionLocation);
        progressDialog = new ProgressDialog(this);
        getSupportActionBar().setTitle("Sell Currency");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
        for (int i = 0; i < currencyName.length; i++) {
            String string = getResources().getString(getResources().getIdentifier(currencyName[i], "string", getApplicationContext().getPackageName()));
            currName[i] = string;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currName);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerSelectCurr.setAdapter(adapter);
        Intent intent = getIntent();
        spinnerSelectCurr.setSelection(intent.getIntExtra("purchaseType", 0));
        editTextSellAmount.setText(String.valueOf(intent.getDoubleExtra("purchaseAmount", 1.0)));
        editTextSellAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextSellAmount.getText().toString().isEmpty() || Double.valueOf(editTextSellAmount.getText().toString()).equals(0)) {
                    textViewTotal.setText("");
                    buttonProceed.setEnabled(false);
                } else {
                    try {
                        updatePrice(spinnerSelectCurr.getSelectedItemPosition());
                    } catch (NumberFormatException e) {

                    }
                    if (radioButtonCredit.isChecked() || radioButtonOnline.isChecked() && !editTextSellAmount.getText().toString().equals("") && Double.valueOf(editTextSellAmount.getText().toString()) > 0) {
                        buttonProceed.setEnabled(true);
                    }
                }
            }
        });
        spinnerSelectCurr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                textViewName.setText(currName[position]);
                try {
                    updatePrice(position);
                } catch (NumberFormatException e) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!editTextSellAmount.getText().toString().equals("")) {
                    if (Double.valueOf(editTextSellAmount.getText().toString()) > 0) {
                        buttonProceed.setEnabled(true);
                        if (radioButtonCredit.isChecked()) {

                        } else if (radioButtonOnline.isChecked()) {

                        }
                    } else {
                        buttonProceed.setEnabled(false);
                    }
                } else {
                    buttonProceed.setEnabled(false);
                }
            }
        });
        myCalendar = Calendar.getInstance();
        updateDate();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDate();
            }

        };
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(SellCurrency.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 3);
                mDatePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                mDatePicker.show();
            }
        });
        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.setMessage("Processing...");
                progressDialog.show();
                storePurchase();
            }
        });
        buttonProceed.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.logEvent("local_click_sell", null);
    }

    public void updateDate() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editTextDate.setText(sdf.format(myCalendar.getTime()));
    }

    public void updatePrice(int position) {
        Double amount = Double.parseDouble(editTextSellAmount.getText().toString());
        float currRate = sharedPref.getFloat(currencyName[position], 0);
        total = amount / currRate;
        textViewTotal.setText("You will get: RM " + String.format(Locale.US, "%.2f", total));
    }

    public void storePurchase() {
        if (radioButtonCredit.isChecked()) {
            mFirebaseAnalytics.logEvent("card_payment", null);
        } else {
            mFirebaseAnalytics.logEvent("online_payment", null);
        }
        String currency = spinnerSelectCurr.getSelectedItem().toString();
        Double purchaseAmount = Double.parseDouble(editTextSellAmount.getText().toString());
        Double purchaseAmountInRM = total;

        int selectID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectID);
        String selectRadioButtonValue = radioButton.getText().toString();

        String collectionDate = editTextDate.getText().toString();
        String collectionLoc = editTextLocation.getText().toString();

        databaseUser = FirebaseDatabase.getInstance().getReference("PurchaseHistory");
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //get the user UID
        String id = currentFirebaseUser.getUid();

        final Purchase purchase = new Purchase(currency, purchaseAmount, purchaseAmountInRM, selectRadioButtonValue, collectionDate, collectionLoc);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String date = sdf.format(new Date());
        String time = date.substring(9, 11) + ":" + date.substring(11, 13) + ":" + date.substring(12, 14);
        final String values = "Currency sold: " + purchase.getCurrency()
                + "\nSell Amount : " + round(purchase.getAmount(), 2)
                + "\nSell Amount In MYR : " + round(purchase.getAmountInRM(), 2)
                + "\nPayment Method : " + purchase.getPayMethod()
                + "\nTransaction Date : " + date + " " + time
                + "\nCollection Date : " + purchase.getCollectionDate()
                + "\nCollection Location: " + purchase.getCollectionLocation();
        Bundle bundle = new Bundle();
        bundle.putString("Currency_Purchased", purchase.getCurrency());
        bundle.putDouble("Purchase_Amount", purchase.getAmount());
        bundle.putDouble("Purchase_Amount_In_MYR", purchase.getAmountInRM());
        bundle.putString("Purchase_Date", date);
        bundle.putString("Purchase_Time", time);
        bundle.putString("Collection_Date", collectionDate);
        bundle.putString("Collection_Location", collectionLoc);
        mFirebaseAnalytics.logEvent("purchase_done", bundle);
        Answers.getInstance().logPurchase(new PurchaseEvent()
                .putItemPrice(BigDecimal.valueOf(purchaseAmountInRM))
                .putItemName(currency)
                .putCurrency(Currency.getInstance("MYR"))
                .putSuccess(true)
        );
        databaseUser.child(id).child("Sell").child(date).setValue(purchase, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Intent intent = new Intent(getApplicationContext(), SuccessPaymentActivity.class);
                    intent.putExtra("purchase", values);
                    Toast.makeText(getApplicationContext(), "Payment Successful!", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    progressDialog.dismiss();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Database failed. Please contact our customer service.", Toast.LENGTH_LONG).show();

                }
            }
        });
    }
}
