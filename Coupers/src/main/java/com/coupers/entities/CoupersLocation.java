package com.coupers.entities;

import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

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
    public HashMap<Integer, CoupersDeal> location_deals = new HashMap<Integer, CoupersDeal>();

    public String CountDeals;
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
        this.location_city = location_city;
        this.location_phone_number1 = location_phone_number1;
        this.location_phone_number2 = location_phone_number2;
        this.location_latitude = location_latitude;
        this.location_longitude = location_longitude;
    }




}