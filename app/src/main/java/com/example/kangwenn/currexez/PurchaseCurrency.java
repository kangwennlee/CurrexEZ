package com.example.kangwenn.currexez;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import com.crashlytics.android.Crashlytics;
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
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import javax.crypto.KeyGenerator;

import io.fabric.sdk.android.Fabric;


public class PurchaseCurrency extends AppCompatActivity {
    static final String DEFAULT_KEY_NAME = "default_key";
    private static final String KEY = "KEY";
    private static final String TAG = PurchaseCurrency.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    private static final String SECRET_MESSAGE = "Very secret message";
    private static final String KEY_NAME_NOT_INVALIDATED = "key_not_invalidated";
    SharedPreferences sharedPref;
    String[] currencyName = {"USD", "EUR", "AUD", "GBP", "SGD", "CNY", "THB", "JPY", "KRW", "HKD", "TWD"};
    String[] currName = new String[currencyName.length];
    Spinner spinnerSelectCurr;
    EditText editTextPurAmount,editTextDate;
    TextView textViewTotal, textViewName, textViewCard;
    Button buttonProceed;
    RadioButton radioButtonCredit, radioButtonOnline;
    RadioGroup radioGroup;
    Double total;
    Calendar myCalendar;
    private FirebaseAnalytics mFirebaseAnalytics;

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private SharedPreferences mSharedPreferences;

    private DatabaseReference databaseUser;
    private FirebaseUser currentFirebaseUser;

