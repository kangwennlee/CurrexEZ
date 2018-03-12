package com.example.kangwenn.currexez;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.kangwenn.currexez.Entity.Purchase;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PurchaseHistory extends AppCompatActivity {

    FirebaseUser mCurrentUser;
    String userID;
    String todaydate;
    private ListView purchaseHistoryListViewPast, purchaseHistoryListViewUpcoming;
    private DatabaseReference databaseReference, currentUser;
    private ArrayList<String> arrayListPast, arrayListUpcoming;
    private ArrayAdapter<String> adapterPast, adapterUpcoming;
    private FirebaseAnalytics mFirebaseAnalytics;

    //round the double value
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history);
        setTitle("Purchase History");
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("PurchaseHistory");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mCurrentUser.getUid();
        currentUser = databaseReference.child(userID);
        arrayListPast = new ArrayList<>();
        arrayListUpcoming = new ArrayList<>();
        purchaseHistoryListViewPast = findViewById(R.id.purchaseHistoryListViewPast);
        purchaseHistoryListViewUpcoming = findViewById(R.id.purchaseHistoryListViewUpcoming);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
        todaydate = sdf.format(new Date());
        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren() ){
                    Purchase purchase = ds.getValue(Purchase.class);
                    String date = ds.getKey().substring(0,4) + "/" + ds.getKey().substring(4,6) + "/" + ds.getKey().substring(6,8);
                    //String month = ds.getKey().toString().substring(4,5);
                    //String date = ds.getKey().toString().substring(6,7);
                    String time = ds.getKey().substring(9,11) + ":" + ds.getKey().substring(11,13) + ":" +ds.getKey().substring(12,14);
                    String values = "Currency : " + purchase.getCurrency()
                            + "\nPuchase Amount : " + round(purchase.getAmount(), 2)
                            + "\nPuchase Amount In MYR : " + round(purchase.getAmountInRM(), 2)
                            + "\nPayment Method : " + purchase.getPayMethod()
                            + "\nPurchase Date : " + date + " " + time
                            + "\nCollection Date : " + purchase.getCollectionDate()
                            + "\nCollection Location: " + purchase.getCollectionLocation();
                    if (todaydate.compareTo(purchase.getCollectionDate()) > 0) {
                        arrayListPast.add(values);
                    } else {
                        arrayListUpcoming.add(values);
                    }
                }
                adapterPast = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayListPast);
                adapterUpcoming = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayListUpcoming);
                purchaseHistoryListViewPast.setAdapter(adapterPast);
                purchaseHistoryListViewUpcoming.setAdapter(adapterUpcoming);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        purchaseHistoryListViewUpcoming.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), SuccessPaymentActivity.class);
                String string = arrayListUpcoming.get(i);
                intent.putExtra("purchase", string);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.logEvent("click_history",null);
    }


}
