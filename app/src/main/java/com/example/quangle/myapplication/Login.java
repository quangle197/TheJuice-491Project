package com.example.quangle.myapplication;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, StartUp.class));
        finish();
    }

    public void forgot(View v)
    {
        startActivity(new Intent(this, ForgotPassword.class));
    }

    public void create(View v)
    {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    public void listItem(View v)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Hat");
        user.put("price", 420);
        user.put("distance", 5);
        user.put("sold", "John");
        user.put("condition", "new");
        user.put("quantity", "1");
        user.put("description", "This black fedora will impress your queen. M'lady");

        db.collection("item")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Added item");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding item",e);
                    }
                });
    }
}
