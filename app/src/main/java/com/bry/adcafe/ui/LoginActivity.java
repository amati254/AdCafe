package com.bry.adcafe.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

    private Context mContext;
    private String mKey = "";
    private boolean mHasLoadingMonthTotalsFailed;
    private boolean mHasLoadingDayTotalsFailed;

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
                if(user!= null){
                    mRelative.setVisibility(View.GONE);
                    mAvi.setVisibility(View.VISIBLE);
                    getMonthAdTotalFromFirebase();
                }
            }
        };
    }



    private void getMonthAdTotalFromFirebase() {
        Log.d(TAG,"---Loading Month AdTotals from firebase.");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
        mRef = query.getRef();
//        mRef.addValueEventListener(val);
        mRef.addListenerForSingleValueEvent(val);

    }

    private void loadTodayAdTotalsFromFirebase(){
        Log.d("LOGIN_ACTIVITY--","Ad totals from today have been loaded from firebase.");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        mRef2 = query.getRef();
//        mRef.addValueEventListener(val2);
        mRef.addListenerForSingleValueEvent(val2);
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
            Variables.setAdTotal(number,mKey);
            Log.d("LOGIN_ACTIVITY--","Ad totals gotten from firebase is --"+number);
            startMainActivity();
            mHasLoadingDayTotalsFailed = false;
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("LOGIN_ACTIVITY--","Failed to load todays ad totals from firebase.");
            mHasLoadingDayTotalsFailed = true;
            loadFromSharedPreferences();
            startMainActivity();
        }
    };

    private void loadFromSharedPreferences(){
        if(mHasLoadingDayTotalsFailed){
            SharedPreferences prefs = getSharedPreferences(Constants.AD_TOTAL,MODE_PRIVATE);
            int number = prefs.getInt("adTotals",0);
            Log.d("LOGIN_ACTIVITY-----","NUMBER GOTTEN FROM SHARED PREFERENCES IS - "+ number);
            Variables.setAdTotal(number,mKey);

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

}
