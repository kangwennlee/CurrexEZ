package com.example.kangwenn.currexez;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.kangwenn.currexez.Entity.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProfile extends AppCompatActivity implements View.OnClickListener {

    //component view var
    private EditText etName,etPhoneNum,etAddress,etPassport;
    private Spinner spNation;
    private String spValue;
    private DatePicker dpUserBirthday;
    private int day,month,year;
    private Button btnConfirm;
    private ProgressDialog progressDialog;

    // Firebase var
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseUser;
    private FirebaseUser currentFirebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //set the firebase var
        firebaseAuth = FirebaseAuth.getInstance();
        databaseUser = FirebaseDatabase.getInstance().getReference("User");
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //find the EditText view component
        etName = findViewById(R.id.etName);
        etPhoneNum = findViewById(R.id.etPhoneNum);
        etAddress = findViewById(R.id.etAddress);
        etPassport = findViewById(R.id.etPassport);

        //set the spinner text value
        spNation = findViewById(R.id.spinnerNation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,R.array.national,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNation.setAdapter(adapter);

        //find and set the DatePicker view component
        dpUserBirthday = findViewById(R.id.dpUserBirthday);
        day = dpUserBirthday.getDayOfMonth();
        month = dpUserBirthday.getMonth();
        year = dpUserBirthday.getYear();

        //find the Button view component
        btnConfirm =  findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);

        progressDialog = new ProgressDialog(UserProfile.this);
    }


    public void uploadInformation(){
        String name = etName.getText().toString();
        String phoneNum = etPhoneNum.getText().toString();
        String address = etAddress.getText().toString();
        String passport = etPassport.getText().toString();
        spValue = spNation.getSelectedItem().toString();
        String brithday = checkDigit(day)+"/"+checkDigit(month)+"/"+year;

        //get the user UID
        String id = currentFirebaseUser.getUid();

        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        User user = new User(id, name, spValue, brithday, phoneNum, address, passport);
        databaseUser.child(id).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null){
                    progressDialog.dismiss();
                }else{
                    Toast.makeText(UserProfile.this,databaseError.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onClick(View view) {
        uploadInformation();
    }

    //add "0" infront of month and day int
    public String checkDigit(int number) {
        return number<=9 ? "0"+number:
                String.valueOf(number);
    }
}
