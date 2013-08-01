package com.coupers.coupers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.coupers.entities.CoupersLocation;
import com.coupers.utils.XMLParser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

public class DealGridFragment extends Fragment {

	private int mPos = -1;
    private int mFilter;
    private int catFilter   =0;
    private ArrayList<CoupersLocation> mData = new ArrayList<CoupersLocation>();
    private boolean mNearby = false;
    ArrayList<HashMap<String, String>> DealsList = new ArrayList<HashMap<String, String>>();

    public DealGridFragment(ArrayList<CoupersLocation> data,boolean nearby){
        mData=data;
        mNearby = nearby;
    }

    public DealGridFragment(){

    }

    public boolean NearbyDeal(){
        return mNearby;
    }


	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ArrayList<HashMap<String, String>> FilteredDealsList = new ArrayList<HashMap<String, String>>();

        //GridView gv = (GridView) inflater.inflate(R.layout.list_grid, null);
        final GridView gv = new GridView(getActivity()); //GridView) container.findViewById(R.id.gridView);
        //container.removeView(container.findViewById(R.id.gridView));

        DealAdapter adapter = new DealAdapter(this.getActivity());
        for (CoupersLocation location : mData) {
            if (location.Nearby==this.mNearby)
                adapter.addLocation(location);
            }

        gv.setAdapter(adapter);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (getActivity() == null)
                    return;

                int location_id = ((DealAdapter)gv.getAdapter()).getLocationId(position);
                //TextView dealID = (TextView) view.findViewById(R.id.deal_id);
                ResponsiveUIActivity activity = (ResponsiveUIActivity) getActivity();

                activity.onDealPressed(location_id);

            }
        });

		return gv;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

}
