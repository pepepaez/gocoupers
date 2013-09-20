package com.coupers.coupers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.CoupersMenuItem;

import java.util.ArrayList;

/**
 * Created by pepe on 7/11/13.
 */
public class MenuAdapter extends BaseAdapter {

    private Activity activity;
    private static LayoutInflater inflater=null;
    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_FAVORITE = 2;
    private static final int TYPE_MAX_COUNT = TYPE_FAVORITE + 1;
    private ArrayList <CoupersMenuItem> mDataSet = new ArrayList<CoupersMenuItem>();


    public MenuAdapter(Activity a)
    {
        activity=a;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

    public void addItem(final CoupersMenuItem item_details)
    {
        mDataSet.add(item_details);
        notifyDataSetChanged();
    }

    public void addHeader(final CoupersMenuItem item)
    {
        addItem(item);
    }

    public void addFavorite(final CoupersMenuItem item)
    {
        addItem(item);
    }

    public void removeFavorite(final int location_id)
    {

        CoupersMenuItem favorite = findFavorite(location_id);
        if (favorite !=null)
            mDataSet.remove(favorite);
        if (countFavorite()<=0)
        {
            CoupersMenuItem header = findHeader(activity.getString(R.string.favorites));
            if (header!=null)
                mDataSet.remove(header);
        }
        notifyDataSetChanged();
        //notifyDataSetInvalidated();
    }

    public void insertFavorite(final CoupersMenuItem favorite)
    {
        ArrayList<CoupersMenuItem> tempSet = new ArrayList<CoupersMenuItem>();
        CoupersMenuItem temp_header = new CoupersMenuItem(activity.getString(R.string.favorites));
        for (CoupersMenuItem item : mDataSet)
        {
            if (item.item_type==CoupersMenuItem.TYPE_HEADER)
                if (item.item_text == activity.getString(R.string.i_want_to))
                {
                    if (countFavorite()==0)
                        tempSet.add(temp_header);
                    tempSet.add(favorite);
                }
            tempSet.add(item);
        }
        mDataSet = null;
        mDataSet = tempSet;
        notifyDataSetChanged();
    }

    private int countFavorite(){
        int i = 0;
        for (CoupersMenuItem item : mDataSet)
            if (item.item_type==CoupersMenuItem.TYPE_LOCATION)
                i++;
        return i;
    }
    private CoupersMenuItem findFavorite(int location_id)
    {
        for (CoupersMenuItem item : mDataSet)
        {
            if (item.item_type==CoupersMenuItem.TYPE_LOCATION)
            {
                CoupersLocation location = item.getLocation();
                if (location.location_id==location_id)
                    return item;
            }
        }
        return null;
    }

    private CoupersMenuItem findHeader(String header_text){
        for (CoupersMenuItem item : mDataSet)
        {
            if (item.item_type==CoupersMenuItem.TYPE_HEADER)
            {
                if (item.item_text == header_text)
                    return item;
            }
        }
        return null;
    }

    public int getLocationId(int position){

        if(mDataSet.get(position).item_type==CoupersMenuItem.TYPE_LOCATION)
            return mDataSet.get(position).getLocation().location_id;
        else
            return -1;

    }

    public CoupersLocation getLocation(int position){
        if(mDataSet.get(position).item_type==CoupersMenuItem.TYPE_LOCATION)
            return mDataSet.get(position).getLocation();
        else
            return null;

    }

    public int getCategoryId(int position) {
        if (mDataSet.get(position).item_type==CoupersMenuItem.TYPE_CATEGORY)
            return mDataSet.get(position).category_id;
        else
            return -1;
    }

    @Override
    public int getItemViewType(int position) {
        return mDataSet.get(position).item_type;
    }

    @Override
    public int getViewTypeCount()
    {
        return TYPE_MAX_COUNT;
    }

    public Object getItem(int position) {
        return mDataSet.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void AnimateInOut(View view, boolean b){

        ImageView indicator = (ImageView) view.findViewById(R.id.selected_indicator);

        ScaleAnimation anim;
        if (b){
            anim = new ScaleAnimation(1, 20, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setFillAfter(true);
        }
        else{
            anim = new ScaleAnimation(20, 1, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setFillAfter(true);
        }
        anim.setDuration(200);

        anim.setFillEnabled(true);
        indicator.startAnimation(anim);
        //indicator.animate();
        //indicator.getAnimation().startNow();
    }


    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        int type = getItemViewType(position);
        //if(convertView==null){
            holder = new ViewHolder();
            switch (type){
                case TYPE_CATEGORY:
                    convertView = inflater.inflate(R.layout.list_menu_item,null);
                    holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                    holder.textView.setText(mDataSet.get(position).item_text);
                    holder.icon = (ImageView) convertView.findViewById(R.id.item_menu_icon);
                    holder.icon.setImageResource(mDataSet.get(position).item_icon);
                    convertView.setBackgroundResource(mDataSet.get(position).item_bg);
                    holder.indicator = (ImageView) convertView.findViewById(R.id.selected_indicator);
                    //holder.indicator.setBackgroundResource(android.R.color.white);
                    //holder.indicator.setPadding(5,0,0,0);
                    break;
                case TYPE_HEADER:
                    convertView = inflater.inflate(R.layout.list_menu_header,null);
                    holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                    holder.textView.setText(mDataSet.get(position).item_text);
                    break;
                case TYPE_FAVORITE:
                    convertView = inflater.inflate(R.layout.list_menu_favorite,null);
                    CoupersLocation location = mDataSet.get(position).getLocation();
                    holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                    holder.textView.setText(String.valueOf(location.location_id));
                    holder.logo = (ImageView) convertView.findViewById(R.id.location_logo);
                    holder.textView.setVisibility(View.INVISIBLE);
                    AQuery aq = new AQuery(convertView);
                    aq.id(R.id.location_logo).image(location.location_logo,true,true);
                    holder.dealcount = (TextView) convertView.findViewById(R.id.new_deal_count);
                    holder.dealcount.setText(String.valueOf(location.CountDeals));
                    convertView.setBackgroundResource(mDataSet.get(position).item_bg);
                    break;
            }
            convertView.setTag(holder);

  /*      }else{
            holder = (ViewHolder) convertView.getTag();
        }*/

        return convertView;

    }

    public static class ViewHolder{
        public TextView textView;
        public ImageView indicator;
        public ImageView logo;
        public TextView dealcount;
        public ImageView icon;
    }
}