package com.bry.adcafe.adapters;

import android.content.Context;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 01/01/2018.
 */

@NonReusable
@Layout(R.layout.ad_stats_date_item)
public class DateForAdStats {
    @View(R.id.dateText) private TextView mDateTextView;
    private String mDateText;
    private Context mContext;
    private PlaceHolderView mPlaceHolderView;

    public DateForAdStats(Context context, String dateText, PlaceHolderView phview){
        this.mContext = context;
        this.mDateText = dateText;
        this.mPlaceHolderView = phview;
    }

    @Resolve
    private void onResolved(){
        mDateTextView.setText(mDateText);
    }
}
