package com.coupers.entities;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by pepe on 7/27/13.
 */
public class CoupersDeal implements Serializable{
    public int deal_id;
    public String deal_start_date;
    public String deal_end_date;
    public HashMap<Integer, CoupersDealLevel> deal_levels = new HashMap<Integer, CoupersDealLevel>();

    public CoupersDeal(int deal_id, String deal_start_date, String deal_end_date) {
        this.deal_id = deal_id;
        this.deal_start_date = deal_start_date;
        this.deal_end_date = deal_end_date;
    }
}
