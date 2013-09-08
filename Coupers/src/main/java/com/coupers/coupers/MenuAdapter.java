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
import com.coupers.utils.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by pepe on 7/11/13.
 */
public class MenuAdapter extends BaseAdapter {

    private Activity activity;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_FAVORITE = 2;
    private static final int TYPE_MAX_COUNT = TYPE_FAVORITE + 1;
    private ArrayList mData = new ArrayList();
    private TreeSet mHeaderSet = new TreeSet();
    private TreeSet mFavoriteSet = new TreeSet();
    private HashMap<String, HashMap<String, String>> mFavoriteData2 = new HashMap<String, HashMap<String, String>>();
    private HashMap<String, CoupersLocation> mFavoriteData = new HashMap<String, CoupersLocation>();
    private ArrayList<DealMenuFragment.CoupersMenuItem> mItems = new ArrayList<DealMenuFragment.CoupersMenuItem>();



    public MenuAdapter(Activity a)
    {
        activity=a;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public void addItem(final DealMenuFragment.CoupersMenuItem item_details)
    {
        mData.add(item_details.item_text);
        mItems.add(item_details);
        notifyDataSetChanged();
    }

    public void addHeader(final String item)
    {
        mData.add(item);
        mItems.add(null);
        mHeaderSet.add(mData.size() - 1);
        notifyDataSetChanged();

    }

    /*public void addFavorite (final HashMap<String,String> item)
    {
        mData.add(item.get(CoupersData.LOCATION_ID).toString());
        mItems.add(null);
        mFavoriteSet.add(mData.size() - 1);
        mFavoriteData.put("item" + String.valueOf(mData.size() - 1), item);
        notifyDataSetChanged();

    }*/

    public void addFavorite (final CoupersLocation item){
        mData.add(item.location_id);
        mItems.add(null);
        mFavoriteSet.add(mData.size() - 1);
        mFavoriteData.put("item" + String.valueOf(mData.size() - 1), item);
    }

    public int getLocationId(int position){

        return Integer.valueOf(mFavoriteData.get("item"+String.valueOf(position)).location_id);

    }

    public int getCategoryId(int position) {
        return mItems.get(position).category_id;
    }

    @Override
    public int getItemViewType(int position) {
        return mHeaderSet.contains(position) ? TYPE_HEADER : mFavoriteSet.contains(position ) ? TYPE_FAVORITE : TYPE_ITEM;
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
        if(convertView==null){
            holder = new ViewHolder();
            switch (type){
                case TYPE_ITEM:
                    convertView = inflater.inflate(R.layout.list_menu_item,null);
                    holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                    holder.textView.setText(mData.get(position).toString());
                    holder.icon = (ImageView) convertView.findViewById(R.id.item_menu_icon);
                    holder.icon.setImageResource(mItems.get(position).item_icon);
                    convertView.setBackgroundResource(mItems.get(position).item_bg);
                    holder.indicator = (ImageView) convertView.findViewById(R.id.selected_indicator);
                    //holder.indicator.setBackgroundResource(android.R.color.white);
                    //holder.indicator.setPadding(5,0,0,0);
                    break;
                case TYPE_HEADER:
                    convertView = inflater.inflate(R.layout.list_menu_header,null);
                    holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                    holder.textView.setText(mData.get(position).toString());
                    break;
                case TYPE_FAVORITE:
                    convertView = inflater.inflate(R.layout.list_menu_favorite,null);
                    CoupersLocation location = mFavoriteData.get("item"+String.valueOf(position));
                    holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                    holder.textView.setText(String.valueOf(location.location_id));
                    holder.logo = (ImageView) convertView.findViewById(R.id.location_logo);
                    holder.textView.setVisibility(View.INVISIBLE);
                    AQuery aq = new AQuery(convertView);
                    aq.id(R.id.location_logo).image(location.location_logo,true,true);
                    holder.dealcount = (TextView) convertView.findViewById(R.id.new_deal_count);
                    holder.dealcount.setText(location.CountDeals);
                    break;
            }
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }

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