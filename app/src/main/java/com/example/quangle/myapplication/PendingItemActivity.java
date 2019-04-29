package com.example.quangle.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PendingItemActivity extends DefaultActionbar {

    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> id = new ArrayList<>();
    private ArrayList<String> conditions = new ArrayList<>();
    private ArrayList<String> buyerID = new ArrayList<>();
    private ArrayList<String> sellerID = new ArrayList<>();
    private static final String TAG = "PendingItemActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private DatabaseReference mCartDatabase;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private String sessionId;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.cart, null, false);
        drawer.addView(contentView, 0);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sessionId = extras.getString("EXTRA_SESSION_ID");
            //The key argument here must match that used in the other activity
            if(sessionId.equals("selling"))
            {
                getSupportActionBar().setTitle("Offers");
            }
            else if(sessionId.equals("buying"))
            {
                getSupportActionBar().setTitle("Buying");
            }
            getIDs();
        }

    }

    @Override
    public void onBackPressed()
    {
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


    public void getIDs()
    {
        final String cartRef = "pending";
        mCartDatabase= database.getReference(cartRef);
        mCartDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                names.clear();
                urls.clear();
                prices.clear();
                conditions.clear();
                id.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    PendingItemClass item = ds.getValue(PendingItemClass.class);
                    if( sessionId.equals("buying")&& uid.equals(item.getbuyerID()))
                    {
                        buyerID.add(item.getbuyerID());
                        sellerID.add(item.getsellerID());
                        getBuyings(item.getItemID());
                    }
                    else if(sessionId.equals("selling")&& uid.equals(item.getsellerID()))
                    {
                        sellerID.add(item.getsellerID());
                        buyerID.add(item.getbuyerID());
                        getSellings(item.getItemID());
                    }
                }

                Log.d(TAG, "Value is: " + cartRef);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void getBuyings( String search)
    {
        DocumentReference docRef = db.collection("item").document(search);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        names.add(document.getString("name"));
                        conditions.add(document.getString("condition"));
                        prices.add(document.getDouble("price"));
                        id.add(document.getId());
                        if(document.getString("image1") != null)
                        {
                            urls.add(document.getString("image1"));
                        }
                        else
                        {
                            urls.add("https://firebasestorage.googleapis.com/v0/b/we-sell-491.appspot.com/o/itemImages%2Fdefault.png?alt=media&token=d4cb0d3c-7888-42d5-940f-d5586a4e0a4a");
                        }

                        showBuyingItems();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }


    private void getSellings(String search)
    {DocumentReference docRef = db.collection("item").document(search);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        names.add(document.getString("name"));
                        conditions.add(document.getString("condition"));
                        prices.add(document.getDouble("price"));
                        id.add(document.getId());
                        if(document.getString("image1") != null)
                        {
                            urls.add(document.getString("image1"));
                        }
                        else
                        {
                            urls.add("https://firebasestorage.googleapis.com/v0/b/we-sell-491.appspot.com/o/itemImages%2Fdefault.png?alt=media&token=d4cb0d3c-7888-42d5-940f-d5586a4e0a4a");
                        }

                        showSellingItems();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    public void sellerRemove( String id, final String buyerID)
    {
        Query query = database.getReference().child("pending").orderByChild("itemID").equalTo(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        PendingItemClass item = ds.getValue(PendingItemClass.class);
                        if (item.getsellerID().equals(uid) && item.getbuyerID().equals(buyerID)) {
                            ds.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void buyerRemove(final String id)
    {
        DatabaseReference getPending = database.getReference("pending");
        Query query = getPending.orderByChild("itemID").equalTo(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        PendingItemClass item = ds.getValue(PendingItemClass.class);
                        if( item.getbuyerID().equals(uid)  ) {
                            ds.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void markSold(String id, String buyerID)
    {
        sellerRemove(id,buyerID);
        Map<String, Object> sold = new HashMap<>();
        sold.put("soldStatus", true);
        sold.put("buyerID", buyerID);
        sold.put("rating", false);
        db.collection("item").document(id)
                .set(sold, SetOptions.merge());
    }

    private void showSellingItems()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleVView  );
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterVertical adapter = new RecycleViewAdapterVertical(this, names, urls, prices, conditions, 1, new RecycleViewAdapterVertical.AdapterListener() {
            @Override
            public void removeButtonOnClick(View v, int position)
            {
                sellerRemove(id.get(position),buyerID.get(position));
            }

            @Override
            public void soldButtonOnClick(View v, int position)
            {
                markSold(id.get(position),buyerID.get(position));
            }
        });

        recyclerView.setAdapter(adapter);

    }

    private void showBuyingItems()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleVView  );
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterVertical adapter = new RecycleViewAdapterVertical(this, names, urls, prices, conditions, 0, new RecycleViewAdapterVertical.AdapterListener() {
            @Override
            public void removeButtonOnClick(View v, int position)
            {
                buyerRemove(id.get(position));
            }

            @Override
            public void soldButtonOnClick(View v, int position)
            {

            }
        });

        recyclerView.setAdapter(adapter);
    }


}
