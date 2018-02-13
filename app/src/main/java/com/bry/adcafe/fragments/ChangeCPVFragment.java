package com.bry.adcafe.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bry.adcafe.R;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.ui.Dashboard;

/**
 * Created by bryon on 13/02/2018.
 */

public class ChangeCPVFragment extends DialogFragment implements View.OnClickListener{
    private Button cancelBtn1;
    private Button continueBtn;
    private Button cancelBtn2;
    private Button changeBtn;

    private LinearLayout mainLayout;
    private LinearLayout chooseAmountLayout;
    private Context mContext;
    private View mRootView;

    public void setContext(Context context){
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_change_cpv, container, false);
        mRootView = rootView;
        cancelBtn1 = (Button) rootView.findViewById(R.id.cancelBtn);
        continueBtn = (Button) rootView.findViewById(R.id.continueBtn);
        cancelBtn2 = (Button) rootView.findViewById(R.id.cancelButton);
        changeBtn = (Button) rootView.findViewById(R.id. submitButton);

        mainLayout = (LinearLayout) rootView.findViewById(R.id.mainLayout);
        chooseAmountLayout = (LinearLayout) rootView.findViewById(R.id.chooseAmountLayout);
        onclicks();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(v == cancelBtn1){
            dismiss();
        } else if(v == cancelBtn2){
            dismiss();
        } else if(v == continueBtn){
            mainLayout.setVisibility(View.GONE);
            chooseAmountLayout.animate().setDuration(140).translationX(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                }
            });
        }else if(v == changeBtn){
            int cpv;
            RadioButton button3 = (RadioButton) mRootView.findViewById(R.id.radioButton3);
            RadioButton button5 = (RadioButton) mRootView.findViewById(R.id.radioButton5);
            RadioButton button8 = (RadioButton) mRootView.findViewById(R.id.radioButton8);
            if(button3.isChecked()){
                cpv = 3;
            }else if(button5.isChecked()){
                cpv = 5;
            }else{
                cpv = 8;
            }
            if(isNetworkConnected(mContext)) makeChanges(cpv);
            else Toast.makeText(mContext,"You need an internet connection to make that change.",Toast.LENGTH_SHORT).show();
        }
    }

    private void onclicks(){
        cancelBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        cancelBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainLayout.setVisibility(View.GONE);
                chooseAmountLayout.setVisibility(View.VISIBLE);
                chooseAmountLayout.animate().setDuration(140).translationX(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
            }
        });
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cpv;
                RadioButton button3 = (RadioButton) mRootView.findViewById(R.id.radioButton1);
                RadioButton button5 = (RadioButton) mRootView.findViewById(R.id.radioButton3);
                RadioButton button8 = (RadioButton) mRootView.findViewById(R.id.radioButton6);
                if(button3.isChecked()) cpv = 1;
                else if(button5.isChecked()) cpv = 3;
                else cpv = 6;
                if(isNetworkConnected(mContext)) makeChanges(cpv);
                else Toast.makeText(mContext,"You need an internet connection to make that change.",Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void makeChanges(int newCpv) {
        new DatabaseManager().setBooleanForResetSubscriptions(newCpv,mContext);
        Intent intent = new Intent("SHOW_PROMPT");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        dismiss();
    }


    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }
}
