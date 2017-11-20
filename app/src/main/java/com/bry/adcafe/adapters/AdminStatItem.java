package com.bry.adcafe.adapters;

import android.content.Context;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.models.Advert;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 19/11/2017.
 */

@NonReusable
@Layout(R.layout.admin_stats_item)
public class AdminStatItem {
    @View(R.id.EmailText) private TextView mEmail;
    @View(R.id.TargetedNumber) private TextView mTargetedNumber;
    @View(R.id.NumberReached) private TextView mNumberReached;
    @View(R.id.AmountToReemburse) private TextView mAmountToReimburse;
    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;

    public AdminStatItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    private void onResolved(){
        mEmail.setText(mAdvert.getUserEmail());
        mTargetedNumber.setText("Targeted : "+mAdvert.getNumberOfUsersToReach()+" users");
        mNumberReached.setText("Number Reached : "+mAdvert.getNumberOfTimesSeen()+" users.");

        int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen();
        String number = Integer.toString(numberOfUsersWhoDidntSeeAd);
        mAmountToReimburse.setText("Amount to reimburse : "+number+" Ksh.");
    }

}
