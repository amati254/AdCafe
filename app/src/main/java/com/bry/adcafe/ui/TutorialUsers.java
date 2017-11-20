package com.bry.adcafe.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.services.SliderPrefManager;
import com.google.firebase.auth.FirebaseAuth;

public class TutorialUsers extends AppCompatActivity {
    private ViewPager viewPager;
    private TutorialUsers.MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout myDotsLayout;
    private TextView[] myDots;
    private int[] myLayouts;
    private Button myBtnSkip, myBtnNext;
    private SliderPrefManager myPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_tutorial_users);
        myPrefManager = new SliderPrefManager(getApplicationContext());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        myDotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        myBtnSkip = (Button) findViewById(R.id.btn_skip);
        myBtnNext = (Button) findViewById(R.id.btn_next);

        // layouts of all welcome sliders
        myLayouts = new int[]{
                R.layout.welcome_slider_5,
                R.layout.welcome_slider_4,
                R.layout.welcome_slider_3,
                R.layout.welcome_slider_2,
                R.layout.welcome_slider_1,
                R.layout.welcome_slider_0
        };
        // adding bottom dots
        addBottomDots(0);
        changeStatusBarColor();

        myViewPagerAdapter  = new TutorialUsers.MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPageChangeListener);
        myBtnSkip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LaunchNextActivity();
            }
        });

        myBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(+1);
                if (current < myLayouts.length){
                    viewPager.setCurrentItem(current);
                }else {
                    LaunchNextActivity();
                }
            }
        });
    }

    private void addBottomDots(int currentPage){
        myDots = new TextView[myLayouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int [] colorInActiive = getResources().getIntArray(R.array.array_dot_inactive);

        myDotsLayout.removeAllViews();
        for (int i = 0;i < myDots.length;i++){
            myDots[i] = new TextView(this);
            myDots[i].setText(Html.fromHtml("&#8226;"));
            myDots[i].setTextSize(35);
            myDots[i].setTextColor(colorInActiive[currentPage]);
            myDotsLayout.addView(myDots[i]);
        }
        if (myDots.length > 0)
            myDots[currentPage].setTextColor(colorsActive[currentPage]);

    }

    private int getItem(int i){
        return viewPager.getCurrentItem()+ i ;
    }

    private void LaunchNextActivity(){
        if(Variables.isInfo){
//            startActivity(new Intent(TutorialUsers.this,Dashboard.class));
            Intent intent = new Intent(TutorialUsers.this,Dashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            myPrefManager.setFirstTimeLaunch(false);
            startActivity(new Intent(TutorialUsers.this,CreateAccountActivity.class));
            finish();
        }

    }

    ViewPager.OnPageChangeListener viewPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            if (position == myLayouts.length - 1){
                myBtnNext.setText(getString(R.string.start));
                myBtnSkip.setVisibility(View.GONE);
            }else {
                myBtnNext.setText(getString(R.string.next));
                myBtnSkip.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onBackPressed(){
        LaunchNextActivity();
    }

//     Making notification bar transparent
    private void changeStatusBarColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater myLayoutInflater;


        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater myLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = myLayoutInflater.inflate(myLayouts[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return myLayouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
