package com.example.hello;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

    TextView text3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        text3 = (TextView)findViewById(R.id.textView3);
        Intent intent = getIntent();
        int temp = intent.getExtras().getInt("temp");
        text3.setText(String.valueOf(temp));
    }
}