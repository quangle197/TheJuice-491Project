package com.example.quangle.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    final Map<String, Object> user = new HashMap<>();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        //final Handler handler = new Handler();
        //final FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        final EditText username_editText = (EditText) findViewById(R.id.input_username_signUpPage);
        final EditText password_editText = (EditText) findViewById(R.id.input_password_signUpPage);
        final EditText email_editText = (EditText) findViewById(R.id.input_email_signUpPage);
        final EditText password_confirm_editText = (EditText) findViewById(R.id.input_confirmPassword_signUpPage);
        //final Map<String, Object> user = new HashMap<>();
        final String[] fields = new String[4];
        Button signup = findViewById(R.id.signup_button_signUpPage);
        ImageButton googleButton = findViewById(R.id.googleButton_signUpPage);

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
                    if(firebaseAuth.getCurrentUser()!= null && firebaseAuth.getCurrentUser().isEmailVerified()){
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    }
                    // Create user, add to Authentication Users, and
                    // then send verification link and redirect to log in page
                    firebaseAuth.createUserWithEmailAndPassword(fields[3], fields[1])
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    user.put("username", fields[0]);
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
                                                        user.put("verified", true);
                                                        clearFields();
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

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && data!= null)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch(ApiException e){
                display("Google sign in failed: " + e);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount googleSignInAccount) {
        Log.e("tag", "fireBaseAuthWithGoogle : " + googleSignInAccount.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    display("Google Sign in success!");
                    db.collection("users")
                            .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                                    if (task2.isSuccessful()) {
                                        if (task2.getResult().getDocuments().size() == 0) {
                                            user.put("username", googleSignInAccount.getEmail());
                                            user.put("verified", true);
                                            user.put("email", googleSignInAccount.getEmail());
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
                                        }
                                    }
                                }
                            });
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    if (currentUser != null) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                } else {
                    display("No success");
                }
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
