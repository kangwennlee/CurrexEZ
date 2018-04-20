package com.example.kangwenn.currexez;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.braintreepayments.cardform.OnCardFormSubmitListener;
import com.braintreepayments.cardform.utils.CardType;
import com.braintreepayments.cardform.view.CardEditText;
import com.braintreepayments.cardform.view.CardForm;
import com.braintreepayments.cardform.view.SupportedCardTypesView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCard extends AppCompatActivity implements OnCardFormSubmitListener, CardEditText.OnCardTypeChangedListener {
    private static final CardType[] SUPPORTED_CARD_TYPES = {CardType.AMEX, CardType.MASTERCARD, CardType.VISA, CardType.DINERS_CLUB};
    protected CardForm cardForm;
    private SupportedCardTypesView mSupportedCardTypesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment);

        mSupportedCardTypesView = findViewById(R.id.supported_card_types);
        mSupportedCardTypesView.setSupportedCardTypes(SUPPORTED_CARD_TYPES);

        cardForm = findViewById(R.id.card_form);
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .mobileNumberRequired(true)
                .mobileNumberExplanation("SMS is required on this number")
                .actionLabel("Card Payment")
                .setup(this);
        cardForm.setOnCardFormSubmitListener(this);
        cardForm.setOnCardTypeChangedListener(this);

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onCardFormSubmit() {
        if(cardForm.isValid()){
            Toast.makeText(this, R.string.valid, Toast.LENGTH_SHORT).show();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Card").child(FirebaseAuth.getInstance().getUid());
            mDatabase.child(cardForm.getCardNumber()).child("ExpirationMonth").setValue(cardForm.getExpirationMonth());
            mDatabase.child(cardForm.getCardNumber()).child("ExpirationYear").setValue(cardForm.getExpirationYear());
            mDatabase.child(cardForm.getCardNumber()).child("CountryCode").setValue(cardForm.getCountryCode());
            mDatabase.child(cardForm.getCardNumber()).child("MobileNumber").setValue(cardForm.getMobileNumber());
            mDatabase.child(cardForm.getCardNumber()).child("Cvv").setValue(cardForm.getCvv());
            Intent i = new Intent();
            i.putExtra("Card", cardForm.getCardNumber());
            setResult(151, i);
            hideKeyboard(AddCard.this);
            finish();
        }else{
            cardForm.validate();
            Toast.makeText(this, R.string.invalid, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCardTypeChanged(CardType cardType) {
        if(cardType == CardType.EMPTY){
            mSupportedCardTypesView.setSupportedCardTypes(SUPPORTED_CARD_TYPES);
        }
        else{
            mSupportedCardTypesView.setSelected(cardType);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.card_io_item){
            cardForm.scanCard(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (cardForm.isCardScanningAvailable()) {
            getMenuInflater().inflate(R.menu.card_io, menu);
        }
        return true;

    }
}
