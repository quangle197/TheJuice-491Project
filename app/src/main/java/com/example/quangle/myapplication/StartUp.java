package com.example.quangle.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class StartUp extends AppCompatActivity {
   private FirebaseAuth.AuthStateListener mAuthListener;
   private GoogleSignInAccount mAccount;
   private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState)
   {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.start_up);

       // If user is currently loged in, send to main activity
       mAuthListener = new FirebaseAuth.AuthStateListener() {
           @Override
           public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               if(firebaseAuth.getCurrentUser() != null){
                   intent = new Intent(StartUp.this, MainActivity.class);
               }
           }
       };

       // If user had logged in with google previously
       mAccount = GoogleSignIn.getLastSignedInAccount(this);
       if (mAccount != null) {
           intent = new Intent(StartUp.this, MainActivity.class);
       }

       Button login = (Button) findViewById(R.id.login);
       Button signup = (Button) findViewById(R.id.signUpButton_startUpPage);
       login.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v)
           {
               intent = new Intent(v.getContext(), Login.class);
               startActivity(intent);
               finish();
           }

       });
       signup.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v)
           {
               intent = new Intent(v.getContext(), SignUpActivity.class);
               startActivity(intent);
               finish();
           }
       });
   }
}
