package com.example.quangle.myapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.ContentValues.TAG;
public class ForgotPassword extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        final EditText email = (EditText) findViewById(R.id.email);
        final ImageView enter = (ImageView) findViewById(R.id.enter);
        final FirebaseAuth auth = FirebaseAuth.getInstance();


        enter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                String emailReset = email.getText().toString();
                auth.sendPasswordResetEmail(emailReset)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                    display("A reset email has sent to your account");
                                }
                                else
                                {
                                    display("Incorrect email. Try again");
                                }
                            }
                        });
            }
        });


    }

    public void display(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
