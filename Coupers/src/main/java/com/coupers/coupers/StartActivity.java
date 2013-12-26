package com.coupers.coupers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersDeal;
import com.coupers.entities.CoupersDealLevel;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import static com.coupers.entities.CoupersData.*;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;


import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class StartActivity extends Activity {

    private static final int NO_METHOD_DEFINED = -999;
    private static final int FACEBOOK_LOGIN = 2;
    private static final String NO_LOCATION_AVAILABLE = "nla";
    private static final String NO_USER_ID_AVAILABLE = "nuida";
    private int login_method=NO_METHOD_DEFINED;
    public String user_id;
    public String user_location;
    private boolean registered = false;
    private boolean first_time = true;
    private boolean ready = false;
    private boolean city_selected = false;
    private int saved_deals = 0;
    private int loaded_saved_deals =0;
    private GraphUser fb_user;
    private Activity a;

    private boolean isResumed = false;
    private CoupersApp app = null;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private int load_step=1;
    private static final int LOAD_LOCATIONS = 1;
    private static final int LOAD_DEALS = LOAD_LOCATIONS+1;
    private static final int LOAD_LEVELS = LOAD_DEALS+1;
    private static final int LOAD_COMPLETE = LOAD_LEVELS+1;


    //GCM Variables
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "1020354503637";

    static final String TAG = "CoupersLog";

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    //Context context;

    String regid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add code to print out the key hash
       /* try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.coupers.coupers",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/
        setTitle(R.string.main_hub_ui);

        // TODO Get all preferences data up front
        login_method = PreferenceManager.getDefaultSharedPreferences(this).getInt("login_method", NO_METHOD_DEFINED);
        user_id = PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", NO_USER_ID_AVAILABLE);
        registered = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("registered", false);
        first_time = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_time", true);
        String closest_city= GetClosestCityName();
        user_location = closest_city.toLowerCase();
        if (user_location.equals(getString(R.string.no_locality_found))) {
            user_location = PreferenceManager.getDefaultSharedPreferences(this).getString("user_location", NO_LOCATION_AVAILABLE);
        }
        else {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("user_location", user_location).commit();
        }
        if (!user_location.equals(NO_LOCATION_AVAILABLE) && !first_time)
            city_selected=true;

        // Initialize
        // Coupers App
        app = (CoupersApp) getApplication();
        app.initialize(this);
        app.setUser_id(user_id);


        // Facebook Helper
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        // Register GCM Service
        if (checkPlayServices())
        {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this);

            if (regid.isEmpty())
                registerInBackground();
            else
            {
                //sendRegistrationIdToBackend(regid);
                Log.i(TAG, regid);
            }
        } else
            Log.i(TAG, "No valid Google Play Services APK found.");

        a = this;

        //First time loading or no data found
        if (login_method==NO_METHOD_DEFINED){
            //Login screen
            setContentView(R.layout.activity_login_only_facebook);
        }else if (login_method==FACEBOOK_LOGIN){
            //Facebook UI Helper to keep track of session state

            Session session = Session.getActiveSession();
            if (session != null && session.isOpened()){
                setContentView(R.layout.activity_start);
                initiateLoading();
            }else if (session != null && session.getState()==SessionState.CREATED_TOKEN_LOADED){
                setContentView(R.layout.activity_start);
            }else
                setContentView(R.layout.activity_login_only_facebook);
        }else
            {
                setContentView(R.layout.activity_login_only_facebook);
            }

    }

    private void initiateLoading(){

        if (!ready)
        {
            if (city_selected)
            {
                if (first_time)
                {
                    // Load from WS
                    switch (load_step){
                        case LOAD_LOCATIONS:
                            loadDealsWS();
                            break;
                        case LOAD_DEALS:
                            loadSavedDealsWS();
                            break;
                        case LOAD_LEVELS:
                            loadSavedDealLevelsWS();
                            break;
                    }
                }
                if (!first_time)
                    loadData(); //all normal just load off of the local db
            }
            else
            {
                String city_selection_message;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View city_view =  this.getLayoutInflater().inflate(R.layout.set_city, null, false);
                TextView city_text = (TextView) city_view.findViewById(R.id.city_text);
                final Spinner spin_city = (Spinner) city_view.findViewById(R.id.city_spinner);
                final SpinnerAdapter spin_adapter = spin_city.getAdapter();

                if (user_location.equals(NO_LOCATION_AVAILABLE))
                {
                    // Location not found, show user list of cities for him to select
                    city_selection_message = getString(R.string.location_missing);
                }
                else
                {
                    //Show identified location and have user confirm.
                    city_selection_message = getString(R.string.confirm_location);
                    int i;
                    for (i=0;i<spin_adapter.getCount();i++)
                        if (spin_adapter.getItem(i).equals(user_location))
                            spin_city.setSelection(i);
                }

                city_text.setText(city_selection_message);
                builder.setView(city_view);
                builder.setNeutralButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //load all locations from web server
                        user_location = spin_city.getSelectedItem().toString().toLowerCase();
                        PreferenceManager.getDefaultSharedPreferences(a).edit().putString("user_location", user_location).commit();
                        city_selected=true;
                        initiateLoading();

                    }
                });
                builder.setTitle(getString(R.string.city_selection_title));
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        }

        if (ready)
        {
            startMainActivity();
        }

    }

    // <editor-fold desc="Coupers Methods - Deal Loading">
    private void loadData(){
        ArrayList<CoupersLocation> data;
        app.locations=new ArrayList<CoupersLocation>();
        data = app.db.getAllLocations(user_location);
        if (data.size()>0)
        {
            app.locations=data;
            app.setShowAll();
            findNearbyLocations(app.locations);
            ready = true;
        }
        else
        {
            first_time = true;
            initiateLoading();
        }
    }

    private void loadDealsWS(){
        CoupersObject obj = new CoupersObject(CoupersData.Methods.GET_CITY_DEALS);
        obj.addParameter(CoupersData.Parameters.CITY,user_location);
        String _tag[]={
                CoupersData.Fields.LOCATION_ID,
                CoupersData.Fields.LOCATION_NAME,
                CoupersData.Fields.LOCATION_LOGO,
                CoupersData.Fields.LOCATION_ADDRESS,
                CoupersData.Fields.LOCATION_CITY,
                CoupersData.Fields.CATEGORY_ID,
                CoupersData.Fields.LATITUDE,
                CoupersData.Fields.LONGITUDE,
                CoupersData.Fields.LOCATION_DESCRIPTION,
                CoupersData.Fields.LOCATION_WEBSITE_URL,
                CoupersData.Fields.LOCATION_THUMBNAIL,
                CoupersData.Fields.LOCATION_PHONE_NUMBER1,
                CoupersData.Fields.LOCATION_PHONE_NUMBER2,
                CoupersData.Fields.LOCATION_HOURS_OPERATION1,
                CoupersData.Fields.LEVEL_DEAL_LEGEND,
                CoupersData.Fields.COUNTDEALS};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
                //Parse data before loading

                if (result.size()>0)
                {

                    for (HashMap<String, String> map : result){
                        CoupersLocation mLocation = new CoupersLocation(
                                Integer.valueOf(map.get(CoupersData.Fields.LOCATION_ID)),
                                Integer.valueOf(map.get(CoupersData.Fields.CATEGORY_ID)),
                                map.get(CoupersData.Fields.LOCATION_NAME),
                                map.get(CoupersData.Fields.LOCATION_DESCRIPTION),
                                map.get(CoupersData.Fields.LOCATION_WEBSITE_URL),
                                map.get(CoupersData.Fields.LOCATION_LOGO),
                                map.get(CoupersData.Fields.LOCATION_THUMBNAIL),
                                map.get(CoupersData.Fields.LOCATION_ADDRESS),
                                map.get(CoupersData.Fields.LOCATION_CITY),
                                map.get(CoupersData.Fields.LOCATION_PHONE_NUMBER1),
                                map.get(CoupersData.Fields.LOCATION_PHONE_NUMBER2),
                                Double.valueOf(map.get(CoupersData.Fields.LATITUDE)),
                                Double.valueOf(map.get(CoupersData.Fields.LONGITUDE)));
                        mLocation.TopDeal = map.get(CoupersData.Fields.LEVEL_DEAL_LEGEND);
                        mLocation.CountDeals = Integer.valueOf(map.get(CoupersData.Fields.COUNTDEALS));
                        mLocation.show=true;

                        if (!app.db.exists(mLocation))
                        {
                            app.db.addLocation(mLocation);
                            app.locations.add(mLocation);
                        }
                    }

                    app.gps_available=false;
                    app.nearby_locations=false;
                    findNearbyLocations(app.locations);
                    load_step = LOAD_DEALS;
                    initiateLoading();
                }
                else
                {
                    String city_selection_message;
                    AlertDialog.Builder builder = new AlertDialog.Builder(a);
                    View city_view =  a.getLayoutInflater().inflate(R.layout.set_city, null, false);
                    TextView city_text = (TextView) city_view.findViewById(R.id.city_text);
                    final Spinner spin_city = (Spinner) city_view.findViewById(R.id.city_spinner);
                    final SpinnerAdapter spin_adapter = spin_city.getAdapter();

                    if (user_location.equals(NO_LOCATION_AVAILABLE))
                    {
                        // Location not found, show user list of cities for him to select
                        city_selection_message = getString(R.string.location_missing);
                    }
                    else
                    {
                        //Show identified location and have user confirm.
                        city_selection_message = getString(R.string.confirm_location);
                        int i;
                        for (i=0;i<spin_adapter.getCount();i++)
                            if (spin_adapter.getItem(i).equals(user_location))
                                spin_city.setSelection(i);
                    }

                    city_text.setText(city_selection_message);
                    builder.setView(city_view);
                    builder.setNeutralButton("OK",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //load all locations from web server
                            user_location = spin_city.getSelectedItem().toString().toLowerCase();
                            PreferenceManager.getDefaultSharedPreferences(a).edit().putString("user_location", user_location).commit();
                            city_selected=true;
                            initiateLoading();

                        }
                    });
                    builder.setTitle(getString(R.string.city_selection_title));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });

        server.execute();
    }

    private void findNearbyLocations(ArrayList<CoupersLocation> data){

        double latitude;
        double longitude;
        boolean nearby_locations=false;
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location geoloc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(geoloc != null){
            latitude = geoloc.getLatitude();
            longitude = geoloc.getLongitude();
        }else{
            latitude = -999;
            longitude = -999;
        }
        float[] results = new float[1];
        float distance = 0;

        for (CoupersLocation location : data){
            location.show=true;
            if (geoloc!=null){
                Location.distanceBetween(latitude,longitude,location.location_latitude,location.location_longitude,results);
                distance = results[0];
                if (distance < 1000){
                    location.Nearby=true;
                    nearby_locations = true;
                }
            }
        }
        app.nearby_locations = nearby_locations;
        app.gps_available = geoloc!=null;
    }

    private void loadSavedDealsWS(){
        CoupersObject obj = new CoupersObject(CoupersData.Methods.GET_SAVED_DEALS);
        obj.addParameter(CoupersData.Parameters.USER_ID,app.getUser_id());
        String _tag[]={
                Fields.USER_ID,
                Fields.DEAL_ID,
                Fields.LEVEL_ID,
                Fields.SAVED_DEAL_SHARE_COUNT,
                Fields.FACEBOOK_POST_ID,
                Fields.LOCATION_ID,
                Fields.LOCATION_NAME,
                Fields.LOCATION_LOGO,
                Fields.LOCATION_ADDRESS,
                Fields.LOCATION_CITY,
                Fields.LEVEL_DEAL_LEGEND,
                Fields.CATEGORY_ID,
                Fields.LOCATION_DESCRIPTION,
                Fields.LOCATION_WEBSITE_URL,
                Fields.LOCATION_THUMBNAIL,
                Fields.LOCATION_PHONE_NUMBER1,
                Fields.LOCATION_PHONE_NUMBER2,
                Fields.LOCATION_HOURS_OPERATION1,
                Fields.LATITUDE,
                Fields.LONGITUDE};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
                //Parse data before loading
                CoupersLocation new_location = null;

                if (result.size()>0)
                {
                    for (HashMap<String, String> map : result){

                        //First check if location,deal exists in app data
                        CoupersLocation location = app.findLocation(Integer.valueOf(map.get(Fields.LOCATION_ID)));
                        if (location!=null) //If found then check if deal exists
                        {
                            CoupersDeal deal = location.findDeal(Integer.valueOf(map.get(Fields.DEAL_ID)));
                            if (deal!=null) //If found then check as saved, check current level, update share count and close
                            {
                                deal.saved_deal = true;
                                deal.current_level_id = Integer.valueOf(map.get(Fields.LEVEL_ID));
                                deal.share_count = Integer.valueOf(map.get(Fields.SAVED_DEAL_SHARE_COUNT));
                                deal.fb_post_id = map.get(Fields.FACEBOOK_POST_ID);
                            }
                            else //Create Deal object and add to location
                            {
                                deal = new CoupersDeal(
                                        location.location_id,
                                        Integer.valueOf(map.get(Fields.DEAL_ID)),
                                        map.get(Fields.DEAL_START_DATE),
                                        map.get(Fields.DEAL_END_DATE));
                                deal.saved_deal=true;
                                deal.current_level_id = Integer.valueOf(map.get(Fields.LEVEL_ID));
                                deal.share_count = Integer.valueOf(map.get(Fields.SAVED_DEAL_SHARE_COUNT));
                                deal.fb_post_id = map.get(Fields.FACEBOOK_POST_ID);
                                if (!app.db.exists(deal))
                                {
                                    app.db.addDeal(deal);
                                }
                                location.location_deals.add(deal);
                                //getDealLevels(location.location_id, deal.deal_id);
                            }
                        }
                        else //Location not found, create objects and add to app data
                        {
                            new_location = new CoupersLocation(
                                    Integer.valueOf(map.get(CoupersData.Fields.LOCATION_ID)),
                                    Integer.valueOf(map.get(CoupersData.Fields.CATEGORY_ID)),
                                    map.get(CoupersData.Fields.LOCATION_NAME),
                                    map.get(CoupersData.Fields.LOCATION_DESCRIPTION),
                                    map.get(CoupersData.Fields.LOCATION_WEBSITE_URL),
                                    map.get(CoupersData.Fields.LOCATION_LOGO),
                                    map.get(CoupersData.Fields.LOCATION_THUMBNAIL),
                                    map.get(CoupersData.Fields.LOCATION_ADDRESS),
                                    map.get(CoupersData.Fields.LOCATION_CITY),
                                    map.get(CoupersData.Fields.LOCATION_PHONE_NUMBER1),
                                    map.get(CoupersData.Fields.LOCATION_PHONE_NUMBER2),
                                    Double.valueOf(map.get(CoupersData.Fields.LATITUDE)),
                                    Double.valueOf(map.get(CoupersData.Fields.LONGITUDE)));
                            new_location.TopDeal = map.get(CoupersData.Fields.LEVEL_DEAL_LEGEND);
                            new_location.show=true;
                            CoupersDeal new_deal = new CoupersDeal(new_location.location_id,
                                    Integer.valueOf(map.get(Fields.DEAL_ID)),
                                    map.get(Fields.DEAL_START_DATE),
                                    map.get(Fields.DEAL_END_DATE));
                            new_deal.saved_deal = true;
                            new_deal.current_level_id = Integer.valueOf(map.get(Fields.LEVEL_ID));
                            new_deal.share_count = Integer.valueOf(map.get(Fields.SAVED_DEAL_SHARE_COUNT));
                            new_deal.fb_post_id = map.get(Fields.FACEBOOK_POST_ID);
                            new_location.location_deals.add(new_deal);
                            if (!app.db.exists(new_deal))
                            {
                                app.db.addDeal(new_deal);
                            }
                            if (app.findLocation(new_location.location_id)==null)
                                    app.locations.add(new_location);
                            //getDealLevels(new_location.location_id, new_deal.deal_id);

                            if (!app.db.exists(new_location))
                            {
                                app.db.addLocation(new_location);
                            }
                        }

                    }

                }
                load_step=LOAD_LEVELS;
                initiateLoading();
            }
        });

        server.execute();
    }

    private void loadSavedDealLevelsWS(){

        saved_deals = countSavedDeals();
        if (saved_deals>0)
        {
            for (CoupersLocation location : app.locations)
            {
                for (CoupersDeal deal : location.location_deals){
                    if (deal.saved_deal)
                    {
                        getDealLevels(location.location_id,deal.deal_id);
                    }
                }
            }
        }
        else
        {
            first_time=false;
        }

    }

    private int countSavedDeals(){
        int count = 0;
        for (CoupersLocation location : app.locations)
            for (CoupersDeal deal : location.location_deals)
                if (deal.saved_deal) count++;
        return count;
    }

    private void getDealLevels(final int location_id, final int deal_id){
        CoupersObject obj = new CoupersObject(Methods.GET_DEAL_LEVELS);
        obj.addParameter(Parameters.DEAL_ID,String.valueOf(deal_id));
        String _tag[]={
                Fields.LEVEL_ID,
                Fields.LEVEL_START_AT,
                Fields.LEVEL_SHARE_CODE,
                Fields.LEVEL_REDEEM_CODE,
                Fields.LEVEL_DEAL_LEGEND,
                Fields.LEVEL_DEAL_DESCRIPTION,
                Fields.LEVEL_AWARD_LIMIT
        };
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
                //Parse data before loading
                ArrayList<CoupersDealLevel> levels = new ArrayList<CoupersDealLevel>();
                if (result.size()>0)
                {

                    for (HashMap<String, String> map : result){
                        CoupersDealLevel level = new CoupersDealLevel(
                                deal_id,
                                Integer.valueOf(map.get(Fields.LEVEL_ID)),
                                Integer.valueOf(map.get(Fields.LEVEL_START_AT)),
                                map.get(Fields.LEVEL_SHARE_CODE),
                                map.get(Fields.LEVEL_REDEEM_CODE),
                                map.get(Fields.LEVEL_DEAL_LEGEND),
                                map.get(Fields.LEVEL_DEAL_DESCRIPTION));
                        levels.add(level);
                        if (!app.db.exists(level))
                        {
                            app.db.addDealLevel(level);
                        }
                    }

                }
                if (levels.size()>0)
                    app.findLocation(location_id).findDeal(deal_id).deal_levels = levels;

                loaded_saved_deals++;
                if (loaded_saved_deals==saved_deals)
                {
                    first_time=false;
                    initiateLoading();
                }
            }
        });

        server.execute();
    }

    private void startMainActivity(){
        PreferenceManager.getDefaultSharedPreferences(a).edit().putBoolean("first_time",false).commit();
        app.setUser_city(user_location);
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        findViewById(R.id.textView).setVisibility(View.INVISIBLE);
        Intent intent = new Intent(StartActivity.this,MainActivity.class);
        sendAccessTokenToBackend(Session.getActiveSession().getAccessToken());
        sendRegistrationIdToBackend(regid);
        app.exit_next=true;
        app.reload=false;
        startActivity(intent);
    }

    public String GetClosestCityName(){
        String result="nada";
        Geocoder geocoder = new Geocoder(this);
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (lm!=null)
        {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            List<Address> list = new ArrayList<Address>();
            if (location!=null)
            {
                try{
                    list= geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 4);
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
                if (list != null)
                {
                    if (list.size() > 0)
                    {
                        String locality;
                        for (Address address:list)
                        {
                            locality = address.getLocality();
                            if (locality!=null)
                                result=locality;
                        }
                    }
                }
            }
        }
        return result;

    }
    //</editor-fold>

    // <editor-fold desc="Activity Control">
    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed=false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed=true;

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (app.reload)
        {
            app.locations= new ArrayList<CoupersLocation>();
            setContentView(R.layout.activity_start);
            loaded_saved_deals=0;
            ready=false;
            city_selected=true;
            user_location=app.getUser_city();
            first_time=true;
            load_step=LOAD_LOCATIONS;
            initiateLoading();
        }
        else
        {
            if (isTaskRoot() && app.exit_next)
                finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
    // </editor-fold>

    // <editor-fold desc="GCM & Google Play Services">
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(StartActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected String doInBackground(Object[] objects) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    if(registered)
                        sendRegistrationIdToBackend(regid);

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(getApplicationContext(), regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
            }

            /*@Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }*/
        }.execute(null, null, null);
    }

    /**
         * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
         * or CCS to send messages to your app. Not needed for this demo since the
         * device sends upstream messages to a server that echoes back the message
         * using the 'from' address in the message.
         */
    private void sendRegistrationIdToBackend(String pn_id) {
            // Your implementation here.
            CoupersObject obj = new CoupersObject(CoupersData.Methods.SAVE_PUSH_NOTIFICATION_ID);
            obj.addParameter(CoupersData.Parameters.USER_ID,app.getUser_id());
            obj.addParameter(CoupersData.Parameters.GCM_ID,pn_id);

            String _tag[]={
                    Fields.RESULT_CODE};
            obj.setTag(_tag);

            CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
                @Override
                public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {

                    if (e==null)
                    {
                        if (result.size()>0)
                        {
                            HashMap<String,String> map = result.get(0);
                            //if
                            // (map.get(Fields.RESULT_CODE).equals())
                        }

                    }
                    else
                    {
                        //handleError(e);
                        Toast.makeText(a,getString(R.string.coupers_server_error),Toast.LENGTH_LONG).show();
                    }
                   //Check result code
                }
            });

            server.execute();

        }

    private void sendAccessTokenToBackend(String access_token) {
        // Your implementation here.
        CoupersObject obj = new CoupersObject(Methods.SAVE_FACEBOOK_ACCESS_TOKEN);
        obj.addParameter(Parameters.USER_ID,app.getUser_id());
        obj.addParameter(Parameters.ACCESS_TOKEN,access_token);

        String _tag[]={
                Fields.RESULT_CODE};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
                //Check result code
            }
        });

        server.execute();

    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public void CompleteRegistration(ArrayList<HashMap<String, String>> aData){
        HashMap<String, String> map;
        if (aData.size()>0){
            map = aData.get(0);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("user_id",map.get(CoupersData.Fields.USER_ID)).commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("user_location",user_location).commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("registered", true).commit();
            app.setUser_id(map.get(CoupersData.Fields.USER_ID));
            sendRegistrationIdToBackend(regid);
            sendAccessTokenToBackend(Session.getActiveSession().getAccessToken());
            initiateLoading();
        }
    }

    // </editor-fold>

    // <editor-fold desc="Facebook Methods">
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {

            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
            if (state.equals(SessionState.OPENED)) {

                if(registered)
                    sendAccessTokenToBackend(session.getAccessToken());

                Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            Log.i("FB User Id ", user.getId());
                            PreferenceManager.getDefaultSharedPreferences(a).edit().putInt("login_method",FACEBOOK_LOGIN).commit();
                            setContentView(R.layout.activity_start);
                            //LoadData();
                            Session session = Session.getActiveSession();
                            if(session!=null && session.isOpened()) makeMeRequest(session);
                        }
                    }
                });
            } else if (state.isClosed()) {
                PreferenceManager.getDefaultSharedPreferences(a).edit().putInt("login_method", NO_METHOD_DEFINED).commit();
                setContentView(R.layout.activity_login_only_facebook);
            }
        }
    }


    private void makeMeRequest(final Session session) {
        // Make an API call to get user data and define a
        // new callback to handle the response.
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        // If the response is successful
                        if (session == Session.getActiveSession()) {
                            if (user != null) {
                                /*Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
                                List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
                                if (addresses.size() > 0)
                                    System.out.println(addresses.get(0).getLocality());*/
                                if(!registered)
                                    RegisterUser(user.getId(),user.getName(),user_location,user.getUsername());
                                else
                                    initiateLoading();
                                fb_user = user;
                                //LoadData();
                            }
                        }
                        if (response.getError() != null) {
                            // Handle errors, will do so later.
                        }
                    }
                });
        request.executeAsync();
    }

    private void RegisterUser(String fb_user_id, String fb_user_name, String fb_user_location, String fb_user_username) {

        CoupersObject obj = new CoupersObject(CoupersData.Methods.LOGIN_FACEBOOK);
        obj.addParameter(CoupersData.Parameters.FACEBOOK_ID, fb_user_id);
        obj.addParameter(CoupersData.Parameters.USER_CITY, fb_user_location);
        obj.addParameter(CoupersData.Parameters.USERNAME, fb_user_username);

        String _tag[] = {
                CoupersData.Fields.USER_ID,
                CoupersData.Fields.RESULT_CODE};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj, new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {

                CompleteRegistration(result);
            }
        });

        server.execute();
    }

    //</editor-fold>
}
