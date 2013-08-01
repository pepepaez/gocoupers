package com.coupers.coupers;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.coupers.entities.CoupersLocation;
import com.coupers.entities.WebServiceDataFields;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.coupers.utils.XMLParser;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends Activity {


    // variables for test of coupers on Amazon AWS

    private static final String URL_WS = "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx";



    private static final String[] sampleACTV = new String[] {
            "android", "iphone", "blackberry"
    };


    // All static variables
    static final String URL = "http://marvinduran.com/pepe/data/dealslogos.xml";

    private boolean DealsLoaded = false;
    private boolean FavoritesLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.main_hub_ui);

        //TODO DONE figure out if need to implement another imageloader library (this one seems to take too long to load images for the first time)


        //LoadDeals loader = new LoadDeals();
        //CoupersDealWS loader = new CoupersDealWS();

        //loader.execute(new String[] {URL});

        LoadData();

        setContentView(R.layout.activity_main);

    }

    private void LoadData(){
        CoupersObject obj = new CoupersObject("http://tempuri.org/GetCityDeals",
                "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx",
                "GetCityDeals");
        obj.addParameter("city",getResources().getString(R.string.city));
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

    public void UpdateMenu(ArrayList<HashMap<String, String>> aData, String WSExecuted){

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
        Intent intent = new Intent(MainActivity.this,ResponsiveUIActivity.class);
        intent.putExtra("data",mData);
        intent.putExtra("gps",geoloc!=null);
        intent.putExtra("nearby",nearby_locations);

        startActivity(intent);

    }



    //TODO Remove if not used in the end.
    private class LoadDeals extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = null;
            XMLParser parser = new XMLParser();

            for(String url : urls){
                response=parser.getXmlFromUrl(url);
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView).setVisibility(View.INVISIBLE);
            Intent intent = new Intent(MainActivity.this,ResponsiveUIActivity.class);
            intent.putExtra("deals",result);

            startActivity(intent);
        }
    }

   /* private class CoupersDealWS extends AsyncTask<String,Void,String>{

        private static final String NAMESPACE = "http://tempuri.org/";
        private static final String SOAP_ACTION = "http://tempuri.org/GetPromociones";
        private static final String URL = "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx";
        private static final String METHOD_NAME = "GetPromociones";

        ArrayList<HashMap<String, String>> DealsList = new ArrayList<HashMap<String, String>>();

        @Override
        protected String doInBackground(String... params){
            String response = null;

            for(String param : params){

                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                SoapSerializationEnvelope envelope =  new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet=true;
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

                try {
                    androidHttpTransport.call(SOAP_ACTION, envelope);
                    SoapObject result = (SoapObject) envelope.bodyIn;
                    SoapObject GetPromocionesResult = (SoapObject) result.getProperty("GetPromocionesResult");
                    SoapObject diffgram = (SoapObject) GetPromocionesResult.getProperty("diffgram") ;
                    SoapObject NewDataSet = (SoapObject) diffgram.getProperty("NewDataSet") ;
                    SoapObject Table;



                    String _tag[]={ResponsiveUIActivity.KEY_ID,
                            ResponsiveUIActivity.KEY_TYPE,
                            ResponsiveUIActivity.KEY_DEAL_DESC,
                            ResponsiveUIActivity.KEY_DEAL_START,
                            ResponsiveUIActivity.KEY_DEAL_END,
                            ResponsiveUIActivity.KEY_DEAL_TIP,
                            ResponsiveUIActivity.KEY_LOCATION_ID,
                            ResponsiveUIActivity.KEY_LOCATION_LOGO,
                            ResponsiveUIActivity.KEY_THUMB_URL};

                    for (int j=0;j<NewDataSet.getPropertyCount();j++)
                    {
                        Table = (SoapObject) NewDataSet.getProperty(j) ;
                        //System.out.println(Table.toString());
                        HashMap<String, String> map = new HashMap<String, String>();
                        //TODO Use same static fields from ResponsiveUIActivity to create the map
                        for(int p =0;p<_tag.length;p++)
                            map.put(_tag[p].toString(),Table.getPropertyAsString(_tag[p]));
                        DealsList.add(map);
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

            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView).setVisibility(View.INVISIBLE);
            //if (result == null) TODO setup dialog if XML results in null, exit the application
            Intent intent = new Intent(MainActivity.this,ResponsiveUIActivity.class);
            intent.putExtra("deals",DealsList);

            startActivity(intent);
        }

    }*/
}
