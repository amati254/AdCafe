package com.bry.adstudio.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bry.adstudio.Constants;
import com.bry.adstudio.R;

public class ReportDialogFragment extends DialogFragment {
    private Context mContext;

    public void setfragcontext(Context context){
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_report_dialog, container, false);
        RadioGroup surveyRadioGroup = (RadioGroup) rootView.findViewById(R.id.reportRadioGroup);
        int selectedId = surveyRadioGroup.getCheckedRadioButtonId();
        final RadioButton selectedRadioButton = (RadioButton) rootView.findViewById((selectedId));

        Button cancelButton = (Button) rootView.findViewById(R.id.cancelButton);
        Button submitButton = (Button) rootView.findViewById(R.id.submitButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("testing",selectedRadioButton.getText().toString());
                Toast.makeText(mContext,"Duly reported.",Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });



        return rootView;
    }

}
