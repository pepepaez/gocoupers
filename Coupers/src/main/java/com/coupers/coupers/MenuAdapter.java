package com.coupers.coupers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.coupers.utils.ImageLoader;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by pepe on 7/11/13.
 */
public class MenuAdapter extends BaseAdapter {

    private Activity activity;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
    private ArrayList mData = new ArrayList();
    private TreeSet mHeaderSet = new TreeSet();


    public MenuAdapter(Activity a)
    {
        activity=a;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public void addItem(final String item)
    {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addHeader(final String item)
    {
        mData.add(item);
        mHeaderSet.add(mData.size()-1);
        notifyDataSetChanged();

    }
    @Override
    public int getItemViewType(int position) {
        return mHeaderSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount()
    {
        return TYPE_MAX_COUNT;
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        int type = getItemViewType(position);
        if(convertView==null){
            holder = new ViewHolder();
            switch (type){
                case TYPE_ITEM:
                    convertView = inflater.inflate(R.layout.list_menu_item,null);
                    holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                    holder.indicator = (ImageView) convertView.findViewById(R.id.selected_indicator);
                    holder.indicator.setBackgroundColor(0);
                    break;
                case TYPE_SEPARATOR:
                    convertView = inflater.inflate(R.layout.list_menu_header,null);
                    holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                    break;
            }
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(mData.get(position).toString());
        return convertView;

    }

    public static class ViewHolder{
        public TextView textView;
        public ImageView indicator;
    }
}