package com.bry.adcafe.ui;

import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.fragments.ChangeCPVFragment;
import com.bry.adcafe.fragments.FeedbackFragment;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.SliderPrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;

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

    @Bind(R.id.NotificationBtn) public ImageButton mNotfBtn;
    @Bind(R.id.dotForNotification) public View mDotForNotf;
    @Bind(R.id.ChangeCPVBtn) public ImageButton mCPVBtn;
    @Bind(R.id.LogoutBtn) public ImageButton mLogout;
    @Bind(R.id.payoutBtn) public ImageButton payoutBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Variables.isDashboardActivityOnline = true;
        mContext = this.getApplicationContext();
        ButterKnife.bind(this);

        loadViews();
        setValues();
        setClickListeners();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        try{
            notificationManager.cancel(NOTIFICATION_ID2);
        }catch (Exception e){
            e.printStackTrace();
        }
        setListeners();
    }

    @Override
    protected void onResume(){
        super.onResume();
        setValues();
    }

    private void setListeners(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForShowingPrompt,
                new IntentFilter("SHOW_PROMPT"));
    }

    private void removeListeners(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowingPrompt);
    }

    private BroadcastReceiver mMessageReceiverForShowingPrompt = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            promptUserAboutChanges();
        }
    };

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

        mNotfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserAboutNotifications();
            }
        });

        mCPVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserAboutChangingPrice();
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserIfSureToLogout();
            }
        });

        payoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserAboutPayout();
            }
        });

    }

    @Override
    protected void onDestroy(){
        Variables.isDashboardActivityOnline = false;
        super.onDestroy();
        removeListeners();
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

        if(Variables.doesUserWantNotifications)mDotForNotf.setVisibility(View.VISIBLE);
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

    private void promptUserAboutNotifications(){
        String message;
        if(Variables.doesUserWantNotifications)
            message = "Do you wish to put off daily morning notifications about new ads?";
        else message = "Do you wish to put back on daily morning notifications?";


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Morning Notifications");
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean newValue = !Variables.doesUserWantNotifications;
                        setUsersPreferedNotfStatus(newValue);
                    }
                })
                .setNegativeButton("No.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void promptUserAboutChangingPrice(){
        FragmentManager fm = getFragmentManager();
        ChangeCPVFragment cpvFragment = new ChangeCPVFragment();
        cpvFragment.setMenuVisibility(false);
        cpvFragment.show(fm,"Change cpv fragment");
        cpvFragment.setContext(mContext);
    }

    private void promptUserIfSureToLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AdCafé");
        builder.setMessage("Are you sure you want to log out?")
                .setCancelable(true)
                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutUser();
                    }
                })
                .setNegativeButton("No!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void promptUserAboutPayout(){

    }

    public void promptUserAboutChanges(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AdCafé");
        builder.setMessage("Your changes will take effect starting tomorrow.")
                .setCancelable(true)
                .setPositiveButton("Cool.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void logoutUser() {
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        Intent intent = new Intent(Dashboard.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setUsersPreferedNotfStatus(boolean value){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        SharedPreferences pref7 = mContext.getSharedPreferences(Constants.PREFERRED_NOTIF,MODE_PRIVATE);
        SharedPreferences.Editor editor7 = pref7.edit();
        editor7.clear();
        editor7.putBoolean(Constants.PREFERRED_NOTIF,Variables.doesUserWantNotifications);
        Log.d("DashBoard","Set the users preference for seing notifications to : "+Variables.doesUserWantNotifications);
        editor7.apply();

        DatabaseReference adRef11 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PREFERRED_NOTIF);
        adRef11.setValue(value);

        Variables.doesUserWantNotifications = value;

        if(Variables.doesUserWantNotifications)mDotForNotf.setVisibility(View.VISIBLE);
        else mDotForNotf.setVisibility(View.INVISIBLE);

        Toast.makeText(mContext,"Your preference has been set.",Toast.LENGTH_SHORT).show();
    }

}
