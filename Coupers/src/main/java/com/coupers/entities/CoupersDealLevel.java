package com.coupers.entities;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import static com.coupers.entities.CoupersData.SQLiteDictionary.*;

/**
 * Created by pepe on 7/27/13.
 */
public final class CoupersDealLevel implements Serializable{
    public int deal_id;
    public int level_id;
    public int level_start_at;
    public String level_share_code;
    public String level_redeem_code;
    public String level_deal_legend;
    public String level_deal_description;

    public CoupersDealLevel(int deal_id, int level_id, int level_start_at, String level_share_code, String level_redeem_code, String level_deal_legend, String level_deal_description) {
        this.deal_id = deal_id;
        this.level_id = level_id;
        this.level_start_at = level_start_at;
        this.level_share_code = level_share_code;
        this.level_redeem_code = level_redeem_code;
        this.level_deal_legend = level_deal_legend;
        this.level_deal_description = level_deal_description;
    }

    public CoupersDealLevel(Cursor cursor){
        this.deal_id = cursor.getInt(cursor.getColumnIndex(tb_DealLevel.deal_id));
        this.level_id = cursor.getInt(cursor.getColumnIndex(tb_DealLevel.level_id));
        this.level_start_at = cursor.getInt(cursor.getColumnIndex(tb_DealLevel.level_start_at));
        this.level_share_code = cursor.getString(cursor.getColumnIndex(tb_DealLevel.level_share_code));
        this.level_redeem_code = cursor.getString(cursor.getColumnIndex(tb_DealLevel.level_redeem_code));
        this.level_deal_legend = cursor.getString(cursor.getColumnIndex(tb_DealLevel.level_deal_legend));
        this.level_deal_description = cursor.getString(cursor.getColumnIndex(tb_DealLevel.level_deal_description));
    }

    public ContentValues getSQLiteValues(){
        ContentValues values = new ContentValues();

        values.put(tb_DealLevel.level_id,this.level_id);
        values.put(tb_DealLevel.deal_id,this.deal_id);
        values.put(tb_DealLevel.level_start_at, this.level_start_at);
        values.put(tb_DealLevel.level_share_code,this.level_share_code);
        values.put(tb_DealLevel.level_redeem_code,this.level_redeem_code);
        values.put(tb_DealLevel.level_deal_legend,this.level_deal_legend);
        values.put(tb_DealLevel.level_deal_description,this.level_deal_description);

        return values;
    }
}
