package com.example.quangle.myapplication;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        Button login = (Button) findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }

        });
    }
}
