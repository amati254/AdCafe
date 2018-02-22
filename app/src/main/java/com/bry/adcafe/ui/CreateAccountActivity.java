package com.bry.adcafe.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.bry.adcafe.services.DatabaseManager;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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
        mConfirmPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    mCreateUserButton.performClick();
                    Log.i(TAG,"Enter pressed");
                }
                return false;
            }
        });

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

        final String password = mPasswordEditText.getText().toString().trim();
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
            mRelative.setVisibility(View.GONE);
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG,"authentication successful");
                        createFirebaseUserProfile(task.getResult().getUser());
                        Variables.userName = name;
                        Variables.setPassword(password);
                    }else {
                        mRelative.setVisibility(View.VISIBLE);
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
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedCreatingUserSpace,
                new IntentFilter(Constants.CREATE_USER_SPACE_COMPLETE));
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener!=null) mAuth.removeAuthStateListener(mAuthListener);
//        if(mRef1!=null){
//            mRef1.removeEventListener(val);
//        }
//        if(mRef2!=null){
//            mRef2.removeEventListener(val);
//        }
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedCreatingUserSpace);
    }

    private BroadcastReceiver mMessageReceiverForFinishedCreatingUserSpace = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished creating user space");
            startSelectCategory();
        }
    };

    private BroadcastReceiver mMessageReceiverForFinishedCreatingUserSubscriptionList = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished creating user subscription list");
//            startMainActivity();
        }
    };


    private void createAuthStateListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    setUpUserSpace();
                    user.sendEmailVerification();
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
        }else {
            Toast.makeText(mContext,"Your email is not verified!",Toast.LENGTH_SHORT).show();
        }
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

    private void startSelectCategory(){
        Variables.isStartFromLogin = true;
        mAvi.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);
        Intent intent = new Intent(CreateAccountActivity.this, SelectCategory.class);
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
            mEmailEditText.setError("We need your actual email address please");
            return false;
        }
        return isGoodEmail;
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            mNameEditText.setError("Please enter your name");
            return false;
        }
        if(name.length()>16){
            mNameEditText.setError("Your name is too long");
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

    private void setUpUserSpace(){
        mRelative.setVisibility(View.GONE);
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.createUserSpace(mContext);
    }


}

