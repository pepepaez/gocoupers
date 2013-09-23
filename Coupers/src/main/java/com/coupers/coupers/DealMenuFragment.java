package com.coupers.coupers;

import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.CoupersMenuItem;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.HashMap;


public class DealMenuFragment extends Fragment {

    private ViewGroup mContainer = null;
    private View last_view_selected = null;
    private ProfilePictureView profilePictureView;
    private TextView userNameView;
    private CoupersApp app = null;

    //ACTIVITY CONTROL

    public DealMenuFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //mContainer = getParentFragment().getFragmentManager().getFragment()

        this.app=(CoupersApp)getActivity().getApplication();

        this.app.registerCallBack(new CoupersData.Interfaces.CallBack() {
            @Override
            public void update(String result) {

            }

            @Override
            public void update(int location_id) {
                GridView lv;
                MenuAdapter adapter=null;
                lv= (GridView) mContainer.findViewById(R.id.gridView);
                if (lv!=null)
                    adapter = (MenuAdapter) lv.getAdapter();
                if (adapter!=null)
                    adapter.removeFavorite(location_id);

            }

            @Override
            public void update(CoupersLocation location) {
                GridView lv;
                MenuAdapter adapter=null;
                CoupersMenuItem item = new CoupersMenuItem(location);
                lv= (GridView) mContainer.findViewById(R.id.gridView);
                if (lv!=null)
                    adapter = (MenuAdapter) lv.getAdapter();
                if (adapter!=null)
                    adapter.insertFavorite(item);
            }
        });

        Session session = Session.getActiveSession();
        if(session!=null && session.isOpened()) makeMeRequest(session);

        ImageButton settings = (ImageButton) mContainer.findViewById(R.id.settings);
        settings.setImageResource(android.R.drawable.ic_menu_manage);

        profilePictureView = (ProfilePictureView) mContainer.findViewById(R.id.userpic);
        userNameView = (TextView) mContainer.findViewById(R.id.username);

