package com.coupers.coupers;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.coupers.entities.CoupersLocation;
import com.coupers.entities.WebServiceDataFields;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.coupers.utils.XMLParser;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;


//TODO add icons to menu options
//TODO add settings or my deals option to menu
public class DealMenuFragment extends Fragment {

    private String mFilter = "food"; //TODO change hardcode to setting or last category used?
    private ImageView last_selected = null;
    private ViewGroup mContainer = null;
    private ArrayList<CoupersLocation> mData = new ArrayList<CoupersLocation>();

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContainer = container;

        ImageButton settings = (ImageButton) container.findViewById(R.id.settings);
        settings.setImageResource(android.R.drawable.ic_menu_manage);


        GridView lv = (GridView) container.findViewById(R.id.gridView);
        container.removeView(container.findViewById(R.id.gridView));

        LoadFavorites();

		return lv;
	}

    public void LoadFavorites(){
        //TODO if not saved on the device then
        LoadFromServer();
        //TODO otherwise use from the device
    }

    public void LoadFromServer(){
        CoupersObject obj = new CoupersObject("http://tempuri.org/GetUserFavoriteLocations",
                "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx",
                "GetUserFavoriteLocations");
        obj.addParameter("user_id","1");
        String _tag[]={
                WebServiceDataFields.FAVLOC_LOCATION_ID,
                WebServiceDataFields.FAVLOC_CATEGORY_ID,
                WebServiceDataFields.FAVLOC_LOCATION_NAME,
                WebServiceDataFields.FAVLOC_LOCATION_LOGO,
                WebServiceDataFields.FAVLOC_NEW_DEAL_COUNT};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,this);

        server.execute("dummy string");
    }

    public void Update(ArrayList<HashMap<String, String>> aResult, String WebServiceExecuted)
    {

        if (WebServiceExecuted=="GetUserFavoriteLocations") UpdateMenu(aResult);

        if (WebServiceExecuted == "GetCategoryDeals") UpdateDeals(aResult);

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

            for (int j =0;j< (aFavLocList.size()>3 ? 3: aFavLocList.size());j++)
            {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(WebServiceDataFields.FAVLOC_LOCATION_ID, aFavLocList.get(j).get(WebServiceDataFields.FAVLOC_LOCATION_ID).toString());
                map.put(WebServiceDataFields.FAVLOC_CATEGORY_ID, aFavLocList.get(j).get(WebServiceDataFields.FAVLOC_CATEGORY_ID).toString());
                map.put(WebServiceDataFields.FAVLOC_LOCATION_NAME, aFavLocList.get(j).get(WebServiceDataFields.FAVLOC_LOCATION_NAME).toString());
                map.put(WebServiceDataFields.FAVLOC_LOCATION_LOGO, aFavLocList.get(j).get(WebServiceDataFields.FAVLOC_LOCATION_LOGO).toString());
                map.put(WebServiceDataFields.FAVLOC_NEW_DEAL_COUNT, aFavLocList.get(j).get(WebServiceDataFields.FAVLOC_NEW_DEAL_COUNT).toString());

                adapter.addFavorite(map);

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

                ImageView option_selected = (ImageView) view.findViewById(R.id.selected_indicator);
                if (option_selected != null) {
                    if (last_selected != null) {
                        ViewGroup.LayoutParams lp = last_selected.getLayoutParams();
                        lp.width = 10;
                        last_selected.setLayoutParams(lp);
                    }
                    ViewGroup.LayoutParams lp = option_selected.getLayoutParams();
                    lp.width = 80;
                    option_selected.setLayoutParams(lp);
                    last_selected = option_selected;
                    TypedArray dealsmenu = getResources().obtainTypedArray(R.array.deals_menu_id);
                    int category_id = ((MenuAdapter) lv.getAdapter()).getCategoryId(position);

                    LoadCategoryDeals(category_id);
                    //Fragment newContent = new DealGridFragment(mData,true);
                    //if (newContent != null)
                    //switchFragment(newContent);
                }

                ImageView favorite_location = (ImageView) view.findViewById(R.id.favorite_location);
                if (favorite_location != null) {
                    //TODO call CardFlipActivity for selected location
                    int location_id = ((MenuAdapter) lv.getAdapter()).getLocationId(position);
                    loadLocation(location_id);
                }

            }
        });
    }

    private void LoadCategoryDeals(int CategoryId){
        CoupersObject obj = new CoupersObject("http://tempuri.org/GetCategoryDeals",
                "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx",
                "GetCategoryDeals");
        obj.addParameter("city",getResources().getString(R.string.city));
        obj.addParameter("category_id",String.valueOf(CategoryId));
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

        //findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        //findViewById(R.id.textView).setVisibility(View.INVISIBLE);

        if (getActivity() instanceof ResponsiveUIActivity) {
            ResponsiveUIActivity ra = (ResponsiveUIActivity) getActivity();
            ra.switchContent(mData,geoloc!=null, nearby_locations);
        }

    }


    public DealMenuFragment(ArrayList<CoupersLocation> data)
    {
        mData= data;
    }

    public DealMenuFragment(){

    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("filter", mFilter);
        //outState.putSerializable("lk",mNL);

    }

    private void loadLocation(int location_id) {
        if (getActivity() == null)
            return;

        if (getActivity() instanceof ResponsiveUIActivity) {
            ResponsiveUIActivity ra = (ResponsiveUIActivity) getActivity();
            ra.onDealPressed(location_id);
        }
    }


    //LOAD FAVORITES
    private class LoadFavorites extends AsyncTask<String,Void,String> {

        private static final String NAMESPACE = "http://tempuri.org/";
        private static final String SOAP_ACTION = "http://tempuri.org/GetUserFavoriteLocations";
        private static final String URL = "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx";
        private static final String METHOD_NAME = "GetUserFavoriteLocations";

        ArrayList<HashMap<String, String>> FavLocList = new ArrayList<HashMap<String, String>>();

        @Override
        protected String doInBackground(String... params){
            String response = null;

            for(String param : params){

                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                SoapSerializationEnvelope envelope =  new SoapSerializationEnvelope(SoapEnvelope.VER11);PropertyInfo property = new PropertyInfo();
                request.addProperty("user_id",1);
                envelope.dotNet=true;
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

                try {
                    androidHttpTransport.call(SOAP_ACTION, envelope);
                    SoapObject result = (SoapObject) envelope.bodyIn;
                    SoapObject soData = (SoapObject) ((SoapObject) ((SoapObject) result.getProperty(0)).getProperty(1)).getProperty(0);
                    /*SoapObject soResponse = (SoapObject) result.getProperty("GetUserFavoriteLocationsResponse");
                    SoapObject soResult = (SoapObject) soResponse.getProperty("GetUserFavoriteLocationsResult");
                    SoapObject soDiffgram = (SoapObject) soResult.getProperty("diffgram") ;
                    SoapObject soNewDataSet = (SoapObject) soDiffgram.getProperty("NewDataSet") ;*/
                    SoapObject soTable;



                    String _tag[]={
                            WebServiceDataFields.FAVLOC_LOCATION_ID,
                            WebServiceDataFields.FAVLOC_CATEGORY_ID,
                            WebServiceDataFields.FAVLOC_LOCATION_NAME,
                            WebServiceDataFields.FAVLOC_LOCATION_LOGO,
                            WebServiceDataFields.FAVLOC_NEW_DEAL_COUNT};

                    for (int j=0;j<soData.getPropertyCount();j++)
                    {
                        soTable = (SoapObject) soData.getProperty(j) ;
                        //System.out.println(Table.toString());
                        HashMap<String, String> map = new HashMap<String, String>();
                        //TODO Use same static fields from ResponsiveUIActivity to create the map
                        for(int p =0;p<_tag.length;p++)
                            map.put(_tag[p].toString(),soTable.getPropertyAsString(_tag[p]));
                        FavLocList.add(map);
                    }
                    response="ok";
                } catch (Exception e) {
                    e.printStackTrace();
                    response=getString(R.string.server_connection_error);
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals(getString(R.string.server_connection_error)))
            {
                //TODO instantiate an activity to show server connection error, finalize app.
            }

            //mContainer.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            //mContainer.findViewById(R.id.textView).setVisibility(View.INVISIBLE);

            //UpdateMenu(FavLocList);

        }

    }


}
