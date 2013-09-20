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
    private String user_id;
    private Context context;
    public CoupersDBHelper db;
    private boolean refresh = false;
    private List<String> need_refresh;
    private ArrayList<HashMap<String, String>> FavoriteLocations2 = new ArrayList<HashMap<String, String>>();
    private ArrayList<CoupersLocation> FavoriteLocations = new ArrayList<CoupersLocation>();
    private CoupersData.Interfaces.CallBack callBack=null;

    public void registerCallBack(CoupersData.Interfaces.CallBack listener){
        this.callBack=listener;
    }
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void initialize(Context context)
    {
        this.context = context;
        db = new CoupersDBHelper(this.context);
        this.FavoriteLocations= new ArrayList<CoupersLocation>();
    }

    public List<String> getNeed_refresh() {
        return need_refresh;
    }

    public void RefreshFavorites(){
        refresh=true;
        //need_refresh.add("favorites");
    }

    public boolean NeedRefresh(){
        return refresh;
    }

    public void ResetRefresh(){
        refresh=false;
    }

    public void addFavorite (final HashMap<String,String> item)
    {
        //FavoriteLocations.add(item);
    }

    public void addFavorite(final CoupersLocation item){
        FavoriteLocations.add(item);
        item.location_isfavorite=true;
        if (!db.exists(item))
            db.addLocation(item);
        else
            db.toggleLocationFavorite(item.location_id,item.location_isfavorite);
        if (callBack!=null) callBack.update(item);
    }

    public boolean removeFavorite (int location_id){
        boolean result = false;
        CoupersLocation loc=null;
        for (CoupersLocation location: FavoriteLocations){
            if (Integer.valueOf(location.location_id)==location_id)
                loc=location;
                result=true;
        }
        if (result && loc!=null)
        {
            loc.location_isfavorite=false;
            if (!db.exists(loc))
                db.addLocation(loc);
            else
                db.toggleLocationFavorite(loc.location_id,loc.location_isfavorite);

            FavoriteLocations.remove(loc);
            if (callBack!=null) callBack.update(location_id);
        }
        return result;
    }

    public boolean isFavorite(int location_id){
        boolean result = false;
        for (CoupersLocation location: FavoriteLocations){
            if (Integer.valueOf(location.location_id)==location_id)
                result=true;
        }
        return result;
    }

    //TODO set saved deal
    public void setSavedDeal(int deal_id){

    }

    //TODO set FB post ID
    public void setFB_PostID(int deal_id, String post_id){

    }
}
