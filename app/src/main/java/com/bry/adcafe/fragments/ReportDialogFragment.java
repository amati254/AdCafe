package com.bry.adcafe.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReportDialogFragment extends DialogFragment {
    private Context mContext;
    private String mKey = "";

    public void setfragcontext(Context context){
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_report_dialog, container, false);
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
                RadioGroup surveyRadioGroup = (RadioGroup) rootView.findViewById(R.id.reportRadioGroup);
                final int selectedId = surveyRadioGroup.getCheckedRadioButtonId();
                final RadioButton selectedRadioButton = (RadioButton) rootView.findViewById((selectedId));

                Log.d("ReportDialog---",selectedRadioButton.getText().toString());
                Log.d("ReportDialog---","Ad being reported is : "+ Variables.currentAdvert.getPushId());

                flagTheAd(selectedRadioButton.getText().toString());
            }
        });

        return rootView;
    }

    private void flagTheAd(String Message) {
        DatabaseReference mRef3 = FirebaseDatabase.getInstance().getReference(Constants.REPORTED_ADS).child(getDate())
                .child(Integer.toString(User.getClusterID(mKey)))
                .child(Variables.currentAdvert.getPushId());
        mRef3.setValue(Message);
        Toast.makeText(mContext,"Duly reported.",Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private String getDate(){
        long date = System.currentTimeMillis();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        String MonthString = sdfMonth.format(date);

        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        String dayString = sdfDay.format(date);

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        String yearString = sdfYear.format(date);

        Calendar c = Calendar.getInstance();
        String yy = Integer.toString(c.get(Calendar.YEAR));
        String mm = Integer.toString(c.get(Calendar.MONTH)+1);
        String dd = Integer.toString(c.get(Calendar.DAY_OF_MONTH));

        String todaysDate = (dd+":"+mm+":"+yy);

        return todaysDate;
    }
}
