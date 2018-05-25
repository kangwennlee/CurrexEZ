package com.example.kangwenn.currexez;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class QRPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrpage);
        TextView textView = findViewById(R.id.textViewScanned);
        Intent intent = getIntent();
        String id = intent.getStringExtra("product");
        textView.setText(id);
    }
}
