package com.example.quangle.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ItemScreenActivity extends DefaultActionbar {

    String sessionId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<String> images = new ArrayList<>();
    String itemName;
    Double itemPrice;
    Double itemDistance;
    String itemCondition;
    int itemQuantity;
    String itemDesc;
    String itemSellerID;
    String sellerName;

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
        }
        //sessionId= getIntent().getStringExtra("EXTRA_SESSION_ID");

        getImages();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

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
                        itemSellerID=document.getString("sold");
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

                        initView();
                        getItemInfo();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    public void getItemInfo()
    {
        TextView itemName = findViewById(R.id.itemNameEditText);
        TextView itemPrice = findViewById(R.id.itemPriceEditText);
        TextView itemDistance = findViewById(R.id.itemDistanceEditText);
        TextView itemCondition = findViewById(R.id.conditionEditText);
        TextView itemQuantity = findViewById(R.id.quantityEditText);
        TextView itemDesc = findViewById(R.id.descriptionEditText);
        TextView itemSeller = findViewById(R.id.itemSellerEditText);

        itemName.setText(this.itemName);
        itemPrice.setText(Double.toString(this.itemPrice));
        itemDistance.append(Double.toString(this.itemDistance));
        itemCondition.append(this.itemCondition);
        itemQuantity.append(Integer.toString(this.itemQuantity));
        itemDesc.append(this.itemDesc);
        itemSeller.append(this.sellerName);
    }
}
