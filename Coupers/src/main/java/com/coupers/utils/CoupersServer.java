package com.coupers.utils;

import android.os.AsyncTask;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pepe on 7/24/13.
 */

public class CoupersServer extends AsyncTask<Void,Void,String> {

    private CoupersObject mObject;
    private ResultCallback call_me;
    private Exception exception = null;
    ArrayList<HashMap<String, String>> aServerData = new ArrayList<HashMap<String, String>>();

    public CoupersServer(CoupersObject obj, ResultCallback listener)
    {
        mObject=obj;
        call_me = listener;

    }

    @Override
    protected String doInBackground(Void... voids){
        String response = null;

        SoapObject request = new SoapObject(mObject.getNAMESPACE(), mObject.getMETHOD_NAME());
        SoapSerializationEnvelope envelope =  new SoapSerializationEnvelope(SoapEnvelope.VER11);PropertyInfo property = new PropertyInfo();
        if (mObject.getParameterCount()>0)
            for (int i = 0; i < mObject.getParameterCount(); i++) {
                request.addProperty(mObject.getParameterKey(i), mObject.getParameterValue(i));
            }

        envelope.dotNet=true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(mObject.getURL());

        try
        {
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
        } catch (Exception e)
        {
            e.printStackTrace();
            exception = e;
        }

        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        call_me.Update(aServerData,mObject.getMETHOD_NAME(),exception);

    }

    public interface ResultCallback{
        public void Update(ArrayList<HashMap<String,String>> result, String method_name, Exception e);
    }

}
