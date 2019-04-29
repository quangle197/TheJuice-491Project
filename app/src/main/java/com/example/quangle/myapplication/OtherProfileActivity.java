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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageTask;

import java.util.ArrayList;
import static android.content.ContentValues.TAG;

public class OtherProfileActivity extends DefaultActionbar
        implements NavigationView.OnNavigationItemSelectedListener{
    private String uid;
    private StorageTask uploadTask;
    private Uri imageURL;
    private static int IMAGE_REQUEST=1;
    private String path;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> id = new ArrayList<>();
    private static final String TAG = "OtherProfileActivity";
    private String sellerName;
    private double sellerRating;
    private int totalRating;
    String sessionId;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.profile_page);

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.other_profile, null, false);
        drawer.addView(contentView, 0);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sessionId = extras.getString("EXTRA_SESSION_ID");
            //The key argument here must match that used in the other activity
            uid = sessionId;
            getSellerInfo(uid);
            getImage();
        }



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

    private void getImage()
    {
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        db.collection("item")
                .whereEqualTo("sellerID", uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getString("name"));
                                if(document.getString("image1") != null)
                                {
                                    urls.add(document.getString("image1"));
                                }
                                else
                                {
                                    urls.add("https://firebasestorage.googleapis.com/v0/b/we-sell-491.appspot.com/o/itemImages%2Fdefault.png?alt=media&token=d4cb0d3c-7888-42d5-940f-d5586a4e0a4a");
                                }
                                String name = document.getString("name");
                                Double price = document.getDouble("price");
                                names.add(name);
                                prices.add(price);
                                id.add(document.getId());

                            }
                            initRecyclerView();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void getSellerInfo(String id)
    {
        DocumentReference docRef = db.collection("users").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        sellerName=document.getString("username");
                        sellerRating=document.getDouble("rating");
                        totalRating= document.getLong("total rating").intValue();
                        changePicture(document.getString("profilePicture"));
                        Log.d(TAG, "DocumentSnapshot data: " + document.getString("profilePicture"));
                        setSeller();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
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
    }

    private void changePicture(String s)
    {
        ImageView userPic = (ImageView)findViewById(R.id.imageViewOther);

        Glide.with(this)
                .asBitmap()
                .load(s)
                .into(userPic);

    }

    public void listOtherItem(View v)
    {
            Intent intent = new Intent(this, UserInventoryActivity.class);
            intent.putExtra("EXTRA_SESSION_ID", uid);
            this.startActivity(intent);
    }

    public void setSeller()
    {
        TextView sellerName = findViewById(R.id.userName);
        RatingBar sellerRating = findViewById(R.id.userReview);
        TextView totalRating = findViewById(R.id.totalRatings);

        sellerName.setText(this.sellerName);
        sellerRating.setRating((float)this.sellerRating);
        totalRating.setText(String.valueOf(this.totalRating));


    }

}
