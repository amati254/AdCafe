package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeHead;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeTouch;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by bryon on 6/11/2017.
 */

//@Animate(Animation.CARD_RIGHT_IN_DESC)
@Layout(R.layout.ad_card_view)
public class AdvertCard{
    @View(R.id.profileImageView) private ImageView profileImageView;
    @View(R.id.errorImageView) private ImageView errorImageView;
    @View(R.id.adCardAvi) private AVLoadingIndicatorView mAvi;
    @View(R.id.WebsiteIcon) private ImageView webIcon;
    @View(R.id.websiteText) private TextView webText;
    @View(R.id.smallDot) private android.view.View mDot;
    @View(R.id.bookmark2Btn) private ImageView bookmarkBtn;
    @View(R.id.reportBtn) private ImageView reportBtn;


    private Advert mAdvert;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private static final String START_TIMER= "startTimer";
    private String mKey = "";

    private static boolean clickable;
    private static String mLastOrNotLast;
    private static boolean mIsNoAds;
    private static boolean hasAdLoaded;
    private boolean hasBeenSwiped = true;
    private Bitmap bs;
    private String igsNein = "none";
    private byte[] mImageBytes;

    private int amount = 0;
    private double mDistance = 0;
    private List<Bitmap> blurredImageList = new ArrayList<>();
    private LongOperationBL BackgroundBlurrProcess;
    private boolean isSupposedToStartTimer = false;
    private boolean isBackgroundTaskRunning = false;
    private boolean needToContinueBackground = false;


    private int positionBL = 0;
    private boolean isFirstCard = false;


    public AdvertCard(Context context, Advert advert, SwipePlaceHolderView swipeView,String lastOrNotLast){
        mContext = context;
        mAdvert = advert;
        mSwipeView = swipeView;
        mLastOrNotLast = lastOrNotLast;
        Variables.hasBeenPinned = false;
    }

    @Resolve
    private void onResolved(){
        if(mLastOrNotLast.equals(Constants.NO_ADS)) loadAdPlaceHolderImage();
        else new LongOperationFI().execute("");

        setListeners();
    }

