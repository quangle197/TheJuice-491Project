package com.example.quangle.myapplication;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    final Map<String, Object> user = new HashMap<>();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);
        final Map<String, Object> user = new HashMap<>();
        final EditText text_password = (EditText) findViewById(R.id.login_passwordEditText);
        final EditText text_email = (EditText) findViewById(R.id.login_emailEditText);
        firebaseAuth = FirebaseAuth.getInstance();
        /*final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();*/
        //final GoogleSignInClient mGoogleSignInClient;// = GoogleSignIn.getClient(this, gso);
        final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        final String[] fields = new String[2];

        // check if email and password matches users
        // check if user has pressed email verification link
        // log them in
        Button login = (Button) findViewById(R.id.loginButton);
        ImageButton googleButton = findViewById(R.id.googleButton_signUpPage);



        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fields[1] = text_password.getText().toString();
                fields[0] = text_email.getText().toString();
                if (!verified(fields)) return;
                else {
                    firebaseAuth.signInWithEmailAndPassword(fields[0], fields[1])
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        if (!firebaseAuth.getCurrentUser().isEmailVerified()) {
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

//        googleButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
////                startActivityForResult(signInIntent, 1);
////            }
////        });
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
