package com.example.quangle.myapplication;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BoughtItemActivity extends DefaultActionbar {

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> sellerID = new ArrayList<>();
    private ArrayList<String> conditions = new ArrayList<>();
    private ArrayList<Boolean> rate = new ArrayList<>();
    private static final String TAG = "RecycleViewAdapter";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private RecycleViewAdapterRating adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        //set up the screen
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.buy_history, null, false);
        drawer.addView(contentView, 0);

        //get the items
        getResults();
    }

    @Override
    public void onBackPressed() {
        //startActivity(new Intent(this, MainActivity.class));
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

    //function to get items
    private void getResults()
    {
        db.collection("item")
                .whereEqualTo("buyerID", uid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        //empty out all arrays
                        names.clear();
                        urls.clear();
                        prices.clear();
                        conditions.clear();
                        id.clear();
                        rate.clear();
                        sellerID.clear();

                        //add items' elements into arrays
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.getString("image1") != null) {
                                urls.add(doc.getString("image1"));
                            } else {
                                urls.add("https://firebasestorage.googleapis.com/v0/b/we-sell-491.appspot.com/o/itemImages%2Fdefault.png?alt=media&token=d4cb0d3c-7888-42d5-940f-d5586a4e0a4a");
                            }
                            names.add(doc.getString("name"));
                            prices.add(doc.getDouble("price"));
                            id.add(doc.getId());
                            conditions.add(doc.getString("condition"));
                            sellerID.add(doc.getString("sellerID"));
                            if (doc.get("rating") != null) {
                                rate.add(doc.getBoolean("rating"));
                            }

                        }

                        //display items
                        if(names!=null) {
                            showItems();
                        }

                        Log.d(TAG, "rating: " );
                    }
                });

    }

    //function to rate another user
    public void rateItem(final String sID, final String itemID)
    {
        final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);

        LinearLayout linearLayout = new LinearLayout(this);
        final RatingBar rating = new RatingBar(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rating.setLayoutParams(lp);
        rating.setNumStars(5);
        rating.setStepSize(1);
        LayerDrawable stars = (LayerDrawable) rating.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);

        //add ratingBar to linearLayout
        linearLayout.addView(rating);

        popDialog.setTitle("Add Rating: ");

        //add linearLayout to dailog
        popDialog.setView(linearLayout);



        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                System.out.println("Rated val:"+v);
            }
        });



        // Button OK
        popDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        avgRating(sID, rating.getProgress());
                        Map<String, Object> data = new HashMap<>();
                        data.put("rating", true);
                        db.collection("item").document(itemID)
                                .set(data, SetOptions.merge());
                        dialog.dismiss();
                    }

                })

                // Button Cancel
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        popDialog.create();
        popDialog.show();
    }

    //function to calculate and add rating to seller
    private void avgRating(final String id, final double r)
    {
        DocumentReference docRef = db.collection("users").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        int totalRating = document.getLong("total rating").intValue();
                        int newTotal = totalRating +1;
                        double oldRating = document.getDouble("rating")*totalRating;
                        double newRating = (oldRating +r) / newTotal;

                        Map<String, Object> data = new HashMap<>();
                        data.put("rating", newRating);
                        data.put("total rating", newTotal);
                        db.collection("users").document(id)
                                .set(data, SetOptions.merge());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //function create recycler adapter to hold items
    private void showItems()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");
        final TextView noShow = (TextView) findViewById(R.id.noResultHistory);
        noShow.setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleRView );
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecycleViewAdapterRating(this, names, urls, prices, conditions, rate, new RecycleViewAdapterRating.AdapterListener() {
            @Override
            public void rateButtonOnClick(View v, int position)
            {
                //rateItem(id.get(position));
                rateItem(sellerID.get(position),id.get(position));
            }

        });

        recyclerView.setAdapter(adapter);
    }
}
