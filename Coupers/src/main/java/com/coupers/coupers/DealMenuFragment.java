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
import android.widget.ImageView;
import android.widget.TextView;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.HashMap;


//TODO add icons to menu options
//TODO add settings or my deals option to menu
public class DealMenuFragment extends Fragment {

    private String mFilter = "food"; //TODO change hardcode to setting or last category used?
    private ImageView last_selected = null;
    private ViewGroup mContainer = null;
    private ArrayList<CoupersLocation> mData = new ArrayList<CoupersLocation>();
    private View last_view_selected = null;
    private int last_position=-999;
    private ProfilePictureView profilePictureView;
    private TextView userNameView;
    private CoupersApp app = null;
    private CoupersApp.StatusCallback callme = new CoupersApp.StatusCallback() {
        @Override
        public void call(int something) {

        }
    };

    public class CoupersMenuItem{
        public String item_text = "";
        public int item_icon = R.drawable.coupers_icon3;
        public int item_bg = R.drawable.list_selector_eat;
        public int category_id = -999;
        public int location_id = -999;
        public boolean is_location = false;
        public CoupersMenuItem(String text, int icon, int bg, int id){
            this.item_text = text;
            this.item_icon = icon;
            this.item_bg = bg;
            this.category_id = id;
            this.is_location = false;
        }
    }

    //ACTIVITY CONTROL
    public DealMenuFragment(CoupersApp app) {
        this.app = app;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("filter", mFilter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Session session = Session.getActiveSession();
        if(session!=null && session.isOpened()) makeMeRequest(session);

    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContainer = container;

        ImageButton settings = (ImageButton) container.findViewById(R.id.settings);
        settings.setImageResource(android.R.drawable.ic_menu_manage);

        profilePictureView = (ProfilePictureView) container.findViewById(R.id.userpic);
        userNameView = (TextView) container.findViewById(R.id.username);


        GridView lv = (GridView) container.findViewById(R.id.gridView);
        container.removeView(container.findViewById(R.id.gridView));

        LoadFavorites();

		return lv;
	}

    //COUPERS CONTROL
    public void LoadFavorites(){
        //TODO if not saved on the device then
        LoadFavoriteFromServer();
        //TODO otherwise use from the device
    }

    public void LoadFavoriteFromServer(){
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
               UpdateMenu(result);
           }
       });

        server.execute("dummy string");
    }

    private void LoadCategoryDeals(int CategoryId){
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
                UpdateDeals(result);
            }
        });

        server.execute("dummy string");
    }

    public void UpdateMenu(ArrayList<HashMap<String, String>> aFavLocList){
        final GridView lv = (GridView) mContainer.findViewById(R.id.gridView);
        // Get Menu
        TypedArray deals_menu = getResources().obtainTypedArray(R.array.deals_menu);
        TypedArray deals_menu_id = getResources().obtainTypedArray(R.array.deals_menu_id);
        TypedArray deals_menu_background = getResources().obtainTypedArray(R.array.deals_menu_background);
        TypedArray deals_menu_icon = getResources().obtainTypedArray(R.array.deals_menu_icon);

        //Create adapter
        MenuAdapter adapter=new MenuAdapter(this.getActivity());

        if (aFavLocList.size()>0)
        {
            adapter.addHeader(getString(R.string.favorites));

            int j;
            j=0;
            for (HashMap<String, String> map : aFavLocList){
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
                        0,0);
                        //Double.valueOf(map.get(CoupersData.Fields.LATITUDE)),
                        //Double.valueOf(map.get(CoupersData.Fields.LONGITUDE)));
                mLocation.CountDeals = map.get(CoupersData.Fields.FAVORITE_NEW_DEAL_COUNT);
                j++;

                if (j<=3){
                    adapter.addFavorite(mLocation);
                }
                if(app!=null) app.addFavorite(mLocation);
            }

        }
        adapter.addHeader(getString(R.string.i_want_to));

        for (int i=0; i<deals_menu.length();i++)
        {
            CoupersMenuItem item = new CoupersMenuItem(deals_menu.getString(i),deals_menu_icon.getResourceId(i,R.drawable.coupers_icon3),deals_menu_background.getResourceId(i,R.drawable.list_selector_eat),Integer.valueOf(deals_menu_id.getString(i)));
            adapter.addItem(item);
        }

        lv.setAdapter(adapter);


        //Set OnClick event
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position,
                                    long id) {
                if (getActivity() == null)
                    return;
                ((MenuAdapter) lv.getAdapter()).AnimateInOut(view, true);
                if (last_view_selected != null)
                    ((MenuAdapter) lv.getAdapter()).AnimateInOut(last_view_selected, false);
                last_view_selected = view;
                ImageView option_selected = (ImageView) view.findViewById(R.id.selected_indicator);
                if (option_selected != null) {
                    int category_id = ((MenuAdapter) lv.getAdapter()).getCategoryId(position);
                    view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                    LoadCategoryDeals(category_id);
                } else {
                    ImageView favorite_location = (ImageView) view.findViewById(R.id.favorite_location);
                    if (favorite_location != null) {
                        //TODO call CardFlipActivity for selected location
                        int location_id = ((MenuAdapter) lv.getAdapter()).getLocationId(position);
                        loadLocation(location_id);
                    }
                }

            }
        });
    }

    public void UpdateDeals(ArrayList<HashMap<String, String>> aData)
    {
        ArrayList<CoupersLocation> mData = new ArrayList<CoupersLocation>();
        LocationManager lm = (LocationManager)this.getActivity().getSystemService(Context.LOCATION_SERVICE);
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
            mLocation.CountDeals = map.get(CoupersData.Fields.COUNTDEALS);
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

        //findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        //findViewById(R.id.textView).setVisibility(View.INVISIBLE);

        if (getActivity() instanceof MainActivity) {
            MainActivity ra = (MainActivity) getActivity();
            last_view_selected.findViewById(R.id.loading).setVisibility(View.INVISIBLE);
            ra.switchContent(mData,geoloc!=null, nearby_locations);
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

    private void loadLocation(int location_id) {
        if (getActivity() == null)
            return;

        if (getActivity() instanceof MainActivity) {
            MainActivity ra = (MainActivity) getActivity();
            ra.onDealPressed(location_id);
        }
    }

}
