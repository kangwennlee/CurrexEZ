package com.example.kangwenn.currexez;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.kangwenn.currexez.Entity.Purchase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PurchaseHistory extends AppCompatActivity {

    private ListView puchaseHistoryListView;

    private DatabaseReference databaseReference, currentUser;
    FirebaseUser mCurrentUser;
    String userID;
    private ArrayList<String> arrayList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("PurchaseHistory");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mCurrentUser.getUid();
        currentUser = databaseReference.child(userID);

        currentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren() ){
                    Purchase purchase = ds.getValue(Purchase.class);

                    String values = "Currency : " + purchase.getCurrency();
                    arrayList.add(values);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);

        puchaseHistoryListView = findViewById(R.id.puchaseHistoryListView);
        puchaseHistoryListView.setAdapter(adapter);
    }


}
