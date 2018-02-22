package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;

/**
 * Created by bryon on 22/02/2018.
 */

public class FragmentAdvertiserPayoutBottomsheet extends BottomSheetDialogFragment {
    private Activity mActivity;
    private View mContentView;
    private LinearLayout mPayoutOptionsLayout;
    private LinearLayout mEnterPayoutDetailsPart;
    private String mPhoneNo;
    private LinearLayout mConfirmLayout;
    private double mTotals;
    private String mPassword;


    public void setActivity(Activity activity){
        this.mActivity = activity;
    }

    public void setDetails(double totals,String password){
        mTotals = totals;
        mPassword = password;
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:{
                    Log.d("BSB","collapsed") ;
                }
                case BottomSheetBehavior.STATE_SETTLING:{
                    Log.d("BSB","settling") ;
                }
                case BottomSheetBehavior.STATE_EXPANDED:{
                    Log.d("BSB","expanded") ;
                }
                case BottomSheetBehavior.STATE_HIDDEN: {
                    Log.d("BSB" , "hidden") ;
                    dismiss();
                }
                case BottomSheetBehavior.STATE_DRAGGING: {
                    Log.d("BSB","dragging") ;
                }
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            Log.d("BSB","sliding " + slideOffset ) ;
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.fragment_advertiser_payout_bottomsheet, null);
        dialog.setContentView(contentView);
        mContentView = contentView;

        mPayoutOptionsLayout = contentView.findViewById(R.id.payoutOptions);
        mEnterPayoutDetailsPart = contentView.findViewById(R.id.enterPayoutDetailsPart);
        mConfirmLayout = contentView.findViewById(R.id.confirmLayout);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        contentView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        contentView.findViewById(R.id.continueButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPayoutOptionsLayout.setVisibility(View.GONE);
                showPayoutDetailsPart();
            }
        });

        contentView.findViewById(R.id.cancelBtn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

    private void showPayoutDetailsPart(){
        mEnterPayoutDetailsPart.setVisibility(View.VISIBLE);
        mEnterPayoutDetailsPart.animate().translationX(0).setDuration(150);
        final EditText phoneEdit = mContentView.findViewById(R.id.phoneEditText);
        final EditText passwordEdit = mContentView.findViewById(R.id.passwordEditText);

        final Button continueBtn = mContentView.findViewById(R.id.continueButton2);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phoneEdit.getText().toString().trim().equals("")){
                    phoneEdit.setError("We need your phone number.");
                }else if(passwordEdit.getText().toString().trim().equals("")){
                    passwordEdit.setError("We need your password.");
                }else if(phoneEdit.getText().toString().trim().length()<10){
                    phoneEdit.setError("That's not a real phone number.");
                }else{
                    String phoneNo = phoneEdit.getText().toString().trim();
                    try{
                        Integer.parseInt(phoneNo);
                        String password = passwordEdit.getText().toString().trim();
                        if(!password.equals(mPassword)){
                            passwordEdit.setError("That's not your password!");
                        }else{
                            mEnterPayoutDetailsPart.setVisibility(View.GONE);
                            mPhoneNo = phoneNo;
                            showConfirmDetailsPart();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        phoneEdit.setError("That's not a real phone number.");
                    }

                }
            }
        });
        passwordEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    continueBtn.performClick();
                    Log.i("FragmentPaymentsDetails","Enter pressed");
                }
                return false;
            }
        });
    }

    private void showConfirmDetailsPart(){
        mConfirmLayout.setVisibility(View.VISIBLE);
        mConfirmLayout.animate().translationX(0).setDuration(150);

        TextView amountToBeSentView = mContentView.findViewById(R.id.amountToBeSent);
        TextView phoneNumberView = mContentView.findViewById(R.id.phoneNumber);
        Button continueBtn = mContentView.findViewById(R.id.startButton);

        amountToBeSentView.setText("Amount To Be Sent: "+mTotals+" Ksh.");
        phoneNumberView.setText("Phone number: "+mPhoneNo);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variables.phoneNo = mPhoneNo;
                dismiss();
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent("START_PAYOUT"));
            }
        });

    }
}
