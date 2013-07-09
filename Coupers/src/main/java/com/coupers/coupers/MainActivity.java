package com.coupers.coupers;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.coupers.utils.ImageLoader;
import com.coupers.utils.XMLParser;


public class MainActivity extends Activity {


    // All static variables
    static final String URL = "http://marvinduran.com/pepe/data/dealslogos.xml";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.main_hub_ui);
        ImageLoader iml = new ImageLoader(this);
        //iml.clearCache();

        LoadDeals loader = new LoadDeals();

        loader.execute(new String[] {URL});

        setContentView(R.layout.activity_main);
        //comments


    }

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
            //if (result == null) TODO setup dialog if XML results in null, exit the application
            Intent intent = new Intent(MainActivity.this,ResponsiveUIActivity.class);
            intent.putExtra("deals",result);

            startActivity(intent);
        }
    }
}
