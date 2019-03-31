package com.example.quangle.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DefaultActionbar extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawer;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //@Override
    //public void onCreateDrawer(Bundle savedInstanceState)
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_default);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();


        this.drawer = (DrawerLayout) findViewById(R.id.drawer_layout_default);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_default);
        navigationView.setNavigationItemSelectedListener(this);
        getUserProfile();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_message) {
            startActivity(new Intent(this, MessageActivity.class));
        } else if (id == R.id.nav_order) {
            startActivity(new Intent(this, Payment.class));
        } else if (id == R.id.nav_selling) {
            startActivity(new Intent(this, ListItemActivity.class));
        } else if (id == R.id.nav_contact) {
            startActivity(new Intent(this, ProfilePageActivity.class));
            //openImg();
        } else if (id == R.id.nav_signout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class));

        }

        this.drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    public void getUserProfile()
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String uid = user.getUid();
        StorageReference storageReference = storageRef.child("userImages").child(uid);
        NavigationView navMain = (NavigationView) findViewById(R.id.nav_view_default);
        View header = (View) navMain.getHeaderView(0);
        TextView userName = (TextView) header.findViewById(R.id.drawerUser);
        TextView userEmail = (TextView) header.findViewById(R.id.drawerEmail);
        ImageView userPic = (ImageView) header.findViewById(R.id.imageView);

        if (user != null)
        {
            final Uri[] pathImg = new Uri[1];

            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Uri f = uri;
                    pathImg[0] = uri;

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            userName.setText(user.getDisplayName());
            userEmail.setText(user.getEmail());
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(userPic);
        }

        userPic.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                startActivity(new Intent(v.getContext(), ProfilePageActivity.class));
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

    }

}
