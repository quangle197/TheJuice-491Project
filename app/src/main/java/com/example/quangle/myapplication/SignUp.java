package com.example.quangle.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        Button signup = (Button) findViewById(R.id.signup_button_signUpPage);
        signup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                String[] fields = new String[4];
                EditText username_editText = (EditText) findViewById(R.id.input_username_signUpPage);
                EditText password_editText = (EditText) findViewById(R.id.input_password_signUpPage);
                EditText email_editText = (EditText) findViewById(R.id.input_email_signUpPage);
                EditText password_confirm_editText = (EditText) findViewById(R.id.input_confirmPassword_signUpPage);
                fields[0] = username_editText.getText().toString(); // username
                fields[1] = password_editText.getText().toString(); // password
                fields[3] = email_editText.getText().toString(); // password confirmation
                fields[2] = password_confirm_editText.getText().toString();// email
                Map<String, Object> user = new HashMap<>();
                boolean filled = true;
                for(String field : fields) {
                    if (field.equals("")) {
                        filled = false;
                        break;
                    }
                }
                if(!filled){
                    display("Not all fields have been entered");
                    return;
                }
                else if(!emailIsValid(fields[3])){
                    display("Email entered is not valid");
                    return;
                }
                else if(!fields[1].equals(fields[2]))
                {
                    display("Passwords do not match.");
                    return;
                }
                else {
                    user.put("username", fields[0]);
                    user.put("password", fields[1]);
                    user.put("email", fields[3]);
                    user.put("verified", false);
                    display("Congratulations, you made a new account");
                    db.collection("users")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("tag", "DocumentSnapshot added with ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("tag", "Error adding document", e);
                                }
                            });
                    // wait 2 seconds for user to read message
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            public void display(String message){
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
            public boolean emailIsValid(String email)
            {
                String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                        "[a-zA-Z0-9_+&*-]+)*@" +
                        "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                        "A-Z]{2,7}$";

                Pattern pat = Pattern.compile(emailRegex);
                if (email == null)
                    return false;
                return pat.matcher(email).matches();
            }
        });
    }
}
