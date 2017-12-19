package com.bry.adcafe.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.SelectCategoryItem;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.DatabaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SelectCategory extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = SelectCategory.class.getSimpleName();
    @Bind(R.id.submitCategoriesButton) Button mSubmitButton;
    @Bind(R.id.selectCategoriesLayout) LinearLayout mainView;
    @Bind(R.id.failedLoadLayout) LinearLayout failedToLoadLayout;
    @Bind(R.id.retryLoading) Button retryLoadingButton;
    @Bind(R.id.loadingLayout) LinearLayout loadingLayout;
    @Bind(R.id.categoryPlaceHolderView) PlaceHolderView placeHolderView;
    private Context mContext;
    private boolean isUserInActivity;
    private boolean hasDataBeenLoaded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);
        ButterKnife.bind(this);

        mContext = this.getApplicationContext();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedCreatingUserSubscriptionList, new IntentFilter(Constants.SET_UP_USERS_SUBSCRIPTION_LIST));
        if (isOnline(mContext)) {
            loadCategoriesFromFirebase();
        }else{
            mainView.setVisibility(View.GONE);
            failedToLoadLayout.setVisibility(View.VISIBLE);
        }
        mSubmitButton.setOnClickListener(this);
        retryLoadingButton.setOnClickListener(this);
    }

    private void loadCategoriesFromFirebase() {
        failedToLoadLayout.setVisibility(View.GONE);
        mainView.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.CATEGORY_LIST);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    String category = snap.getKey();
                    String details = snap.getValue(String.class);
                    placeHolderView.addView(new SelectCategoryItem(mContext,placeHolderView,category,details,false));
                }
                loadingLayout.setVisibility(View.GONE);
                mainView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                failedToLoadLayout.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.GONE);
            }
        });
    }


    @Override
    protected void onResume() {
        if(hasDataBeenLoaded) startMainActivity();
        isUserInActivity = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isUserInActivity = false;
        super.onPause();
    }

    @Override
    protected  void onDestroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedCreatingUserSubscriptionList);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v==mSubmitButton) {
            if (Variables.selectedCategoriesToSubscribeTo.size() == 0) {
                Snackbar.make(findViewById(R.id.select_Categories), "At least choose one category.",
                        Snackbar.LENGTH_LONG).show();
            }else{
                if(isOnline(mContext)) {
                    mainView.setVisibility(View.GONE);
                    loadingLayout.setVisibility(View.VISIBLE);
                    hasDataBeenLoaded = false;
                    new DatabaseManager().setUpUserSubscriptions(Variables.selectedCategoriesToSubscribeTo);
                }else{
                    Toast.makeText(mContext,"To continue,you need an internet connection",Toast.LENGTH_SHORT).show();
                }
            }
        }else if(v==retryLoadingButton){
            if(isOnline(mContext)) {
                loadCategoriesFromFirebase();
            }else{
                Toast.makeText(mContext,"To continue,you need an internet connection",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BroadcastReceiver mMessageReceiverForFinishedCreatingUserSubscriptionList = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished creating user subscription list");
            hasDataBeenLoaded = true;
            if(isUserInActivity) startMainActivity();
        }
    };

    private void startMainActivity(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        User.setUid(uid);
        Variables.setCurrentSubscriptionIndex(0);
        Variables.setCurrentAdInSubscription(0);
        Variables.setAdTotal(0,"");
        Variables.setMonthAdTotals("",0);
        Variables.isStartFromLogin = true;
        loadingLayout.setVisibility(View.GONE);
        Intent intent = new Intent(SelectCategory.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }
}
