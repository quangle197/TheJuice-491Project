package com.example.quangle.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserInventoryActivity extends DefaultActionbar {
    private static final String TAG = "RecycleViewAdapter";
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> conditions = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.user_inventory, null, false);
        drawer.addView(contentView, 0);

        Bundle extras = getIntent().getExtras();
        if(extras!= null)
        {
            String id = extras.getString("EXTRA_SESSION_ID");
            getResults(id);
            getSupportActionBar().setTitle("Selling Items");
        }
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    public void listItem(View v)
    {
        startActivity(new Intent(this, ListItemActivity.class));
        finish();
    }


    private void getResults(final String userquery)
    {
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        db.collection("item")
                .whereEqualTo("sellerID", userquery)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getString("name"));
                                if(!document.getBoolean("soldStatus")) {
                                    if (document.getString("image1") != null) {
                                        urls.add(document.getString("image1"));
                                    } else {
                                        urls.add("https://firebasestorage.googleapis.com/v0/b/we-sell-491.appspot.com/o/itemImages%2Fdefault.png?alt=media&token=d4cb0d3c-7888-42d5-940f-d5586a4e0a4a");
                                    }
                                    names.add(document.getString("name"));
                                    conditions.add(document.getString("condition"));
                                    prices.add(document.getDouble("price"));
                                    id.add(document.getId());
                                }
                            }
                            if(userquery.equals(uid))
                            {
                                showItems();
                            }
                            else
                            {
                                showItemsOther();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    public void removeItemAt(String id)
    {
        db.collection("item").document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    private void showItems()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleVView  );
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterVertical adapter = new RecycleViewAdapterVertical(this, names, urls, prices, conditions, 0,id, new RecycleViewAdapterVertical.AdapterListener() {
            @Override
            public void removeButtonOnClick(View v, int position)
            {
                removeItemAt(id.get(position));
            }

            @Override
            public void soldButtonOnClick(View v, int position)
            {

            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void showItemsOther()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleVView  );
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterVertical adapter = new RecycleViewAdapterVertical(this, names, urls, prices, conditions,id);
        recyclerView.setAdapter(adapter);
    }
}