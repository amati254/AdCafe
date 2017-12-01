package com.bry.adcafe.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bry.adcafe.R;

/**
 * Created by bryon on 30/11/2017.
 */

public class SelectCategoryFragment extends DialogFragment {
    private Context mContext;
    private String mKey = "";

    public void setFragContext(Context context){
        mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_report_dialog, container, false);

        return rootView;
    }

}
