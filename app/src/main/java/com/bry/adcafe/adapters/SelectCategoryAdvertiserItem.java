package com.bry.adcafe.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.ui.AdUpload;
import com.bry.adcafe.ui.SelectCategoryAdvertiser;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 01/12/2017.
 */

@NonReusable
@Layout(R.layout.select_category_advertiser_list_item)
public class SelectCategoryAdvertiserItem {
    @View(R.id.cat_name) private TextView categoryName;
    @View(R.id.cat_details) private TextView categoryDetails;
    @View(R.id.categoryView) private LinearLayout categoryView;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String category;
    private String details;


    public SelectCategoryAdvertiserItem(Context context, PlaceHolderView placeHV, String Category, String Details) {
        this.mContext = context;
        this.mPlaceHolderView = placeHV;
        this.category = Category;
        this.details = Details;
    }

    @Resolve
    private void onResolve(){
        categoryName.setText(category);
        categoryDetails.setText(details);
    }

    @Click(R.id.categoryView)
    private void onClick(){
        Variables.SelectedCategory = category;
        Intent intent = new Intent("SELECTED_CATEGORY");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
