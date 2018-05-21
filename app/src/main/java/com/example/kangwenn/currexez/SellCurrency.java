package com.example.kangwenn.currexez;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jesusm.kfingerprintmanager.KFingerprintManager;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

public class SellCurrency extends AppCompatActivity implements accNumEnterDialog.dialogListener{

    SharedPreferences sharedPref;
    private static final String KEY = "KEY";
    String[] currencyName = {"USD", "EUR", "AUD", "GBP", "SGD", "CNY", "THB", "JPY", "KRW", "HKD", "TWD"};
    String[] currName = new String[currencyName.length];
    Spinner spinnerSelectCurr;
    EditText editTextSellAmount, editTextDate, editTextLocation;
    TextView textViewTotal, textViewName, textViewCard;
    Button buttonProceed;
    RadioButton radioButtonOnline;
    RadioGroup radioGroup;
    Double total;
    Calendar myCalendar;
    private ProgressDialog progressDialog;
    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference databaseUser;
    private FirebaseUser currentFirebaseUser;

    String[] bankName = {"MayBank","Public Bank", "AmBank","HSBC", "CIMB"};

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
        //
        spinnerSelectCurr = findViewById(R.id.spinnerSelectSellCurr);
        editTextSellAmount = findViewById(R.id.editTextSellAmount);
        textViewTotal = findViewById(R.id.textViewPrice);
        textViewName = findViewById(R.id.textViewCurrency);
        buttonProceed = findViewById(R.id.buttonProceed);
        radioButtonOnline = findViewById(R.id.radioButtonOnline);
        radioGroup = findViewById(R.id.radioGroupMethod);
        editTextDate = findViewById(R.id.editTextCollectionDate);
        editTextLocation = findViewById(R.id.editTextCollectionLocation);
        textViewCard = findViewById(R.id.textViewCreditSelected);
        progressDialog = new ProgressDialog(this);
        //
        getSupportActionBar().setTitle("Sell Currency");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        initialize();
    }

    private void initialize() {
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
                    if (radioButtonOnline.isChecked() && !editTextSellAmount.getText().toString().equals("") && Double.valueOf(editTextSellAmount.getText().toString()) > 0) {
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
                if (radioButtonOnline.isChecked()) {
                    //textViewCard.setText("");
                    addBankAccount();
                } else {
                    buttonProceed.setEnabled(true);
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
                //calendar.add(Calendar.DAY_OF_YEAR,1);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR) + 1);
                //mDatePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                mDatePicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
                mDatePicker.show();
            }
        });
        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextLocation.getText().toString().isEmpty() || editTextDate.getText().toString().isEmpty() || editTextSellAmount.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Complete the form!", Toast.LENGTH_LONG).show();
                } else {
                    fingerAuth();
                }
            }
        });
        buttonProceed.setEnabled(false);
    }

    private void fingerAuth() {
        createFingerprintManagerInstance().authenticate(new KFingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationSuccess() {
                //messageText.setText("Successfully authenticated");
                progressDialog.setMessage("Processing...");
                progressDialog.show();
                pickCard();
            }

            @Override
            public void onSuccessWithManualPassword(@NotNull String password) {
                //messageText.setText("Manual password: " + password);
            }

            @Override
            public void onFingerprintNotRecognized() {
                Toast.makeText(getApplicationContext(), "Fingerprint not recognized", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailedWithHelp(@Nullable String help) {
                //messageText.setText(help);
            }

            @Override
            public void onFingerprintNotAvailable() {
                //messageText.setText("Fingerprint not available");
            }

            @Override
            public void onCancelled() {
                //messageText.setText("Operation cancelled by user");
            }
        }, getSupportFragmentManager());

    }

    private KFingerprintManager createFingerprintManagerInstance() {
        KFingerprintManager fingerprintManager = new KFingerprintManager(this, KEY);
        //fingerprintManager.setAuthenticationDialogStyle(dialogTheme);
        return fingerprintManager;
    }

    private void pickCard() {
        Query query = FirebaseDatabase.getInstance().getReference().child("Card").child(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SellCurrency.this);
                builder.setTitle("Select your card");
                ArrayList<String> cardArrayList = new ArrayList<>();
                cardArrayList.add("Add New Card");
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String cardNumber = userSnapshot.getKey().toString();
                    if (!cardArrayList.contains(cardNumber)) {
                        cardArrayList.add(cardNumber);
                    }
                }
                final CharSequence[] cs = cardArrayList.toArray(new CharSequence[cardArrayList.size()]);
                builder.setItems(cs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        switch (i) {
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), AddCard.class);
                                startActivityForResult(intent, 150);
                                break;
                            default:
                                textViewCard.setText("Card Selected: " + cs[i].toString());
                                storePurchase();
                        }
                    }
                });
                builder.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addBankAccount(){
        buttonProceed.setEnabled(false);
        Query query = FirebaseDatabase.getInstance().getReference().child("AccountNumber").child(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SellCurrency.this);
                builder.setTitle(R.string.banktitle);
                ArrayList<String> bankAccArrayList = new ArrayList<>();
                bankAccArrayList.add("Add New Bank Account");

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String accountNumber = userSnapshot.getKey().toString();
                    if (!bankAccArrayList.contains(accountNumber)) {
                        bankAccArrayList.add(accountNumber);
                    }
                }

                final CharSequence[] cs = bankAccArrayList.toArray(new CharSequence[bankAccArrayList.size()]);
                builder.setItems(cs, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        switch (i) {
                            case 0:
                                bankListDialog();
                                break;
                            default:
                                textViewCard.setText("Bank Selected: " + cs[i].toString());
                                buttonProceed.setEnabled(true);
                        }
                    }
                });
                builder.show();
            }

            private void bankListDialog(){
                AlertDialog.Builder bankDialog = new AlertDialog.Builder(SellCurrency.this);
                bankDialog.setTitle(R.string.banktitle);
                bankDialog.setItems(bankName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        accNumEnterDialog accNumEnterDialog = new accNumEnterDialog(bankName[i]);
                       // accNumEnterDialog.setBankName(bankName[i]);
                        accNumEnterDialog.show(getSupportFragmentManager(),"Account Number Dialog");
                    }
                });
                bankDialog.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void storeToDatabase(String bank, String accountNum) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("AccountNumber").child(FirebaseAuth.getInstance().getUid());
        mDatabase.child(bank).child("AccountNumber").setValue(accountNum);
        Toast.makeText(this, "Account Number added successfully.", Toast.LENGTH_SHORT).show();
        addBankAccount();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != 151) {
            Toast.makeText(getApplicationContext(), "Payment Cancelled!", Toast.LENGTH_LONG).show();
        } else {
            textViewCard.setText("Card Selected: " + data.getStringExtra("Card"));
            storePurchase();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.logEvent("local_click_sell", null);
    }

    public void updateDate() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        myCalendar.add(Calendar.DATE, 1);
        editTextDate.setText(sdf.format(myCalendar.getTime()));
    }

    public void updatePrice(int position) {
        Double amount = Double.parseDouble(editTextSellAmount.getText().toString());
        float currRate = sharedPref.getFloat(currencyName[position], 0);
        total = amount / currRate;
        textViewTotal.setText("You will get: RM " + String.format(Locale.US, "%.2f", total));
    }

    public void storePurchase() {
        if (radioButtonOnline.isChecked()) {
            mFirebaseAnalytics.logEvent("deposit_bank", null);
        } else {
            mFirebaseAnalytics.logEvent("no_deposit", null);
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
