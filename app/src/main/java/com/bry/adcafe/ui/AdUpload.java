package com.bry.adcafe.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.fragments.FragmentModalBottomSheet;
import com.bry.adcafe.fragments.FragmentSelectPaymentOptionBottomSheet;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

import butterknife.Bind;
import butterknife.ButterKnife;

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
    @Bind(R.id.numberOfUsersToAdvertiseToLayout) RelativeLayout mNumberOfUsersToAdvertiseTo;
    @Bind(R.id.categoryText)TextView mCategoryText;
    @Bind(R.id.helpIcon) ImageView mHelpIcon;



    private boolean mHasNumberBeenLoaded;
    private boolean mHasUserChosenAnImage;
    private boolean mHasNumberBeenChosen;
    private boolean mHasUserPayed;

    private List<Integer> clustersToUpLoadTo = new ArrayList<>();
    private List<Integer> failedClustersToUploadTo = new ArrayList<>();
    private int mNumberOfClusters = 1;
    private int mClusterTotal;
    private int mClusterToStartFrom;
    private int noOfChildrenInClusterToStartFrom;
    private int noOfChildrenInLatestCluster;

    private DatabaseReference mRef;
    private DatabaseReference mRef2;
    private DatabaseReference mRef3;
    private DatabaseReference mRef4;
    private DatabaseReference mRef5;
    private DatabaseReference mRef6;
    private DatabaseReference boolRef;
    private String date;

    private Bitmap bm;
    private int cycleCount = 0;
    private static TextView tv;
    static Dialog d;
    private ImageButton b;

    private boolean uploading = false;
    private String pushrefInAdminConsole;
    private String mLink = "none";
    private String mCategory;
    private ProgressDialog mAuthProgressDialog;
    private int numberOfClustersBeingUploadedTo = 0;

    private int mAmountToPayPerTargetedView;
    private int mAmountPlusOurShare;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_upload);
        mContext = this.getApplicationContext();
        mHasUserChosenAnImage = false;
        mHasNumberBeenLoaded = false;
        ButterKnife.bind(this);

        mCategory = Variables.SelectedCategory;
        Log.d(TAG,"Category gotten from Variables class is : "+mCategory);
        mCategoryText.setText("Category: "+mCategory);

        mAmountToPayPerTargetedView = Variables.amountToPayPerTargetedView;
        mAmountPlusOurShare = Variables.amountToPayPerTargetedView+2;
        Log.d(TAG,"Amount to pay per targeted user is : "+ mAmountToPayPerTargetedView);
        Log.d(TAG,"Amount to pay per targeted user is : "+ mAmountPlusOurShare);

        setUpViews();
        createProgressDialog();
        startGetNumberOfClusters();
        setUpListeners();

    }

    private ChildEventListener chilForRefresh = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if(!uploading){
                resetAndRestart();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void loadListenerForRecreate() {
        mRef6 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTER_TO_START_FROM)
                .child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory+"_cluster_to_start_from");
        mRef6.addChildEventListener(chilForRefresh);
    }

    private void resetAndRestart(){
//        if(!clustersToUpLoadTo.isEmpty())clustersToUpLoadTo.clear();
//        mHasNumberBeenChosen = false;
//        startGetNumberOfClusters();
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
        //When restructuring to advertising to specific type of users,add .child("%AdvertCategory%");
        mRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.CLUSTERS_LIST)
                .child(Integer.toString(mAmountToPayPerTargetedView)).child(mCategory);
        mRef.addListenerForSingleValueEvent(val);

    }



    ValueEventListener val= new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            long numberOfClusters;
            if(dataSnapshot.getChildrenCount() == 0){
                numberOfClusters = 1;
            }else{
                numberOfClusters = dataSnapshot.getChildrenCount();
            }
            mClusterTotal = (int)numberOfClusters;
            Log.d(TAG,"---Number of clusters gotten from firebase is-- "+mClusterTotal);
            getClusterToStartForm();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
