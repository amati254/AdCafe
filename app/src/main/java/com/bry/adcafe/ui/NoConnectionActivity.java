package com.bry.adcafe.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bry.adcafe.R;

public class NoConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection);
        startRecurringChecker();
    }

    private void startRecurringChecker() {
//        boolean connection = NetworkStateReceiver.checkConnection(this.getApplicationContext());
//        if(!connection){
//            Intent intent = new Intent(NoConnectionActivity.this,MainActivity.class);
//            startActivity(intent);
//            finish();
//        }else{
////            startRecurringChecker();
////            findViewById(R.id.)
//        }
    }
}
