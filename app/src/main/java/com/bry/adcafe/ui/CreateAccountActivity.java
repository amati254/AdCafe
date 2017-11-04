package com.bry.adcafe.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private Context mContext;
    private String mKey = "";

    @Bind(R.id.createUserButton) Button mCreateUserButton;
    @Bind(R.id.nameEditText) EditText mNameEditText;
    @Bind(R.id.emailEditText) EditText mEmailEditText;
    @Bind(R.id.passwordEditText) EditText mPasswordEditText;
    @Bind(R.id.confirmPasswordEditText) EditText mConfirmPasswordEditText;
    @Bind(R.id.loginTextView) TextView mLoginTextView;
    @Bind(R.id.signUpRelative) RelativeLayout mRelative;
    @Bind(R.id.SignUpAvi) AVLoadingIndicatorView mAvi;
    @Bind(R.id.creatingAccountLoadingText) TextView mLoadingText;
    @Bind(R.id.ConfirmEmailLayout) LinearLayout mConfirmEmailLayout;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String mName;
    private DatabaseReference mRef1;
    private DatabaseReference mRef2;
    private int mClusterID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        createAuthStateListener();
        mContext = this.getApplicationContext();

        mLoginTextView.setOnClickListener(this);
        mCreateUserButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v){
        if(v == mLoginTextView){
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if(v == mCreateUserButton){
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            createNewUser();
        }
    }

    private void createNewUser() {
        final String name = mNameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();

        String password = mPasswordEditText.getText().toString().trim();
        String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password,confirmPassword);
        boolean validName = isValidName(name);
        if(!validEmail || !validName || !validPassword)return;

        if(!isOnline(mContext)){
            Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.SignUpNoConnection,
                    Snackbar.LENGTH_LONG).show();
        }else{
            mAvi.setVisibility(View.VISIBLE);
            mLoadingText.setVisibility(View.VISIBLE);
            mRelative.setAlpha(0.2f);
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG,"authentication successful");
                        createFirebaseUserProfile(task.getResult().getUser());
                    }else {
//                        mRelative.setVisibility(View.VISIBLE);
                        mRelative.setAlpha(1.0f);
                        mAvi.setVisibility(View.GONE);
                        mLoadingText.setVisibility(View.GONE);
                        Toast.makeText(CreateAccountActivity.this, "Sign Up has failed.Another user with your info may exist.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

    private void createFirebaseUserProfile(final FirebaseUser user) {
        UserProfileChangeRequest addProfileName = new UserProfileChangeRequest.Builder().setDisplayName(mName).build();

        user.updateProfile(addProfileName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG,"Created new username");
                }
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener!=null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if(mRef1!=null){
            mRef1.removeEventListener(val);
        }
        if(mRef2!=null){
            mRef2.removeEventListener(val);
        }
    }


    private void createAuthStateListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    generateClusterIDFromFlagedClusters();
//                    sendVerificationEmail();
                }
            }
        };
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Variables.isVerifyingEmail = true;
                    mAvi.setVisibility(View.GONE);
                    mLoadingText.setVisibility(View.GONE);
                    mConfirmEmailLayout.setVisibility(View.VISIBLE);
                    findViewById(R.id.confirmedEmailButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkIfEmailIsVerified();
                        }
                    });
                    findViewById(R.id.recreateAccount).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mConfirmEmailLayout.setVisibility(View.GONE);
                            mRelative.setVisibility(View.VISIBLE);
                            mRelative.setAlpha(1.0f);
                            FirebaseAuth.getInstance().signOut();
                        }
                    });
                }
            }
        });
    }

    private void checkIfEmailIsVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user.isEmailVerified()){
            mAvi.setVisibility(View.VISIBLE);
            mLoadingText.setVisibility(View.VISIBLE);
            mConfirmEmailLayout.setVisibility(View.GONE);

            generateClusterIDFromFlagedClusters();
        }else {
            Toast.makeText(mContext,"Your email is not verified!",Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpFirebaseNodes() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        mRelative.setAlpha(0.0f);

        //Creates nodes for totals seen today and sets them to 0;
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_TODAY);
        adRef.setValue(Variables.getAdTotal(mKey));

        //Creates nodes for totals seen all month and sets them to 0;
        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.TOTAL_NO_OF_ADS_SEEN_All_MONTH);
        adRef2.setValue(Variables.getMonthAdTotals(mKey));

        //Creates node for cluster ID and sets its value to ID;
        DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.CLUSTER_ID);
        adRef3.setValue(mClusterID);

        //Adds the new users id to children in its respective cluster.
        DatabaseReference adRef4 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.CLUSTERS_LIST).child(Integer.toString(mClusterID));
        DatabaseReference pushRef4 = adRef4.push();
        String pushId  = pushRef4.getKey();
        pushRef4.setValue(uid);

        //sets pushref key generated from adding user to cluster to clusterListPushrefID;
        DatabaseReference adRef5 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.CLUSTER_LIST_PUSHREF_ID);
        adRef5.setValue(pushId);

        //sets value for boolean if user has made any payments for uploading an advert to false.
        DatabaseReference adRef6 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants. HAS_USER_MADE_PAMENTS);
        adRef6.setValue(false);

        //sets the date for when last used in firebase.
        DatabaseReference adRef7 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.DATE_IN_FIREBASE);
        adRef7.setValue(getDate());

        startMainActivity();

    }

    private void startMainActivity(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        User.setUid(uid);
        Variables.isStartFromLogin = true;
        mAvi.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);
        Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        if (password.length() < 6) {
            mPasswordEditText.setError("Please create a password containing at least 6 characters");
            return false;
        } else if (!password.equals(confirmPassword)) {
            mPasswordEditText.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        boolean isGoodEmail = (email!=null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if(!isGoodEmail){
            mEmailEditText.setError("Please enter a valid email address");
            return false;
        }
        return isGoodEmail;
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            mNameEditText.setError("Please enter your name");
            return false;
        }
        return true;
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private void generateClusterID(){
        Log.d(TAG,"---Generating Cluster ID normally.");
        mRef1 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.CLUSTERS_LIST);
        if(mRef1!=null){
            mRef1.addListenerForSingleValueEvent(val);
        }else{
            mClusterID = 1 ;
            setUpFirebaseNodes();
        }

    }

    ValueEventListener val = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            long currentCluster;
            if(dataSnapshot.getChildrenCount() == 0){
                 currentCluster = dataSnapshot.getChildrenCount()+1;
            }else{
                currentCluster = dataSnapshot.getChildrenCount();
            }
            Log.d(TAG,"--NUMBER OF CLUSTERS IN FIREBASE IS --"+currentCluster);
            DataSnapshot UsersInCurrentCluster = dataSnapshot.child(Integer.toString((int)currentCluster));

            long numberOfUsersInCurrentCluster;
            if(UsersInCurrentCluster.getChildrenCount()==0){
                numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount();
                Log.d(TAG,"--NUMBER OF USERS IN CURRENT CLUSTER IN FIREBASE IS --"+numberOfUsersInCurrentCluster);
            }else{
                numberOfUsersInCurrentCluster = UsersInCurrentCluster.getChildrenCount()+1;
                Log.d(TAG,"--NUMBER OF USERS IN CURRENT CLUSTER IN FIREBASE IS --"+numberOfUsersInCurrentCluster);
            }

            if(numberOfUsersInCurrentCluster<1000){
                Log.d(TAG,"--NUMBER OF USERS IS LESS THAN LIMIT.SETTING mClusterID TO --"+currentCluster);
                mClusterID = (int)currentCluster;
            }else{
                Log.d(TAG,"--NUMBER OF USERS EXCEEDS LIMIT.SETTING mClusterID TO --"+(currentCluster+1));
                mClusterID = (int)currentCluster+1;
            }

            Log.d(TAG,"---Cluster id generated for firebase is-- "+mClusterID);
            User.setID(mClusterID,mKey);
            setUpFirebaseNodes();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("CREATE_ACCOUNT_ACT---","Unable to get cluster with least users due to error.");
            Toast.makeText(mContext,"Unable to create your account at the moment, try again in a few minutes.",Toast.LENGTH_LONG).show();
        }
    };

    private void generateClusterIDFromFlagedClusters(){
        Variables.setMonthAdTotals(mKey,0);
        Variables.setAdTotal(0,mKey);
        Log.d(TAG,"--Generating clusterID from flagged ads.");
        mRef2 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.FLAGGED_CLUSTERS);
        if(mRef2!=null){
            mRef2.addListenerForSingleValueEvent(val2);
        }else{
            generateClusterID();
        }
    }

    ValueEventListener val2 = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.hasChildren()){
                Log.d(TAG,"Flagged clusters has got children in it.");
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    mClusterID = snap.getValue(int.class);
                    Log.d(TAG,"Cluster id gotten from Flagged cluster is --"+mClusterID);
                    User.setID(mClusterID,mKey);
                    removeId(snap.getKey());
                    break;
                }
            }else{
                Log.d(TAG,"--Flagged clusters has got no children in it. Generating normally");
                generateClusterID();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("CREATE_ACCOUNT_ACT---","Unable to generate cluster from flagged clusters."+databaseError.getMessage());
            Toast.makeText(mContext,"Unable to create your account at the moment, try again in a few minutes.",Toast.LENGTH_LONG).show();
        }
    };

    private void removeId(String key) {
        DatabaseReference dbr = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.FLAGGED_CLUSTERS).child(key);
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"---Removing cluster id from the flagged clusters list");
                dataSnapshot.getRef().removeValue();
                setUpFirebaseNodes();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"---Unable to remove cluster id from the flagged clusters list."+databaseError.getMessage());
                Log.d(TAG,"---generating cluster id normally instead.");
                generateClusterID();
            }
        });
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
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String todaysDate = (dd+":"+mm+":"+yy);

        return todaysDate;
    }
}
