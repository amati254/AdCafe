package com.bry.adcafe.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.ConnectionChecker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = LoginActivity.class.getSimpleName();
    @Bind(R.id.emailEditText)  EditText mEmail;
    @Bind(R.id.passwordEditText) EditText mPassword;
    @Bind(R.id.LoginButton)  Button mLoginButton;
    @Bind(R.id.registerLink) TextView mRegisterLink;
    @Bind(R.id.LoginAvi) AVLoadingIndicatorView mAvi;
    @Bind(R.id.LoginRelative) RelativeLayout mRelative;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private DatabaseReference mRef2;
    private DatabaseReference mRef3;
    private DatabaseReference mRef4;
    private DatabaseReference adRef;

    private Context mContext;
    private String mKey = "";
    private boolean mHasLoadingMonthTotalsFailed;
    private boolean mHasLoadingDayTotalsFailed;
    private boolean mIsLastOnlineToday;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mRegisterLink.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        mContext = this.getApplicationContext();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOffline,new IntentFilter(Constants.CONNECTION_OFFLINE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForConnectionOnline,new IntentFilter(Constants.CONNECTION_ONLINE));


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!= null &&isOnline(mContext)){
                    mRelative.setVisibility(View.GONE);
                    mAvi.setVisibility(View.VISIBLE);
                    lastUsed();
                }
            }
        };
    }

    private void lastUsed(){
        Log.d(TAG,"---Loading date from when last used from firebase.");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.DATE_IN_FIREBASE);
        mRef4 = query.getRef();
        mRef4.addListenerForSingleValueEvent(val4);
    }

    private void getMonthAdTotalFromFirebase() {
        Log.d(TAG,"---Loading Month AdTotals from firebase.");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
        mRef = query.getRef();
        mRef.addListenerForSingleValueEvent(val);

    }

    private void loadTodayAdTotalsFromFirebase(){
        Log.d("LOGIN_ACTIVITY--","Ad totals from today have been loaded from firebase.");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        mRef2 = query.getRef();
        mRef.addListenerForSingleValueEvent(val2);
    }

    private void loadUserIDFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.CLUSTER_ID);
        mRef3 = query.getRef();
        mRef3.addListenerForSingleValueEvent(val3);
    }

    ValueEventListener val = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            int number = dataSnapshot.getValue(int.class);
            Log.d(TAG,"---Month ad totals is--"+number);
            Variables.setMonthAdTotals(mKey,number);
            mHasLoadingMonthTotalsFailed = false;
            loadTodayAdTotalsFromFirebase();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG,"---Failed to load Month AdTotals from firebase.");
            mHasLoadingMonthTotalsFailed = true;
            loadFromSharedPreferences();
            loadTodayAdTotalsFromFirebase();
        }
    };

    ValueEventListener val2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            int number = dataSnapshot.getValue(int.class);
            if(mIsLastOnlineToday) {
                Variables.setAdTotal(number,mKey);
                Log.d(TAG,"User was last online today,thus will set adtotals normally");
                Log.d("LOGIN_ACTIVITY--","Ad totals set from firebase is --"+number);
            } else {
                Variables.setAdTotal(0,mKey);
                Log.d(TAG,"User was not last online today,thus will set ad totals to 0");
                setLastUsedDateInFirebaseDate();
                resetAdTotalsInFirebase();
            }

            loadUserIDFromFirebase();
            mHasLoadingDayTotalsFailed = false;
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("LOGIN_ACTIVITY--","Failed to load todays ad totals from firebase.");
            mHasLoadingDayTotalsFailed = true;
            loadFromSharedPreferences();
        }
    };


    ValueEventListener val3 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            int clusterID = dataSnapshot.getValue(int.class);
            User.setID(clusterID,mKey);
            startMainActivity();

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mAvi.setVisibility(View.GONE);
            mRelative.setVisibility(View.VISIBLE);
            Toast.makeText(mContext,"Your connection may be too unreliable.Perhaps try again later.",Toast.LENGTH_LONG).show();
        }
    };

    ValueEventListener val4 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue()!=null){
                String dateInFirebase = dataSnapshot.getValue(String.class);
                Log.d(TAG,"Date gotten from firebase is : "+dateInFirebase);
                String currentDate = getDate();
                if(dateInFirebase.equals(currentDate)){
                    mIsLastOnlineToday = true;
                    Log.d(TAG,"---Date in firebase matches date in system,thus User was last online today");
                }else{
                    Log.d(TAG,"---Date in firebase  does not match date in system , thus User was not online last today");
                    Log.d(TAG,"---Date from firebase is--"+dateInFirebase+"--while date in system is "+currentDate);
                    mIsLastOnlineToday = false;
                }
            }else{
                setLastUsedDateInFirebaseDate();
                mIsLastOnlineToday = false;
            }
            getMonthAdTotalFromFirebase();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void setLastUsedDateInFirebaseDate() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.DATE_IN_FIREBASE);
        adRef.setValue(getDate());
    }

    private void resetAdTotalsInFirebase() {
        Log.d(TAG,"---Resetting ad total in firebase to 0 due to it being a new day.");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(0);
    }

    private void loadFromSharedPreferences(){
        if(mHasLoadingDayTotalsFailed){
            SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL,MODE_PRIVATE);
            int number = prefs.getInt("adTotals",0);
            Log.d("LOGIN_ACTIVITY-----","NUMBER GOTTEN FROM SHARED PREFERENCES IS - "+ number);
            if(mIsLastOnlineToday) Variables.setAdTotal(number,mKey);
            else Variables.setAdTotal(0,mKey);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
            mHasLoadingDayTotalsFailed = false;
        }
        if(mHasLoadingMonthTotalsFailed){
            SharedPreferences prefs2 = getSharedPreferences(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH,MODE_PRIVATE);
            int number2 = prefs2.getInt(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH,0);
            Log.d("LOGIN_ACTIVITY-----","NUMBER GOTTEN FROM MONTHLY SHARED PREFERENCES IS - "+ number2);
            Variables.setMonthAdTotals(mKey,number2);

            SharedPreferences.Editor editor2 = prefs2.edit();
            editor2.clear();
            editor2.commit();
            mHasLoadingMonthTotalsFailed = false;
        }

    }


    private void startMainActivity(){
        mAvi.setVisibility(View.GONE);
        Intent intent = new Intent (LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private BroadcastReceiver mMessageReceiverForConnectionOffline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-MAIN_A","Connection has been dropped");
        }
    };

    private BroadcastReceiver mMessageReceiverForConnectionOnline = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("CONNECTION_C-MAIN_A","Connection has come online");
        }
    };

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if(mRef!=null){
            mRef.removeEventListener(val);
        }
        if(mRef2!=null){
            mRef2.removeEventListener(val2);
        }
        if(mRef3!=null){
            mRef3.removeEventListener(val3);
        }
    }

    @Override
    public void onClick(View v){
        if(v == mRegisterLink){
            Intent intent = new Intent(LoginActivity.this,CreateAccountActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if(v == mLoginButton){
            loginUserWithPassword();
        }
    }

    private void loginUserWithPassword() {
     String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        if(email.equals("")){
            mEmail.setText("Please enter your email");
            return;
        }
        if(password.equals("")){
            mEmail.setText("Password cannot be blank");
            return;
        }
        if(!isOnline(mContext)){
            Snackbar.make(findViewById(R.id.loginCoordinatorLayout), R.string.LogInNoConnection,
                    Snackbar.LENGTH_LONG).show();
        }else{
            mAvi.setVisibility(View.VISIBLE);
            mRelative.setVisibility(View.GONE);
            Log.d(TAG,"--Logging in user with username and password...");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            mAvi.setVisibility(View.GONE);
                            Log.d(TAG,"signInWithEmail:onComplete"+task.isSuccessful());
                            if(!task.isSuccessful()){
                                Log.w(TAG,"SignInWithEmail",task.getException());
                                mRelative.setVisibility(View.VISIBLE);
                                mAvi.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this,"You may have mistyped your username or password.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
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

}
