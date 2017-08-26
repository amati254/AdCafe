package com.bry.adstudio.adapters;

import android.content.Context;
import android.widget.TextView;

import com.bry.adstudio.R;
import com.bry.adstudio.Variables;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 26/08/2017.
 */

@Layout(R.layout.top_bar_view)
public class AdCounterBar {
    @View(R.id.adCounter)
    private TextView adCounter;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;

    public AdCounterBar(Context context, PlaceHolderView PlaceHolderView){
        mContext = context;
        mPlaceHolderView = PlaceHolderView;
    }
    @Resolve
    private void onResolved() {
        adCounter.setText(Integer.toString(Variables.numberOfAds));
    }

    @LongClick(R.id.adCounter)
    private void onLongClick(){

    }

}
