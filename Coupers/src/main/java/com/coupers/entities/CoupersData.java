package com.coupers.entities;

import android.view.View;

/**
 * Created by pepe on 7/20/13.
 */
public final class CoupersData {

    public static final class Categories{
        public final static int EAT = 10;
        public final static int HAVE_FUN = 20;
        public final static int RELAX = 30;
        public final static int FEEL_GOOD = 40;
        public final static int LOOK_GOOD = 50;
    }
    public static final class Parameters{
        public final static String USER_ID="user_id";
        public final static String CITY = "city";
        public final static String CATEGORY_ID = "category_id";
        public final static String DEAL_ID = "deal_id";
        public final static String LOCATION_ID = "location_id";
        public final static String FACEBOOK_ID = "facebook_id";
        public final static String USER_CITY = "user_city";
        public final static String USERNAME = "username";
        public final static String FACEBOOK_POST_ID = "post_id";
        public final static String GCM_ID = "pushnotification_id";
    }

    public static final class Fields {
        //Favorite Locations WebService Data Dictionary
        public static final String FAVORITE_NEW_DEAL_COUNT = "CountDeal";

        //City deals WebService Data Dictionary
        public static final String LOCATION_ID = "location_id";
        public static final String LOCATION_NAME = "location_name";
        public static final String LOCATION_LOGO = "location_logo";
        public static final String LOCATION_ADDRESS = "location_address";
        public static final String LOCATION_CITY = "location_city";
        public static final String CATEGORY_ID = "category_id";
        public static final String LATITUDE = "latitud";
        public static final String LONGITUDE = "longitud";
        public static final String LOCATION_DESCRIPTION = "location_description";
        public static final String LOCATION_WEBSITE_URL = "location_website_url";
        public static final String LOCATION_THUMBNAIL = "location_thumbnail";
        public static final String LOCATION_PHONE_NUMBER1 = "location_phone_number1";
        public static final String LOCATION_PHONE_NUMBER2 = "location_phone_number2";
        public static final String LOCATION_HOURS_OPERATION1 = "location_hours_operation";
        public static final String LEVEL_DEAL_LEGEND = "level_deal_legend";
        public static final String COUNTDEALS = "CountDeals";

        public static final String DEAL_ID = "deal_id";
        public static final String DEAL_START_DATE = "deal_start_date";
        public static final String DEAL_END_DATE = "deal_end_date";
        public static final String DEAL_DAY_SPECIAL = "deal_day_special";
        public static final String LEVEL_ID = "level_id";
        public static final String LEVEL_START_AT = "level_start_at";
        public static final String LEVEL_SHARE_CODE = "level_share_code";
        public static final String LEVEL_REDEEM_CODE = "level_redeem_code";
        public static final String LEVEL_DEAL_DESCRIPTION = "level_deal_descripcion";
        public static final String USER_ID = "user_id";
        public static final String RESULT_CODE = "result_code";
        public static final String COLUMN1 = "Column1";




        public static final int CATEGORY_ID_EAT = 10;
        public static final int CATEGORY_ID_HAVE_FUN = 20;
        public static final int CATEGORY_ID_RELAX = 30;
        public static final int CATEGORY_ID_FEEL_GOOD = 40;
        public static final int CATEGORY_ID_LOOK_GOOD = 50;
    }

    public static final class Methods{
        public final static String GET_USER_FAVORITE_LOCATIONS = "GetUserFavoriteLocations";
        public final static String GET_CATEGORY_DEALS = "GetCategoryDeals";
        public final static String SAVE_DEAL ="AddDealToUserSavedDeals";
        public final static String ADD_LOCATION_FAVORITE = "AddUserLocToFav";
        public final static String REMOVE_LOCATION_FAVORITE = "RemoveUserLocFromFav";
        public final static String GET_LOCATION_DEALS = "GetLocationDeals";
        public final static String GET_CITY_DEALS = "GetCityDeals";
        public final static String LOGIN_FACEBOOK = "LoginUserFacebook";
        public final static String SHARE_DEAL_FACEBOOK = "ShareDealFacebook";
        public final static String SAVE_PUSH_NOTIFICATION_ID = "SavePushNotificationid";
    }

    public static final class Interfaces{
        public interface CallBack{
            public void update(String result);
            public void update(int location_id);
            public void update(CoupersLocation location);
        }
    }

}