//            setAllOtherViewsToBeVisible();
            mAvi.setVisibility(View.GONE);
            mNoConnection.setVisibility(View.VISIBLE);
            mBottomNavs.setVisibility(View.GONE);
        }
    }; //triggers getClusterToStartFrom method.

    private void getClusterToStartForm() {
        Log.d(TAG,"---getting cluster to start from");
        //When changing to specific clusters, this will need to change from this to .getReference("%AdvertCategory%_cluster_to_start_from");
        mRef2 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTER_TO_START_FROM)
                .child(Integer.toString(mAmountToPayPerTargetedView)).child(mCategory+"_cluster_to_start_from");
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
            mClusterToStartFrom = clusterGotten;
            Log.d(TAG,"---Cluster to start from is -- "+mClusterToStartFrom);
            loadClusterToStartFromChildrenNo();

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
//            setAllOtherViewsToBeVisible();
            mNoConnection.setVisibility(View.VISIBLE);
            mBottomNavs.setVisibility(View.GONE);
            mAvi.setVisibility(View.GONE);
            Log.d(TAG,"---Unable to connect to firebase at the moment. "+databaseError.getMessage());
            Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.cannotUploadFailedFirebase,
                    Snackbar.LENGTH_LONG).show();
        }
    };

    private void loadClusterToStartFromChildrenNo() {
        Log.d(TAG,"---Starting query for no of ads in cluster to start from.");
        //When changing to targeted advertising,this will need to have .child("%AdvertCategory%") between getNextDay and clusterToStartFrom child;
        DatabaseReference boolRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay()).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory).child(Integer.toString(mClusterToStartFrom));
        boolRef.addListenerForSingleValueEvent(val3);
    }

    ValueEventListener val3 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            noOfChildrenInClusterToStartFrom = (int)dataSnapshot.getChildrenCount();
            Log.d(TAG,"--Number of children in cluster to start from gotten from firebase is  -"+noOfChildrenInClusterToStartFrom);
            getNumberOfUploadedAdsInLatestCluster();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
//            setAllOtherViewsToBeVisible();
            mNoConnection.setVisibility(View.VISIBLE);
            mBottomNavs.setVisibility(View.GONE);
            mAvi.setVisibility(View.GONE);
            Log.d(TAG,"---Unable to connect to firebase at the moment. "+databaseError.getMessage());
            Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.cannotUploadFailedFirebase,
                    Snackbar.LENGTH_LONG).show();
        }
    };

    private void getNumberOfUploadedAdsInLatestCluster(){
        Log.d(TAG,"---Starting query for no of ads in Latest cluster now.");
        //When changing to targeted advertising,this will need to have .child("%AdvertCategory%") between getNextDay and clusterTotal child;
        DatabaseReference boolRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay()).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory).child(Integer.toString(mClusterTotal));
        boolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    noOfChildrenInLatestCluster = (int)dataSnapshot.getChildrenCount();
                }else{
                    noOfChildrenInLatestCluster = 0;
                }
                Log.d(TAG,"---the number of children gotten is: "+noOfChildrenInLatestCluster);
                setAllOtherViewsToBeVisible();
                mAvi.setVisibility(View.GONE);
                mLoadingTextView.setVisibility(View.GONE);
                OnClicks();
