package com.example.kangwenn.currexez;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.example.kangwenn.currexez.Entity.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static final String FILE_NAME = "temp.jpg";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    private static final int MAX_DIMENSION = 1200;
    private static final String TAG = EditProfile.class.getSimpleName();
    Calendar myCalendar;
    byte[] icData;
    ImageView imageView;
    //component view var
    private EditText etName,etPhoneNum,etAddress,etPassport,etBirthday;
    private Spinner spNation;
    private String spValue;
    List<String> textBlocks;
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
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Edit Profile")
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Fabric.with(this, new Answers(), new Crashlytics());
        getSupportActionBar().setTitle("Edit Your Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //set the firebase var
        firebaseAuth = FirebaseAuth.getInstance();
        databaseUser = FirebaseDatabase.getInstance().getReference("User");
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        textBlocks = new LinkedList<>();

        //find the EditText view component
        etName = findViewById(R.id.etName);
        etPhoneNum = findViewById(R.id.etPhoneNum);
        etAddress = findViewById(R.id.etAddress);
        etPassport = findViewById(R.id.etPassport);
        etBirthday = findViewById(R.id.Birthday);
        imageView = findViewById(R.id.imageViewIC);

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
                Calendar calendar = Calendar.getInstance();
                calendar.set(2000, 1, 1);
                mDatePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                mDatePicker.show();
            }
        });

        //find the Button view component
        btnConfirm =  findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);
        //btnConfirm.setEnabled(false);
        btnPhoto = findViewById(R.id.buttonICPhoto);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                builder
                        .setMessage("Select your IC picture")
                        .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startCamera();
                            }
                        });
                builder.create().show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //Intent i = new Intent(this, UserProfile.class);
            //startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"), GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(EditProfile.this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FirebaseVisionImage image = null;
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            setImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            setImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void setImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);
                //callCloudVision(bitmap);
                runTextRecognition(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                icData = baos.toByteArray();
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, "Image picker error", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, "Image picker error", Toast.LENGTH_LONG).show();
        }
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, Context context) {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = 0;
        try {
            sensorOrientation = cameraManager
                    .getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e(TAG, "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }

    private void runTextRecognition(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance()
                .getVisionTextDetector();
        Task<FirebaseVisionText> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...
                                processTextRecognitionResult(firebaseVisionText);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.Block> blocks = texts.getBlocks();
        if ( blocks.size()==0 ){
            Toast.makeText(getApplicationContext(),"No Text", Toast.LENGTH_SHORT).show();
        }

        for (FirebaseVisionText.Block block: blocks) {
            String text = block.getText();
            textBlocks.add(block.getText());
            if (text.matches("\\d{6,6}.\\d{2,2}.\\d{4,4}")) {
                text = text.replace(" ", "-");
                etPassport.setText(text);
                Toast.makeText(getApplicationContext(), "IC detected: IC Number: " + text, Toast.LENGTH_SHORT).show();
                spNation.setSelection(1);
                String dob = text.substring(0, 6);
                myCalendar.set(Integer.parseInt("19" + dob.substring(0, 2)), Integer.parseInt(dob.substring(2, 4)) - 1, Integer.parseInt(dob.substring(4, 6)));
                updateLabel();
            } else if (text.matches("([A-Z][A-Z]*)[\\s-]([A-Z][A-Z]*)[\\s-]([A-Z][A-Z]*)")) {
                etName.setText(text);
                Toast.makeText(getApplicationContext(), "Name detected: Name: " + text, Toast.LENGTH_SHORT).show();
            } else if (text.length() > 15) {
                etAddress.setText(text);
            }
        }
        FirebaseDatabase.getInstance().getReference("Text").child(currentFirebaseUser.getUid()).setValue(textBlocks);
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
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

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("nationality", spValue);
        editor.apply();

        mFirebaseAnalytics.setUserProperty("nationality", spValue);

        User user = new User(id, name, spValue, birthday, phoneNum, address, passport);
        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("IC").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        filepath.putBytes(icData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(EditProfile.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                //Intent i = new Intent(getApplicationContext(), UserProfile.class);
                //startActivity(i);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int errorCode = ((StorageException) e).getErrorCode();
                String errorMessage = e.getMessage();
                // test the errorCode and errorMessage, and handle accordingly
                Toast.makeText(getApplicationContext(), "Update profile failed.", Toast.LENGTH_LONG).show();
            }
        });
        databaseUser.child(id).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null){
                    //Toast.makeText(EditProfile.this, "Profile updated successfully!", Toast.LENGTH_LONG).show();
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
                progressDialog.setMessage("Uploading...");
                progressDialog.show();
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }
}
