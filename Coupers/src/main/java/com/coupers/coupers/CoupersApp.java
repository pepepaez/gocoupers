package com.coupers.coupers;

import android.app.Application;
import android.content.Context;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.CoupersDBHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pepe on 8/25/13.
 */
public class CoupersApp extends Application {

    // APP DATA
    private String user_id;
    private ArrayList<CoupersLocation> FavoriteLocations = new ArrayList<CoupersLocation>();
    public ArrayList<CoupersLocation> locations = new ArrayList<CoupersLocation>();
    public CoupersLocation selected_location = null;
    public int selected_category=10;
    public boolean nearby_locations = false;
    public boolean gps_available = false;

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
        this.FavoriteLocations= new ArrayList<CoupersLocation>();
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

    public void setCategory(){
        for (CoupersLocation location : locations)
        {
            if (location.category_id==selected_category)
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
        locations.add(favorite_location);
        //Update Menu
        if (callBack!=null) callBack.update(favorite_location);

    }

    public void unsetFavorite(CoupersLocation favorite_location){

        //Update DB
        favorite_location.location_isfavorite=false;
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
            if (location.location_isfavorite)
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
    public void setSavedDeal(int deal_id){

    }

    //TODO set FB post ID
    public void setFB_PostID(int deal_id, String post_id){

    }
}
