package com.coupers.coupers;

import android.app.Application;
import android.content.Context;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersDeal;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.CoupersDBHelper;

import java.util.ArrayList;

/**
 * Created by pepe on 8/25/13.
 */
public class CoupersApp extends Application {

    // APP DATA
    private String user_id;
    private String user_city;
    public ArrayList<CoupersLocation> locations = new ArrayList<CoupersLocation>();
    private int selected_location_id;
    public int selected_category=10;
    public boolean nearby_locations = false;
    public boolean gps_available = false;

    public boolean exit_next=false;
    public boolean reload = false;

    // APP UTILS
    private Context context;
    public CoupersDBHelper db;
    private CoupersData.Interfaces.CallBack callBack=null;

    // APP LOGIC CONTROL
    private boolean refresh = false;

    public void initialize(Context context)
    {
        this.context = context;
        db = new CoupersDBHelper(this.context);
        this.locations = null;
        this.locations = new ArrayList<CoupersLocation>();
    }

    public void registerCallBack(CoupersData.Interfaces.CallBack listener){
        this.callBack=listener;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_city() {
        return user_city;
    }

    public void setUser_city(String user_city) {
        this.user_city = user_city;
    }

    public void setSelectedLocation(int location_id)
    {
        this.selected_location_id = location_id;
    }

    public CoupersLocation getSelectedLocation(){
        return findLocation(this.selected_location_id);
    }

    public boolean exists(CoupersLocation find_location){
        for (CoupersLocation location : locations)
        {
            if (location.location_id==find_location.location_id)
            return true;
        }
        return false;
    }

    public void resetShow(){
        this.gps_available=false;
        this.nearby_locations=false;
        for (CoupersLocation location : locations)
        {
           location.show=false;
        }
    }

    public void setShowAll(){
        for (CoupersLocation location : locations)
        {
            location.show=true;
        }
    }

    public void setCategory(){
        for (CoupersLocation location : locations)
        {
            if (location.category_id==selected_category)
                location.show=true;
        }

    }

    public void showSavedDeals(){
        for (CoupersLocation location : locations)
        {
            if (location.location_city.equals(getUser_city()))
            {
                for (CoupersDeal deal : location.location_deals)
                {
                    if (deal.saved_deal)
                        location.show=true;
                }
            }
        }
    }

    public void showAllDeals(String city){
        for (CoupersLocation location : locations)
        {
            if (location.location_city.equals(city))
                location.show=true;
        }
    }

    public void setFavorite(CoupersLocation favorite_location){

        //Update on DB
        if (!db.exists(favorite_location))
            db.addLocation(favorite_location);
        else
            db.toggleLocationFavorite(favorite_location.location_id,favorite_location.location_isfavorite);


        //Update App data
        for (CoupersLocation location : locations)
        {
            if (location.location_id==favorite_location.location_id)
            {
                location.location_isfavorite=true;
                //Update Menu
                if (callBack!=null) callBack.update(location);
            return;
            }
            //TODO Save to DB
        }

        // if not found then add to list of favorites
        //locations.add(favorite_location);
        //Update Menu
        if (callBack!=null) callBack.update(favorite_location);

    }

    public void unsetFavorite(CoupersLocation favorite_location){

        //Update DB
        //favorite_location.location_isfavorite=false;
        if (!db.exists(favorite_location))
            db.addLocation(favorite_location);
        else
            db.toggleLocationFavorite(favorite_location.location_id,favorite_location.location_isfavorite);

        //Update Menu
        if (callBack!=null) callBack.update(favorite_location.location_id);

        //Update App data
        for (CoupersLocation location : locations)
        {
            if (location.location_id==favorite_location.location_id)
            {
                location.location_isfavorite=false;
            }
            //TODO Save to DB
        }
    }

    public ArrayList<CoupersLocation> getFavorites(){
        ArrayList<CoupersLocation> favorites = new ArrayList<CoupersLocation>();
        for (CoupersLocation location : locations)
        {
            if (location.location_isfavorite && location.location_city.equals(getUser_city()))
                favorites.add(location);
        }
        return favorites;
    }

    public CoupersLocation findLocation(int location_id){
        for (CoupersLocation location:locations)
        {
            if (location.location_id==location_id)
                return location;
        }
        return null;
    }

    //TODO set saved deal
    public void setSavedDeal(CoupersDeal deal){

        CoupersLocation location = findLocation(deal.location_id);
        if (location!=null)
        {
            CoupersDeal sDeal = location.findDeal(deal.deal_id);

            if (sDeal==null)
                location.location_deals.add(deal);
            else
                sDeal.saved_deal=true;

            if (db.exists(deal))
                db.toggleSavedDeal(deal.deal_id,true);
            else
                db.addDeal(deal);
        }
    }

    public void unsetSavedDeal(CoupersDeal deal){

        CoupersLocation location = findLocation(deal.location_id);
        if (location!=null)
        {
            CoupersDeal sDeal = location.findDeal(deal.deal_id);

            if (sDeal==null)
               location.location_deals.add(deal);
            else
                sDeal.saved_deal=false;

            if (db.exists(deal))
                db.toggleSavedDeal(deal.deal_id, false);
            else
                db.addDeal(deal);
        }
    }

    //TODO set FB post ID
    public void setFB_PostID(CoupersDeal deal, String post_id){

        CoupersLocation location = findLocation(deal.location_id);

        if (location!=null)
        {
            CoupersDeal sDeal = location.findDeal(deal.deal_id);

            if (sDeal==null)
                location.location_deals.add(deal);
            else
                sDeal.fb_post_id = post_id;

            if (db.exists(deal))
                db.savePostID(deal.deal_id,post_id);
            else
                db.addDeal(deal);

        }


    }
}
