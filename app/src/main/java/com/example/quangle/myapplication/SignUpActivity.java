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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        //final Handler handler = new Handler();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final EditText username_editText = (EditText) findViewById(R.id.input_username_signUpPage);
        final EditText password_editText = (EditText) findViewById(R.id.input_password_signUpPage);
        final EditText email_editText = (EditText) findViewById(R.id.input_email_signUpPage);
        final EditText password_confirm_editText = (EditText) findViewById(R.id.input_confirmPassword_signUpPage);
        final Map<String, Object> user = new HashMap<>();
        final String[] fields = new String[4];
        Button signup = (Button) findViewById(R.id.signup_button_signUpPage);

        signup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                fields[0] = username_editText.getText().toString(); // username
                fields[1] = password_editText.getText().toString(); // password
                fields[3] = email_editText.getText().toString(); // password confirmation
                fields[2] = password_confirm_editText.getText().toString();// email

                if(!verified(fields)) return;
                else
                {
                    //TODO: 1. check if this email has been used before, if so, then redirect to login

                    // Create user, add to Authentication Users, and
                    // then send verification link and redirect to log in page
                    firebaseAuth.createUserWithEmailAndPassword(fields[3], fields[1])
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    user.put("username", fields[0]);
                                    //user.put("password", fields[1]);
                                    user.put("email", fields[3]);
                                    user.put("verified", false);
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
                                    firebaseAuth.getCurrentUser().sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        display("Registration success! Please check email and verify.");
                                                        //get the user name (haven't test)
                                                        /*UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                .setDisplayName(fields[0])
                                                                .build();
                                                        firebaseAuth.getCurrentUser().updateProfile(profileUpdates);
                                                        clearFields();*/
                                                    } else {
                                                        display(task.getException().getMessage());
                                                    }
                                                }
                                            });
                                }else{
                                    display(task.getException().getMessage());
                                    Log.d("error: ", task.getException().getMessage());
                                }
                            }
                        });
                    Intent intent = new Intent(v.getContext(), Login.class);
                    startActivity(intent);
                    finish();
                }
            }

            public void clearFields(){
                username_editText.setText("");
                password_confirm_editText.setText("");
                email_editText.setText("");
                password_editText.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, StartUp.class));
        finish();
    }

    public boolean verified(String[] fields){
        for(String field : fields) {
            if (field.equals("")) {
                display("Not all fields have been entered");
                return false;
            }
        }
        if(!emailIsValid(fields[3])){
            display("Email entered is not valid");
            return false;
        }
        else if(fields[1].length() < 6){
            display("Password must be at least 6 characters");
            return false;
        }
        else if(!fields[1].equals(fields[2]))
        {
            display("Passwords do not match.");
            return false;
        }
        return true;
    }

    public void display(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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
  
    public void login(View v)
    {
        startActivity(new Intent(this, Login.class));
    }
}