    private void setListeners(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,
                new IntentFilter(Constants.UNREGISTER_ALL_RECEIVERS));

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForTimerHasEnded,
                new IntentFilter(Constants.TIMER_HAS_ENDED));

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnblurrImage,
                new IntentFilter("UNBLURR_IMAGE"+mAdvert.getPushRefInAdminConsole()));

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToStartTimer,
                new IntentFilter("START_TIMER_NOW"));

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForTimerStarted,
                new IntentFilter(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER));

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSetImageForSharing,
                new IntentFilter("SET_IMAGE_FOR_SHARING"+mAdvert.getPushRefInAdminConsole()));
    }

    private void setUpListOfBlurrs(){
        for(int i = 0;i<3;i++){
            blurredImageList.add(bs);
        }
        if(Variables.hasTimerStarted){
            needToContinueBackground = true;
        }else{
            BackgroundBlurrProcess =  new LongOperationBL();
            BackgroundBlurrProcess.execute();
        }
    }

    private void setImage() {
        try {
            bs = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            Log.d("SavedAdsCard---","Image has been converted to bitmap.");
            mImageBytes = bitmapToByte(bs);
            mAdvert.setImageBitmap(bs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void loadAdPlaceHolderImage() {
        mIsNoAds = true;
//        MultiTransformation multi = new MultiTransformation(new BlurTransformation(mContext, 30));
        Glide.with(mContext).load(R.drawable.noads5).into(profileImageView);
        lockViews();
        clickable=false;
        Variables.setCurrentAdvert(mAdvert);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        bs = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.noads5, options);
        setUpListOfBlurrs();
    }

    private void loadAllAds(){
        Log.d("ADVERT_CARD--","LOADING AD NORMALLY.");
        MultiTransformation multi = new MultiTransformation(new BlurTransformation(mContext, 30));
        Glide.with(mContext).load(mImageBytes).bitmapTransform(multi).listener(new RequestListener<byte[], GlideDrawable>() {
            @Override
            public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                Log.d("ADVERT_CARD--","The image has failed to load due to error."+e.getMessage());
                errorImageView.setVisibility(android.view.View.VISIBLE);
                mAvi.setVisibility(android.view.View.GONE);
                if(isFirstResource) {unLockViews();}
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                Log.d("ADVERT_CARD--","The image has loaded successfully");
                mAvi.setVisibility(android.view.View.GONE);
                errorImageView.setVisibility(android.view.View.GONE);
                setUpListOfBlurrs();
                if(Variables.getAdFromVariablesAdList(Variables.getCurrentAdNumberForAllAdsList()).getPushRefInAdminConsole().equals(mAdvert.getPushRefInAdminConsole())) profileImageView.setImageBitmap(bs);
                if(isFirstResource && mLastOrNotLast.equals(Constants.NOT_LAST) && !mLastOrNotLast.equals(Constants.ANNOUNCEMENTS)) {
                    Log.d("ADVERT_CARD---","sending broadcast to start timer...");
                    isSupposedToStartTimer = true;
                    isFirstCard = true;
                    if(!mAdvert.getWebsiteLink().equals(igsNein)){
                        webIcon.setAlpha(1.0f);
                        webText.setAlpha(1.0f);
                    }
                    setLastAdSeen();
                }else{
                    Log.d("ADVERT_CARD","not starting timer because:");
                    Log.d("ADVERT_CARD","is first resource is supposed to be true and is: "+isFirstResource);
                    Log.d("ADVERT_CARD","mLastOrNotLast is :" +mLastOrNotLast+" and is supposed to be "+Constants.NOT_LAST);
                    Log.d("ADVERT_CARD","mLastOrNotLast is :" +mLastOrNotLast+" and is not supposed to be: "+Constants.ANNOUNCEMENTS);
                }
                clickable=false;
                return false;
            }
        }).into(profileImageView);
    }

    private void loadOnlyLastAd(){
        Log.d("ADVERT_CARD--","LOADING ONLY LAST AD.");
        lockViews();
//        MultiTransformation multi = new MultiTransformation(new BlurTransformation(mContext, 30));
        Glide.with(mContext).load(mImageBytes).listener(new RequestListener<byte[], GlideDrawable>() {
            @Override
            public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                errorImageView.setVisibility(android.view.View.VISIBLE);
                mAvi.setVisibility(android.view.View.GONE);
                hasAdLoaded = false;
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                mAvi.setVisibility(android.view.View.GONE);
                errorImageView.setVisibility(android.view.View.GONE);
                setUpListOfBlurrs();
                hasAdLoaded = true;
                return false;
            }
        }).into(profileImageView);
        lockViews();
        clickable=false;
        Variables.setCurrentAdvert(mAdvert);
        if(!mAdvert.getWebsiteLink().equals(igsNein)){
            webIcon.setAlpha(1.0f);
            webText.setAlpha(1.0f);
        }
//        sendBroadcast(Constants.LAST);
    }



    @Click(R.id.profileImageView)
    private void onClick(){
        Log.d("EVENT", "profileImageView click");
        if (clickable) {
            mSwipeView.enableTouchSwipe();
            hasBeenSwiped = true;
        }
        if(mLastOrNotLast.equals(Constants.ANNOUNCEMENTS) || mLastOrNotLast.equals(Constants.LAST)){
            mSwipeView.enableTouchSwipe();
        }

    }

    @SwipeOut
    private void onSwipedOut(){
        Log.d("EVENT----", "onSwipedOut");
        if(!mLastOrNotLast.equals(Constants.ANNOUNCEMENTS)){
//            Variables.removeAd();
            hasBeenSwiped = true;
//            sendBroadcast(START_TIMER);
        }
//        if(mSwipeView.getChildCount()==2 && mLastOrNotLast.equals(Constants.ANNOUNCEMENTS)){
//            Toast.makeText(mContext,"That's all we have today.",Toast.LENGTH_SHORT).show();
//            lockViews();
//        }
//        Intent intent = new Intent("SWIPED");
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @SwipeIn
    private void onSwipeIn(){
        Log.d("EVENT----", "onSwipedIn");
        if(!mLastOrNotLast.equals(Constants.ANNOUNCEMENTS)){
//            Variables.removeAd();
            hasBeenSwiped = true;
//            sendBroadcast(START_TIMER);
        }
//        if(mSwipeView.getChildCount()==2 && mLastOrNotLast.equals(Constants.ANNOUNCEMENTS)){
//            Toast.makeText(mContext,"That's all we have today.",Toast.LENGTH_SHORT).show();
//            lockViews();
//        }
//        Intent intent = new Intent("SWIPED");
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }




    @SwipeHead
    private void onSwipeHeadCard() {
        Log.d("EVENT----------", "onSwipeHeadCard");
        profileImageView.setImageBitmap(bs);
        Log.d("AdvertCard","Set the normal image to the image view");

        if(!firstAd()){
            Log.d("AdvertCard","Card is not first one so continuing process of starting timer...");
            if(mLastOrNotLast.equals(Constants.NOT_LAST)||mLastOrNotLast.equals(Constants.LOAD_MORE_ADS)){
                Log.d("AdvertCard","mLastOrNotLast is : "+mLastOrNotLast+" So starting process of sending timer");
                Variables.removeAd();
                sendBroadcast(START_TIMER);
            }
        }

        if(mLastOrNotLast.equals(Constants.ANNOUNCEMENTS)){
            if(mSwipeView.getChildCount()==1){
                Toast.makeText(mContext,"That's all we have today.",Toast.LENGTH_SHORT).show();
                lockViews();
            }
            setBottomButtonsToBeTranslucent();
            Intent intent = new Intent("SWIPED");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    private void setBottomButtonsToBeTranslucent(){
        webIcon.setAlpha(0.3f);
        webText.setAlpha(0.3f);
        mDot.setAlpha(0.0f);
        bookmarkBtn.setAlpha(0.3f);
        reportBtn.setAlpha(0.3f);
    }

    private boolean firstAd() {
       return mAdvert.getPushRefInAdminConsole().equals(Variables.firstAd.getPushRefInAdminConsole());
    }




    private void sendBroadcast(String message ) {
        if(message.equals(START_TIMER)){
            Log.d("AdvertCard - ", "Sending message to start timer");
            lockViews();
            clickable = false;
            setBooleanForResumingTimer();
            Intent intent = new Intent(Constants.ADVERT_CARD_BROADCAST_TO_START_TIMER);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            Variables.hasBeenPinned = false;
            setLastAdSeen();
            if(mSwipeView.getChildCount()<3) sendBroadcast(Constants.LOAD_MORE_ADS);
        }else if(message.equals(Constants.LAST)){
            Intent intent = new Intent(Constants.LAST);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
//            setLastAdSeen();
        }else if(message.equals(Constants.LOAD_MORE_ADS)){
            Intent intent = new Intent(Constants.LOAD_MORE_ADS);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    private void setLastAdSeen(){
        Variables.setLastSeenAd(Variables.getAdFromVariablesAdList(Variables.getCurrentAdNumberForAllAdsList()).getPushId());
        Variables.setCurrentAdvert(Variables.getAdFromVariablesAdList(Variables.getCurrentAdNumberForAllAdsList()));
        Log.d("ADVERT-CARD","Setting the current advert to ad - "+Variables.getCurrentAdvert().getPushRefInAdminConsole());
        Log.d("ADVERT_CARD---","Setting the last ad seen in Variables class... "+
                Variables.getCurrentAdvert().getPushRefInAdminConsole());
    }




    private BroadcastReceiver mMessageReceiverForTimerStarted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseBackgroundTasks();
        }
    };

    private void pauseBackgroundTasks(){
        if(isBackgroundTaskRunning){
            Log.d("AdvertCard","Pausing background task since timer is starting.");
            BackgroundBlurrProcess.cancel(true);
            needToContinueBackground = true;
        }
    }

    private void resumeBackgroundTasksIfRunning(){
        if(needToContinueBackground){
            Log.d("AdvertCard","Resuming background task since process wasn't finished");
            BackgroundBlurrProcess = null;
            BackgroundBlurrProcess =  new LongOperationBL();
            BackgroundBlurrProcess.execute();
        }
    }

    private BroadcastReceiver mMessageReceiverToStartTimer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isSupposedToStartTimer && Variables.firstAd.getPushRefInAdminConsole().equals(mAdvert.getPushRefInAdminConsole())){
                Log.d("AdvertCard","Received Message from main activity that background stuff is finished and to start timer.");
                Variables.hasFinishedLoadingBlurredImages = true;
                profileImageView.setImageBitmap(bs);
                sendBroadcast(START_TIMER);
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToStartTimer);
            }
        }
    };

    private BroadcastReceiver mMessageReceiverForTimerHasEnded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        doTheStuffWhenTimerHasEnded();
        }
    };

    private void doTheStuffWhenTimerHasEnded(){
        Log.d("ADVERT_CARD--","message from adCounterBar that timer has ended has been received.");
        if(mSwipeView.getChildCount() > 1){
            unLockViews();
            clickable = true;
            hasBeenSwiped = false;
        }else if(mSwipeView.getChildCount()==1 && !mLastOrNotLast.equals(Constants.ANNOUNCEMENTS)){
            Log.d("ADVERT_CARD","Sending broadcast for last ad. Also setting isLockedBecauseOfNoMoreAds");
            sendBroadcast(Constants.LAST);
            Variables.isLockedBecauseOfNoMoreAds = true;
        }
        resumeBackgroundTasksIfRunning();
    }

    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ADVERT_CARD--","Received broadcast to Unregister all receivers");
            unregisterAllReceivers();
        }
    };

    private void unregisterAllReceivers(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForTimerHasEnded);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnblurrImage);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToStartTimer);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForTimerStarted);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSetImageForSharing);
    }

    private BroadcastReceiver mMessageReceiverForUnblurrImage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            profileImageView.setImageBitmap(bs);
        }
    };

    private BroadcastReceiver mMessageReceiverForSetImageForSharing = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Variables.imageToBeShared = mAdvert.getImageBitmap();
            Intent intent2  = new Intent("TRY_SHARE_IMAGE_AGAIN");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
        }
    };



    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        Bitmap bitm = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        return getResizedBitmap(bitm,700);
