package com.coupers.coupers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.coupers.entities.CoupersLocation;
import com.coupers.entities.WebServiceDataFields;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pepe on 6/30/13.
 */
public class DealAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<CoupersLocation> data = new ArrayList<CoupersLocation>();
    private static LayoutInflater inflater=null;

    private static final int SINGLE_DEAL = 0;
    private static final int MULTIPLE_DEALS = 1;
    private static final int TYPE_MAX_COUNT = MULTIPLE_DEALS + 1;


    public DealAdapter(Activity a){
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    public void addLocation(CoupersLocation data) {
        this.data.add(data);
    }

    @Override
    public int getItemViewType(int position) {
        return Integer.valueOf(data.get(position).CountDeals)==1 ? SINGLE_DEAL : MULTIPLE_DEALS;
    }

    @Override
    public int getViewTypeCount()
    {
        return TYPE_MAX_COUNT;
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        CoupersLocation location = data.get(position);

        ViewHolder holder = null;
        int type = getItemViewType(position);
        if (vi == null){
            holder = new ViewHolder();
            switch (type)
            {
                case SINGLE_DEAL:
                    vi = inflater.inflate(R.layout.list_rower_logo,null);
                    break;
                case MULTIPLE_DEALS:
                    vi = inflater.inflate(R.layout.list_rower_logo_multiple,null);
                    break;
            }
        }else{
            holder= (ViewHolder) vi.getTag();
        }
        AQuery aq = new AQuery(vi);


        TextView deal_tip = null; // deal tip
        if (vi != null) {
            deal_tip = (TextView)vi.findViewById(R.id.deal_tip);
        }

        if (location !=null)
        {
            switch(location.category_id)
            {
                case WebServiceDataFields.CATEGORY_ID_EAT:
                    vi.setBackgroundResource(R.drawable.list_selector_eat);
                    break;
                case WebServiceDataFields.CATEGORY_ID_FEEL_GOOD:
                    vi.setBackgroundResource(R.drawable.list_selector_feel_good);
                    break;
                case WebServiceDataFields.CATEGORY_ID_HAVE_FUN:
                    vi.setBackgroundResource(R.drawable.list_selector_have_fun);
                    break;
                case WebServiceDataFields.CATEGORY_ID_LOOK_GOOD:
                    vi.setBackgroundResource(R.drawable.list_selector_look_good);
                    break;
                case WebServiceDataFields.CATEGORY_ID_RELAX:
                    vi.setBackgroundResource(R.drawable.list_selector_relax);
                    break;

            }
        }

        // Setting all values in listview
        aq.id(R.id.logo_image).progress(R.id.progressBarLogo).image(location.location_logo,true,true);
        aq.id(R.id.list_image).progress(R.id.progressBarThumb).image(location.location_thumbnail,true,true);
        deal_tip.setText(location.TopDeal);

        return vi;
    }

    public static class ViewHolder{
        public ImageView logo_image;
        public ImageView list_image;
        public TextView deal_tip;
    }
}