package com.example.quangle.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;


import org.imperiumlabs.geofirestore.GeoFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ItemScreenActivity extends DefaultActionbar {

    String sessionId;

    //variables for database related
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mCartQuery = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCartDatabase;
    private DatabaseReference mPendingDatabase;
    private DatabaseReference mChat = FirebaseDatabase.getInstance().getReference("chatRoom");
    private CollectionReference geoFirestoreRef = db.collection("users");
    ArrayList<String> images = new ArrayList<>();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();

    //variables for item attribute
    String itemName;
    Double itemPrice;
    Double itemDistance;
    String itemCondition;
    int itemQuantity;
    String itemDesc;
    String itemSellerID;
    String sellerName;
    String cartRef;

    //check if a chat already exist
    private boolean roomexist = false;

    private static final String TAG = ItemScreenActivity.class.getSimpleName();
    private String []names = {"image1", "image2", "image3", "image4", "image5"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.item_screen, null, false);
        drawer.addView(contentView, 0);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            sessionId = extras.getString("EXTRA_SESSION_ID");
            //The key argument here must match that used in the other activity
            getImages();
        }

    }

    //set delete and mark as sold button if the user is the owner
    public void setVisibility(String id)
    {
        if(uid.equals(id))
        {
            Button deleteButton = (Button) findViewById(R.id.deleteItem);
            Button soldButton = (Button) findViewById(R.id.markSold);

            deleteButton.setVisibility(View.VISIBLE);
            soldButton.setVisibility(View.VISIBLE);

            Button contact = (Button) findViewById(R.id.contactSellerButton);
            Button add = (Button) findViewById(R.id.addToCartButton);
            Button offer = (Button) findViewById(R.id.offerUpButton);

            contact.setVisibility(View.GONE);
            add.setVisibility(View.GONE);
            offer.setVisibility(View.GONE);
        }
    }

    //function to delete item in database
    public void deleteItemInDB()
    {
        db.collection("item").document(sessionId)
                .delete()
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

    //button to delete item
    public void deleteItem(View v)
    {
        //mCartQuery = database.getReference("cart");
        mCartQuery.child("cart").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    if(ds.exists())
                        ds.child(sessionId).getRef().removeValue();
                }
                deleteItemInDB();

                Log.d(TAG, "Value is: " + sessionId);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        Query query = database.getReference().child("pending").orderByChild("itemID").equalTo(sessionId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        PendingItemClass item = ds.getValue(PendingItemClass.class);
                        if (item.getsellerID().equals(uid)) {
                            ds.getRef().removeValue();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        finish();
    }

    //mark sold button
    public void markSold(View v)
    {

        Map<String, Object> sold = new HashMap<>();
        sold.put("soldStatus", true);
        db.collection("item").document(sessionId)
                .set(sold,SetOptions.merge());
        deleteItem(v);
    }

    //add to cart button
    public void addToCart(View v)
    {
        cartRef = "cart/" + uid +"/"+sessionId;
        mCartDatabase= database.getReference(cartRef);
        mCartDatabase.setValue(itemName);
    }

    //get user's location
    public void getCurrentLoc()
    {
        GeoFirestore geoFirestore = new GeoFirestore(geoFirestoreRef);
        geoFirestore.getLocation(uid, new GeoFirestore.LocationCallback() {
            @Override
            public void onComplete(final GeoPoint location, Exception exception) {
                if (exception == null && location != null){
                    Location seller = new Location("seller");
                    seller.setLatitude(location.getLatitude());
                    seller.setLongitude(location.getLongitude());

                    //after that go to get seller location
                    getItemLoc(seller);
                }
            }
        });
    }

    //get seller location
    public void getItemLoc(final Location seller)
    {
        GeoFirestore geoFirestore = new GeoFirestore(geoFirestoreRef);
        geoFirestore.getLocation(itemSellerID, new GeoFirestore.LocationCallback() {
            @Override
            public void onComplete(final GeoPoint location, Exception exception) {
                if (exception == null && location != null){
                    Location seller1 = new Location("seller");
                    seller1.setLatitude(location.getLatitude());
                    seller1.setLongitude(location.getLongitude());

                    //after that, pass the distance to display on screen
                    getItemInfo(seller.distanceTo(seller1));
                }
            }
        });
    }

    //contact seller button
    public void contactSeller(View v)
    {
        checkRoomExist(uid);
    }

    //offer to buy button
    public void offerPrice(View v)
    {

        String pendingRef = "pending";
        mPendingDatabase= database.getReference(pendingRef);
        //generate random key
        String key = mPendingDatabase.push().getKey();
        PendingItemClass item = new PendingItemClass(sessionId, uid,itemSellerID);
        //make pending list which has buyer id, item id, and seller id
        mPendingDatabase.child(key).setValue(item);
    }

    @Override
    public void onBackPressed() {
        //startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    //view seller's profile
    public void goToSeller(View v)
    {
        if(itemSellerID.equals(uid))
        {
            startActivity(new Intent(this, ProfilePageActivity.class));
            finish();
        }
        else
        {
            Intent intent = new Intent(this, OtherProfileActivity.class);
            intent.putExtra("EXTRA_SESSION_ID", itemSellerID);
            startActivity(intent);
        }

    }

    //get item's information
    public void getImages()
    {
        DocumentReference docRef = db.collection("item").document(sessionId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        for(int i=0; i<names.length;i++)
                        {
                            String curImg = document.getString(names[i]);
                            if(curImg!=null)
                            {
                                images.add(curImg);
                            }
                        }
                        itemName=document.getString("name");
                        itemPrice=document.getDouble("price");
                        itemDistance=document.getDouble("distance");
                        itemCondition=document.getString("condition");
                        itemQuantity=document.getLong("quantity").intValue();
                        itemDesc=document.getString("description");
                        itemSellerID=document.getString("sellerID");
                        setVisibility(itemSellerID);
                        getSellerInfo(itemSellerID);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    public void initView()
    {
        ViewPager viewPager = findViewById(R.id.itemPictureItemScreen);
        ImageAdapter adapter = new ImageAdapter(this, images);
        viewPager.setAdapter(adapter);
    }

    //get seller name
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

                        //display item's picture
                        initView();
                        getCurrentLoc();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //display item's information
    public void getItemInfo(double dis)
    {
        TextView itemName = findViewById(R.id.itemNameEditText);
        TextView itemPrice = findViewById(R.id.itemPriceEditText);
        TextView itemDistance = findViewById(R.id.itemDistanceEditText);
        TextView itemCondition = findViewById(R.id.conditionEditText);
        TextView itemQuantity = findViewById(R.id.quantityEditText);
        TextView itemDesc = findViewById(R.id.descriptionEditText);
        TextView itemSeller = findViewById(R.id.itemSellerEditText);

        Spannable spannable = new SpannableString(sellerName);
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE),0,sellerName.length(), 0);

        if(dis > 100)
        {
            dis = dis/1609.34;
            itemDistance.append(String.format("%.2f", dis) + " miles");
        }
        else
        {
            itemDistance.append( String.format("%.2f", dis) + " meters");
        }
        itemName.setText(this.itemName);
        itemPrice.setText("$" +String.format("%.2f", this.itemPrice));
        //itemDistance.append( Double.toString(dis));
        itemCondition.append(this.itemCondition);
        itemQuantity.append(Integer.toString(this.itemQuantity));
        itemDesc.append(this.itemDesc);
        itemSeller.append(spannable);
    }

    //function to check if a chat already exist between user and seller
    public void checkRoomExist(final String id)
    {
        final String cartRef = "chatRoom";
        mChat= database.getReference(cartRef);
        mChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                //for loop to find the chat
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    if(ds.child("sender1").getValue().equals(id) && ds.child("sender2").getValue().equals(itemSellerID))
                    {
                        roomexist=true;
                        goToChat(ds.getKey());
                        return;
                    }
                    else if(ds.child("sender2").getValue().equals(id)&& ds.child("sender1").getValue().equals(itemSellerID))
                    {
                        roomexist=true;
                        goToChat(ds.getKey());
                        return;
                    }
                }

                //if not, create a new one
                if(!roomexist)
                {
                    String key = mChat.push().getKey();
                    makeRoom(key);
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

    //function to create a room
    protected void makeRoom(String roomID)
    {
        mChat.child(roomID).child("sender1").setValue(uid);
        mChat.child(roomID).child("sender2").setValue(itemSellerID);
        goToChat(roomID);
    }

    //go to chat room
    protected void goToChat(String recID)
    {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("EXTRA_SESSION_ID", recID);
        this.startActivity(intent);
        this.finish();
    }

}
