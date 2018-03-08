package com.example.kangwenn.currexez;

import android.app.DatePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    //component view var
    private EditText etName,etPhoneNum,etAddress,etPassport,etBirthday;
    private Spinner spNation;
    private String spValue;
    private Button btnConfirm;
    private ProgressDialog progressDialog;
    Calendar myCalendar;

    // Firebase var
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseUser;
    private FirebaseUser currentFirebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //set the firebase var
        firebaseAuth = FirebaseAuth.getInstance();
        databaseUser = FirebaseDatabase.getInstance().getReference("User");
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //find the EditText view component
        etName = findViewById(R.id.etName);
        etPhoneNum = findViewById(R.id.etPhoneNum);
        etAddress = findViewById(R.id.etAddress);
        etPassport = findViewById(R.id.etPassport);
        etBirthday = findViewById(R.id.Birthday);

        //set the spinner text value
        spNation = findViewById(R.id.spinnerNation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,R.array.national,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNation.setAdapter(adapter);

        //find and set the DatePicker view component
        myCalendar = Calendar.getInstance();
        etBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }

                };
            }
        });

        //find the Button view component
        btnConfirm =  findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);

        progressDialog = new ProgressDialog(EditProfile.this);
    }


    public void uploadInformation(){
        String name = etName.getText().toString();
        String phoneNum = etPhoneNum.getText().toString();
        String address = etAddress.getText().toString();
        String passport = etPassport.getText().toString();
        spValue = spNation.getSelectedItem().toString();
        String birthday = etBirthday.getText().toString();

        //get the user UID
        String id = currentFirebaseUser.getUid();

        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        User user = new User(id, name, spValue, birthday, phoneNum, address, passport);
        databaseUser.child(id).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null){
                    progressDialog.dismiss();
                    Toast.makeText(EditProfile.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(EditProfile.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
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

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        etBirthday.setText(sdf.format(myCalendar.getTime()));
    }
}
