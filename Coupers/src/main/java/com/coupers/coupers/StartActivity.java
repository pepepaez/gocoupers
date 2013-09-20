package com.coupers.coupers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
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
    private GraphUser fb_user;
    private Activity a;
    public boolean exit_next=false;
    private boolean isResumed = false;
    private CoupersApp app = null;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

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
    Context context;

    String regid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        if (checkPlayServices())
        {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty())
            {
                registerInBackground();
            }
            else
            {

                Log.i(TAG, regid);
            }
        } else
        {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        setTitle(R.string.main_hub_ui);
        a = this;

        //read login method id
        login_method = PreferenceManager.getDefaultSharedPreferences(this).getInt("login_method", NO_METHOD_DEFINED);
        user_id = PreferenceManager.getDefaultSharedPreferences(this).getString("user_id", NO_USER_ID_AVAILABLE);
        user_location = PreferenceManager.getDefaultSharedPreferences(this).getString("user_location",NO_LOCATION_AVAILABLE);
        registered = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("registered", false);

        //get app
        app = (CoupersApp) getApplication();
        app.initialize(this);

        app.setUser_id(user_id);


        if (!regid.isEmpty())
            sendRegistrationIdToBackend(regid);

        //Initialize Facebook helper
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);


        //First time loading or no data found
        if (login_method==NO_METHOD_DEFINED){
            //Login screen
            setContentView(R.layout.activity_login_only_facebook);
            Button login_btn = (Button) findViewById(R.id.login_button);
            if (login_btn!=null){
                login_btn.setOnClickListener(new View.OnClickListener() {
                    //TODO implement email login mechanism
                    @Override
                    public void onClick(View view) {
                        setContentView(R.layout.activity_start);
                        loadDeals();
                    }
                });
            }
        }else if (login_method==FACEBOOK_LOGIN){
            //Facebook UI Helper to keep track of session state

            Session session = Session.getActiveSession();
            if (session != null && session.isOpened()){
                setContentView(R.layout.activity_start);
                loadDeals();
            }else if (session != null && session.getState()==SessionState.CREATED_TOKEN_LOADED){
                setContentView(R.layout.activity_start);
            }else
                setContentView(R.layout.activity_login_only_facebook);
        }else
            {
                setContentView(R.layout.activity_login_only_facebook);
            }

    }

    // <editor-fold desc="Coupers Methods - Deal Loading">
    private void loadDeals(){
        ArrayList<CoupersLocation> data;
        data = app.db.getAllLocations();
        if (data.size()>0)
            parseDeals(data);
        else
            loadDealsWS();

    }

    private void loadDealsWS(){
        CoupersObject obj = new CoupersObject(CoupersData.Methods.GET_CITY_DEALS);
        obj.addParameter(CoupersData.Parameters.CITY,"mexicali");
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

                ArrayList<CoupersLocation> mData = new ArrayList<CoupersLocation>();
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
                    mData.add(mLocation);
                    if (!app.db.exists(mLocation))
                        app.db.addLocation(mLocation);
                }

                parseDeals(mData);
            }
        });

        server.execute();
    }

    private void parseDeals(ArrayList<CoupersLocation> locations){

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location geoloc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double latitude;
        double longitude;
        boolean nearby_locations=false;
        if(geoloc != null){
            latitude = geoloc.getLatitude();
            longitude = geoloc.getLongitude();
        }else{
            latitude = -999;
            longitude = -999;
        }
        float[] results = new float[1];
        float distance = 0;

        for (CoupersLocation location : locations){
            if (geoloc!=null){
                Location.distanceBetween(latitude,longitude,location.location_latitude,location.location_longitude,results);
                distance = results[0];
                if (distance < 1000){
                    location.Nearby=true;
                    nearby_locations = true;
                }
            }
        }

        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        findViewById(R.id.textView).setVisibility(View.INVISIBLE);
        Intent intent = new Intent(StartActivity.this,MainActivity.class);
        intent.putExtra("data",locations);
        intent.putExtra("gps",geoloc!=null);
        intent.putExtra("nearby",nearby_locations);
        exit_next=true;
        startActivity(intent);

    }

    public String GetClosestCityName(){
        String result="nada";
        Geocoder geocoder = new Geocoder(context);
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        List<Address> list = new ArrayList<Address>();
        try{
            list= geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 4);
        }catch (IOException e)
        {
            e.printStackTrace();
        }
        if (list != null & list.size() > 0) {
            Address address = list.get(0);
            result = address.getLocality();
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
        if (isTaskRoot() && exit_next)
            finish();
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
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend(regid);

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
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
                    CoupersData.Fields.COLUMN1};
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
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("user_location","mexicali").commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("registered", true).commit();
            app.setUser_id(map.get(CoupersData.Fields.USER_ID));
            loadDeals();
        }
    }

    // </editor-fold>

    // <editor-fold desc="Facebook Methods">
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
            //TODO Review if need to use fragments on login
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
            if (state.equals(SessionState.OPENED)) {
                //TODO check if first time login

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
                                    RegisterUser(user.getId(),user.getName(),"mexicali",user.getUsername());
                                else
                                    loadDeals();
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
