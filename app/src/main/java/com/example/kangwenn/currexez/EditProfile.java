package com.example.kangwenn.currexez;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kangwenn.currexez.Entity.User;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    Calendar myCalendar;
    byte[] icData;
    //component view var
    private EditText etName,etPhoneNum,etAddress,etPassport,etBirthday;
    private Spinner spNation;
    private String spValue;
    private Button btnConfirm, btnPhoto;
    private ProgressDialog progressDialog;
    // Firebase var
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseUser;
    private FirebaseUser currentFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.logEvent("click_edit_profile",null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            icData = baos.toByteArray();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
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
        final String[] nation = getResources().getStringArray(R.array.national);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.spinner_item, nation) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter1.setDropDownViewResource(R.layout.spinner_item);
        spNation.setAdapter(adapter1);

        //find and set the DatePicker view component
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
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(EditProfile.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                mDatePicker.show();
            }
        });

        //find the Button view component
        btnConfirm =  findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);

        btnPhoto = findViewById(R.id.buttonICPhoto);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        progressDialog = new ProgressDialog(EditProfile.this);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    etAddress.setText(dataSnapshot.child("address").getValue().toString());
                    String birthday = dataSnapshot.child("birthday").getValue().toString();
                    etBirthday.setText(birthday);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy", Locale.US);
                    try {
                        Date date = formatter.parse(birthday);
                        myCalendar.setTime(date);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    etPassport.setText(dataSnapshot.child("icAndPassport").getValue().toString());
                    etName.setText(dataSnapshot.child("name").getValue().toString());
                    int position = 0;
                    for (int i = 0; i < nation.length; i++) {
                        if (nation[i].equals(dataSnapshot.child("nation").getValue().toString())) {
                            position = i;
                        }
                    }
                    spNation.setSelection(position);
                    etPhoneNum.setText(dataSnapshot.child("phoneNumber").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

        mFirebaseAnalytics.setUserProperty("nationality", spValue);

        User user = new User(id, name, spValue, birthday, phoneNum, address, passport);
        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("IC").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        filepath.putBytes(icData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(EditProfile.this, "IC uploaded successfully!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int errorCode = ((StorageException) e).getErrorCode();
                String errorMessage = e.getMessage();
                // test the errorCode and errorMessage, and handle accordingly
                Toast.makeText(getApplicationContext(), "Upload unsuccessful.", Toast.LENGTH_LONG).show();
            }
        });
        databaseUser.child(id).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null){
                    progressDialog.dismiss();
                    Toast.makeText(EditProfile.this, "Profile updated successfully!", Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    Toast.makeText(EditProfile.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onClick(View view) {
        //Validation here
        String regxforIC = "^\\d{6}-\\d{2}-\\d{4}$";
        String regxforPhoneNum = "^6?01\\d{8}$";


        if (!etName.getText().toString().isEmpty() &&
                !etPhoneNum.getText().toString().isEmpty() &&
                !etPassport.getText().toString().isEmpty() &&
                !etName.getText().toString().isEmpty() &&
                !etAddress.getText().toString().isEmpty() &&
                !etBirthday.getText().toString().isEmpty() &&
                spNation.getSelectedItemPosition() != 0 && icData != null) {
            if(!etPassport.getText().toString().matches(regxforIC)){
                Toast.makeText(EditProfile.this, "Please enter correct format of Malaysia IC", Toast.LENGTH_SHORT).show();
            }else if(!etPhoneNum.getText().toString().matches(regxforPhoneNum)){
                Toast.makeText(EditProfile.this, "Please enter correct format of Malaysia Phone Number", Toast.LENGTH_SHORT).show();
            }else{
                uploadInformation();
            }
        } else {
            Toast.makeText(EditProfile.this, "Please complete the form", Toast.LENGTH_SHORT).show();
        }

    }

    //add "0" infront of month and day int
    public String checkDigit(int number) {
        return number<=9 ? "0"+number:
                String.valueOf(number);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etBirthday.setText(sdf.format(myCalendar.getTime()));
    }
}