//                loadListenerForRecreate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                setAllOtherViewsToBeVisible();
                mNoConnection.setVisibility(View.VISIBLE);
                mBottomNavs.setVisibility(View.GONE);
                mAvi.setVisibility(View.GONE);
                Log.d(TAG,"---Unable to connect to firebase at the moment. "+databaseError.getMessage());
                Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.cannotUploadFailedFirebase,
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }



    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mRef!=null) mRef.removeEventListener(val);
        if(mRef2!=null) mRef2.removeEventListener(val2);
        if(boolRef!=null) boolRef.removeEventListener(val3);
        if(mRef6!=null) mRef6.removeEventListener(chilForRefresh);
        removeListeners();
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

        mHelpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdUpload.this,TutorialAdvertisers.class);
                startActivity(intent);
            }
        });

        if(findViewById(R.id.WebsiteIcon)!=null){
            findViewById(R.id.WebsiteIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    promptUserForLink();
                }
            });
        }

        if(findViewById(R.id.progressBarTimerExample)!=null){
            findViewById(R.id.progressBarTimerExample).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("bryonyoni@gmail.com")) {
                        if (isOnline(mContext)) {
                            if (bm != null) {
                                uploadImageAsAnnouncement();
                            } else {
                                Toast.makeText(mContext, "Please choose your image again.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, "Check your internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return false;
                }
            });
        }
        if(findViewById(R.id.uploadIcon)!=null){
            findViewById(R.id.uploadIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mHasNumberBeenChosen){
                        Toast.makeText(mContext,"You may need to choose number of users to advertise to first!",Toast.LENGTH_LONG).show();
                    }else if(!mHasUserChosenAnImage){
                        Toast.makeText(mContext, R.string.pleaseChooseIcon,Toast.LENGTH_SHORT).show();
                    }else if(!isOnline(mContext)){
                        Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.UploadAdNoConnection,
                            Snackbar.LENGTH_LONG).show();
                    }else{
                        if(bm!=null && !uploading){
                            //This method will begin the process for uploading ad;
                            showDialogForPayments();
                        }else{
                            Toast.makeText(mContext,"Please choose your image again.",Toast.LENGTH_LONG).show();
                        }
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
                    showNumberPicker();
                }
            });
        }
        if(findViewById(R.id.reupload)!=null){
            findViewById(R.id.reupload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(Integer failedCluster : failedClustersToUploadTo){
                        clustersToUpLoadTo.add(failedCluster);
                    }
                    failedClustersToUploadTo.clear();
                    if(bm!=null){
                        setAllOtherViewsToBeGone();
                        mAvi.setVisibility(View.VISIBLE);
                        mLoadingTextView.setVisibility(View.VISIBLE);
                        mLoadingTextView.setText(R.string.uploadMessage);
                        setNewValueToStartFrom();

                        uploadImage(bm);
                    }else{
                        Toast.makeText(mContext,"Please choose your image again.",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void setUpListeners(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForAddingToSharedPreferences,
                new IntentFilter("PROCEED_CARD_DETAILS_PART"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForStartPayments,
                new IntentFilter("START_PAYMENTS_INTENT"));
    }

    private void removeListeners(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForAddingToSharedPreferences);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForStartPayments);
    }



    //////This method will start the upload process.Call it when your done with the payments....
    private void startProcessForUpload(){
        mAuthProgressDialog.show();
        uploading = true;
        setNewValueToStartFrom();
        date = getNextDay();
        numberOfClustersBeingUploadedTo = clustersToUpLoadTo.size();
        recheckNoOfChildrenInClulsterToStartFrom();
    }
    //Remember to comment out the one being called in the onClick listener (Line 420)////////

    private void promptUserForLink() {
        final Dialog d = new Dialog(AdUpload.this);
        d.setTitle("Ad any relevant link.");
        d.setContentView(R.layout.dialog5);
        Button b1 = d.findViewById(R.id.cancelBtn);
        Button b2 =  d.findViewById(R.id.buttonOk);
        final EditText e =  d.findViewById(R.id.editText);
        if(mLink.equals("none")) e.setText("");
        else e.setText(mLink);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(e.getText().toString().equals("")) {
                    findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
                    mLink = "none";
                }else{
                    mLink = e.getText().toString();
                    findViewById(R.id.smallDot).setVisibility(View.VISIBLE);
                    Log.d(TAG,"Link gotten is :---"+mLink);
                }
                d.dismiss();
            }
        });
        d.show();
    }

    private void showDialogForPayments() {
        showMessageBeforeBottomsheet();
    }

    private BroadcastReceiver mMessageReceiverForAddingToSharedPreferences = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast has been received show bottomsheet.");
            showBottomSheetFragment();
        }
    };

    private BroadcastReceiver mMessageReceiverForStartPayments = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast has been received show bottomsheet.");
            startPayments();
        }
    };




    private void startPayments() {
        Toast.makeText(mContext,"Payments should start",Toast.LENGTH_SHORT).show();
        String cardNumber = Variables.cardNumber;
        String expiration = Variables.expiration;
        String cvv = Variables.cvv;
        String postalCode = Variables.postalCode;
        double amount = Variables.amountToPayForUpload;
        startProcessForUpload();
    }

    private void showMessageBeforeBottomsheet(){
        FragmentSelectPaymentOptionBottomSheet fragmentModalBottomSheet = new FragmentSelectPaymentOptionBottomSheet();
        fragmentModalBottomSheet.setActivity(AdUpload.this);
        fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");
    }

    private void showBottomSheetFragment(){
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FragmentModalBottomSheet fragmentModalBottomSheet = new FragmentModalBottomSheet();

        fragmentModalBottomSheet.setDetails((mNumberOfClusters*Constants.NUMBER_OF_USERS_PER_CLUSTER),
                mAmountPlusOurShare, getNextDay(), mCategory,userEmail,Variables.userName);
        fragmentModalBottomSheet.setActivity(AdUpload.this);

        fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");
    }



    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Log.i(TAG,"----value is"+" "+newVal);

    }

    public void showNumberPicker() {
        final Dialog d = new Dialog(AdUpload.this);
        d.setTitle("Targeted people no.");
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
                tv.setText(String.valueOf(np.getValue()*Constants.NUMBER_OF_USERS_PER_CLUSTER));
                mHasNumberBeenChosen = true;
                mNumberOfClusters = np.getValue();
//                addToClusterListToUploadTo(mNumberOfClusters);
                mNumberOfUsersToAdvertiseTo.setVisibility(View.VISIBLE);
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
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "---Data gotten from activity is ok.");
            if (data.getData() != null) {
                mFilepath = data.getData();
                try {
                    mCardviewForShowingPreviewOfAd.setVisibility(View.VISIBLE);
                    mTopBarPreview.setVisibility(View.VISIBLE);
                    mHasUserChosenAnImage = true;

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilepath);
                    bm = bitmap;
                    Glide.with(mContext).load(bitmapToByte(bitmap)).asBitmap().override(400, 300).into(mProfileImageViewPreview);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "---Unable to get and set image. " + e.getMessage());
                }
            } else {
                Log.d(TAG, "---Unable to work on the result code for some reason.");
                Toast.makeText(mContext,"the data.getData method returns null for some reason...",Toast.LENGTH_SHORT).show();
            }
        }

    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private String encodeBitmapForFirebaseStorage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return imageEncoded;
    }

    private void uploadImageAsAnnouncement(){
        if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("bryonyoni@gmail.com")){
            Toast.makeText(mContext,"Uploading announcement to firebase",Toast.LENGTH_SHORT).show();
            String encodedImageToUpload = encodeBitmapForFirebaseStorage(bm);

            DatabaseReference dba = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS).child(getNextDay());
            DatabaseReference pushRef = dba.push();
            String key = pushRef.getKey();

            Advert announcement = new Advert(encodedImageToUpload);
            announcement.setNumberOfTimesSeen(0);
            announcement.setNumberOfUsersToReach(1000);
            announcement.setPushRefInAdminConsole(key);
            announcement.setUserEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            announcement.setWebsiteLink(mLink);
            announcement.setHasBeenReimbursed(false);
            announcement.setDateInDays(getDateInDays());
            announcement.setCategory("technology");
            announcement.setPushId(key);

            pushRef.setValue(announcement).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(mContext,"Announcement Uploaded.",Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext,"Announcement has failed to upload.",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void recheckNoOfChildrenInClulsterToStartFrom(){
        DatabaseReference boolRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay()).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory).child(Integer.toString(mClusterToStartFrom));
        boolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noOfChildrenInClusterToStartFrom = (int)dataSnapshot.getChildrenCount();
                recheckNoOfChildrenInLatestClusters();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recheckNoOfChildrenInLatestClusters(){
        DatabaseReference boolRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay()).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory).child(Integer.toString(mClusterTotal));
        boolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    noOfChildrenInLatestCluster = (int)dataSnapshot.getChildrenCount();
                }else{
                    noOfChildrenInLatestCluster = 0;
                }
                uploadImageToManagerConsole();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void uploadImageToManagerConsole() {
        String encodedImageToUpload = encodeBitmapForFirebaseStorage(bm);
        addToClusterListToUploadTo(mNumberOfClusters);
        Log.d(TAG, "Uploading Ad to AdminConsole.");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE).child(getNextDay());
        DatabaseReference pushRef = adRef.push();
        String pushId = pushRef.getKey();

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getNextDay());
        DatabaseReference pushRef2 = adRef2.push();
        pushRef2.setValue(pushId);

        Advert advert = new Advert(encodedImageToUpload);
        advert.setNumberOfTimesSeen(0);
        advert.setNumberOfUsersToReach(mNumberOfClusters*Constants.NUMBER_OF_USERS_PER_CLUSTER);
        advert.setPushRefInAdminConsole(pushId);
        advert.setUserEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        advert.setWebsiteLink(mLink);
        advert.setAmountToPayPerTargetedView(mAmountPlusOurShare);
        advert.setHasBeenReimbursed(false);
        advert.setDateInDays(getDateInDays());