        loadFavorites();

    }

	/*@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContainer = container;

        GridView lv = (GridView) container.findViewById(R.id.gridView);
        container.removeView(container.findViewById(R.id.gridView));

		return lv;
	}*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    //COUPERS CONTROL
    public void loadFavorites(){
        //TODO if not saved on the device then
        loadFavoriteFromServer();
        //TODO otherwise use from the device
    }

    public void loadFavoriteFromServer(){
        CoupersObject obj = new CoupersObject(CoupersData.Methods.GET_USER_FAVORITE_LOCATIONS);
        obj.addParameter(CoupersData.Parameters.USER_ID,app.getUser_id());
        String _tag[]={
                CoupersData.Fields.LOCATION_ID,
                CoupersData.Fields.CATEGORY_ID,
                CoupersData.Fields.LOCATION_NAME,
                CoupersData.Fields.LOCATION_LOGO,
                CoupersData.Fields.LOCATION_ADDRESS,
                CoupersData.Fields.LOCATION_CITY,
                CoupersData.Fields.LOCATION_DESCRIPTION,
                CoupersData.Fields.LOCATION_WEBSITE_URL,
                CoupersData.Fields.LOCATION_THUMBNAIL,
                CoupersData.Fields.LOCATION_PHONE_NUMBER1,
                CoupersData.Fields.LOCATION_PHONE_NUMBER2,
                CoupersData.Fields.LOCATION_HOURS_OPERATION1,
                CoupersData.Fields.FAVORITE_NEW_DEAL_COUNT};
        obj.setTag(_tag);

       CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
           @Override
           public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
           if (isAdded())
               updateMenu(parseLocations(result, true));
           }
       });

        server.execute();
    }

    private ArrayList<CoupersLocation> parseLocations(ArrayList<HashMap<String, String>> data,boolean set_favorite){

        for (HashMap<String, String> map : data){
            CoupersLocation location = new CoupersLocation(
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
                    0,0);

            if(!set_favorite)
            {
                location.TopDeal = map.get(CoupersData.Fields.LEVEL_DEAL_LEGEND);
                location.CountDeals = Integer.valueOf(map.get(CoupersData.Fields.COUNTDEALS));
                if (app!=null)
                {
                    if (!app.exists(location))
                        app.locations.add(location);
                    else
                    {
                        CoupersLocation loc = app.findLocation(location.location_id);
                        if (loc!=null)
                            loc.CountDeals=location.CountDeals;
                    }
                }
            }
            else
            {
                location.CountDeals = Integer.valueOf(map.get(CoupersData.Fields.FAVORITE_NEW_DEAL_COUNT));
                if(app!=null)
                    app.setFavorite(location);
            }


        }

        return app.getFavorites();
    }

    public void updateMenu(ArrayList<CoupersLocation> locations){
        final GridView lv = (GridView) mContainer.findViewById(R.id.gridView);
        // Get Menu
        TypedArray deals_menu = getResources().obtainTypedArray(R.array.deals_menu);
        TypedArray deals_menu_id = getResources().obtainTypedArray(R.array.deals_menu_id);
        TypedArray deals_menu_background = getResources().obtainTypedArray(R.array.deals_menu_background);
        TypedArray deals_menu_icon = getResources().obtainTypedArray(R.array.deals_menu_icon);

        //Create adapter
        final MenuAdapter adapter=new MenuAdapter(this.getActivity());

        if (locations.size()>0)
        {
            adapter.addHeader(new CoupersMenuItem(getString(R.string.favorites)));

            int j;
            j=0;
            for (CoupersLocation location : locations)
            {
                j++;
                CoupersMenuItem item = new CoupersMenuItem(location);
                adapter.addFavorite(item);
            }

        }
        adapter.addHeader(new CoupersMenuItem(getString(R.string.i_want_to)));

        for (int i=0; i<deals_menu.length();i++)
        {
            CoupersMenuItem item = new CoupersMenuItem(deals_menu.getString(i),deals_menu_icon.getResourceId(i,R.drawable.coupers_icon3),deals_menu_background.getResourceId(i,R.drawable.list_selector_eat),Integer.valueOf(deals_menu_id.getString(i)));
            adapter.addItem(item);
        }

        if(lv!=null)
        {
            lv.setAdapter(adapter);

            //Set OnClick event
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View view, int position,
                                        long id) {
                    if (getActivity() == null)
                        return;

                    switch (adapter.getItemViewType(position))
                    {
                        case CoupersMenuItem.TYPE_CATEGORY:
                            adapter.AnimateInOut(view,true);
                            if (last_view_selected != null)
                                adapter.AnimateInOut(last_view_selected, false);
                            last_view_selected = view;
                            view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                            app.selected_category =adapter.getCategoryId(position);
                            loadCategoryLocations(app.selected_category);
                            return;
                        case CoupersMenuItem.TYPE_LOCATION:
                            loadLocation(adapter.getLocation(position));
                    }
                }
            });
        }
    }

    private void loadLocation(CoupersLocation location) {
        if (getActivity() == null)
            return;

        if (getActivity() instanceof MainActivity) {
            MainActivity ra = (MainActivity) getActivity();
            app.selected_location=location;
            ra.onLocationPressed();
        }
    }
    private void loadCategoryLocations(int CategoryId){
        CoupersObject obj = new CoupersObject(CoupersData.Methods.GET_CATEGORY_DEALS);
        obj.addParameter(CoupersData.Parameters.CITY,getResources().getString(R.string.city));
        obj.addParameter(CoupersData.Parameters.CATEGORY_ID,String.valueOf(CategoryId));
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
                parseLocations(result,false);
                showLocations();
            }
        });

        server.execute();
    }

    public void showLocations()
    {
        LocationManager lm = (LocationManager)this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location geoloc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double latitude;
        double longitude;



        if(geoloc != null){
            latitude = geoloc.getLatitude();
            longitude = geoloc.getLongitude();
        }else{
            latitude = -999;
            longitude = -999;
        }
        float[] results = new float[1];
        float distance = 0;

        app.resetShow();
        app.setCategory();

        //Go through data to create 2 data sets, one with locations nearby and the rest
        for (CoupersLocation location : app.locations){
            if (location.show)
            if (geoloc!=null){
                Location.distanceBetween(latitude,longitude,location.location_latitude,location.location_longitude,results);
                distance = results[0];
                if (distance < 1000){
                    location.Nearby=true;
                    app.nearby_locations=true;
                }

            }
        }
        app.gps_available = geoloc!=null;

        if (getActivity() instanceof MainActivity) {
            MainActivity ra = (MainActivity) getActivity();
            last_view_selected.findViewById(R.id.loading).setVisibility(View.INVISIBLE);
            ra.switchContent();
        }

    }

    //FACEBOOK REQUESTS
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
                                // Set the id for the ProfilePictureView
                                // view that in turn displays the profile picture.
                                profilePictureView.setProfileId(user.getId());
                                // Set the Textview's text to the user's name.
                                userNameView.setText(user.getName());
                            }
                        }
                        if (response.getError() != null) {
                            // Handle errors, will do so later.
                        }
                    }
                });
        request.executeAsync();
    }



}
