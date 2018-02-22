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
import com.bry.adcafe.services.SliderPrefManager;

public class TutorialAdvertisers extends AppCompatActivity {
    private ViewPager viewPager;
    private TutorialAdvertisers.MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout myDotsLayout;
    private TextView[] myDots;
    private int[] myLayouts;
    private Button myBtnSkip, myBtnNext;
    private SliderPrefManager myPrefManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Checking for first time launch - before calling setContentView()
        if (Build.VERSION.SDK_INT >= 21){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_tutorial_advertisers);
        myPrefManager = new SliderPrefManager(this);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        myDotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        myBtnSkip = (Button) findViewById(R.id.btn_skip);
        myBtnNext = (Button) findViewById(R.id.btn_next);

        // layouts of all welcome sliders
        myLayouts = new int[]{
                R.layout.tutorial_advertiser_slider_1,
                R.layout.tutorial_advertiser_slider_2,
                R.layout.tutorial_advertiser_slider_3,
                R.layout.tutorial_advertiser_slider_4,
                R.layout.tutorial_advertiser_slider_5,
                R.layout.tutorial_advertiser_slider_6_2,
                R.layout.tutorial_advertiser_slider_6_3,
                R.layout.tutorial_advertiser_slider_6_4,
                R.layout.tutorial_advertiser_slider_6_5,
                R.layout.tutorial_advertiser_slider_6_6,
                R.layout.tutorial_advertiser_slider_6,
                R.layout.tutorial_advertiser_slider_7
        };

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter  = new TutorialAdvertisers.MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPageChangeListener);

        myBtnSkip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LaunchHomeScreen();
            }
        });

        myBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(+1);
                if (current < myLayouts.length){
                    viewPager.setCurrentItem(current);
                }else {
                    LaunchHomeScreen();
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

    private void LaunchHomeScreen(){
        if (myPrefManager.isFirstTimeLaunchForAdvertisers()){
            startActivity(new Intent(TutorialAdvertisers.this, SelectCategoryAdvertiser.class));
            myPrefManager.setIsFirstTimeLaunchInAdvertise(false);
            finish();
        }else{
          onBackPressed();
        }

    }

    //  viewpager change listener
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

    /**
     * Making notification bar transparent
     */

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

