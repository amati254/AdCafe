package com.bry.adcafe.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.SelectCategoryAdvertiserItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SelectCategoryAdvertiser extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = SelectCategoryAdvertiser.class.getSimpleName();
    @Bind(R.id.selectCategoriesLayout) LinearLayout mainView;
    @Bind(R.id.failedLoadLayout) LinearLayout failedToLoadLayout;
    @Bind(R.id.retryLoading) Button retryLoadingButton;
    @Bind(R.id.loadingLayout) LinearLayout loadingLayout;
    @Bind(R.id.categoryPlaceHolderView) PlaceHolderView placeHolderView;
    private Context mContext;
    private Context acCont;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category_advertiser);
        ButterKnife.bind(this);
        mContext = this.getApplicationContext();
        acCont = SelectCategoryAdvertiser.this;
        if(isOnline(mContext)) loadCategoriesFromFirebase();
        else{
            mainView.setVisibility(View.GONE);
            failedToLoadLayout.setVisibility(View.VISIBLE);
        }
        retryLoadingButton.setOnClickListener(this);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSelectingCategory,
                new IntentFilter("SELECTED_CATEGORY"));

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
                    placeHolderView.addView(new SelectCategoryAdvertiserItem(mContext,placeHolderView,category,details));
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

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private BroadcastReceiver mMessageReceiverForSelectingCategory = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Selected category : "+ Variables.SelectedCategory);
            getAmountPerUser();
//            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private void startAdUpload() {
        Intent intent = new Intent(SelectCategoryAdvertiser.this, AdUpload.class);
        startActivity(intent);
        finish();
    }

    private void getAmountPerUser(){
        final Dialog d = new Dialog(acCont);
        d.setTitle("Targeted people category.");
        d.setContentView(R.layout.dialog6);
        Button b1 = (Button) d.findViewById(R.id.submitButton);
        Button b2 = (Button) d.findViewById(R.id.cancelButton);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cpv;
                RadioButton button3 = (RadioButton) d.findViewById(R.id.radioButton3);
                RadioButton button5 = (RadioButton) d.findViewById(R.id.radioButton5);
                RadioButton button8 = (RadioButton) d.findViewById(R.id.radioButton8);
                if(button3.isChecked()){
                    cpv = 3;
                }else if(button5.isChecked()){
                    cpv = 5;
                }else{
                    cpv = 8;
                }
                Variables.amountToPayPerTargetedView = cpv;
                d.dismiss();
                startAdUpload();
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSelectingCategory);
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


    @Override
    public void onClick(View v) {
        if(v==retryLoadingButton){
            if(isOnline(mContext)) {
                loadCategoriesFromFirebase();
            }else{
                Toast.makeText(mContext,"To continue,you need an internet connection",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
