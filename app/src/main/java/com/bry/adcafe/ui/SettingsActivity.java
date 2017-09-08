package com.bry.adcafe.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView mLogoutImageView;
    private TextView mLogoutText;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mContext = this.getApplicationContext();

        loadViews();
        setClickListeners();
    }

    private void setClickListeners() {
        mLogoutImageView.setOnClickListener(this);
        mLogoutText.setOnClickListener(this);
    }

    private void loadViews() {
        mLogoutImageView = (ImageView) findViewById(R.id.exitIcon);
        mLogoutText = (TextView) findViewById(R.id.exitText);
    }


    @Override
    public void onClick(View v) {
        if(v == mLogoutImageView || v == mLogoutText){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }
}
