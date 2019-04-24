package com.example.quangle.myapplication;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.location.Location;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.GeoQueryDataEventListener;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerClickListener {

    //arrays for items
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<String> id = new ArrayList<>();

    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FIND_LOCATION =1;
    private static final int DEFAULT_ZOOM = 15;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(33.783535, -118.110226);

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient fusedLocationClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user.getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageTask uploadTask;
    private Uri imageURL;
    private static int IMAGE_REQUEST=1;
    private String path;
    private DocumentReference docRef ;
    private Map<String, Object> coordinates = new HashMap<>();
    private Map<String, Object> location = new HashMap<>();
    private boolean sharing=false;
    private double minLat, maxLat, minLong, maxLong;
    private Timer t;
    private boolean timerStatus;
    private CollectionReference geoFirestoreRef = FirebaseFirestore.getInstance().collection("users");
    private GeoFirestore geoFirestore = new GeoFirestore(geoFirestoreRef);
    private GeoQuery geoQuery;

    MenuItem locationItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get location
        if(savedInstanceState!= null)
        {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        //show main contents and set up menu drawer and map
        setContentView(R.layout.activity_main);

        mGeoDataClient = Places.getGeoDataClient(this);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

       // t = new Timer();
        Menu navMenu = navigationView.getMenu();
        locationItem = navMenu.findItem(R.id.nav_location);
        showCurrentPlace();
        getImage();
        getUserProfile();

    }

    //call these function when activity is started
    @Override
    protected void onStart() {
        super.onStart();
        db.collection("users")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        else if(geoQuery== null)
                        {
                            getSellersTest();
                        }
                        else
                        {
                            geoQuery.removeAllListeners();
                            getSellersTest();
                        }
                        Log.d(TAG, "Updated sellers ");
                    }
                });

        listenPermission();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(t!= null)
        {
            t.cancel();
            t = null;
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(t!= null)
        {
            t.cancel();
            t = null;
        }
        if(geoQuery!= null) {
            geoQuery.removeAllListeners();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        listenPermission();
    }

    //do these functions when activity stop
    @Override
    protected void onStop()
    {
        super.onStop();
        if(t != null)
        {
            t.cancel();
        }

    }

    //back button
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
        getMenuInflater().inflate(R.menu.cart, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.menu_search);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        //set up cart button
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

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
            startActivity(new Intent(this, MessageActivity.class));
        } else if (id == R.id.nav_order) {
            startActivity(new Intent(this, Payment.class));
        } else if (id == R.id.nav_listAnItem) {
            startActivity(new Intent(this, ListItemActivity.class));
        } else if (id == R.id.nav_selling) {
            openPending("selling");
        } else if (id == R.id.nav_buying) {
            openPending("buying");
        } else if (id == R.id.nav_contact) {
            startActivity(new Intent(this, ContactPageActivity.class));
            //openImg();
        } else if (id == R.id.nav_location) {
            locationSharing(!sharing);
        } else if(id == R.id.nav_venmo){
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        /*
        LatLng sydney = new LatLng(33.783535, -118.110226);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Test"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */

        getLocationPermission();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMarkerClickListener(this);
        updateLocationUI();
        getDeviceLocation();


    }

    //Request permission for device's location
    private void getLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            mLocationPermissionGranted = true;

        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FIND_LOCATION);
        }
    }

    //@Override
    public void onRequestPermissionResult(int requestCode, @NonNull String permission[], @NonNull int [] grantResults)
    {
        mLocationPermissionGranted = false;
        switch(requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FIND_LOCATION:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    mLocationPermissionGranted=true;
                }
            }
        }
        updateLocationUI();
    }

    private void getSellers()
    {
        mMap.clear();
        //getDeviceLocation();
        if(mLastKnownLocation !=null) {
            calculateMinMax(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            GeoPoint minLoc = new GeoPoint(minLat, minLong);
            GeoPoint maxLoc = new GeoPoint(maxLat, maxLong);
            Log.i(TAG, "Location: " + minLoc);
            db.collection("users")
                    .whereEqualTo("locationPerm", true)
                    .whereGreaterThanOrEqualTo("location", minLoc)
                    .whereLessThanOrEqualTo("location", maxLoc)
                    .limit(10)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    GeoPoint seller = document.getGeoPoint("location");
                                    LatLng anotherPerson = new LatLng(seller.getLatitude(),
                                            seller.getLongitude());
                                    Toast.makeText(getApplicationContext(), "update map", Toast.LENGTH_SHORT).show();
                                    mMap.addMarker(new MarkerOptions().position(anotherPerson)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_face)));
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker)
    {
        openIntent((String) marker.getTag());
        return true;
    }

    private void getSellersTest()
    {
        //mMap.clear();
        if(mLastKnownLocation!=null)
        {
            if(geoQuery== null) {
                geoQuery = geoFirestore.queryAtLocation(new GeoPoint(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 1);
            }
                mMap.clear();

                geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                    @Override
                    public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                        if (!documentSnapshot.getId().equals(uid)  && documentSnapshot.getBoolean("Permission")) {
                            LatLng anotherPerson = new LatLng(geoPoint.getLatitude(),
                                    geoPoint.getLongitude());
                            Toast.makeText(getApplicationContext(), "update map", Toast.LENGTH_SHORT).show();
                            Marker seller = mMap.addMarker(new MarkerOptions()
                                    .position(anotherPerson)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_face)));
                            seller.setTag(documentSnapshot.getId());
                        }
                    }

                    @Override
                    public void onDocumentExited(DocumentSnapshot documentSnapshot) {

                    }

                    @Override
                    public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                        mMap.clear();
                        LatLng anotherPerson = new LatLng(geoPoint.getLatitude(),
                                geoPoint.getLongitude());
                        Toast.makeText(getApplicationContext(), "update map", Toast.LENGTH_SHORT).show();
                        mMap.addMarker(new MarkerOptions().position(anotherPerson)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_face)));

                    }

                    @Override
                    public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {

                    }

                    @Override
                    public void onGeoQueryReady() {

                    }

                    @Override
                    public void onGeoQueryError(Exception e) {

                    }
                });

        }
    }
    private void updateLocationUI() {

        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }



    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        getDeviceLocation();
        return false;
    }

    //update device current location
    private void updateLocation()
    {

        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if(mLastKnownLocation!=null) {
                                /*GeoPoint devLoc = new GeoPoint(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                location.put("location", devLoc);
                                db.collection("users").document(user.getUid())
                                        .set(location, SetOptions.merge());*/

                                geoFirestore.setLocation(uid,
                                        new GeoPoint(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFirestore.CompletionListener() {
                                            @Override
                                            public void onComplete(Exception exception) {
                                                if (exception == null){
                                                    Log.d(TAG, "cannot save location");
                                                }
                                            }
                                        });
                            }

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //get current device's location and update it to firestore
    private void getDeviceLocation() {
        /*
          Get the best and most recent location of the device, which may be null in rare
          cases when a location is not available.
         */
        //get the user data


        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            updateLocation();
                            //getSellers();
                            getSellersTest();
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") //final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                //openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    private void getImage()
    {
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        db.collection("item")
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

    private void initRecyclerView()
    {
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recycleHView  );
        recyclerView.setLayoutManager(layoutManager);
        RecycleViewAdapterProfile adapter = new RecycleViewAdapterProfile(this, names, urls,prices,id);
        recyclerView.setAdapter(adapter);
    }

    public void updateUserProfile()
    {
        //initialize storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();


        if(imageURL!=null)
        {
            final StorageReference imagesRef = storageRef.child("userImages").child(uid);
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
                                .setDisplayName("Quang Le")
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
            }
        }
    }



    public void getUserProfile()
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String uid = user.getUid();
        StorageReference storageReference = storageRef.child("userImages").child(uid);
        NavigationView navMain = (NavigationView) findViewById(R.id.nav_view);
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
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

    }

    private void locationSharing(boolean perm)
    {
        Map<String, Object> permission = new HashMap<>();
        SpannableString s = new SpannableString(locationItem.getTitle());
        if(perm)
        {
            permission.put("Permission",true);
            db.collection("users").document(user.getUid())
                    .set(permission,SetOptions.merge());
            s.setSpan(new ForegroundColorSpan(Color.BLUE), 0, s.length(), 0);
            locationItem.setTitle(s);
            startTimer();
        }
        else
        {

            permission.put("Permission",false);
            db.collection("users").document(user.getUid())
                    .set(permission,SetOptions.merge());
            s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
            locationItem.setTitle(s);
            stopTimer();

        }
    }


    private void listenPermission()
    {
        final DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    sharing = snapshot.getBoolean("Permission");
                    locationSharing(sharing);

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }


    //start updating device location
    private void startTimer()
    {
        if(t==null)
        {
            t = new Timer();
            t.scheduleAtFixedRate
                    (new TimerTask()
                     {
                         @Override
                         public void run()
                         {

                             updateLocation();
                         }

                     },
                            0,
                            10000);
        }
    }

    //stop updating device location
    private void stopTimer()
    {
        if(t != null)
        {
            t.cancel();
            t.purge();
        }
    }

    public void calculateMinMax(double latCenter, double longCenter)
    {
        //about 1 mile radius
        double d = 0.0090;
        minLat = latCenter - d;
        maxLat = latCenter + d;
        minLong = longCenter - (d / Math.cos(latCenter*Math.PI/180));
        maxLong = longCenter + (d / Math.cos(latCenter*Math.PI/180));
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
}
