package com.bry.adcafe.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;

import java.util.LinkedHashMap;

/**
 * Created by bryon on 14/02/2018.
 */

public class GetAmmountPerUserFragment extends DialogFragment {
    private Context mContext;
    private LinkedHashMap<Integer,LinkedHashMap<String,Long>> userStats = new LinkedHashMap<>();

    public void setContext(Context context){
        this.mContext = context;
    }

    public void setStats(LinkedHashMap<Integer,LinkedHashMap<String,Long>> theStats){
        userStats = theStats;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.dialog6, container, false);
        Button b1 = (Button) rootView.findViewById(R.id.submitButton);
        Button b2 = (Button) rootView.findViewById(R.id.cancelButton);

        TextView t3 = (TextView) rootView.findViewById(R.id.shStats3);
        TextView t5 = (TextView) rootView.findViewById(R.id.shStats5);
        TextView t8 = (TextView) rootView.findViewById(R.id.shStats8);
        long numberOfUsersIn1;
        long numberOfUsersIn3;
        long numberOfUsersIn6;

        if (userStats.containsKey(1)) {
            if(userStats.get(1).containsKey(Variables.SelectedCategory)) {
                numberOfUsersIn1 = userStats.get(1).get(Variables.SelectedCategory);
            } else numberOfUsersIn1 = 0;
        } else numberOfUsersIn1 = 0;

        if (userStats.containsKey(3)) {
            if (userStats.get(3).containsKey(Variables.SelectedCategory)) {
                numberOfUsersIn3 = userStats.get(3).get(Variables.SelectedCategory);
            } else numberOfUsersIn3 = 0;
        }else numberOfUsersIn3 = 0;

        if (userStats.containsKey(6)){
            if(userStats.get(6).containsKey(Variables.SelectedCategory)) {
                numberOfUsersIn6 = userStats.get(6).get(Variables.SelectedCategory);
            }else numberOfUsersIn6 = 0;
        } else numberOfUsersIn6 = 0;

        t3.setText(String.format("Number of users for 3Ksh is: %s", numberOfUsersIn1));
        t5.setText(String.format("Number of users for 5Ksh is: %s", numberOfUsersIn3));
        t8.setText(String.format("Number of users for 8Ksh is: %s", numberOfUsersIn6));
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cpv;
                RadioButton button3 = (RadioButton) rootView.findViewById(R.id.radioButton3);
                RadioButton button5 = (RadioButton) rootView.findViewById(R.id.radioButton5);
                RadioButton button8 = (RadioButton) rootView.findViewById(R.id.radioButton8);
                if(button3.isChecked()){
                    cpv = 3;
                }else if(button5.isChecked()){
                    cpv = 5;
                }else{
                    cpv = 8;
                }
                Variables.amountToPayPerTargetedView = cpv;
                Intent intent = new Intent("START_NEXT_ACTIVITY");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return rootView;
    }

}
