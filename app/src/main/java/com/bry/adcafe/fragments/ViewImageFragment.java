package com.bry.adcafe.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by bryon on 20/11/2017.
 */

public class ViewImageFragment extends DialogFragment {
    private Context mContext;
    private String mKey = "";
    private ImageView mImageView;
    private ImageButton mBackButton;
    private ImageButton mShareButton;
    private ImageButton mWebsiteLink;
//    private ImageButton mDeleteButton;
    private Advert mAdvert;

    public void setfragcontext(Context context){
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_view_image_dialog, container, false);
        mBackButton = (ImageButton) rootView.findViewById(R.id.backBtn);
        mShareButton = (ImageButton) rootView.findViewById(R.id.shareBtn);
        mWebsiteLink = (ImageButton) rootView.findViewById(R.id.Website);
//        mDeleteButton = (ImageButton) rootView.findViewById(R.id.Delete);
//        if(mAdvert.getWebsiteLink()==null) mWebsiteLink.setAlpha(0.1f);
        mImageView = (ImageView) rootView.findViewById(R.id.theAdImage);
        mAdvert = Variables.adToBeViewed;
        mImageView.setImageBitmap(mAdvert.getImageBitmap());

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ImageFragment","Setting ad to be shared.");
                Variables.adToBeShared = mAdvert;
                Intent intent = new Intent("SHARE");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });
        mWebsiteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(mAdvert.getWebsiteLink()!=null){
//                    Toast.makeText(mContext,"This will take user to website",Toast.LENGTH_SHORT).show();
//                }
            }
        });

//        mDeleteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });



        return rootView;
    }
}
