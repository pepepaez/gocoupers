package com.coupers.entities;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.ArrayList;

import static com.coupers.entities.CoupersData.SQLiteDictionary.*;

/**
 * Created by pepe on 7/13/13.
 */
public class CoupersLocation implements Serializable
{

    public int location_id;
    public int category_id;
    public String location_name;
    public String location_description;
    public String location_website_url;
    public String location_logo;
    public String location_thumbnail;
    public String location_address;
    public String location_city;
    public String location_phone_number1;
    public String location_phone_number2;
    public double location_latitude;
    public double location_longitude;
    public boolean location_isfavorite = false;
    public boolean show = false;
    public ArrayList<CoupersDeal> location_deals = new ArrayList<CoupersDeal>();

    public int CountDeals=0;
    public String TopDeal;
    public boolean Nearby = false;

    public CoupersLocation(int location_id, int category_id, String location_name, String location_description, String location_website_url, String location_logo, String location_thumbnail, String location_address, String location_city, String location_phone_number1, String location_phone_number2, double location_latitude, double location_longitude) {
        this.location_id = location_id;
        this.category_id = category_id;
        this.location_name = location_name;
        this.location_description = location_description;
        this.location_website_url = location_website_url;
        this.location_logo = location_logo;
        this.location_thumbnail = location_thumbnail;
        this.location_address = location_address;
        this.location_city = location_city.toLowerCase();
        this.location_phone_number1 = location_phone_number1;
        this.location_phone_number2 = location_phone_number2;
        this.location_latitude = location_latitude;
        this.location_longitude = location_longitude;
    }

    public CoupersLocation(Cursor dbcursor){
        this.location_id = dbcursor.getInt(dbcursor.getColumnIndex(tb_Location.location_id));
        this.category_id = dbcursor.getInt(dbcursor.getColumnIndex(tb_Location.category_id));
        this.location_name = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.location_name));
        this.location_description = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.location_description));
        this.location_website_url = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.location_website));
        this.location_logo = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.location_logo));
        this.location_thumbnail = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.location_thumbnail));
        this.location_address = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.location_address));
        this.location_city = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.location_city)).toLowerCase();
        this.location_phone_number1 = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.location_phone_number1));
        this.location_phone_number2 = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.location_phone_number2));
        this.location_latitude = dbcursor.getDouble(dbcursor.getColumnIndex(tb_Location.location_latitude));;
        this.location_longitude = dbcursor.getDouble(dbcursor.getColumnIndex(tb_Location.location_longitude));;
        this.TopDeal = dbcursor.getString(dbcursor.getColumnIndex(tb_Location.top_deal));
        if (dbcursor.getInt(dbcursor.getColumnIndex(tb_Location.isFavorite))==1)
            this.location_isfavorite=true;
        this.CountDeals = dbcursor.getInt(dbcursor.getColumnIndex(tb_Location.deal_count));
    }

    public int isFavorite(){
        int result = 0;
        if (location_isfavorite)
            result = 1;
        return result;
    }

    public ContentValues getSQLiteValues(){
        ContentValues values = new ContentValues();

        values.put(tb_Location.location_id, this.location_id);
        values.put(tb_Location.category_id, this.category_id);
        values.put(tb_Location.location_name,this.location_name);
        values.put(tb_Location.location_description,this.location_description);
        values.put(tb_Location.location_website, this.location_website_url);
        values.put(tb_Location.location_logo, this.location_logo);
        values.put(tb_Location.location_thumbnail, this.location_thumbnail);
        values.put(tb_Location.location_thumbnail, this.location_thumbnail);
        values.put(tb_Location.location_address, this.location_address);
        values.put(tb_Location.location_city, this.location_city.toLowerCase());
        values.put(tb_Location.location_phone_number1, this.location_phone_number1);
        values.put(tb_Location.location_phone_number2, this.location_phone_number2);
        values.put(tb_Location.location_latitude, this.location_latitude);
        values.put(tb_Location.location_longitude, this.location_longitude);
        values.put(tb_Location.location_hours_operation, "nothing really");
        values.put(tb_Location.isFavorite,this.isFavorite());
        values.put(tb_Location.top_deal, this.TopDeal);
        values.put(tb_Location.deal_count,this.CountDeals);


        return values;
    }

    public void addDeals(ArrayList<CoupersDeal> deals){
        this.location_deals=deals;

    }

    public CoupersDeal findDeal(int deal_id){
        for (CoupersDeal deal : this.location_deals)
        {
            if (deal.deal_id == deal_id)
                return deal;
        }
        return null;
    }


}