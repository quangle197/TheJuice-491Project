package com.example.quangle.myapplication;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

//import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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
    private String uid = user.getUid();
    NavigationView navigationView;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_default);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.drawer = (DrawerLayout) findViewById(R.id.drawer_layout_default);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_default);
        navigationView.setNavigationItemSelectedListener(this);
        getUserProfile();
        hideButton();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.defaultmenu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.cart:
                startActivity(new Intent(this, CartActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else if (id == R.id.nav_message) {
            startActivity(new Intent(this, ChatActivity.class));
        } else if (id == R.id.nav_order) {
            startActivity(new Intent(this, Payment.class));
        } else if (id == R.id.nav_listAnItem) {
            startActivity(new Intent(this, ListItemActivity.class));
        } else if (id == R.id.nav_offers) {
            openPending("selling");
        } else if (id == R.id.nav_buying) {
            openPending("buying");
        } else if (id == R.id.nav_bought) {
            startActivity(new Intent(this, BoughtItemActivity.class));
        } else if (id == R.id.nav_sold) {
            startActivity(new Intent(this,SoldItemActivity.class));
        } else if (id == R.id.nav_selling) {
            openSelling();
        } else if (id == R.id.nav_contact) {
            startActivity(new Intent(this, ContactPageActivity.class));
            //openImg();
        }  else if(id == R.id.nav_venmo){
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.venmo");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }
            else
            {
                Toast.makeText(getApplicationContext(),"You need to install Venmo",Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_signout) {
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
                drawer.closeDrawer(GravityCompat.START);
            }
        });

    }
    public void openIntent(String s)
    {
        Intent intent = new Intent(this, OtherProfileActivity.class);
        intent.putExtra("EXTRA_SESSION_ID", s);
        this.startActivity(intent);

    }

    public void openPending(String s)
    {
        Intent intent = new Intent(this, PendingItemActivity.class);
        intent.putExtra("EXTRA_SESSION_ID", s);
        this.startActivity(intent);
    }

    public void openSelling()
    {
        Intent intent = new Intent(this, UserInventoryActivity.class);
        intent.putExtra("EXTRA_SESSION_ID", uid);
        this.startActivity(intent);
    }

    public void hideButton()
    {
        navigationView =    (NavigationView) findViewById(R.id.nav_view_default);
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_location).setVisible(false);
    }

}
