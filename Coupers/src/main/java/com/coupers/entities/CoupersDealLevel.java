package com.coupers.entities;

import java.io.Serializable;

/**
 * Created by pepe on 7/27/13.
 */
public final class CoupersDealLevel implements Serializable{
    public int level_id;
    public int level_start_at;
    public String level_share_code;
    public String level_redeem_code;
    public String level_deal_legend;
    public String level_deal_description;

    public CoupersDealLevel(int level_id, int level_start_at, String level_share_code, String level_redeem_code, String level_deal_legend, String level_deal_description) {
        this.level_id = level_id;
        this.level_start_at = level_start_at;
        this.level_share_code = level_share_code;
        this.level_redeem_code = level_redeem_code;
        this.level_deal_legend = level_deal_legend;
        this.level_deal_description = level_deal_description;
    }
}
