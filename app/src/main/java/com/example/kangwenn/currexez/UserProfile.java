package com.example.kangwenn.currexez;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import io.fabric.sdk.android.Fabric;

public class UserProfile extends AppCompatActivity {
    Button editProfile;
    TextView userName,userBirthday,userIC,userNation,userPhone,userAddress;
    DatabaseReference databaseReference, currentUser;
    FirebaseUser mCurrentUser;
    String userID;
    ImageView imageView;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Fabric.with(this, new Answers(), new Crashlytics());
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        editProfile = findViewById(R.id.buttonEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EditProfile.class);
                startActivity(i);
                //finish();
            }
        });
        userName = findViewById(R.id.textViewUserName);
        userBirthday = findViewById(R.id.textViewUserBirthdate);
        userAddress = findViewById(R.id.textViewUserAddress);
        userIC = findViewById(R.id.textViewUserIC);
        userNation = findViewById(R.id.textViewUserNationality);
        userPhone = findViewById(R.id.textViewUserPhone);
        imageView = findViewById(R.id.imageViewIC);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = mCurrentUser.getUid();
        currentUser = databaseReference.child(userID);
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
        mFirebaseAnalytics.logEvent("click_user_profile", null);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("User Profile")
        );
        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userAddress.setText(dataSnapshot.child("address").getValue().toString());
                userBirthday.setText(dataSnapshot.child("birthday").getValue().toString());
                userIC.setText(dataSnapshot.child("icAndPassport").getValue().toString());
                userName.setText(dataSnapshot.child("name").getValue().toString());
                userNation.setText(dataSnapshot.child("nation").getValue().toString());
                userPhone.setText(dataSnapshot.child("phoneNumber").getValue().toString());
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseStorage.getInstance().getReference().child("IC").child(uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
                        task.execute(uri.toString());
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }
}
