package com.bry.adcafe.ui;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.BlankItem;
import com.bry.adcafe.adapters.DateItem;
import com.bry.adcafe.adapters.SavedAdsCard;
import com.bry.adcafe.fragments.ViewImageFragment;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
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
    private ProgressDialog mAuthProgressDialog;
    private ProgressDialog mProg;

    private int cycleCount = 0;
    private LinkedHashMap<Long,List> HashOfAds = new LinkedHashMap<>();
    private boolean isDone = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        mContext = getApplicationContext();

        loadPlaceHolderViews();
        registerReceivers();
        createProgressDialog();

        if(isNetworkConnected(mContext)){
//            loadAdsFromFirebase2();
            new LongOperation().execute("");
        }else{
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.connectionDropped,
                    Snackbar.LENGTH_INDEFINITE).show();
        }

    }


    @Override
    protected void onDestroy(){
        mPlaceHolderView.removeAllViews();
        hideProg();
        unregisterAllReceivers();
        super.onDestroy();
    }

    private void unregisterAllReceivers() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpinned);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForReceivingUnableToPinAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSharingAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForViewingAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowingAreYouSureText);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowingAreYouSureText2);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForContinue);

        Intent intent = new Intent("UNREGISTER");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForUnpinned,new IntentFilter(Constants.REMOVE_PINNED_AD));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForReceivingUnableToPinAd,new IntentFilter(Constants.UNABLE_TO_REMOVE_PINNED_AD));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSharingAd,new IntentFilter("SHARE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForViewingAd,new IntentFilter("VIEW"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowingAreYouSureText,new IntentFilter("ARE_YOU_SURE_INTENT"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowingAreYouSureText2,new IntentFilter("ARE_YOU_SURE2"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForContinue,new IntentFilter("DONE!!"));

    }


    private void showProg(){
        mProg.show();
    }

    private void hideProg(){
        mProg.hide();
    }

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
            mAuthProgressDialog.dismiss();
        }
    };

    private BroadcastReceiver mMessageReceiverForReceivingUnableToPinAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Unable to unpin ad.");
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.failedUnpinned,
                    Snackbar.LENGTH_SHORT).show();
            mAuthProgressDialog.dismiss();
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

    private BroadcastReceiver mMessageReceiverForShowingAreYouSureText = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to prompt user if they're sure they want to delete.");
            promptUserIfTheyAreSureIfTheyWantToDeleteAd();
        }
    };

    private BroadcastReceiver mMessageReceiverForShowingAreYouSureText2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to prompt user if they're sure they want to delete.");
            promptUserIfTheyAreSureIfTheyWantToDeleteAd2();
        }
    };

    private BroadcastReceiver mMessageReceiverForContinue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to continue");
            startLoadAdsIntoViews();
        }
    };

    private void promptUserIfTheyAreSureIfTheyWantToDeleteAd2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to unpin that?")
                .setCancelable(true)
                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Variables.adToBeUnpinned.getPushRefInAdminConsole());
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        mAuthProgressDialog.show();
                    }
                })
                .setNegativeButton("No!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void promptUserIfTheyAreSureIfTheyWantToDeleteAd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to unpin that?")
                .setCancelable(true)
                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Intent intent = new Intent("DELETE_PINNED_AD");
