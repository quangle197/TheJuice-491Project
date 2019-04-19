package com.example.quangle.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ListItemActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String name, sold, condition, description;
    private int distance, quantity;
    private StorageTask uploadTask;
    private double price;
    private ArrayList<Uri> images= new ArrayList<>();
    private static int IMAGE_REQUEST=1;
    private String []names = {"image1", "image2", "image3", "image4", "image5"};

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_listing);

        final EditText nameInput = (EditText) findViewById(R.id.title);
        final EditText descInput = (EditText) findViewById(R.id.description);
        final EditText priceInput = (EditText) findViewById(R.id.price);
        final EditText conditionInput = (EditText) findViewById(R.id.condition);
        final EditText distanceInput = (EditText) findViewById(R.id.category);
        final EditText quantityInput = (EditText) findViewById(R.id.quantity);


        Button addItem = (Button) findViewById(R.id.button);
        addItem.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                name = nameInput.getText().toString();
                sold = user.getUid();
                condition = conditionInput.getText().toString();
                description = descInput.getText().toString();
                price =  Double.parseDouble(priceInput.getText().toString());
                distance = Integer.parseInt(distanceInput.getText().toString());
                quantity = Integer.parseInt(quantityInput.getText().toString());

                listItem(name, sold, condition, description, price, distance, quantity);
            }
        });

        Button addPicture = (Button) findViewById(R.id.addPicture);
        addPicture.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                uploadImg();
            }
        });
    }

    public void listItem(String name, String sold, String condition, String description, double price, int distance, int quantity)
    {

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("price", price);
        user.put("distance", distance);
        user.put("sold", sold);
        user.put("condition", condition);
        user.put("quantity", quantity);
        user.put("description", description);
        user.put("lowercasename",name.toLowerCase());

        db.collection("item")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Added item");
                        String id = documentReference.getId();
                        for(int i =0; i < images.size();i++)
                        {
                            updateImages(images.get(i),id,names[i]);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding item",e);
                    }
                });
    }

    //upload images
    public void uploadImg()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"),IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK)
        {
            if(data.getClipData() != null)
            {
                int totalPictures = data.getClipData().getItemCount();
                if(totalPictures <= 5)
                {
                    for (int i = 0; i < totalPictures; i++) {
                        images.add(data.getClipData().getItemAt(i).getUri());
                    }
                    Toast.makeText(getApplicationContext(),Integer.toString(totalPictures),Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Please select less than 5 images",Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Select Image Failed",Toast.LENGTH_SHORT).show();
        }
    }

    public void updateImages(Uri uri, final String id, final String n)
    {
        //initialize storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();


        if(uri!=null)
        {
            final StorageReference imagesRef = storageRef.child("itemImages").child(id).child(n);
            uploadTask = imagesRef.putFile(uri);
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
                        Map<String, Object> imageLink = new HashMap<>();
                        Uri downloadUrl =task.getResult();
                        String path = downloadUrl.toString();

                        imageLink.put(n,path);
                        db.collection("item").document(id)
                                .set(imageLink, SetOptions.merge());
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
}
