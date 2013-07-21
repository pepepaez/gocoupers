package com.coupers.coupers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.coupers.utils.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pepe on 6/30/13.
 */
public class DealAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;

    public DealAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;

        if(convertView==null)
            vi = inflater.inflate(R.layout.list_rower_logo, null);
        AQuery aq = new AQuery(vi);

        /*TextView deal_desc = null; // description
        if (vi != null) {
            deal_desc = (TextView)vi.findViewById(R.id.deal_desc);
        }
        TextView location_name = null; // location name
        if (vi != null) {
            location_name = (TextView)vi.findViewById(R.id.location_name);
        }*/
        /*ImageView logo_image= null; // thumb image
        if (vi != null) {
            logo_image = (ImageView)vi.findViewById(R.id.logo_image);
            logo_image.setVisibility(View.INVISIBLE);
        }*/
        TextView deal_tip = null; // deal tip
        if (vi != null) {
            deal_tip = (TextView)vi.findViewById(R.id.deal_tip);
        }
        TextView deal_id = null; // deal tip
        if (vi != null) {
            deal_id = (TextView)vi.findViewById(R.id.deal_id);
        }
        /*ImageView thumb_image= null; // thumb image
        if (vi != null) {
            thumb_image = (ImageView)vi.findViewById(R.id.list_image);
            thumb_image.setVisibility(View.INVISIBLE);
        }*/
        //ProgressBar pbThumb = (ProgressBar) vi.findViewById(R.id.progressBarThumb);
        //pbThumb.setVisibility(View.VISIBLE);

        //ProgressBar pbLogo = (ProgressBar)   vi.findViewById(R.id.progressBarLogo);
        //pbLogo.setVisibility(View.VISIBLE);

        HashMap<String, String> deal = new HashMap<String, String>();
        deal = data.get(position);

        String dealType = deal.get(ResponsiveUIActivity.KEY_TYPE);
        if (dealType.equals("food")) {
            vi.setBackgroundResource(R.drawable.list_selector_food);

        } else if (dealType.equals("cafe")) {
            vi.setBackgroundResource(R.drawable.list_selector_cafe);

        } else if (dealType.equals("gym")) {
            vi.setBackgroundResource(R.drawable.list_selector_gym);

        } else if (dealType.equals("beauty")) {
            vi.setBackgroundResource(R.drawable.list_selector_beauty);

        }


        // Setting all values in listview
        //deal_desc.setText(deal.get(ResponsiveUIActivity.KEY_DEAL_DESC));
        //location_name.setText(deal.get(ResponsiveUIActivity.KEY_LOCATION_NAME));
        deal_id.setText(deal.get(ResponsiveUIActivity.KEY_ID));

        aq.id(R.id.logo_image).progress(R.id.progressBarLogo).image(deal.get(ResponsiveUIActivity.KEY_LOCATION_LOGO),true,true);
        //imageLoader.DisplayImage(deal.get(ResponsiveUIActivity.KEY_LOCATION_LOGO),logo_image, pbLogo);
        deal_tip.setText(deal.get(ResponsiveUIActivity.KEY_DEAL_TIP));
        aq.id(R.id.list_image).progress(R.id.progressBarThumb).image(deal.get(ResponsiveUIActivity.KEY_THUMB_URL),true,true);
        //imageLoader.DisplayImage(deal.get(ResponsiveUIActivity.KEY_THUMB_URL), thumb_image, pbThumb);
/*        logo_image.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.findViewById(R.id.progressBarLogo).setVisibility(View.INVISIBLE);
                view.setVisibility(View.VISIBLE);
            }
        });

        thumb_image.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.findViewById(R.id.progressBarThumb).setVisibility(View.INVISIBLE);
                view.setVisibility(View.VISIBLE);
            }
        });*/

        return vi;
    }
}
