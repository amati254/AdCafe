package com.bry.adcafe.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;

public class AdUpload extends AppCompatActivity {
    public static final String TAG = AdUpload.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri mFilepath;

    private Context mContext;
    private ImageView mUploadButton;
    private ImageView mChoosingImage;
    private ImageView mProfileImageViewPreview;
    private CardView mCardviewForShowingPreviewOfAd;
    private ImageView mPlaceHolderImage;
    private LinearLayout mTopBarPreview;
    private AVLoadingIndicatorView mAvi;
    private TextView mLoadingTextView;

    private boolean mHasNumberBeenLoaded;
    private boolean mHasUserChosenAnImage;
    private int mNumberOfClusters;
    private int mClusterTotal;
    private int mClusterToStartFrom;
    private DatabaseReference mRef;
    private DatabaseReference mRef2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_upload);
        mContext = this.getApplicationContext();
        mHasUserChosenAnImage = false;
        mHasNumberBeenLoaded = false;

        setUpViews();
        startGetNUmberOfClusters();
    }

    private void startGetNUmberOfClusters(){
        if(isOnline(mContext)){
            getNumberOfClusters();//in-turn triggers the getClusterToStartFrom method.
        }else{
            Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.UploadAdNoConnection,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void setUpViews() {
        mUploadButton = (ImageView) findViewById(R.id.uploadIcon);
        mChoosingImage = (ImageView) findViewById(R.id.chooseImageIcon);
        mProfileImageViewPreview = (ImageView) findViewById(R.id.profileImageViewPreview);
        mCardviewForShowingPreviewOfAd = (CardView) findViewById(R.id.cardviewForShowingPreviewOfAd);
        mPlaceHolderImage = (ImageView) findViewById(R.id.imagePlaceholderUponLaunch);
        mTopBarPreview = (LinearLayout) findViewById(R.id.topBarPreview);
        mAvi = (AVLoadingIndicatorView) findViewById(R.id.AdUploadAvi);
        mLoadingTextView = (TextView) findViewById(R.id.loadingText);
    }

    private void getNumberOfClusters() {
        mAvi.setVisibility(View.VISIBLE);
        setAllOtherViewsToBeGone();
        Log.d(TAG,"---Getting number of clusters.");
        mRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.CLUSTERS_LIST);
        mRef.addListenerForSingleValueEvent(val);

    }

    ValueEventListener val= new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            long numberOfClusters;
            if(dataSnapshot.getChildrenCount() == 0){
                numberOfClusters = dataSnapshot.getChildrenCount()+1;
            }else{
                numberOfClusters = dataSnapshot.getChildrenCount();
            }
            mClusterTotal = (int)numberOfClusters;
            Log.d(TAG,"---Number of clusters gotten from firebase is-- "+mClusterTotal);
            getClusterToStartForm();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            setAllOtherViewsToBeVisible();
            mAvi.setVisibility(View.GONE);
            Log.d(TAG,"---Unable to connect to firebase at the moment."+databaseError.getMessage());
            Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.cannotUploadFailedFirebase,
                    Snackbar.LENGTH_LONG).show();
        }
    }; //triggers getClusterToStartFrom method.

    private void getClusterToStartForm() {
        Log.d(TAG,"---getting cluster to start from");
        mRef2 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTER_TO_START_FROM);
        mRef2.addListenerForSingleValueEvent(val2);
    }

    ValueEventListener val2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            int clusterGotten;
            if(dataSnapshot.hasChildren()){
                clusterGotten = dataSnapshot.getValue(int.class);
            }else{
                clusterGotten = 1;
            }
            if(clusterGotten == mClusterTotal){
                mClusterToStartFrom = 1;
            }else{
                mClusterToStartFrom = clusterGotten;
            }
            Log.d(TAG,"---Cluster to start from is -- "+mClusterToStartFrom);
            setAllOtherViewsToBeVisible();
            mAvi.setVisibility(View.GONE);

            OnClicks();
            Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.DoNotPayTwice,
                    Snackbar.LENGTH_INDEFINITE).show();
            mHasNumberBeenLoaded = true;
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            setAllOtherViewsToBeVisible();
            mAvi.setVisibility(View.GONE);
            Log.d(TAG,"---Unable to connect to firebase at the moment. "+databaseError.getMessage());
            Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.cannotUploadFailedFirebase,
                    Snackbar.LENGTH_LONG).show();
        }
    };

    private void OnClicks(){
        if(findViewById(R.id.chooseImageIcon)!=null){
            findViewById(R.id.chooseImageIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseImage();
                }
            });
        }
        if(findViewById(R.id.uploadIcon)!=null){
            findViewById(R.id.uploadAnAdIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadImage();
                }
            });
        }
    }

    private void chooseImage() {
        Log.d(TAG,"Starting intent for picking an image.");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            Log.d(TAG,"---Data gotten from activiy is ok.");
            mFilepath = data.getData();
            try{
                mCardviewForShowingPreviewOfAd.setVisibility(View.VISIBLE);
                mPlaceHolderImage.setVisibility(View.GONE);

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),mFilepath);
                mProfileImageViewPreview.setImageBitmap(bitmap);
                mHasUserChosenAnImage = true;

                Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.MakeSureImage,
                        Snackbar.LENGTH_INDEFINITE).show();
            }catch (IOException e){
                e.printStackTrace();
                Log.d(TAG,"---Unable to get and set image. "+e.getMessage());
            }
        }else{
            Log.d(TAG,"---Unable to work on the result code for some reason.");
        }

    }

    private void uploadImage() {
        if(!mHasUserChosenAnImage){
            Toast.makeText(mContext, R.string.pleaseChooseIcon,Toast.LENGTH_SHORT).show();
        }else{

        }

    }

    private void setAllOtherViewsToBeGone(){
        mPlaceHolderImage.setVisibility(View.GONE);
        mChoosingImage.setVisibility(View.GONE);
        mUploadButton.setVisibility(View.GONE);
    }

    private void setAllOtherViewsToBeVisible(){
        mPlaceHolderImage.setVisibility(View.VISIBLE);
        mChoosingImage.setVisibility(View.VISIBLE);
        mUploadButton.setVisibility(View.VISIBLE);
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }
}
