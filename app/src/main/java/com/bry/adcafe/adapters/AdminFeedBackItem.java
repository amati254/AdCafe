package com.bry.adcafe.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 15/12/2017.
 */

@NonReusable
@Layout(R.layout.admin_feedback_item)
public class AdminFeedBackItem {
    @View(R.id.feedbackText) private TextView feedBackView;
    @View(R.id.feedbackUser) private TextView feedBackUserVew;
    @View(R.id.feedbackType) private TextView feedBackTypeView;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String pushId;
    private String feedBack;
    private String FeedbackUser;
    private String feedBackType;


    public AdminFeedBackItem(Context context, PlaceHolderView placeHV,String pushid,String feedback,String user,String feedbackType){
        this.mContext = context;
        this.mPlaceHolderView = placeHV;
        this.pushId = pushid;
        this.feedBack = feedback;
        this.FeedbackUser = user;
        this.feedBackType = feedbackType;
    }

    @Resolve
    private void onResolved(){
        feedBackView.setText(feedBack);
        feedBackTypeView.setText(feedBackType);
        feedBackUserVew.setText(FeedbackUser);
    }

    @LongClick(R.id.adminFeedBackItemLayout)
    private void onLongClick(){
        removeFeedback();
    }

    private void promptAdminToRemoveFeedback() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("AdCafe.");
        builder.setMessage("Are you sure you want remove this piece of feedBack?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeFeedback();
                    }
                })
                .setNegativeButton("No.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void removeFeedback() {
        Toast.makeText(mContext,"Removing Feedback item : "+pushId,Toast.LENGTH_LONG).show();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.FEEDBACK).child(pushId);
        mRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(mContext,"Done.",Toast.LENGTH_SHORT).show();
                removeView();
            }
        });
    }

    private void removeView() {
        mPlaceHolderView.removeView(this);
    }

}
