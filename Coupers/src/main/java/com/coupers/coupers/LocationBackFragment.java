package com.coupers.coupers;

/**
 * Created by pepe on 9/22/13.
 */

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A fragment representing the back of the card.
 */
public class LocationBackFragment extends DialogFragment {
    private CoupersLocation location;

    final LatLng HAMBURG = new LatLng(53.558, 9.927);
    final LatLng KIEL = new LatLng(53.551, 9.993);
    private LatLng location_map;
    private GoogleMap map;
    private CoupersApp app;
    private View fragmentView;

    public LocationBackFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.location_card_back, container, false);
        return fragmentView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.app = (CoupersApp) getActivity().getApplication();
        this.location = app.selected_location;
        MapFragment map_fragment =((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        if (map_fragment!=null)
        {
            map= map_fragment.getMap();
            location_map = new LatLng(location.location_latitude,location.location_longitude);
            map.addMarker(new MarkerOptions()
                    .position(location_map)
                    .title(location.location_name)
                    .snippet(location.location_description)
            );

            // Move the camera instantly to hamburg with a zoom of 15.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location_map, 15));

            // Zoom in, animating the camera.
            //map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        }
        int bgResource=R.drawable.list_selector_eat;
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

        ImageView transparency = (ImageView) fragmentView.findViewById(R.id.transparency);
        TextView location_name = (TextView) fragmentView.findViewById(R.id.location_name);
        TextView location_city = (TextView) fragmentView.findViewById(R.id.location_city);
        TextView location_address = (TextView) fragmentView.findViewById(R.id.location_address);
        TextView location_website_url = (TextView) fragmentView.findViewById(R.id.location_website_url);
        TextView location_phone_number1 = (TextView) fragmentView.findViewById(R.id.location_phone_number1);
        TextView location_phone_number2 = (TextView) fragmentView.findViewById(R.id.location_phone_number2);
        TextView location_hours_operation1 = (TextView) fragmentView.findViewById(R.id.location_hours_operation1);

        if (transparency!=null) transparency.setBackgroundResource(bgResource);
        if (location_name!=null) location_name.setText(location.location_name);
        if (location_city!=null) location_city.setText(location.location_city);
        if (location_address!=null) location_address.setText(location.location_address);
        if (location_website_url!=null) location_website_url.setText(location.location_website_url);
        if (location_phone_number1!=null) location_phone_number1.setText(location.location_phone_number1);
        if (location_phone_number2!=null) location_phone_number2.setText(location.location_phone_number2);


    }
}
