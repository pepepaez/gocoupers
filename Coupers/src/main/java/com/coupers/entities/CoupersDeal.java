package com.coupers.entities;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.ArrayList;

import static com.coupers.entities.CoupersData.SQLiteDictionary.*;

/**
 * Created by pepe on 7/27/13.
 */
public class CoupersDeal implements Serializable{
    public int deal_id;
    public int location_id;
    public String deal_start_date;
    public String deal_end_date;
    public String deal_URL;
    public boolean saved_deal = false;
    public String fb_post_id;
    public int current_level_id;
    public int share_count;

    public ArrayList<CoupersDealLevel> deal_levels = new ArrayList<CoupersDealLevel>();

    public CoupersDeal(int location_id, int deal_id, String deal_start_date, String deal_end_date) {
        this.location_id = location_id;
        this.deal_id = deal_id;
        this.deal_start_date = deal_start_date;
        this.deal_end_date = deal_end_date;
    }

    public CoupersDeal(Cursor cursor){
        this.location_id = cursor.getInt(cursor.getColumnIndex(tb_Deal.location_id));
        this.deal_id = cursor.getInt(cursor.getColumnIndex(tb_Deal.deal_id));
        this.deal_start_date = cursor.getString(cursor.getColumnIndex(tb_Deal.deal_start_date));
        this.deal_end_date = cursor.getString(cursor.getColumnIndex(tb_Deal.deal_end_date));
        if (cursor.getInt(cursor.getColumnIndex(tb_Deal.saved_deal))==1)
            this.saved_deal = true;
        this.fb_post_id = cursor.getString(cursor.getColumnIndex(tb_Deal.fb_post_id));
        this.current_level_id = cursor.getInt(cursor.getColumnIndex(tb_Deal.current_level_id));
        this.share_count = cursor.getInt(cursor.getColumnIndex(tb_Deal.share_count));
    }

    public ContentValues getSQLiteValues(){
        ContentValues values = new ContentValues();

        values.put(tb_Deal.deal_id,this.deal_id);
        values.put(tb_Deal.location_id,this.location_id);
        values.put(tb_Deal.deal_start_date,this.deal_start_date);
        values.put(tb_Deal.deal_end_date,this.deal_end_date);
        values.put(tb_Deal.saved_deal,this.saved_deal?1:0);
        values.put(tb_Deal.fb_post_id,this.fb_post_id);
        values.put(tb_Deal.current_level_id,this.current_level_id);
        values.put(tb_Deal.share_count,this.share_count);

        return values;
    }
}
