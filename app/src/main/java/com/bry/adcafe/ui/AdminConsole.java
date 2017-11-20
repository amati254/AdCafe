package com.bry.adcafe.ui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.AdminStatItem;
import com.bry.adcafe.models.Advert;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AdminConsole extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "AdminConsole";
    private List<Advert> mAdList = new ArrayList<>();
    private Context mContext;
    @Bind(R.id.LoadAdsWhichHaveBeenSeenLess) Button adsWhichHaveBeenSeenLess;
    @Bind(R.id.PlaceHolderViewData) PlaceHolderView DataListsView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_console);
        ButterKnife.bind(this);
        mContext = this.getApplicationContext();

        adsWhichHaveBeenSeenLess.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == adsWhichHaveBeenSeenLess){
            loadAdsWhichHaveBeenSeenLess();
        }
    }

    private void loadAdsWhichHaveBeenSeenLess() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getPreviousDay());

        DatabaseReference dbRef = query.getRef();
        dbRef.orderByChild("numberOfTimesSeen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot snap:dataSnapshot.getChildren()){
                        Advert ad = snap.getValue(Advert.class);
                        if(ad.getNumberOfTimesSeen()<ad.getNumberOfUsersToReach()){
                            mAdList.add(ad);
                        }
                    }
                    loadTheUsersToReemburse();
                }else{
                    Toast.makeText(mContext,"No children from database exist",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext,"Something went wrong loading"+databaseError.getMessage(),Toast.LENGTH_LONG).show();
                Log.d(TAG,"Something went wrong loading the ads"+databaseError.getMessage());
            }
        });

    }

    private void loadTheUsersToReemburse() {
        if(mAdList!=null && mAdList.size()>0){
            for(int i = 0; i<mAdList.size();i++){
                DataListsView.addView(new AdminStatItem(mContext,DataListsView,mAdList.get(i)));
            }
        }else{
            Toast.makeText(mContext,"There is nothing in  the adlist.",Toast.LENGTH_LONG).show();
        }
    }

    private String getPreviousDay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,-1);
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String tomorrowsDate = (dd+":"+mm+":"+yy);

        Log.d(TAG,"Tomorrows date is : "+tomorrowsDate);
        return tomorrowsDate;

    }

}
