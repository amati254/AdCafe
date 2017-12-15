package com.bry.adcafe.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bumptech.glide.Glide;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by bryon on 15/12/2017.
 */

@NonReusable
@Layout(R.layout.admin_ads_item)
public class AdminAdsItem {
    @View(R.id.EmailText) private TextView mEmail;
    @View(R.id.TargetedNumber) private TextView mTargetedNumber;
    @View(R.id.ammountPaid) private TextView mAmountToReimburse;
    @View(R.id.adImage) private ImageView mImage;
    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;

    public AdminAdsItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    private void onResolved(){
        mEmail.setText("Uploaded by : "+mAdvert.getUserEmail());
        mTargetedNumber.setText(String.format("No. of users targeted : %d", mAdvert.getNumberOfUsersToReach()));
        String ammount = Integer.toString(mAdvert.getNumberOfUsersToReach()*4);
        mAmountToReimburse.setText(String.format("Reimbursing amount : %s", ammount));

        try {
//            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
//            mAdvert.setImageBitmap(bm);
            Glide.with(mContext).load(bitmapToByte(getResizedBitmap(decodeFromFirebaseBase64(mAdvert.getImageUrl()),400)))
                    .into(mImage);
            Log.d("AdminAdsItem---","Image has been converted to bitmap and set in model instance.");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext,"something went wrong"+e.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    @Click(R.id.takeDownButton)
    private void onClick(){
        Variables.adToBeFlagged = mAdvert;
        Intent intent = new Intent("TAKE_DOWN");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
