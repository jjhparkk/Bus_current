package com.example.seoulbus_jihyun;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editTextBusNumber = findViewById(R.id.editTextBusNumber);
        Button buttonShowMap = findViewById(R.id.buttonShowMap);

        buttonShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String busNumber = editTextBusNumber.getText().toString();

                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("BUS_NUMBER", busNumber);
                startActivity(intent);
            }
        });
    }
}
