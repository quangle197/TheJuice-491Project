package com.example.quangle.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class UserInventoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_inventory);

        Button addItem = (Button) findViewById(R.id.addItemButton);
        addItem.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(v.getContext(),Login.class);
                startActivity(intent);
                finish();
            }
        });

    }
}