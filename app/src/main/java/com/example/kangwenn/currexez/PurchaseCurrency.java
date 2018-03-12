package com.example.kangwenn.currexez;

import android.app.DatePickerDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.example.kangwenn.currexez.Entity.Purchase;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static com.example.kangwenn.currexez.PurchaseHistory.round;

public class PurchaseCurrency extends AppCompatActivity {
    static final String DEFAULT_KEY_NAME = "default_key";
    private static final String TAG = PurchaseCurrency.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    private static final String SECRET_MESSAGE = "Very secret message";
    private static final String KEY_NAME_NOT_INVALIDATED = "key_not_invalidated";
    SharedPreferences sharedPref;
    String[] currencyName = {"USD", "AUD", "CNY", "THB", "JPY", "GBP", "KRW", "HKD", "SGD"};
    String[] currName = new String[currencyName.length];
    Spinner spinnerSelectCurr;
    EditText editTextPurAmount,editTextDate,editTextLocation;
    TextView textViewTotal, textViewName;
    Button buttonProceed;
    RadioButton radioButtonCredit, radioButtonOnline;
    RadioGroup radioGroup;
    Double total;
    Calendar myCalendar;
    private FirebaseAnalytics mFirebaseAnalytics;

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseUser;
    private FirebaseUser currentFirebaseUser;

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
        radioGroup = findViewById(R.id.radioGroupMethod);
        editTextDate = findViewById(R.id.editTextCollectionDate);
        editTextLocation = findViewById(R.id.editTextCollectionLocation);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseUser = FirebaseDatabase.getInstance().getReference("PurchaseHistory");
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }
        try {
            mKeyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }
        Cipher defaultCipher;
        Cipher cipherNotInvalidated;
        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);

        if (!keyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a fingerprint or lock screen.
            Toast.makeText(this,
                    "Secure lock screen hasn't set up.\n"
                            + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                    Toast.LENGTH_LONG).show();
            buttonProceed.setEnabled(false);
            return;
        }

        // Now the protection level of USE_FINGERPRINT permission is normal instead of dangerous.
        // See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            buttonProceed.setEnabled(false);
            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Go to 'Settings -> Security -> Fingerprint' and register at least one" +
                            " fingerprint",
                    Toast.LENGTH_LONG).show();
            return;
        }
        createKey(DEFAULT_KEY_NAME, true);
        createKey(KEY_NAME_NOT_INVALIDATED, false);
        buttonProceed.setEnabled(false);
        buttonProceed.setOnClickListener(new PurchaseButtonClickListener(defaultCipher, DEFAULT_KEY_NAME));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.logEvent("click_purchase",null);
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
        editTextPurAmount.setText(String.valueOf(intent.getDoubleExtra("purchaseAmount",1.0)));
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
                if (!editTextPurAmount.getText().toString().equals("")) {
                    if (Double.valueOf(editTextPurAmount.getText().toString()) > 0) {
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
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(PurchaseCurrency.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                mDatePicker.show();
            }
        });
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the
     * {@link #createKey(String, boolean)} method.
     *
     * @param keyName the key name to init the cipher
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    private boolean initCipher(Cipher cipher, String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    /**
     * Proceed the purchase operation
     *
     * @param withFingerprint {@code true} if the purchase was made by using a fingerprint
     * @param cryptoObject    the Crypto object
     */
    public void onPurchased(boolean withFingerprint,
                            @Nullable FingerprintManager.CryptoObject cryptoObject) {
        if (withFingerprint) {
            // If the user has authenticated with fingerprint, verify that using cryptography and
            // then show the confirmation message.
            assert cryptoObject != null;
            tryEncrypt(cryptoObject.getCipher());
        } else {
            // Authentication happened with backup password. Just show the confirmation message.
            showConfirmation(null);
        }
    }

    // Show confirmation, if fingerprint was used show crypto information.
    private void showConfirmation(byte[] encrypted) {
        //findViewById(R.id.confirmation_message).setVisibility(View.VISIBLE);
        if (encrypted != null) {
            //TextView v = findViewById(R.id.encrypted_message);
            //v.setVisibility(View.VISIBLE);
            //v.setText(Base64.encodeToString(encrypted, 0 /* flags */));
            if(radioButtonCredit.isChecked()){
                mFirebaseAnalytics.logEvent("card_payment",null);
                Intent i = new Intent(this,CardPayment.class);
                startActivityForResult(i,150);
            }else{
                mFirebaseAnalytics.logEvent("online_payment",null);
                storePurchase();
            }
            //FirebaseDatabase hereee
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==151){
            storePurchase();
        }else{
            Toast.makeText(getApplicationContext(), "Payment Cancelled!", Toast.LENGTH_LONG).show();
        }
    }

    public void storePurchase(){
        String currency = spinnerSelectCurr.getSelectedItem().toString();
        Double purchaseAmount = Double.parseDouble(editTextPurAmount.getText().toString());
        Double purchaseAmountInRM = total;

        int selectID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectID);
        String selectRadioButtonValue = radioButton.getText().toString();

        String collectionDate = editTextDate.getText().toString();
        String collectionLoc = editTextLocation.getText().toString();

        //get the user UID
        String id = currentFirebaseUser.getUid();

        final Purchase purchase = new Purchase(currency,purchaseAmount,purchaseAmountInRM,selectRadioButtonValue);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US);
        String date = sdf.format(new Date());
        String time = date.substring(9,11) + ":" + date.substring(11,13) + ":" + date.substring(12,14);
        final String values = "Currency : " + purchase.getCurrency()
                + "\nPuchase Amount : " + round(purchase.getAmount(),2)
                + "\nPuchase Amount In MYR : " + round(purchase.getAmountInRM(),2)
                + "\nPayment Method : " + purchase.getPayMethod()
                + "\nDate : " + date + "\nTime : " + time;
        databaseUser.child(id).child(date).setValue(purchase, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null){
                    Intent intent = new Intent(getApplicationContext(),SuccessPaymentActivity.class);
                    intent.putExtra("purchase",values);
                    Toast.makeText(getApplicationContext(), "Payment Successful!", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Database failed. Please contact our customer service", Toast.LENGTH_LONG).show();

                }
            }
        });
        Bundle bundle = new Bundle();
        bundle.putString("Currency_Purchased",purchase.getCurrency());
        bundle.putDouble("Purchase_Amount", purchase.getAmount());
        bundle.putDouble("Purchase_Amount_In_MYR", purchase.getAmountInRM());
        bundle.putString("Date",date);
        bundle.putString("Time",time);
        mFirebaseAnalytics.logEvent("purchase_done",bundle);
        finish();
    }

    /**
     * Tries to encrypt some data with the generated key in {@link #createKey} which is
     * only works if the user has just authenticated via fingerprint.
     */
    private void tryEncrypt(Cipher cipher) {
        try {
            byte[] encrypted = cipher.doFinal(SECRET_MESSAGE.getBytes());
            showConfirmation(encrypted);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Toast.makeText(this, "Failed to encrypt the data with the generated key. "
                    + "Retry the purchase", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName                          the name of the key to be created
     * @param invalidatedByBiometricEnrollment if {@code false} is passed, the created key will not
     *                                         be invalidated even if a new fingerprint is enrolled.
     *                                         The default value is {@code true}, so passing
     *                                         {@code true} doesn't change the behavior
     *                                         (the key will be invalidated if a new fingerprint is
     *                                         enrolled.). Note that this parameter is only valid if
     *                                         the app works on Android N developer preview.
     */
    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editTextDate.setText(sdf.format(myCalendar.getTime()));
    }

    private class PurchaseButtonClickListener implements View.OnClickListener {

        Cipher mCipher;
        String mKeyName;

        PurchaseButtonClickListener(Cipher cipher, String keyName) {
            mCipher = cipher;
            mKeyName = keyName;
        }

        @Override
        public void onClick(View view) {

            // Set up the crypto object for later. The object will be authenticated by use
            // of the fingerprint.
            if (initCipher(mCipher, mKeyName)) {

                // Show the fingerprint dialog. The user has the option to use the fingerprint with
                // crypto, or you can fall back to using a server-side verified password.
                FingerprintAuthenticationDialogFragment fragment
                        = new FingerprintAuthenticationDialogFragment();
                fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                boolean useFingerprintPreference = mSharedPreferences
                        .getBoolean(getString(R.string.use_fingerprint_to_authenticate_key),
                                true);
                if (useFingerprintPreference) {
                    fragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
                } else {
                    fragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.PASSWORD);
                }
                fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            } else {
                // This happens if the lock screen has been disabled or or a fingerprint got
                // enrolled. Thus show the dialog to authenticate with their password first
                // and ask the user if they want to authenticate with fingerprints in the
                // future
                FingerprintAuthenticationDialogFragment fragment
                        = new FingerprintAuthenticationDialogFragment();
                fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                fragment.setStage(
                        FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED);
                fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        }
    }

}
