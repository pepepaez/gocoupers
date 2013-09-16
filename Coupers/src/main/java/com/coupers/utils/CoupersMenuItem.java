package com.coupers.utils;

import com.coupers.coupers.R;
import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersLocation;

/**
 * Created by pepe on 9/10/13.
 */
public class CoupersMenuItem{
    public String item_text = "";
    public int item_icon = R.drawable.coupers_icon3;
    public int item_bg = R.drawable.list_selector_eat;
    public int category_id = -999;

    public int item_type;
    public final static int TYPE_CATEGORY = 0;
    public final static int TYPE_HEADER =1;
    public final static int TYPE_LOCATION = 2;

    private CoupersLocation location;

    public CoupersMenuItem(String text, int icon, int bg, int cat_id){
        this.item_text = text;
        this.item_icon = icon;
        this.item_bg = bg;
        this.category_id = cat_id;
        this.item_type=TYPE_CATEGORY;
    }

    public CoupersMenuItem(CoupersLocation location)
    {
        setLocation(location);
        this.item_type=TYPE_LOCATION;
        switch (location.category_id)
        {
            case CoupersData.Categories.EAT:
                this.item_bg = R.drawable.gradient_bg_eat;
                return;
            case CoupersData.Categories.HAVE_FUN:
                this.item_bg = R.drawable.gradient_bg_have_fun;
                return;
            case CoupersData.Categories.RELAX:
                this.item_bg = R.drawable.gradient_bg_relax;
                return;
            case CoupersData.Categories.FEEL_GOOD:
                this.item_bg = R.drawable.gradient_bg_feel_good;
                return;
            case CoupersData.Categories.LOOK_GOOD:
                this.item_bg = R.drawable.gradient_bg_look_good;
        }
    }

    public CoupersMenuItem(String text){
        this.item_text=text;
        this.item_type=TYPE_HEADER;
    }

    public void setLocation(CoupersLocation location){
        this.location=location;
    }

    public CoupersLocation getLocation(){
        return location;
    }
}
