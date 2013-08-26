package com.coupers.coupers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.coupers.entities.CoupersLocation;
import com.coupers.entities.WebServiceDataFields;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.coupers.utils.XMLParser;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphLocation;
import com.facebook.model.GraphUser;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class StartActivity extends Activity {

    private static final int NO_METHOD_DEFINED = -999;
    private static final int MAIL_LOGIN = 1;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.main_hub_ui);
        a = this;
        //store user id
        //PreferenceManager.getDefaultSharedPreferences(this).edit().putString("MYLABEL", "myStringToSave").commit();

        //read login method id
        login_method = PreferenceManager.getDefaultSharedPreferences(this).getInt("login_method", NO_METHOD_DEFINED);
        user_id = PreferenceManager.getDefaultSharedPreferences(this).getString("user_id",NO_USER_ID_AVAILABLE);
        user_location = PreferenceManager.getDefaultSharedPreferences(this).getString("user_location",NO_LOCATION_AVAILABLE);
        registered = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("registered", false);
        app = (CoupersApp) getApplication();
        app.setUser_id(user_id);


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
                        LoadData();
                    }
                });
            }
        }else if (login_method==FACEBOOK_LOGIN){
            //Facebook UI Helper to keep track of session state

            Session session = Session.getActiveSession();
            if (session != null && session.isOpened()){
                setContentView(R.layout.activity_start);
                LoadData();
            }else if (session != null && session.getState()==SessionState.CREATED_TOKEN_LOADED){
                setContentView(R.layout.activity_start);
            }else
                setContentView(R.layout.activity_login_only_facebook);
        }else
            {
                setContentView(R.layout.activity_login_only_facebook);
            }



    }

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
                                    LoadData();
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

    private void RegisterUser(String fb_user_id,
                              String fb_user_name,
                              String fb_user_location,
                              String fb_user_username){
        CoupersObject obj = new CoupersObject("http://tempuri.org/LoginUserFacebook",
                "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx",
                "LoginUserFacebook");
        obj.addParameter("facebook_id",fb_user_id);
        obj.addParameter("user_city",fb_user_location);
        obj.addParameter("username",fb_user_username);

        String _tag[]={
                WebServiceDataFields.USER_ID,
                WebServiceDataFields.RESULT_CODE};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,this);

        server.execute("dummy string");
    }

    private void LoadData(){
        CoupersObject obj = new CoupersObject("http://tempuri.org/GetCityDeals",
                "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx",
                "GetCityDeals");
        obj.addParameter("city","mexicali");
        String _tag[]={
                WebServiceDataFields.LOCATION_ID,
                WebServiceDataFields.LOCATION_NAME,
                WebServiceDataFields.LOCATION_LOGO,
                WebServiceDataFields.LOCATION_ADDRESS,
                WebServiceDataFields.LOCATION_CITY,
                WebServiceDataFields.CATEGORY_ID,
                WebServiceDataFields.LATITUDE,
                WebServiceDataFields.LONGITUDE,
                WebServiceDataFields.LOCATION_DESCRIPTION,
                WebServiceDataFields.LOCATION_WEBSITE_URL,
                WebServiceDataFields.LOCATION_THUMBNAIL,
                WebServiceDataFields.LOCATION_PHONE_NUMBER1,
                WebServiceDataFields.LOCATION_PHONE_NUMBER2,
                WebServiceDataFields.LOCATION_HOURS_OPERATION1,
                WebServiceDataFields.LEVEL_DEAL_LEGEND,
                WebServiceDataFields.COUNTDEALS};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,this);

        server.execute("dummy string");
    }


    public void Update(ArrayList<HashMap<String, String>> aResult, String WebServiceExecuted)
    {

        if (WebServiceExecuted=="LoginUserFacebook") CompleteRegistration(aResult);

        if (WebServiceExecuted == "GetCityDeals") LoadMainUI(aResult);

    }

    public void CompleteRegistration(ArrayList<HashMap<String, String>> aData){
        HashMap<String, String> map;
        if (aData.size()>0){
            map = aData.get(0);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("user_id",map.get(WebServiceDataFields.USER_ID)).commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("user_location","mexicali").commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("registered", true).commit();
            app.setUser_id(map.get(WebServiceDataFields.USER_ID));
            LoadData();
        }
    }

    public void LoadMainUI(ArrayList<HashMap<String, String>> aData){

        ArrayList<CoupersLocation> mData = new ArrayList<CoupersLocation>();
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

        //Go through data to create 2 data sets, one with locations nearby and the rest
        for (HashMap<String, String> map : aData){
            CoupersLocation mLocation = new CoupersLocation(
                    Integer.valueOf(map.get(WebServiceDataFields.LOCATION_ID)),
                    Integer.valueOf(map.get(WebServiceDataFields.CATEGORY_ID)),
                    map.get(WebServiceDataFields.LOCATION_NAME),
                    map.get(WebServiceDataFields.LOCATION_DESCRIPTION),
                    map.get(WebServiceDataFields.LOCATION_WEBSITE_URL),
                    map.get(WebServiceDataFields.LOCATION_LOGO),
                    map.get(WebServiceDataFields.LOCATION_THUMBNAIL),
                    map.get(WebServiceDataFields.LOCATION_ADDRESS),
                    map.get(WebServiceDataFields.LOCATION_CITY),
                    map.get(WebServiceDataFields.LOCATION_PHONE_NUMBER1),
                    map.get(WebServiceDataFields.LOCATION_PHONE_NUMBER2),
                    Double.valueOf(map.get(WebServiceDataFields.LATITUDE)),
                    Double.valueOf(map.get(WebServiceDataFields.LONGITUDE)));
            mLocation.TopDeal = map.get(WebServiceDataFields.LEVEL_DEAL_LEGEND);
            mLocation.CountDeals = map.get(WebServiceDataFields.COUNTDEALS);
            if (geoloc!=null){
                Location.distanceBetween(latitude,longitude,mLocation.location_latitude,mLocation.location_longitude,results);
                distance = results[0];
                if (distance < 1000){
                    mLocation.Nearby=true;
                    nearby_locations = true;
                }
            }
            mData.add(mLocation);
        }

        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        findViewById(R.id.textView).setVisibility(View.INVISIBLE);
        Intent intent = new Intent(StartActivity.this,MainActivity.class);
        intent.putExtra("data",mData);
        intent.putExtra("gps",geoloc!=null);
        intent.putExtra("nearby",nearby_locations);
        exit_next=true;
        startActivity(intent);

    }



}
