package com.bry.adcafe.ui;

import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.fragments.FeedbackFragment;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.SliderPrefManager;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.Bind;

public class Dashboard extends AppCompatActivity {
    private TextView mTotalAdsSeenToday;
    private TextView mTotalAdsSeenAllTime;
    private ImageView mInfoImageView;
    private CardView mUploadAnAdIcon;
    private TextView mAmmountNumber;
    protected String mKey = "";
    private SliderPrefManager myPrefManager;
    private Button mUploadedAdsStats;
    private Context mContext;
    private static int NOTIFICATION_ID2 = 1880;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Variables.isDashboardActivityOnline = true;
        mContext = this.getApplicationContext();

        loadViews();
        setValues();
        setClickListeners();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        try{
            notificationManager.cancel(NOTIFICATION_ID2);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        setValues();
    }

    private void setClickListeners() {
        mInfoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this,TutorialUsers.class);
                startActivity(intent);
                Variables.isStartFromLogin = false;
                Variables.isInfo = true;
                finish();
            }
        });

        mUploadAnAdIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPrefManager = new SliderPrefManager(getApplicationContext());
                if (myPrefManager.isFirstTimeLaunchForAdvertisers()){
                    Intent intent = new Intent(Dashboard.this,TutorialAdvertisers.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(Dashboard.this,SelectCategoryAdvertiser.class);
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.uploadedAdsStats).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, AdStats.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.uploadedAdsStats).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("bryonyoni@gmail.com")){
                    Intent intent = new Intent(Dashboard.this, AdminConsole.class);
                    startActivity(intent);
                }else{
                    Log.d("Dashboard","NOT administrator.");
                }
                return false;
            }
        });

        findViewById(R.id.FeedbackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                Log.d("DASHBOARD","Setting up fragment");
                FeedbackFragment reportDialogFragment = new FeedbackFragment();
                reportDialogFragment.setMenuVisibility(false);
                reportDialogFragment.show(fm, "Feedback.");
                reportDialogFragment.setfragContext(mContext);
            }
        });

        findViewById(R.id.subscriptionsImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, SubscriptionManager.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy(){
        Variables.isDashboardActivityOnline = false;
        super.onDestroy();
    }

    private void loadViews() {
        mTotalAdsSeenAllTime = (TextView) findViewById(R.id.AdsSeenAllTimeNumber);
        mTotalAdsSeenToday = (TextView) findViewById(R.id.AdsSeenTodayNumber);
        mInfoImageView = (ImageView) findViewById(R.id.helpIcon);
        mUploadAnAdIcon = (CardView) findViewById(R.id.uploadAnAdIcon);
        mAmmountNumber = (TextView) findViewById(R.id.ammountNumber);
        mUploadedAdsStats = (Button) findViewById(R.id.uploadedAdsStats);
    }

    private void setValues() {
        int todaysTotals;
        int monthsTotals;
        int reimbursementTotals;
        if(Variables.getMonthAdTotals(mKey) ==0) {
            SharedPreferences prefs = getSharedPreferences("TodayTotals", MODE_PRIVATE);
            todaysTotals = prefs.getInt("TodaysTotals", 0);

            SharedPreferences prefs2 = getSharedPreferences("MonthTotals", MODE_PRIVATE);
            monthsTotals = prefs2.getInt("MonthsTotals", 0);

            SharedPreferences prefs3 = getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
            reimbursementTotals = prefs3.getInt(Constants.REIMBURSEMENT_TOTALS, 0);
        }else{
            todaysTotals = Variables.getAdTotal(mKey);
            monthsTotals = Variables.getMonthAdTotals(mKey);
            reimbursementTotals = Variables.getTotalReimbursementAmount();
        }
        mTotalAdsSeenToday.setText(Integer.toString(todaysTotals));
        mTotalAdsSeenAllTime.setText(Integer.toString(monthsTotals));
        int totalAmounts = (int)(monthsTotals*Constants.CONSTANT_AMMOUNT_FOR_USER);
        mAmmountNumber.setText(Integer.toString(reimbursementTotals));
    }

    @Override
    public void onBackPressed(){
        if(!Variables.isMainActivityOnline){
            Intent intent = new Intent(Dashboard.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            super.onBackPressed();
        }

    }
}