//        return bitm;
    }

    private void lockViews(){
        if(!Variables.isLocked){
            //        mSwipeView.lockViews();
            mSwipeView.getBuilder()
                    .setWidthSwipeDistFactor(1f)
                    .setHeightSwipeDistFactor(1f);
            Variables.isLocked = true;
            Log.e("AdvertCard","Locking views");
        }
    }

    private void unLockViews(){
        if(Variables.isLocked && !Variables.hasTimerStarted){
//        mSwipeView.unlockViews();
            mSwipeView.getBuilder()
                    .setWidthSwipeDistFactor(10f)
                    .setHeightSwipeDistFactor(10f);
            Variables.isLocked = false;
            Log.e("AdvertCard","Unlocking views");
        }

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

    private void readImageBitmapDimensions(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(getResources(), R.id.myimage, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private class LongOperationFI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try{
                setImage();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(mImageBytes!=null){
                if(mLastOrNotLast.equals(Constants.LAST)){
                    mIsNoAds = false;
                    loadOnlyLastAd();
                }else{
                    mIsNoAds = false;
                    loadAllAds();
                }
            }

        }

        @Override
        protected void onPreExecute() {
            mIsNoAds = false;
            mAvi.setVisibility(android.view.View.VISIBLE);
            super.onPreExecute();
        }
    }




    private Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        positionBL++;
        return (bitmap);
    }

    private class LongOperationBL extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            Log.d("Card","Doing in background");
            float scale = 2f;
            Bitmap bm = getResizedBitmap(bs,250);
            for(int i = 0;i<3;i++){
                if(i>=positionBL) blurredImageList.set(i,fastblur(bm,scale,(i+2)));
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("AdvertCard","Finished blurring images in the background.");
            Intent intent = new Intent("BLUREDIMAGESDONE");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            isBackgroundTaskRunning = false;
        }

        @Override
        protected void onPreExecute() {
            Log.d("AdvertCard","Preparing To Start working on the blurred images from the background.");
            super.onPreExecute();
            isBackgroundTaskRunning = true;
        }
    }

    @SwipeTouch
    private void onSwipeTouch(float xStart, float yStart, float xCurrent, float yCurrent) {
        double distance = Math.sqrt(Math.pow(xCurrent - xStart, 2) + (Math.pow(yCurrent - yStart, 2)));
        mDistance = distance;
        int roundedDistance =(((int)distance + 99) / 200 ) * 200;
//        Log.d("DEBUG", "onSwipeTouch " + " distance : " + distance);

        if(distance>200){
            setBooleanForPausingTimer();
        }

        if(distance<149){
            profileImageView.setImageBitmap(bs);
        }else if(distance>149 &&distance<620){
            if(amount!=roundedDistance/200){
                updateImage();
            }
        }

    }

    @SwipeCancelState
    private void onSwipeCancelState(){
        Log.d("EVENT", "onSwipeCancelState");
        setBooleanForResumingTimer();
        profileImageView.setImageBitmap(bs);
    }




    private void updateImage() {
        int roundedDistance =(((int)mDistance + 99) / 200 ) * 200;

        profileImageView.setImageBitmap(blurredImageList.get((roundedDistance/200)-1));
        amount = roundedDistance/200;
    }

    private void setBooleanForPausingTimer(){
        if(Variables.isAllClearToContinueCountDown){
            Log.d("AdvertCard","Setting boolean for pausing timer.");
            Variables.isAllClearToContinueCountDown = false;
        }

    }

    private void setBooleanForResumingTimer(){
        if(!Variables.isAllClearToContinueCountDown){
            Log.d("AdvertCard","Setting boolean for resuming timer.");
            Variables.isAllClearToContinueCountDown = true;
        }
    }

    private boolean isCurrentlyBeingViewed(){
        return mAdvert.getPushRefInAdminConsole().equals(Variables.getCurrentAdvert().getPushRefInAdminConsole());
    }

}