//                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        Intent intent2 = new Intent(Variables.adToBeViewed.getPushRefInAdminConsole());
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
                        Variables.placeHolderView = mPlaceHolderView;
                    }
                })
                .setNegativeButton("No!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void loadAdFragment() {
        FragmentManager fm = getFragmentManager();
        ViewImageFragment imageFragment = new ViewImageFragment();
        imageFragment.setMenuVisibility(false);
        imageFragment.show(fm, "View image.");
        imageFragment.setfragcontext(mContext);
    }

    private void createProgressDialog(){
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("AdCafe.");
        mAuthProgressDialog.setMessage("Updating your preferences...");
        mAuthProgressDialog.setCancelable(false);

        mProg = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mProg.setMessage("Loading your Pins...");
        mProg.setTitle("AdCafe.");
        mProg.setCancelable(false);
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

    private void loadAdsFromFirebase2(){
        String uid = User.getUid();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST);
        DatabaseReference mRef = query.getRef();

        mRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for (final DataSnapshot snap: dataSnapshot.getChildren()){
                        final long noOfDays = Long.parseLong(snap.getKey());
                        final List<Advert> AdList = new ArrayList<>();

                        for(DataSnapshot adSnap: snap.getChildren()){
                            Advert advert = adSnap.getValue(Advert.class);
                            AdList.add(advert);
                            Log.d("BOOKMARKS"," --Loaded ads from firebase.--"+advert.getPushId());
                        }
                        HashOfAds.put(noOfDays,AdList);
                        Log.d(TAG,"Added ads for day : "+noOfDays+" to hashmap.Adlist size is : "+AdList.size());
                    }
                    isDone = true;
//                    startLoadAdsIntoViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Bookmarks","Failed to load ads from firebase.");
            }
        });
    }

    private void startLoadAdsIntoViews(){
        if(HashOfAds.isEmpty()){
            Toast.makeText(mContext,"You do not have any pinned ads.",Toast.LENGTH_SHORT).show();
            noAdsText.setVisibility(View.VISIBLE);
            mAvi.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
        }else{
            if(cycleCount+1<HashOfAds.size()) {
                Long days = getDaysFromHash(cycleCount);
                List adList = HashOfAds.get(days);
                loadDaysAdsIntoViews(adList, days);
            }else{
                hideProg();
            }

        }
    }

    private void loadDaysAdsIntoViews(List<Advert> adList, long noOfDays) {
        if(mPlaceHolderView == null) loadPlaceHolderViews();
        mPlaceHolderView.addView(new DateItem(mContext,mPlaceHolderView,noOfDays,getDateFromDays(noOfDays)));
        mPlaceHolderView.addView(new BlankItem(mContext,mPlaceHolderView,noOfDays,"",false));
        mPlaceHolderView.addView(new BlankItem(mContext,mPlaceHolderView,noOfDays,"",false));
        Log.d(TAG,"Adlist size for "+noOfDays+" is: "+adList.size());
        for(int i = 0; i<adList.size();i++){
            boolean islst = false;
            if(i+1==adList.size()) islst = true;
            mPlaceHolderView.addView(new SavedAdsCard(adList.get(i),mContext,mPlaceHolderView,adList.get(i).getPushId(),noOfDays,islst));
            Log.d(TAG,"Loaded ad : "+adList.get(i).getPushId()+"; isLast item is : "+islst);
        }
        for(int i = 0;i<getNumber(adList.size());i++){
            boolean islst = false;
            if(i+1==getNumber(adList.size())) islst = false;
            mPlaceHolderView.addView(new BlankItem(mContext,mPlaceHolderView,noOfDays,"pineapples",islst));
            Log.d(TAG,"Loaded a blank item for :"+getDateFromDays(noOfDays)+"; isLast item is : "+islst);
        }
        cycleCount++;
    }

    private void loadBookmarkedAdsIntoCards() {
        if(mPlaceHolderView == null){
            loadPlaceHolderViews();
        }
        if(mSavedAds!=null && mSavedAds.size()>0){
            for(int i = 0; i<mSavedAds.size();i++){
//                mPlaceHolderView.addView(new SavedAdsCard(mSavedAds.get(i),mContext,mPlaceHolderView,mSavedAds.get(i).getPushId()));
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

    private String getDateFromDays(long days){
        long currentTimeInMills = -days*(1000*60*60*24);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

//        String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
        String monthName = getMonthName_Abbr(monthOfYear);

        Log.d("Splash","Date gotten is : "+dayOfMonth+" "+monthName+" "+year);

        Calendar cal2 = Calendar.getInstance();
        int year2 = cal2.get(Calendar.YEAR);
        String yearName;

        if(year == year2){
            Log.d(TAG,"Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log.d(TAG,"Ad was pined last year...");
            yearName =", "+Integer.toString(year);
        }else{
            yearName =", "+ Integer.toString(year);
        }

        return dayOfMonth+" "+monthName+yearName;
    }




    public String getMonthName_Abbr(int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        return month_name;
    }

    private int getNumber(int size){
        int newSize = size;
        int number = 0;
        while (newSize%3!=0){
            newSize++;
            number++;
        }

        return number;
    }

    private Long getDaysFromHash(int pos){
        LinkedHashMap map = HashOfAds;
        Long Sub = (new ArrayList<Long>(map.keySet())).get(pos);
        Log.d(TAG, "Date gotten from getDaysFromHash method is :" + Sub);
        return Sub;
    }

    private class LongOperation extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            try{
                loadAdsFromFirebase2();
                while(!isDone){
                    executeStuff();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            startLoadAdsIntoViews();
//            hideProg();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProg();
        }
    }

    private void executeStuff() {

    }


}
