package com.bry.adcafe.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.models.Advert;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdUpload extends AppCompatActivity implements NumberPicker.OnValueChangeListener{
    public static final String TAG = AdUpload.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri mFilepath;

    private Context mContext;
    private ImageView mUploadButton;
    private ImageView mChoosingImage;
    private ImageView mProfileImageViewPreview;
    private CardView mCardviewForShowingPreviewOfAd;
    private LinearLayout mTopBarPreview;
    private AVLoadingIndicatorView mAvi;
    private TextView mLoadingTextView;
    private TextView mSelectText;
    private TextView mUploadText;
    private LinearLayout mNoConnection;
    private LinearLayout mBottomNavs;
    private TextView mNumberOfUsersChosenText;


    private boolean mHasNumberBeenLoaded;
    private boolean mHasUserChosenAnImage;
    private boolean mHasNumberBeenChosen;
    private boolean mHasUserPayed;

    private List<Integer> clustersToUpLoadTo = new ArrayList<>();
    private int mNumberOfClusters = 1;
    private int mClusterTotal;
    private int mClusterToStartFrom;

    private DatabaseReference mRef;
    private DatabaseReference mRef2;
    private DatabaseReference mRef3;
    private DatabaseReference mRef4;
    private DatabaseReference mRef5;
    private DatabaseReference boolRef;

    private Bitmap bm;
    private int cycleCount = 1;
    private static TextView tv;
    static Dialog d;
    private ImageButton b;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_upload);
        mContext = this.getApplicationContext();
        mHasUserChosenAnImage = false;
        mHasNumberBeenLoaded = false;


        setUpViews();
        startGetNumberOfClusters();
    }



    private void startGetNumberOfClusters(){
        if(isOnline(mContext)){
            getNumberOfClusters();//in-turn triggers the getClusterToStartFrom method.
        }else{
            Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.UploadAdNoConnection,
                    Snackbar.LENGTH_LONG).show();
            mNoConnection.setVisibility(View.VISIBLE);
            mBottomNavs.setVisibility(View.GONE);
        }
    }

    private void setUpViews() {
        mBottomNavs = (LinearLayout) findViewById(R.id.bottomNavs);
        mUploadButton = (ImageView) findViewById(R.id.uploadIcon);
        mChoosingImage = (ImageView) findViewById(R.id.chooseImageIcon);
        mProfileImageViewPreview = (ImageView) findViewById(R.id.profileImageViewPreview);
        mCardviewForShowingPreviewOfAd = (CardView) findViewById(R.id.cardviewForShowingPreviewOfAd);
        mTopBarPreview = (LinearLayout) findViewById(R.id.topBarPreview);
        mAvi = (AVLoadingIndicatorView) findViewById(R.id.AdUploadAvi);
        mLoadingTextView = (TextView) findViewById(R.id.loadingText);
        mSelectText = (TextView) findViewById(R.id.selectText);
        mUploadText = (TextView) findViewById(R.id.uploadText);
        mNoConnection = (LinearLayout) findViewById(R.id.noConnectionMessage);
        tv = (TextView) findViewById(R.id.numberOfUsersToAdvertiseTo);
        b = (ImageButton) findViewById(R.id.chooseNumberButton);
        mNumberOfUsersChosenText = (TextView) findViewById(R.id.chooseNumberText);
    }

    private void getNumberOfClusters() {
        mAvi.setVisibility(View.VISIBLE);
        mLoadingTextView.setVisibility(View.VISIBLE);
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
            loadHasUserPayedFromFirebase();

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

    private void loadHasUserPayedFromFirebase() {
        Log.d(TAG,"---Starting query for if user has payed to advertise.");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        DatabaseReference boolRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants. HAS_USER_MADE_PAMENTS);
        boolRef.addListenerForSingleValueEvent(val3);
    }

    ValueEventListener val3 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mHasUserPayed = dataSnapshot.getValue(boolean.class);
            Log.d(TAG,"--boolean gotten from firebase for if user has payed is -"+mHasUserPayed);
            setAllOtherViewsToBeVisible();
            mAvi.setVisibility(View.GONE);
            mLoadingTextView.setVisibility(View.GONE);
            addToClusterListToUploadTo(mNumberOfClusters);
            OnClicks();

            Toast.makeText(mContext, R.string.DoNotPayTwice,Toast.LENGTH_LONG).show();
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



    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mRef!=null) mRef.removeEventListener(val);
        if(mRef2!=null) mRef2.removeEventListener(val2);
        if(boolRef!=null) boolRef.removeEventListener(val3);
    }

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
            findViewById(R.id.uploadIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mHasNumberBeenChosen){
                        Toast.makeText(mContext,"You may need to choose number of users to advertise to first!",Toast.LENGTH_LONG).show();
                    }
