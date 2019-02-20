package com.example.quangle.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartUp extends AppCompatActivity {
   @Override
    protected void onCreate(Bundle savedInstanceState)
   {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.start_up);


       Button login = (Button) findViewById(R.id.login);
       Button signup = (Button) findViewById(R.id.signUpButton_startUpPage);
       login.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v)
           {
               Intent intent = new Intent(v.getContext(), Login.class);
               startActivity(intent);
               finish();
           }

       });
       signup.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v)
           {
               Intent intent = new Intent(v.getContext(), SignUpActivity.class);
               startActivity(intent);
               finish();
           }
       });
   }
}
