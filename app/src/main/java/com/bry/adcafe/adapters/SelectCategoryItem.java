package com.bry.adcafe.adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 29/11/2017.
 */

@NonReusable
@Layout(R.layout.select_category_item)
public class SelectCategoryItem {
    @View(R.id.cat_name) private TextView categoryName;
    @View(R.id.cat_details) private TextView categoryDetails;
    @View(R.id.cat_select) private CheckBox checkBox;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String category;
    private String details;
    private boolean isChecked;

    public SelectCategoryItem(Context context, PlaceHolderView placeHV,String Category,String Details,boolean isChecked){
        this.mContext = context;
        this.mPlaceHolderView = placeHV;
        this.category = Category;
        this.details = Details;
        this.isChecked = isChecked;
    }

    @Resolve
    private void onResolved() {
        categoryName.setText(category);
        categoryDetails.setText(details);
        checkBox.setChecked(isChecked);
    }

    @Click(R.id.cat_select)
    private void onClick(){
        if(isChecked){
            checkBox.setChecked(false);
            Variables.selectedCategoriesToSubscribeTo.remove(category);
            isChecked = false;
            Log.d("SelectCategoryItem - ","Removing category - "+category);
        }else{
            checkBox.setChecked(true);
            Variables.selectedCategoriesToSubscribeTo.add(category);
            isChecked = true;
            Log.d("SelectCategoryItem - ","Adding category - "+category);
        }
    }

}
