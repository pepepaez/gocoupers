package com.coupers.coupers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.CoupersMenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by pepe on 9/23/13.
 */
public class ExpMenuAdapter extends BaseExpandableListAdapter {

    private static final int TYPE_CATEGORY = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_FAVORITE = 2;
    private static final int TYPE_MAX_COUNT = TYPE_FAVORITE + 1;
    private Context context;
    private LayoutInflater inflater;
    private List<CoupersMenuItem> headers = new ArrayList<CoupersMenuItem>(); // header titles
    // child data in format of header title, child title
    private HashMap<String, List<CoupersMenuItem>> children = new HashMap<String, List<CoupersMenuItem>>();

    public ExpMenuAdapter(Context context) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItems(final String header, final List<CoupersMenuItem> item)
    {
        children.put(header, item);
        notifyDataSetChanged();
    }


    public void addHeader(final CoupersMenuItem item)
    {
        headers.add(item);
        notifyDataSetChanged();
    }

    public void addFavorites(final String header, final List<CoupersMenuItem> item)
    {
        children.put(header,item);
        notifyDataSetChanged();
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.children.get(this.headers.get(groupPosition).item_text)
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final CoupersMenuItem child = (CoupersMenuItem) getChild(groupPosition, childPosition);

        ViewHolder holder = null;

        int type = child.item_type;
        //if(convertView==null){
        holder = new ViewHolder();
        switch (type){
            case TYPE_CATEGORY:
                convertView = inflater.inflate(R.layout.list_menu_item,null);
                holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                holder.textView.setText(child.item_text);
                holder.icon = (ImageView) convertView.findViewById(R.id.item_menu_icon);
                holder.icon.setImageResource(child.item_icon);
                convertView.setBackgroundResource(child.item_bg);
                holder.indicator = (ImageView) convertView.findViewById(R.id.selected_indicator);
                //holder.indicator.setBackgroundResource(android.R.color.white);
                //holder.indicator.setPadding(5,0,0,0);
                break;
            case TYPE_FAVORITE:
                convertView = inflater.inflate(R.layout.list_menu_favorite,null);
                CoupersLocation location = child.getLocation();
                holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                holder.textView.setText(String.valueOf(location.location_id));
                holder.logo = (ImageView) convertView.findViewById(R.id.location_logo);
                holder.textView.setVisibility(View.INVISIBLE);
                AQuery aq = new AQuery(convertView);
                aq.id(R.id.location_logo).image(location.location_logo,true,true);
                holder.dealcount = (TextView) convertView.findViewById(R.id.new_deal_count);
                holder.dealcount.setText(String.valueOf(location.CountDeals));
                convertView.setBackgroundResource(child.item_bg);
                break;
        }
        convertView.setTag(holder);

  /*      }else{
            holder = (ViewHolder) convertView.getTag();
        }*/

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.children.get(this.headers.get(groupPosition).item_text)
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.headers.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.headers.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        CoupersMenuItem header = (CoupersMenuItem) getGroup(groupPosition);
        ViewHolder holder = null;

        int type = header.item_type;
        //if(convertView==null){
        holder = new ViewHolder();
        switch (type){
            case TYPE_HEADER:
                convertView = inflater.inflate(R.layout.list_menu_header,null);
                holder.textView = (TextView) convertView.findViewById(R.id.item_menu_text);
                holder.textView.setText(header.item_text);
                break;
        }
        convertView.setTag(holder);

  /*      }else{
            holder = (ViewHolder) convertView.getTag();
        }*/

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public int getChildTypeCount() {
        return TYPE_CATEGORY+TYPE_FAVORITE;
    }

    @Override
    public int getGroupTypeCount() {
        return TYPE_HEADER;
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

    public static class ViewHolder{
        public TextView textView;
        public ImageView indicator;
        public ImageView logo;
        public TextView dealcount;
        public ImageView icon;
    }
}
