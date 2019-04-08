package com.example.quangle.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class ProfilePageActivity extends DefaultActionbar
        implements NavigationView.OnNavigationItemSelectedListener{
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private StorageTask uploadTask;
    private Uri imageURL;
    private static int IMAGE_REQUEST=1;
    private String path;

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private static final String TAG = "ProfilePageActivity";

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            //setContentView(R.layout.profile_page);

            LayoutInflater inflater = (LayoutInflater) this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View contentView = inflater.inflate(R.layout.profile_page, null, false);
            drawer.addView(contentView, 0);

            getImage();

            Button addPicture = (Button) findViewById(R.id.addButton);

            addPicture.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    openImg();
                }
            });

            ImageView userPic = (ImageView)findViewById(R.id.imageView);
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(userPic);
        }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.message:
                startActivity(new Intent(this, MessageActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //get image file
    public void openImg()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }
    public void updateUserProfile()
    {
        //initialize storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();


        if(imageURL!=null)
        {
            final StorageReference imagesRef = storageRef.child("userImages").child(uid);
            //url = https://cdn.frankerfacez.com/emoticon/145947/4;
            uploadTask = imagesRef.putFile(imageURL);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return imagesRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if(task.isSuccessful())
                    {
                        Uri downloadUrl =task.getResult();
                        path = downloadUrl.toString();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(path))
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                        }
                                    }
                                });
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(),"No image selected", Toast.LENGTH_SHORT).show();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == IMAGE_REQUEST && resultCode== RESULT_OK && data !=null && data.getData() != null)
        {
            imageURL=data.getData();
            if(uploadTask != null && uploadTask.isInProgress())
            {
                Toast.makeText(getApplicationContext(),"Upload in progress", Toast.LENGTH_SHORT).show();
            }
            else
            {
                updateUserProfile();
                Toast.makeText(getApplicationContext(),"Your profile picture is uploaded", Toast.LENGTH_SHORT).show();
            }
        }
    }

        private void getImage()
        {
            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://cdn.shopify.com/s/files/1/1499/3122/products/RC_3205_M_Black_Zip_Hoodie_Front_1553_2_d8dfd745-0683-42de-9ab0-daa07894d1de_1024x1024.JPG?v=1549500312");
            names.add("Reigning champ");
            prices.add(10.00);

            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://www.sneakersnstuff.com/images/219550/large.jpg");
            names.add("New Reigning champ midweight tshirt  ");
            prices.add(15.00);

            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://cdn.shopify.com/s/files/1/1499/3122/products/RC_3206_M_Black_Hoodie_Front_copy_1024x1024.jpg?v=1549562065");
            names.add("Reigning champ");
            prices.add(15.00);

            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://www.sneakersnstuff.com/images/219550/large.jpg");
            names.add("Reigning champ tshirt");
            prices.add(15.00);

            Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
            urls.add("https://www.sneakersnstuff.com/images/184924/large.jpg");
            names.add("Reigning champ");
            prices.add(15.00);

            urls.add("https://www.sneakersnstuff.com/images/219550/large.jpg");
            names.add("Reigning champ tshirt");
            prices.add(15.00);

            initRecyclerView();
        }

    private void initRecyclerView()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleHView  );
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterProfile adapter = new RecycleViewAdapterProfile(this, names, urls,prices);
        recyclerView.setAdapter(adapter);
    }
}
