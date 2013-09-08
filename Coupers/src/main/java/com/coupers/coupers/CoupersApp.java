package com.coupers.coupers;

import android.app.Application;

import com.coupers.entities.CoupersLocation;

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

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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
    }

    public boolean removeFavorite (int location_id){
        boolean result = false;
        for (CoupersLocation location: FavoriteLocations){
            if (Integer.valueOf(location.location_id)==location_id)
                FavoriteLocations.remove(location);
                result=true;
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

    public interface StatusCallback {
        public void call(int something);
    }
}
