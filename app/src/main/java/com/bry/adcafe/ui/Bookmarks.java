package com.bry.adcafe.ui;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.Manifest;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.SavedAdsCard;
import com.bry.adcafe.fragments.ViewImageFragment;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.NetworkStateReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wang.avi.AVLoadingIndicatorView;

public class Bookmarks extends AppCompatActivity {
    private static final String TAG = "Bookmarks";
    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mRef;

    private List<Advert> mSavedAds;
    private Runnable mViewRunnable;
    private AVLoadingIndicatorView mAvi;
    private TextView loadingText;
    private TextView noAdsText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        mContext = getApplicationContext();

        loadPlaceHolderViews();
        registerReceivers();

        if(isNetworkConnected(mContext)){
            loadAdsFromThread();
        }else{
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.connectionDropped,
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    }


    @Override
    protected void onDestroy(){
        mPlaceHolderView.removeAllViews();

        unregisterAllReceivers();
        super.onDestroy();
    }

    private void unregisterAllReceivers() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpinned);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForReceivingUnableToPinAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSharingAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForViewingAd);

        Intent intent = new Intent("UNREGISTER");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForUnpinned,new IntentFilter(Constants.REMOVE_PINNED_AD));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForReceivingUnableToPinAd,new IntentFilter(Constants.UNABLE_TO_REMOVE_PINNED_AD));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSharingAd,new IntentFilter("SHARE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForViewingAd,new IntentFilter("VIEW"));
    }



    private void loadAdsFromThread(){
        try{
            startGetAds();
        }catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
    }

    private void startGetAds() {
        mViewRunnable = new Runnable() {
            @Override
            public void run() {
                getAds();
            }
        };
        Thread thread =  new Thread(null, mViewRunnable, "Background");
        thread.start();
//        mProgressBar.setVisibility(View.VISIBLE);
        mAvi.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
    }

    private void getAds() {
        try{
            mSavedAds = new ArrayList<>();
            loadAdsFromFirebase();

            Thread.sleep(50);
            Log.i("ARRAY", ""+ mSavedAds.size());
        }catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
        }
        runOnUiThread(returnRes);
    }



    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
//            loadBookmarkedAdsIntoCards();
        }
    };

    private void loadPlaceHolderViews() {
        mPlaceHolderView = (PlaceHolderView) findViewById(R.id.PlaceHolderView);
        mAvi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        loadingText = (TextView) findViewById(R.id.loadingPinnedAdsMessage);
        mPlaceHolderView.getBuilder().setLayoutManager(new GridLayoutManager(mContext,3));
        noAdsText = (TextView) findViewById(R.id.noPins);
    }






    private BroadcastReceiver mMessageReceiverForUnpinned = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Message received to show toast for unpin action");
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.unpinned,
                    Snackbar.LENGTH_SHORT).show();
        }
    };

    private BroadcastReceiver mMessageReceiverForReceivingUnableToPinAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Unable to unpin ad.");
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.failedUnpinned,
                    Snackbar.LENGTH_SHORT).show();
        }
    };

    private BroadcastReceiver mMessageReceiverForSharingAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to share ad.");
            isStoragePermissionGranted();
        }
    };

    private BroadcastReceiver mMessageReceiverForViewingAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to view ad.");
            loadAdFragment();
        }
    };

    private void loadAdFragment() {
        FragmentManager fm = getFragmentManager();
        ViewImageFragment imageFragment = new ViewImageFragment();
        imageFragment.show(fm, "View image.");
        imageFragment.setfragcontext(mContext);
    }


    private void loadAdsFromFirebase(){
        if(!mSavedAds.isEmpty()){
            mSavedAds.clear();
        }
        String uid = User.getUid();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.PINNED_AD_LIST);
        DatabaseReference mRef = query.getRef();
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    Advert advert = snap.getValue(Advert.class);
                    mSavedAds.add(advert);
                    Log.d("BOOKMARKS"," --Loaded ads from firebase.--"+advert.getPushId());
                }
                loadBookmarkedAdsIntoCards();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("UTILS","Failed to load ads from firebase.");
            }
        });
    }

    private void loadBookmarkedAdsIntoCards() {
        if(mPlaceHolderView == null){
            loadPlaceHolderViews();
        }
        if(mSavedAds!=null && mSavedAds.size()>0){
            for(int i = 0; i<mSavedAds.size();i++){
                mPlaceHolderView.addView(new SavedAdsCard(mSavedAds.get(i),mContext,mPlaceHolderView,mSavedAds.get(i).getPushId()));
            }
        }else{
            Toast.makeText(mContext,"You do not have any pinned ads.",Toast.LENGTH_LONG).show();
            noAdsText.setVisibility(View.VISIBLE);
        }
        mSavedAds.clear();
        mAvi.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);

    }

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void shareImage(Bitmap icon){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(30);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                shareImage(Variables.adToBeShared.getImageBitmap());
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            shareImage(Variables.adToBeShared.getImageBitmap());
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            shareImage(Variables.adToBeShared.getImageBitmap());
        }
    }
}
