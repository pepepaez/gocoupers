package com.coupers.coupers;

import android.app.Application;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersLocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pepe on 8/25/13.
 */
public class CoupersApp extends Application {
    private String user_id;
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

    public void initialize()
    {
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
}
