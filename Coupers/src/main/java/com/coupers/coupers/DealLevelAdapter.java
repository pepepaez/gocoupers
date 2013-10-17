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
import com.coupers.entities.CoupersDealLevel;
import com.coupers.entities.CoupersLocation;

import java.util.ArrayList;

/**
 * Created by pepe on 10/7/13.
 */
public class DealLevelAdapter extends BaseAdapter {

    private ArrayList<CoupersDealLevel> levels = new ArrayList<CoupersDealLevel>();
    private static LayoutInflater inflater = null;


    public DealLevelAdapter(Activity a, ArrayList<CoupersDealLevel> levels)
    {
        this.levels=levels;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return levels.size();
    }

    @Override
    public Object getItem(int i) {
        return levels.get(i);
    }

    @Override
    public long getItemId(int i) {
        return levels.get(i).level_id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        holder = new ViewHolder();
        CoupersDealLevel level = levels.get(i);
        view = inflater.inflate(R.layout.level_item,null);
        holder.level_id = (TextView) view.findViewById(R.id.level_id);
        holder.level_id.setText(String.valueOf(level.level_id));

        holder.level_legend = (TextView) view.findViewById(R.id.deal_legend);
        holder.level_legend.setText(level.level_deal_legend + " : " + String.valueOf(level.level_start_at));

        view.setTag(holder);

        return view;
    }

    public static class ViewHolder{
        public TextView level_id;
        public TextView level_legend;
    }
}
