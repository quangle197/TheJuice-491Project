package com.example.quangle.myapplication;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);
        final EditText text_password = (EditText) findViewById(R.id.login_passwordEditText);
        final EditText text_email = (EditText) findViewById(R.id.login_emailEditText);
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final Intent intent = new Intent(getApplicationContext(), MainActivity.class);;
        final String[] fields = new String[2];

        // check if email and password matches users
        // check if user has pressed email verification link
        // log them in
        Button login = (Button) findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fields[1] = text_password.getText().toString();
                fields[0] = text_email.getText().toString();
                if (!verified(fields)) return;
                else {
                    mAuth.signInWithEmailAndPassword(fields[0], fields[1])
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        if (!mAuth.getCurrentUser().isEmailVerified()) {
                                            display("Please verify email.");
                                            return;
                                        } else {
                                            display("Sign in successful");
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        display("Sign in failed");
                                    }
                                }
                            });

                }
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

    public void display(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public boolean verified(String[] fields){
        for(String field : fields) {
            if (field.equals("")) {
                display("Not all fields have been entered");
                return false;
            }
        }
        if(!emailIsValid(fields[0])){
            display("Email entered is not valid format");
            return false;
        }
        else
            return true;
    }

    public boolean emailIsValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}