    private Spinner spinnerCollectLocation;

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Fabric.with(this, new Answers(), new Crashlytics());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_currency);
        //set return button
        getSupportActionBar().setTitle("Purchase Currency");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //find view
        spinnerSelectCurr = findViewById(R.id.spinnerSelectCurr);
        editTextPurAmount = findViewById(R.id.editTextPurAmount);
        textViewTotal = findViewById(R.id.textViewPrice);
        textViewName = findViewById(R.id.textViewCurrency);
        textViewCard = findViewById(R.id.textViewCreditSelected);
        buttonProceed = findViewById(R.id.buttonProceed);
        radioButtonCredit = findViewById(R.id.radioButtonCredit);
        radioButtonOnline = findViewById(R.id.radioButtonOnline);
        radioGroup = findViewById(R.id.radioGroupMethod);
        editTextDate = findViewById(R.id.editTextCollectionDate);
        //editTextLocation = findViewById(R.id.editTextCollectionLocation);
        spinnerCollectLocation = findViewById(R.id.spinnerCollectLocation);
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
        spinnerSelectCurr.setSelection(intent.getIntExtra("purchaseType",0));
        editTextPurAmount.setText(String.valueOf(intent.getDoubleExtra("purchaseAmount", 100)));
        editTextPurAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editTextPurAmount.getText().toString().isEmpty() || Double.valueOf(editTextPurAmount.getText().toString()).equals(0)) {
                    textViewTotal.setText("");
                    buttonProceed.setEnabled(false);
                } else {
                    try {
                        Double amount = Double.parseDouble(editTextPurAmount.getText().toString());
                        float currRate = sharedPref.getFloat(currencyName[spinnerSelectCurr.getSelectedItemPosition()], 0);
                        total = amount / currRate;
                        textViewTotal.setText("This will cost you: RM " + String.format(Locale.US, "%.2f", total));
                    } catch (NumberFormatException e) {

                    }
                    if (radioButtonCredit.isChecked() || radioButtonOnline.isChecked() && !editTextPurAmount.getText().toString().equals("") && Double.valueOf(editTextPurAmount.getText().toString()) > 0) {
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
                    Double amount = Double.parseDouble(editTextPurAmount.getText().toString());
                    float currRate = sharedPref.getFloat(currencyName[position], 0);
                    total = amount / currRate;
                    textViewTotal.setText("This will cost you: RM " + String.format(Locale.US, "%.2f", total));
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
                buttonProceed.setEnabled(true);
                if (radioButtonCredit.isChecked()) {
                    //Toast.makeText(getApplicationContext(),"Credit card",Toast.LENGTH_SHORT).show();
                    pickCard();

                } else if (radioButtonOnline.isChecked()) {
                    //Toast.makeText(getApplicationContext(),"Online",Toast.LENGTH_SHORT).show();
                    textViewCard.setText("");
                }
            }
        });
        myCalendar = Calendar.getInstance();
        updateLabel();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth -1 ); // minus 1 because the updateLabel function will add one
                updateLabel();
            }

        };
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(PurchaseCurrency.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                Calendar calendar = Calendar.getInstance();
                //calendar.add(Calendar.DAY_OF_YEAR,1);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
                //mDatePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                mDatePicker.getDatePicker().setMinDate(calendar.getTimeInMillis());
                mDatePicker.show();
            }
        });
        buttonProceed.setEnabled(false);
        buttonProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextDate.getText().toString().isEmpty() && spinnerCollectLocation.getSelectedItemPosition() != 0) {
                    fingerAuth();
                }
            }
        });

        final String[] location = getResources().getStringArray(R.array.location);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, location);
        adapter1.setDropDownViewResource(R.layout.spinner_item);

        spinnerCollectLocation.setAdapter(adapter1);
        Intent arrayrIntent = getIntent();
        spinnerSelectCurr.setSelection(arrayrIntent.getIntExtra("location",0));

    }

    private void pickCard() {
        Query query = FirebaseDatabase.getInstance().getReference().child("Card").child(FirebaseAuth.getInstance().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PurchaseCurrency.this);
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

    private void fingerAuth() {
        createFingerprintManagerInstance().authenticate(new KFingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationSuccess() {
                //messageText.setText("Successfully authenticated");
                storePurchase();
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
        mFirebaseAnalytics.logEvent("local_click_purchase", null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != 151) {
            Toast.makeText(getApplicationContext(), "Payment Cancelled!", Toast.LENGTH_LONG).show();
        } else {
            textViewCard.setText("Card Selected: " + data.getStringExtra("Card"));
        }
    }

    public void storePurchase(){
        if (radioButtonCredit.isChecked()) {
            mFirebaseAnalytics.logEvent("card_payment", null);
        } else {
            mFirebaseAnalytics.logEvent("online_payment", null);
        }
        String currency = spinnerSelectCurr.getSelectedItem().toString();
        Double purchaseAmount = Double.parseDouble(editTextPurAmount.getText().toString());
        Double purchaseAmountInRM = total;

        int selectID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectID);
        String selectRadioButtonValue = radioButton.getText().toString();

        String collectionDate = editTextDate.getText().toString();
        String collectionLoc = spinnerCollectLocation.getSelectedItem().toString();

        databaseUser = FirebaseDatabase.getInstance().getReference("PurchaseHistory");
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //get the user UID
        String id = currentFirebaseUser.getUid();

        final Purchase purchase = new Purchase(currency, purchaseAmount, purchaseAmountInRM, selectRadioButtonValue, collectionDate, collectionLoc);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US);
        String date = sdf.format(new Date());
        String time = date.substring(9,11) + ":" + date.substring(11,13) + ":" + date.substring(12,14);
        final String values = "Currency purchased: " + purchase.getCurrency()
                + "\nPurchase Amount : " + round(purchase.getAmount(), 2)
                + "\nPurchase Amount In MYR : " + round(purchase.getAmountInRM(), 2)
                + "\nPayment Method : " + purchase.getPayMethod()
                + "\nTransaction Date : " + date + " " + time
                + "\nCollection Date : " + purchase.getCollectionDate()
                + "\nCollection Location: " + purchase.getCollectionLocation();
        Bundle bundle = new Bundle();
        bundle.putString("Currency_Purchased",purchase.getCurrency());
        bundle.putDouble("Purchase_Amount", purchase.getAmount());
        bundle.putDouble("Purchase_Amount_In_MYR", purchase.getAmountInRM());
        bundle.putString("Purchase_Date", date);
        bundle.putString("Purchase_Time", time);
        bundle.putString("Collection_Date", collectionDate);
        bundle.putString("Collection_Location", collectionLoc);
        mFirebaseAnalytics.logEvent("purchase_done",bundle);
        Answers.getInstance().logPurchase(new PurchaseEvent()
                .putItemPrice(BigDecimal.valueOf(purchaseAmountInRM))
                .putItemName(currency)
                .putCurrency(Currency.getInstance("MYR"))
                .putSuccess(true)
        );
        databaseUser.child(id).child("Buy").child(date).setValue(purchase, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Intent intent = new Intent(getApplicationContext(), SuccessPaymentActivity.class);
                    intent.putExtra("purchase", values);
                    Toast.makeText(getApplicationContext(), "Payment Successful!", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Database failed. Please contact our customer service", Toast.LENGTH_LONG).show();

                }
            }
        });
        finish();
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        myCalendar.add(Calendar.DATE, 1); // add one day because today date can't make deal
        editTextDate.setText(sdf.format(myCalendar.getTime()));

        //Log.d(TAG, editTextDate.getText().toString());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

}
