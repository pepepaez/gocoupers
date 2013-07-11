package com.coupers.coupers;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coupers.utils.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pepe on 7/11/13.
 */
public class MenuAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private TypedArray data_simple;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;

    public MenuAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public MenuAdapter(Activity a,TypedArray d) {
        activity = a;
        data_simple=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        if (data == null)
            return data_simple.length();
        else
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
            vi = inflater.inflate(R.layout.list_menu_item, null);

        ImageView selected_indicator= null; // thumb image
        if (vi != null) {
            selected_indicator = (ImageView)vi.findViewById(R.id.selected_indicator);
            selected_indicator.setBackgroundColor(0);
        }
        TextView item_menu_text = null; // deal tip
        if (vi != null) {
            item_menu_text = (TextView)vi.findViewById(R.id.item_menu_text);
        }

        String option_text = null;
        option_text = data_simple.getString(position);


        // Setting all values in listview

        item_menu_text.setText(option_text);


        return vi;
    }
}