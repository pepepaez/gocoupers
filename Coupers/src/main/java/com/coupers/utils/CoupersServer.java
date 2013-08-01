package com.coupers.utils;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.coupers.coupers.DealMenuFragment;
import com.coupers.coupers.MainActivity;
import com.coupers.coupers.R;
import com.coupers.coupers.ResponsiveUIActivity;
import com.coupers.entities.WebServiceDataFields;

import org.apache.http.impl.cookie.BasicMaxAgeHandler;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pepe on 7/24/13.
 */

public class CoupersServer extends AsyncTask<String,Void,String> {

    private CoupersObject mObject;
    private Fragment mContext;
    private Activity mActivity;
    ArrayList<HashMap<String, String>> aServerData = new ArrayList<HashMap<String, String>>();

    public CoupersServer(CoupersObject obj, Fragment context){
        mObject = obj;
        mContext = context;
    }

    public CoupersServer(CoupersObject obj, Activity activity){
        mObject = obj;
        mActivity = activity;
    }


    @Override
    protected String doInBackground(String... params){
        String response = null;

        for(String param : params){

            SoapObject request = new SoapObject(mObject.getNAMESPACE(), mObject.getMETHOD_NAME());
            SoapSerializationEnvelope envelope =  new SoapSerializationEnvelope(SoapEnvelope.VER11);PropertyInfo property = new PropertyInfo();
            if (mObject.getParameterCount()>0)
                for (int i = 0; i < mObject.getParameterCount(); i++) {
                    request.addProperty(mObject.getParameterKey(i), mObject.getParameterValue(i));
                }

            envelope.dotNet=true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(mObject.getURL());

            try {
                androidHttpTransport.call(mObject.getSOAP_ACTION(), envelope);
                SoapObject result = (SoapObject) envelope.bodyIn;
                SoapObject soData = (SoapObject) ((SoapObject) ((SoapObject) result.getProperty(0)).getProperty(1)).getProperty(0);
                SoapObject soTable;

                String _tag[]=mObject.getTag();

                for (int j=0;j<soData.getPropertyCount();j++)
                {
                    soTable = (SoapObject) soData.getProperty(j) ;
                    HashMap<String, String> map = new HashMap<String, String>();
                    for(int p =0;p<_tag.length;p++)
                        map.put(_tag[p].toString(),soTable.getPropertyAsString(_tag[p]));
                    aServerData.add(map);
                }
                response="ok";
            } catch (Exception e) {
                e.printStackTrace();
                response=mContext==null? mContext.getString(R.string.server_connection_error):mActivity.getString(R.string.server_connection_error);
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);


        if (mContext!=null){
            if (result.equals(mContext.getString(R.string.server_connection_error)))
            {
                //TODO instantiate an activity to show server connection error, finalize app.
            }
            if(mContext instanceof DealMenuFragment){
                ((DealMenuFragment) mContext).Update(aServerData, mObject.getMETHOD_NAME());
            }
        }

        if (mActivity!=null)
        {
            if (mActivity instanceof MainActivity)
                ((MainActivity) mActivity).UpdateMenu(aServerData, mObject.getMETHOD_NAME());
            if(mActivity instanceof ResponsiveUIActivity)
                ((ResponsiveUIActivity) mActivity).Update(aServerData, mObject.getMETHOD_NAME());
        }
    }

}
