package com.bry.adcafe.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bry.adcafe.R;

public class WebActivity extends AppCompatActivity {

    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        WebView myWebView = (WebView)findViewById(R.id.myWebView);
        WebSettings myWebSetttings = myWebView.getSettings();
        myWebSetttings.setJavaScriptEnabled(true);
        myWebView.loadUrl("");
        myWebView.setWebViewClient(new WebViewClient());
    }

    public void onBackPressed(){

    }
}