//        advert.setClustersToUpLoadTo(clustersToUpLoadTo);
        advert.setCategory(mCategory);

        pushrefInAdminConsole = pushId;
        pushRef.setValue(advert).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               uploadImage(bm);
            }
        });
    }

    private void createProgressDialog(){
        mAuthProgressDialog = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mAuthProgressDialog.setTitle("AdCafe.");
//        mAuthProgressDialog.setMessage("Uploading your ad... "+0+"%");
        mAuthProgressDialog.setMessage("Uploading your ad... ");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mAuthProgressDialog.setIndeterminate(true);
//        mAuthProgressDialog.setProgress(0);
    }

    private void uploadImage(final Bitmap bm) {
//        String encodedImageToUpload = encodeBitmapForFirebaseStorage(bm);
        uploading = true;
        try{
            mRef6.removeEventListener(chilForRefresh);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(clustersToUpLoadTo.size()>10){
            for(int i = 0; i < 10; i++){
                String pushId;
                final Integer clusterNumber = clustersToUpLoadTo.get(i);
                if(clusterNumber<mClusterToStartFrom){
                    //push id is set to +2 to avoid setting the same push ID twice.
                    pushId = Integer.toString(noOfChildrenInClusterToStartFrom+2);
                }else{
                    if(clusterNumber==mClusterTotal){
                        //The latest cluster may have fewer children than other cluster, thus should be handled differently.
                        pushId = Integer.toString(noOfChildrenInLatestCluster+1);
                    }else{
                        pushId = Integer.toString(noOfChildrenInClusterToStartFrom+1);
                    }
                }
                Log.d(TAG,"---Uploading encoded image to cluster -"+clusterNumber+" now...");
                Log.d(TAG,"---The custom push id is ---"+pushId);
                //When configuring for targeted advertising based on user prefs, this will change by add ing .child("%AdvertCategory%") between
                //date and cluster number.
                mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(date)
                        .child(Integer.toString(mAmountToPayPerTargetedView))
                        .child(mCategory)
                        .child(Integer.toString(clusterNumber)).child(pushId);
//                Advert advert = new Advert(encodedImageToUpload);
                Advert advert = new Advert();
                advert.setPushId(pushId);
                advert.setWebsiteLink(mLink);
                advert.setCategory(mCategory);
                advert.setFlagged(false);
                advert.setAdminFlagged(false);
                advert.setPushRefInAdminConsole(pushrefInAdminConsole);
                setClusterInAdminAdvert(clusterNumber,Integer.parseInt(pushId));
                mRef3.setValue(advert).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        cycleCount++;
                        clustersToUpLoadTo.remove(clusterNumber);
//                        mAuthProgressDialog.setProgress((clustersToUpLoadTo.size()/numberOfClustersBeingUploadedTo)*100);
//                        int percentage = (clustersToUpLoadTo.size()/numberOfClustersBeingUploadedTo)*100;
//                        String message ="Uploading your ad... "+percentage+"%";
//                        mAuthProgressDialog.setMessage(message);
                        if(clustersToUpLoadTo.isEmpty()){
                            if(!failedClustersToUploadTo.isEmpty()){
                                checkAndNotifyAnyFailed();
                            }else{
//                                mAvi.setVisibility(View.GONE);
//                                mLoadingTextView.setVisibility(View.GONE);
//                                setAllOtherViewsToBeVisible();
                                mAuthProgressDialog.dismiss();
                                Log.d(TAG,"---Ad has been successfully uploaded to one of the clusters in firebase");
//                                setHasPayedInFirebaseToFalse();
                                cycleCount = 1;
                                uploading = false;
                                startDashboardActivity();
                            }
                        }else{
                            if(cycleCount == 10){
                                cycleCount = 0;
                                uploadImage(bm);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        failedClustersToUploadTo.add(clusterNumber);
                        cycleCount++;
                        clustersToUpLoadTo.remove(clusterNumber);
                        if(clustersToUpLoadTo.isEmpty()){
                            checkAndNotifyAnyFailed();
                        }
                    }
                });
            }
        }else{
            for(final Integer number : clustersToUpLoadTo){
                Log.d(TAG,"---Uploading encoded image to cluster -"+number+" now...");
                String pushId;
                if(number<mClusterToStartFrom){
                    //push id is set to +2 to avoid setting the same push ID twice.
                    pushId = Integer.toString(noOfChildrenInClusterToStartFrom+2);
                }else{
                    if(number==mClusterTotal){
                        //The latest cluster may have fewer children than other cluster, thus should be handled differently.
                        pushId = Integer.toString(noOfChildrenInLatestCluster+1);
                    }else{
                        pushId = Integer.toString(noOfChildrenInClusterToStartFrom+1);
                    }
                }
                Log.d(TAG,"---The custom push id is ---"+pushId);
                //When configuring for targeted advertising based on user prefs, this will change by add ing .child("%AdvertCategory%") between
                //date and cluster number.
                mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(date)
                        .child(Integer.toString(mAmountToPayPerTargetedView))
                        .child(mCategory)
                        .child(Integer.toString(number)).child(pushId);
//                Advert advert = new Advert(encodedImageToUpload);
                Advert advert = new Advert();
                advert.setPushId(pushId);
                advert.setWebsiteLink(mLink);
                advert.setCategory(mCategory);
                advert.setFlagged(false);
                advert.setAdminFlagged(false);
                advert.setPushRefInAdminConsole(pushrefInAdminConsole);
                setClusterInAdminAdvert(number,Integer.parseInt(pushId));
                mRef3.setValue(advert).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        cycleCount++;
                        clustersToUpLoadTo.remove(number);
//                        mAuthProgressDialog.setProgress((clustersToUpLoadTo.size()/numberOfClustersBeingUploadedTo)*100);
//                        int percentage = (clustersToUpLoadTo.size()/numberOfClustersBeingUploadedTo)*100;
//                        String message ="Uploading your ad... "+percentage+"%";
//                        mAuthProgressDialog.setMessage(message);
                        if(clustersToUpLoadTo.isEmpty()){
                            if(!failedClustersToUploadTo.isEmpty()){
                                checkAndNotifyAnyFailed();
                            }else{
//                                mAvi.setVisibility(View.GONE);
//                                mLoadingTextView.setVisibility(View.GONE);
//                                setAllOtherViewsToBeVisible();
                                mAuthProgressDialog.dismiss();
                                Log.d(TAG,"---Ad has been successfully uploaded to one of the clusters in firebase");
//                                setHasPayedInFirebaseToFalse();
                                cycleCount = 1;
                                uploading = false;
                                startDashboardActivity();
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        failedClustersToUploadTo.add(number);
                        cycleCount++;
                        clustersToUpLoadTo.remove(number);
                        if(clustersToUpLoadTo.isEmpty()){
                            checkAndNotifyAnyFailed();
                        }
                    }
                });
            }
        }


    }



    private void setClusterInAdminAdvert(int cluster,int pushId){
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getNextDay())
                .child(pushrefInAdminConsole)
                .child("clustersToUpLoadTo")
                .child(Integer.toString(cluster));
        adRef.setValue(pushId);
    }

    private void checkAndNotifyAnyFailed() {
        if(!clustersToUpLoadTo.isEmpty()){
//           Toast.makeText(mContext,"Upload process is incomplete.",Toast.LENGTH_LONG).show();
            mAvi.setVisibility(View.GONE);
            mLoadingTextView.setVisibility(View.GONE);
            setAllOtherViewsToBeVisible();
            findViewById(R.id.reupload).setVisibility(View.VISIBLE);

            final Dialog d = new Dialog(AdUpload.this);
            d.setTitle("Upload incomplete");
            d.setContentView(R.layout.dialog2);
            Button b1 = (Button) d.findViewById(R.id.buttonOk);
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
            d.show();
        }else{
            findViewById(R.id.reupload).setVisibility(View.GONE);
        }
    }

    private void startDashboardActivity() {
        final Dialog d = new Dialog(AdUpload.this);
        d.setTitle("Upload complete");
        d.setContentView(R.layout.dialog4);
        Button b2 =  d.findViewById(R.id.buttonOk);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                finish();
            }
        });
        d.show();

    }



    private void setNewValueToStartFrom() {
        mRef4 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTER_TO_START_FROM).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory+"_cluster_to_start_from");
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

    private void addToClusterListToUploadToTest(int number){
        Log.d(TAG,"The number of total clusters is : "+5000);
        Log.d(TAG,"The cluster to start from is : "+1);
        Log.d(TAG,"Number of clusters to upload to is : "+mNumberOfClusters);

        int numberOfClustersToStartFrom = 1;
        int clusterTotal = 5000;

        for(int i = 0; i < 5000; i++){
            if(numberOfClustersToStartFrom+i>clusterTotal){
                clustersToUpLoadTo.add(numberOfClustersToStartFrom+i-(clusterTotal));
                Log.d(TAG,"Limit has been exceeded.setting cluster to upload to : "+(numberOfClustersToStartFrom+i-(clusterTotal)));
            }else{
                clustersToUpLoadTo.add(numberOfClustersToStartFrom+i);
                Log.d(TAG,"Adding cluster to list normally : "+(numberOfClustersToStartFrom+i));
            }
        }

    }

    private void setHasPayedInFirebaseToFalse() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        mRef5 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants. HAS_USER_MADE_PAMENTS);
        mRef5.setValue(false);
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
        mNumberOfUsersToAdvertiseTo.setVisibility(View.INVISIBLE);
        mHelpIcon.setVisibility(View.GONE);
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
        mHelpIcon.setVisibility(View.VISIBLE);
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

    @Override
    public void onBackPressed(){
        if(uploading){
            final Dialog d = new Dialog(AdUpload.this);
            d.setTitle("Upload incomplete");
            d.setContentView(R.layout.dialog3);
            Button b1 = (Button) d.findViewById(R.id.buttonYes);
            Button b2 = (Button) d.findViewById(R.id.buttonNo);
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDashboardActivity();
                    d.dismiss();
                }
            });
            b2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
            d.show();
        }else{
            super.onBackPressed();
            finish();
        }

    }

    private Long getDateInDays(){
        long currentTimeMillis = System.currentTimeMillis();
        long extraTimeFromMidnight = currentTimeMillis%(1000*60*60*24);
//        long currentDay = (currentTimeMillis-extraTimeFromMidnight)/(1000*60*60*24);
        long currentDay = (currentTimeMillis)/(1000*60*60*24);
        Log.d(TAG,"The current day is : "+currentDay);
        return currentDay;
    }

}
