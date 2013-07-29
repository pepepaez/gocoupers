package com.coupers.coupers;

import android.content.Intent;
import android.content.res.TypedArray;
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
    private NodeList mNL = null;
    private ImageView last_selected = null;
    private XMLParser mParser = null;
    private ViewGroup mContainer = null;

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

    public void UpdateMenu(ArrayList<HashMap<String, String>> aFavLocList, String WebServiceExecuted)
    {


        final GridView lv = (GridView) mContainer.findViewById(R.id.gridView);
        // Get Menu
        TypedArray deals_menu = getResources().obtainTypedArray(R.array.deals_menu);

        //Create adapter
        MenuAdapter adapter=new MenuAdapter(this.getActivity());

        for (int i=0; i<deals_menu.length();i++)
        {
            if(deals_menu.getString(i).toUpperCase().equals("FAVORITOS"))
            {
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

            }else if(deals_menu.getString(i).toUpperCase().equals("CATEGORIAS"))
                adapter.addHeader(getString(R.string.categories));
            else
                adapter.addItem(deals_menu.getString(i));
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
                if (option_selected!=null){
                    if (last_selected != null) last_selected.setBackgroundColor(0);
                    option_selected.setBackgroundResource(R.drawable.list_selector_gym);
                    last_selected = option_selected;
                    TypedArray dealsmenu = getResources().obtainTypedArray(R.array.deals_menu_id);
                    String filter = dealsmenu.getString(position);

                    Fragment newContent = new DealGridFragment(filter, mNL, mParser);
                    if (newContent != null)
                        switchFragment(newContent);
                }

                ImageView favorite_location = (ImageView) view.findViewById(R.id.favorite_location);
                if (favorite_location!=null){
                    //TODO call CardFlipActivity for selected location
                    String location_id = ((MenuAdapter) lv.getAdapter()).getLocationId(position);
                    loadLocation(location_id);
                }

            }
        });
    }

    public DealMenuFragment(String filter, NodeList nl, XMLParser parser){

        mFilter=filter;
        mNL= nl;
        mParser = parser;

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
	
	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof ResponsiveUIActivity) {
			ResponsiveUIActivity ra = (ResponsiveUIActivity) getActivity();
			ra.switchContent(fragment);
		}
	}

    private void loadLocation(String location_id) {
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
