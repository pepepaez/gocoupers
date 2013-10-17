package com.coupers.coupers;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.CirclePageIndicator;

/**
 * Created by pepe on 8/10/13.
 */
public class LocationFrontFragment extends DialogFragment {


    private CoupersLocation location;
    private View fragmentView;
    private LayoutInflater inflater;
    private CoupersApp app;
    private AQuery aq;

    public LocationFrontFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.location_card_front,null);
        this.inflater = inflater;


        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.app=(CoupersApp)getActivity().getApplication();
        this.location = app.getSelectedLocation();

        aq = new AQuery(fragmentView);
        int bgResource = R.drawable.list_selector_eat;

        if (savedInstanceState != null) {

        }

        if (location !=null)
        {
            switch(location.category_id)
            {
                case CoupersData.Fields.CATEGORY_ID_EAT:
                    bgResource = R.drawable.list_selector_eat;
                    break;
                case CoupersData.Fields.CATEGORY_ID_FEEL_GOOD:
                    bgResource= R.drawable.list_selector_feel_good;
                    break;
                case CoupersData.Fields.CATEGORY_ID_HAVE_FUN:
                    bgResource = R.drawable.list_selector_have_fun;
                    break;
                case CoupersData.Fields.CATEGORY_ID_LOOK_GOOD:
                    bgResource= R.drawable.list_selector_look_good;
                    break;
                case CoupersData.Fields.CATEGORY_ID_RELAX:
                    bgResource=R.drawable.list_selector_relax;
                    break;
            }
        }
        ImageView location_logo = (ImageView) fragmentView.findViewById(R.id.location_logo);
        location_logo.setBackgroundResource(bgResource);


        aq.id(R.id.location_logo).image(location.location_logo, true, true);

        ImageOptions options = new ImageOptions();
        options.fileCache=true;
        options.memCache=true;
        options.ratio=AQuery.RATIO_PRESERVE;
        options.anchor= AQuery.ANCHOR_DYNAMIC;
        //aq.id(R.id.list_image).progress(R.id.progressBarThumb).image(location.location_thumbnail,true,true);
        aq.id(R.id.location_thumbnail).image(location.location_thumbnail,true,true);
        ViewPager vp = (ViewPager) fragmentView.findViewById(R.id.deal_pager);
        DealPagerAdapter dealPager=new DealPagerAdapter(this.inflater,getActivity(), bgResource);

        vp.setAdapter(dealPager);

        CirclePageIndicator mIndicator = (CirclePageIndicator)fragmentView.findViewById(R.id.indicator);
        mIndicator.setViewPager(vp);

    }
}

