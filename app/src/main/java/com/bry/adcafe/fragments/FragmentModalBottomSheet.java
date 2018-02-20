package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.cardform.view.CardForm;
import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;

/**
 * Created by bryon on 04/02/2018.
 */

public class FragmentModalBottomSheet extends BottomSheetDialogFragment {
    private Activity mActivity;
    private LinearLayout mEnterCardDetailsPart;
    private Button mContinueButton;
    private LinearLayout ConfirmDetailsPart;
    private Button mStartTransactionThenUpload;
    private LinearLayout CardHolderDetailsPart;

    private long mTargetedUsers = 1000;
    private long mConstantAmountPerUserTargeted = 5;
    private String mAdViewingDate = "19:2:2018";
    private String mCategory = "food";
    private String mUploaderEmail = "john.doe@yaymail.com";
    private String mName = "John Doe";
    private long mAmountToBePaid = (long) ((mTargetedUsers*mConstantAmountPerUserTargeted)+
                ((mTargetedUsers*mConstantAmountPerUserTargeted)*Constants.TOTAL_PAYOUT_PERCENTAGE));
    private View mContentView;


    public void setActivity(Activity activity){
        this.mActivity = activity;
    }

    public void setDetails(long targetedUsers, long constantAmountPerUserTargeted, String adViewingDate,
                           String category, String uploaderEmail, String name){
        this.mTargetedUsers = targetedUsers;
        this.mConstantAmountPerUserTargeted = constantAmountPerUserTargeted;
        this.mAdViewingDate = adViewingDate;
        this.mCategory = category;
        this.mUploaderEmail = uploaderEmail;
        this.mName = name;
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
//        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_modal_bottomsheet, null);
        dialog.setContentView(contentView);
        mContentView  = contentView;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        mEnterCardDetailsPart = contentView.findViewById(R.id.enterCardDetailsPart);
        mContinueButton = contentView.findViewById(R.id.continueButton);
        ConfirmDetailsPart = contentView.findViewById(R.id.confirmLayout);
        mStartTransactionThenUpload = contentView.findViewById(R.id.startButton);
        CardHolderDetailsPart = contentView.findViewById(R.id.cardHolderDetailsLayout);

        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        setUpPaymentsCard(contentView);
    }

    private void setUpPaymentsCard(View contentView) {
        final CardForm cardForm = contentView.findViewById(R.id.card_form);
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .postalCodeRequired(true)
                .mobileNumberRequired(false)
                .actionLabel("Purchase")
                .setup(mActivity);

        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cardForm.isValid()){
                    mEnterCardDetailsPart.setVisibility(View.GONE);
                    showNextSlide2();
                    setDetailsForConfirmation(cardForm);
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    Variables.cvv = cardForm.getCvv();
                    Variables.cardNumber = cardForm.getCardNumber();
                    Variables.postalCode = cardForm.getPostalCode();
                    Variables.expiration = ""+cardForm.getExpirationMonth()+cardForm.getExpirationYear();
                }
                else Toast.makeText(mActivity.getApplication(),"Please use valid details.",Toast.LENGTH_SHORT).show();
            }
        });

        mContentView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        cardForm.getPostalCodeEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    mContinueButton.performClick();
                    Log.i("FragmentPaymentsDetails","Enter pressed");
                }
                return false;
            }
        });

    }

    private void showNextSlide(){
        CardHolderDetailsPart.setVisibility(View.VISIBLE);
        CardHolderDetailsPart.animate().setDuration(140).translationX(0);
        final EditText name =  mContentView.findViewById(R.id.nameEditText);
        final EditText email = mContentView.findViewById(R.id.emailEditText);
        final EditText state = mContentView.findViewById(R.id.stateEditText);
        final EditText phone = mContentView.findViewById(R.id.phoneEditText);

        email.setText(mUploaderEmail);
        name.setText(mName);

        mContentView.findViewById(R.id.cancelCHDBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        phone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    mContentView.findViewById(R.id.continueCHDBtn).performClick();
                    Log.i("FragmentPaymentsDetails","Enter pressed");
                }
                return false;
            }
        });
        mContentView.findViewById(R.id.continueCHDBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                String nameString = name.getText().toString().trim();
                String emailString = email.getText().toString().trim();
                String stateText = state.getText().toString().trim();
                String phoneText = phone.getText().toString().trim();

                if(nameString.equals("")){
                    name.setError("We need your name.");
                }else if(emailString.equals("")){
                    email.setError("We need your email.");
                }else if(stateText.equals("")){
                    state.setError("We need your Province/county.");
                }else if(phoneText.equals("")){
                    phone.setError("We need your Phone Number.");
                }else{
                    CardHolderDetailsPart.setVisibility(View.GONE);
                    showNextSlide2();
                }
            }
        });
    }

    private void showNextSlide2() {
        startNextAnimation();
    }

    private void startNextAnimation(){
        ConfirmDetailsPart.setVisibility(View.VISIBLE);
        ConfirmDetailsPart.animate().setDuration(140).translationX(0);
        mStartTransactionThenUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent("START_PAYMENTS_INTENT"));
            }
        });

    }

    private void setDetailsForConfirmation(CardForm card) {
        TextView targetingView = mContentView.findViewById(R.id.targetingNumber);
        TextView dateView = mContentView.findViewById(R.id.date);
        TextView categoryView = mContentView.findViewById(R.id.category);
        TextView userEmailView = mContentView.findViewById(R.id.userEmail);
        TextView amountToBePaidView = mContentView.findViewById(R.id.amountToBePaid);
        TextView cardToPayVew = mContentView.findViewById(R.id.cardNumber);

        String strLastFourDi = card.getCardNumber().length() >= 4 ? card.getCardNumber().substring(card.getCardNumber().length() - 4): "";

        double chargePayment = (mAmountToBePaid* Constants.PAYMENT_TRANSFER_PERENTAGE)+(mAmountToBePaid*Constants.PAYOUT_TRANSFER_FEE);

        targetingView.setText(Html.fromHtml("Targeting : <b>" + Long.toString(mTargetedUsers) + " users.</b>"));
        dateView.setText(Html.fromHtml("Ad Viewing Date : <b>" + mAdViewingDate + "</b>"));
        categoryView.setText(Html.fromHtml("Category : <b>" + mCategory + "</b>"));
        userEmailView.setText(Html.fromHtml("Uploader : <b>" + mUploaderEmail + "</b>"));
        amountToBePaidView.setText(Html.fromHtml("Amount To Be Paid: <b>" + Long.toString(mAmountToBePaid) + "Ksh.</b>"));
        cardToPayVew.setText(Html.fromHtml("Paying card number : <b>****" + strLastFourDi + "</b>"));



    }


}
