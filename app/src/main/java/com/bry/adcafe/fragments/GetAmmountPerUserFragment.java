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

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;

/**
 * Created by bryon on 14/02/2018.
 */

public class GetAmmountPerUserFragment extends DialogFragment {
    private Context mContext;

    public void setContext(Context context){
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.dialog6, container, false);
        Button b1 = (Button) rootView.findViewById(R.id.submitButton);
        Button b2 = (Button) rootView.findViewById(R.id.cancelButton);
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
