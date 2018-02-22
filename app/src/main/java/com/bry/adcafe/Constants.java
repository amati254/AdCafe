package com.bry.adcafe;

import com.google.android.gms.wallet.WalletConstants;

/**
 * Created by bryon on 6/4/2017.
 */

public class Constants {
    public static String ADVERT_CARD_BROADCAST_TO_AD_COUNTER = "ADVERT_CARD_BROADCAST_TO_AD_COUNTER";
    public static String ADVERT_CARD_BROADCAST = "ADVERT_CARD_BROADCAST";
    public static String AD_TIMER_BROADCAST = "AD_TIMER_BROADCAST";
    public static String NUMBER_OF_ADS  = "NUMBER_OF_ADS";
    public static final String TIMER_HAS_ENDED = "TIMER_HAS_ENDED";


    public static String AD_TOTAL = "AD_TOTAL";
    public static String STOP_TIMER = "STOP_TIMER";
    public static String ADVERT_CARD_BROADCAST_TO_START_TIMER = "ADVERT_CARD_BROADCAST_TO_START_TIMER";
    public static String ADD_TO_SHARED_PREFERENCES = "ADD_TO_SHARED_PREFERENCES";
    public static String LAST = "LAST";


    public static String NOT_LAST = "NOT_LAST";
    public static String UNREGISTER_ALL_RECEIVERS = "UNREGISTER_ALL_RECEIVERS";
    public static String CONNECTION_OFFLINE = "CONNECTION_OFFLINE";
    public static String PIN_AD = "PIN_AD";
    public static String CONNECTION_ONLINE = "CONNECTION_ONLINE";


    public static String PINNING_FAILED = "PINNING_FAILED";
    public static String PINNING_SUCCESS = "PINNING_SUCCESS";
    public static String REMOVE_PINNED_AD = "REMOVE_PINNED_AD";
    public static String UNABLE_TO_REMOVE_PINNED_AD = "UNABLE_TO_REMOVE_PINNED_AD";
    public static String PINNED_AD_LIST = "pinnedAdList";


    public static String FIREBASE_CHILD_ADS = "savedAds";
    public static String FIREBASE_CHILD_USERS = "users";
    public static String TOTAL_NO_OF_ADS_SEEN_TODAY = "TodaysTotalAds";
    public static String DATE_IN_FIREBASE = "date";
    public static String TOTAL_NO_OF_ADS_SEEN_All_MONTH = "allTimeTotals";
    public static String CLUSTER_ID = "clusterID";
    public static String CLUSTERS = "clusters";
    public static String CLUSTERS_LIST = "cluster_list";
    public static String CLUSTER_LIST_PUSHREF_ID = "cluster_list_pushref_id";
    public static String FLAGGED_CLUSTERS = "FlaggedClusters";
    public static String CLUSTER_TO_START_FROM = "cluster_to_start_from";
    public static String ADVERTS = "Adverts";
    public static String REPORTED_ADS = "Flagged_ads";
    public static String HAS_USER_MADE_PAMENTS = "has_payed";

    public static String NO_ADS = "NO_ADS";
    public static String DATE = "date";
    public static String LAST_AD_SEEN = "LastAdSeen";
    public static String LOAD_MORE_ADS = "LoadMoreAds";
    public static String ANNOUNCEMENTS = "Announcements";
    public static String ADS_FOR_CONSOLE = "AdsForConsole";
    public static String UPLOADED_AD_LIST = "UploadedAdList";
    public static String FEEDBACK = "Feedback";

    public static double CONSTANT_AMOUNT_PER_AD = 4;
    public static double CONSTANT_AMMOUNT_FOR_USER = 2;
    public static String TOTAL_ALL_TIME_ADS = "AllUserTotals";
    public static String SUBSCRIPTION_lIST = "UserSubscriptionList";
    public static String CREATE_USER_SPACE_COMPLETE = "UserSpaceCompleted";
    public static String SET_UP_USERS_SUBSCRIPTION_LIST = "setUpUsersSubscriptionList";

    public static String CURRENT_SUBSCRIPTION_INDEX = "currentSub";
    public static String CURRENT_AD_IN_SUBSCRIPTION = "currentAdInSubscription";
    public static String LOADED_USER_DATA_SUCCESSFULLY = "LoadedUserDataSuccessfully";
    public static String FAILED_TO_LOAD_USER_DATA = "FailedToLoadUserData";

    public static String CATEGORY_LIST = "categoryList";
    public static String FINISHED_UNSUBSCRIBING = "finishedUnsubscribing";

    public static String STARTING_UPDATING = "startingUpdating";
    public static String FINISHED_UPDATING = "FinishedUpdating";
    public static String CONFIRM_START = "ConfirmStart";
    public static String ALL_CLEAR = "AllClear";
    public static String CANCELLED = "canceled";


    public static String NO_OF_CATEGORIES_KNOWN = "NoOfCategoriesKnown";
    public static String IS_AD = "Is_ad";
    public static String IS_ANNOUNCEMENT = "Is_announcement";
    public static String PINNED_AD_POOL = "PinnedAdPool";
    public static String NO_OF_TIMES_PINNED = "NumberOfTimesPinned";



    public static int NO_OF_ADS_TO_LOAD = 3;
    public static int NO_OF_ADS_TO_LOAD2 = 2;
    public static String REIMBURSEMENT_TOTALS = "ReimbursementTotals";
    public static String CONSTANT_AMMOUNT_PER_VIEW = "AmountPerView";
    public static String FINISHED_SETUP_FOR_RESETTING_SUBS = "FINISHED_SETUP_FOR_RESETTING_SUBS";
    public static String RESET_ALL_SUBS_BOOLEAN = "ResetAllSubs";
    public static String NEW_CPV = "NewCPV";
    public static String PREFERRED_NOTIF = "NotificationsPref";

    public static String PREFERRED_NOTF_HOUR = "PreferredNotfHour";
    public static String PREFERRED_NOTF_MIN = "PreferredNotfMin";
    public static String USER_NICKNAME = "UserName";
    public static String LAST_SEEN_DATE_IN_DAYS = "LSDate";
    public static int NUMBER_OF_USERS_PER_CLUSTER = 1000;
    public static double PAYMENT_TRANSFER_PERENTAGE = 0.035;
    public static double PAYOUT_TRANSFER_FEE = 0.015;
    public static double TOTAL_PAYOUT_PERCENTAGE = 0.05;

    public static String USER_PASSCODE = "Password";

}