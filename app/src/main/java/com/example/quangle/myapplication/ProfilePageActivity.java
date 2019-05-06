package com.example.quangle.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class ProfilePageActivity extends DefaultActionbar
        implements NavigationView.OnNavigationItemSelectedListener{
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private StorageTask uploadTask;
    private Uri imageURL;
    private static int IMAGE_REQUEST=1;
    private String path;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> id = new ArrayList<>();
    private static final String TAG = "ProfilePageActivity";
    private ListenerRegistration listenToDB;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.profile_page);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.profile_page, null, false);
        drawer.addView(contentView, 0);
        getImageTest();

        Button addPicture = (Button) findViewById(R.id.addButton);

        addPicture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openImg();
            }
        });

        changePicture();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        getImageTest();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        listenToDB.remove();
    }

    //show all items button
    public void showAllItems(View v)
    {
        Intent intent = new Intent(this, UserInventoryActivity.class);
        intent.putExtra("EXTRA_SESSION_ID", uid);
        this.startActivity(intent);
        finish();
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
                startActivity(new Intent(this, ChatActivity.class));
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

    //update user's profile information
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
                                            uploadProfilePicture(path);
                                            changePicture();
                                            finish();
                                            startActivity(getIntent());
                                        }
                                    }
                                });


                        //updateProfilePicture();
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

    //after upload image
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

    //get user's item
    private void getImageTest()
    {
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        Query query = db.collection("item")
                .whereEqualTo("sellerID", uid);
        listenToDB= query.addSnapshotListener(this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        names.clear();
                        urls.clear();
                        prices.clear();
                        id.clear();

                        for (QueryDocumentSnapshot document : value) {
                            if (!document.getBoolean("soldStatus")) {
                                if (document.getString("image1") != null) {
                                    urls.add(document.getString("image1"));
                                } else {
                                    urls.add("https://firebasestorage.googleapis.com/v0/b/we-sell-491.appspot.com/o/itemImages%2Fdefault.png?alt=media&token=d4cb0d3c-7888-42d5-940f-d5586a4e0a4a");
                                }
                                String name = document.getString("name");
                                Double price = document.getDouble("price");
                                names.add(name);
                                prices.add(price);
                                id.add(document.getId());
                            }
                        }
                        initRecyclerView();
                        Log.d(TAG, "Added documents: " + names);
                    }
                });
    }

    private void initRecyclerView()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleHView  );
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterProfile adapter = new RecycleViewAdapterProfile(this, names, urls,prices,id);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //update profile picture link
    private void uploadProfilePicture(String u)
    {
        HashMap<String, Object> url = new HashMap<>();
        url.put("profilePicture",u );
        db.collection("users").document(user.getUid())
                .set(url, SetOptions.merge());
    }

    //update profile picture
    private void changePicture()
    {
        user = FirebaseAuth.getInstance().getCurrentUser();
        ImageView userPic = (ImageView)findViewById(R.id.imageView);
        Glide.with(this)
                .asBitmap()
                .load(user.getPhotoUrl())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true))
                .into(userPic);

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        setRating(document.getDouble("rating"),document.getLong("total rating").intValue());
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //list item button
    public void listItem(View v)
    {
        startActivity(new Intent(this, ListItemActivity.class));
        finish();
    }

    //display rating
    public void setRating(double sellerRate,int totalRate)
    {
        RatingBar sellerRating = findViewById(R.id.userReview);
        TextView totalRating = findViewById(R.id.totalRatings);

        sellerRating.setRating((float)sellerRate);
        totalRating.setText(String.valueOf(totalRate));
    }

}