//                    else if(!mHasUserPayed){
//                       Toast.makeText(mContext,"Please pay first.",Toast.LENGTH_SHORT).show();
//                   }
                   else{
                       uploadImage();
                   }
                }
            });
        }
        if(findViewById(R.id.profileImageViewPreview)!=null){
            findViewById(R.id.profileImageViewPreview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseImage();
                }
            });
        }

        if(findViewById(R.id.chooseNumberButton)!=null){
            findViewById(R.id.chooseNumberButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    show();
                }
            });
        }
    }

    private void showDialogForPayments() {
        buildTransactionForPayment();
    }

    private void buildTransactionForPayment() {

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Log.i(TAG,"----value is"+" "+newVal);

    }

    public void show() {

        final Dialog d = new Dialog(AdUpload.this);
        d.setTitle("No. of people");
        d.setContentView(R.layout.dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(mClusterTotal);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(String.valueOf(np.getValue()*1000));
                mHasNumberBeenChosen = true;
                mNumberOfClusters = np.getValue();
                findViewById(R.id.numberOfUsersToAdvertiseToLayout).setVisibility(View.VISIBLE);
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();

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
                mTopBarPreview.setVisibility(View.VISIBLE);
                mHasUserChosenAnImage = true;

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),mFilepath);
                bm = bitmap;
                Glide.with(mContext).load(bitmapToByte(bitmap)).asBitmap().override(400,300).into(mProfileImageViewPreview);

                Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.MakeSureImage,
                        Snackbar.LENGTH_LONG).show();
            }catch (IOException e){
                e.printStackTrace();
                Log.d(TAG,"---Unable to get and set image. "+e.getMessage());
            }
        }else{
            Log.d(TAG,"---Unable to work on the result code for some reason.");
        }

    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,90,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private String encodeBitmapForFirebaseStorage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return imageEncoded;
    }




    private void uploadImage() {
        if(!mHasUserChosenAnImage){
            Toast.makeText(mContext, R.string.pleaseChooseIcon,Toast.LENGTH_SHORT).show();
        }else if(!isOnline(mContext)){
            Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.UploadAdNoConnection,
                    Snackbar.LENGTH_LONG).show();
        }else{
            if(bm!=null){
                setAllOtherViewsToBeGone();
                mAvi.setVisibility(View.VISIBLE);
                mLoadingTextView.setVisibility(View.VISIBLE);
                mLoadingTextView.setText(R.string.uploadMessage);

                for(Integer number:clustersToUpLoadTo){
                    Log.d(TAG,"---Uploading encoded image to cluster -"+number+" now...");

                    String encodedImageToUpload = encodeBitmapForFirebaseStorage(bm);
                    mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(getNextDay()).child(Integer.toString(number));
                    Advert advert = new Advert(encodedImageToUpload);
                    DatabaseReference pushref= mRef3.push();
                    String pushID = pushref.getKey();
                    advert.setPushId(pushID);
                    pushref.setValue(advert).addOnSuccessListener(succ1).addOnFailureListener(fal);

                    clustersToUpLoadTo.remove(number);
                }
            }else{
                Toast.makeText(mContext,"Please choose your image again.",Toast.LENGTH_LONG).show();
            }
        }

    }

    OnSuccessListener succ1 = new OnSuccessListener() {
        @Override
        public void onSuccess(Object o) {
            if(cycleCount == mNumberOfClusters){
                setNewValueToStartFrom();
                mAvi.setVisibility(View.GONE);
                mLoadingTextView.setVisibility(View.GONE);
                setAllOtherViewsToBeVisible();

                Log.d(TAG,"---Ad has been successfully uploaded to one of the clusters in firebase");
                Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.successfullyUploaded,
                        Snackbar.LENGTH_LONG).show();
                setHasPayedInFirebaseToFalse();
                cycleCount = 1;
                bm = null;
                Toast.makeText(mContext, R.string.successfullyUploaded,Toast.LENGTH_LONG).show();
                startDashboardActivity();
            }else{
                cycleCount+=1;
            }

        }
    };

    private void startDashboardActivity() {
        Intent intent = new Intent(AdUpload.this,Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    OnFailureListener fal = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            mAvi.setVisibility(View.GONE);
            mLoadingTextView.setVisibility(View.GONE);
            setAllOtherViewsToBeVisible();

            Toast.makeText(mContext, R.string.unsuccessfullyUploaded,Toast.LENGTH_LONG).show();
            Log.d(TAG,"---Unable to upload ad. "+e.getMessage());
        }
    };




    private void setNewValueToStartFrom() {
        mRef4 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTER_TO_START_FROM);
        if(mClusterToStartFrom + mNumberOfClusters > mClusterTotal){
            mRef4.setValue((mClusterToStartFrom + mNumberOfClusters)-mClusterTotal);
        }else{
            mRef4.setValue(mClusterToStartFrom + mNumberOfClusters);
        }

    }

    private void addToClusterListToUploadTo(int number){
        Log.d(TAG,"The number of total clusters is : "+mClusterTotal);
        Log.d(TAG,"The cluster to start from is : "+mClusterToStartFrom);
        Log.d(TAG,"Number of clusters to upload to is : "+mNumberOfClusters);

        for(int i = 0; i < number; i++){
            if(mClusterToStartFrom+i>mClusterTotal){
                clustersToUpLoadTo.add(mClusterToStartFrom+i-(mClusterTotal));
                Log.d(TAG,"Limit has been exceeded.setting cluster to upload to : "+(mClusterToStartFrom+i-(mClusterTotal)));
            }else{
                clustersToUpLoadTo.add(mClusterToStartFrom+i);
                Log.d(TAG,"Adding cluster to list normally : "+(mClusterToStartFrom+i));
            }
        }

    }

    private void setHasPayedInFirebaseToFalse() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        mRef5 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants. HAS_USER_MADE_PAMENTS);
        mRef5.setValue(false);
    }

    private void setHasPayedInFirebaseToTrue(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        mRef5 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants. HAS_USER_MADE_PAMENTS);
        mRef5.setValue(true);
    }



    private void setAllOtherViewsToBeGone(){
        mChoosingImage.setVisibility(View.GONE);
        mUploadButton.setVisibility(View.GONE);
        mNumberOfUsersChosenText.setVisibility(View.GONE);
        b.setVisibility(View.GONE);
        mCardviewForShowingPreviewOfAd.setVisibility(View.GONE);
        mSelectText.setVisibility(View.GONE);
        mUploadText.setVisibility(View.GONE);
        mTopBarPreview.setVisibility(View.GONE);

    }

    private void setAllOtherViewsToBeVisible(){
        mChoosingImage.setVisibility(View.VISIBLE);
        mUploadButton.setVisibility(View.VISIBLE);
        mNumberOfUsersChosenText.setVisibility(View.VISIBLE);
        b.setVisibility(View.VISIBLE);
        mCardviewForShowingPreviewOfAd.setVisibility(View.VISIBLE);
        mSelectText.setVisibility(View.VISIBLE);
        mUploadText.setVisibility(View.VISIBLE);
        mTopBarPreview.setVisibility(View.VISIBLE);
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private String getDate(){
        long date = System.currentTimeMillis();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        String MonthString = sdfMonth.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        String dayString = sdfDay.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        String yearString = sdfYear.format(date);

        Calendar c = Calendar.getInstance();
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH));
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String todaysDate = (dayString+":"+MonthString+":"+yearString);

        return todaysDate;
    }

    private String getNextDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d(TAG,"Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }


}
