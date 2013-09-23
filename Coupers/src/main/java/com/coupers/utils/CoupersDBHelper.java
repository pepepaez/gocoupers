package com.coupers.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersDeal;
import com.coupers.entities.CoupersDealLevel;
import com.coupers.entities.CoupersLocation;
import com.google.android.gms.internal.cu;

import java.util.ArrayList;
import java.util.List;

import static com.coupers.entities.CoupersData.*;
import static com.coupers.entities.CoupersData.SQLiteDictionary.*;

/**
 * Created by pepe on 9/18/13.
 */
public class CoupersDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "coupers.db";



    public CoupersDBHelper(Context context){
        super(context,DATABASE_NAME,null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String create_tb_location =
                "CREATE TABLE " + tb_Location.table_name +
                        " (" +
                        tb_Location.location_id + " INTEGER PRIMARY KEY," +
                        tb_Location.category_id + " INTEGER, " +
                        tb_Location.location_name + " TEXT," +
                        tb_Location.location_description + " TEXT, " +
                        tb_Location.location_website + " TEXT, " +
                        tb_Location.location_logo + " TEXT, " +
                        tb_Location.location_thumbnail + " TEXT, " +
                        tb_Location.location_address + " TEXT, " +
                        tb_Location.location_city + " TEXT, " +
                        tb_Location.location_phone_number1 + " TEXT, " +
                        tb_Location.location_phone_number2 + " TEXT, " +
                        tb_Location.location_latitude + " REAL, " +
                        tb_Location.location_longitude + " REAL, " +
                        tb_Location.location_hours_operation + " TEXT, " +
                        tb_Location.isFavorite + " INTEGER" + ")";

        String create_tb_deal =
                "CREATE TABLE " + tb_Deal.table_name +
                        " (" +
                        tb_Deal.deal_id + " INTEGER PRIMARY KEY," +
                        tb_Deal.location_id + " INTEGER," +
                        tb_Deal.deal_start_date + " TEXT," +
                        tb_Deal.deal_end_date + " TEXT," +
                        tb_Deal.saved_deal + " TEXT," +
                        tb_Deal.fb_post_id + " TEXT," +
                        tb_Deal.current_level_id + " INTEGER," +
                        tb_Deal.share_count + " INTEGER" + ")";

        String create_tb_deal_level =
                "CREATE TABLE " + tb_DealLevel.table_name +
                        " (" +
                        tb_DealLevel.deal_id + " INTEGER," +
                        tb_DealLevel.level_id + " INTEGER," +
                        tb_DealLevel.level_deal_legend + " TEXT," +
                        tb_DealLevel.level_deal_description + " TEXT," +
                        tb_DealLevel.level_redeem_code + " TEXT," +
                        tb_DealLevel.level_share_code + " TEXT," +
                        tb_DealLevel.level_start_at + " INTEGER" + ")";

        db.execSQL(create_tb_location);
        db.execSQL(create_tb_deal);
        db.execSQL(create_tb_deal_level);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("DROP TABLE IF EXISTS " + tb_Location.table_name);
        db.execSQL("DROP TABLE IF EXISTS " + tb_Deal.table_name);
        db.execSQL("DROP TABLE IF EXISTS " + tb_DealLevel.table_name);

        onCreate(db);
    }

    public boolean exists(CoupersLocation location) {
        boolean found = false;
        if (getLocation(location.location_id)!=null)
            found=true;
        return found;
    }

    public boolean exists(CoupersDeal deal) {
        boolean found = false;
        if (getDeal(deal.deal_id)!=null)
            found=true;
        return found;
    }

    public boolean exists(CoupersDealLevel level) {
        boolean found = false;
        if (getDealLevel(level.deal_id,level.level_id)!=null)
            found=true;
        return found;
    }

    public void addLocation(CoupersLocation location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values  = location.getSQLiteValues();

        db.insert(tb_Location.table_name, null, values);
        db.close();
    }

    public void addDeal(CoupersDeal deal) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values  = deal.getSQLiteValues();

        db.insert(tb_Deal.table_name, null, values);

        if (deal.deal_levels.size()>0)
            for (CoupersDealLevel level : deal.deal_levels)
                    addDealLevel(level);
        db.close();
    }

    public void addDealLevel(CoupersDealLevel level){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = level.getSQLiteValues();

        db.insert(tb_DealLevel.table_name,null,values);
        db.close();

    }

    public CoupersLocation getLocation(int location_id){
        @SuppressWarnings("unchecked")
        CoupersLocation location = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " +tb_Location.table_name + " WHERE location_id = " + String.valueOf(location_id), null);

        if (cursor!=null)
        {
            cursor.moveToFirst();
            if (!cursor.isAfterLast())
                location = new CoupersLocation(cursor);
            cursor.close();
        }

        db.close();

        return location;
    }

    public CoupersDeal getDeal(int deal_id){
        CoupersDeal deal = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+tb_Deal.table_name+" WHERE deal_id = "+String.valueOf(deal_id),null);

        if (cursor!=null)
        {
            cursor.moveToFirst();
            if (!cursor.isAfterLast())
                deal = new CoupersDeal(cursor);
            cursor.close();
        }
        db.close();
        return deal;
    }

    public CoupersDealLevel getDealLevel(int deal_id, int level_id){
        CoupersDealLevel level = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+tb_DealLevel.table_name+ " WHERE deal_id = "+String.valueOf(deal_id)+" AND level_id = "+String.valueOf(level_id),null);

        if (cursor!=null)
        {
            cursor.moveToFirst();
            if (!cursor.isAfterLast())
                level = new CoupersDealLevel(cursor);
            cursor.close();
        }
        db.close();

        return level;
    }

    public ArrayList<CoupersLocation> getAllLocations(){
        ArrayList<CoupersLocation> locations = new ArrayList<CoupersLocation>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + tb_Location.table_name,null);

        if (cursor != null)
        {
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                CoupersLocation location = new CoupersLocation(cursor);
                ArrayList<CoupersDeal> deals = getAllDeals(location.location_id);
                if (deals.size()>0)
                {
                    for (CoupersDeal deal : deals) {
                        ArrayList<CoupersDealLevel> levels = getAllDealLevels(deal.deal_id);
                        deal.deal_levels=levels;
                    }
                    location.location_deals=deals;
                    location.CountDeals = deals.size();
                    location.TopDeal=deals.get(0).deal_levels.get(0).level_deal_legend;
                }

                locations.add(location);
                cursor.moveToNext();
            }

            cursor.close();
        }

        db.close();

        return  locations;
    }

    public ArrayList<CoupersDeal> getAllDeals(int location_id){
        ArrayList<CoupersDeal> deals = new ArrayList<CoupersDeal>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + tb_Deal.table_name + " WHERE location_id="+String.valueOf(location_id),null);

        if (cursor != null)
        {
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                CoupersDeal deal = new CoupersDeal(cursor);
                deals.add(deal);
                cursor.moveToNext();
            }

            cursor.close();
        }

        db.close();

        return  deals;
    }

    public ArrayList<CoupersDealLevel> getAllDealLevels(int deal_id){
        ArrayList<CoupersDealLevel> levels = new ArrayList<CoupersDealLevel>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + tb_DealLevel.table_name + " WHERE deal_id = "+String.valueOf(deal_id),null);

        if (cursor != null)
        {
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                CoupersDealLevel level = new CoupersDealLevel(cursor);
                levels.add(level);
                cursor.moveToNext();
            }

            cursor.close();
        }

        db.close();

        return  levels;
    }

    public void deleteLocation(int location_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.rawQuery("DELETE FROM " + tb_Location.table_name + " WHERE location_id = " + String.valueOf(location_id), null);

        deleteDeals(location_id);

        db.close();
    }

    public void deleteDeal(int deal_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.rawQuery("DELETE FROM " + tb_Deal.table_name + " WHERE deal_id = " + String.valueOf(deal_id), null);
        deleteDealLevels(deal_id);

        db.close();
    }

    public void deleteDeals(int location_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        List<CoupersDeal> deals = getAllDeals(location_id);
        int deal_id;
        if (deals.size()>0)
        {
            deal_id=deals.get(0).deal_id;
            deleteDealLevels(deal_id);
        }

        db.rawQuery("DELETE FROM " + tb_Deal.table_name + " WHERE location_id = " + String.valueOf(location_id), null);

        db.close();

    }

    public void deleteDealLevel(int deal_id, int level_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.rawQuery("DELETE FROM " + tb_DealLevel.table_name + " WHERE deal_id = " + String.valueOf(deal_id)+ " AND level_id = "+ String.valueOf(level_id), null);
        deleteDealLevels(deal_id);

        db.close();
    }

    public void deleteDealLevels(int deal_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.rawQuery("DELETE FROM " + tb_DealLevel.table_name + " WHERE deal_id = " + String.valueOf(deal_id), null);

        db.close();
    }

    public void toggleLocationFavorite(int location_id,boolean is_favorite){
        SQLiteDatabase db = this.getWritableDatabase();
        String fav = "0";

        if (is_favorite)
            fav="1";

        db.rawQuery("UPDATE " + tb_Location.table_name + " SET " + tb_Location.isFavorite + " = " + fav,null );

        db.close();
    }


}
