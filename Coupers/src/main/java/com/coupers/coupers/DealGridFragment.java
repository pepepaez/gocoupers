package com.coupers.coupers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.coupers.entities.CoupersLocation;


public class DealGridFragment extends Fragment {

    private boolean mShowNearby = false;
    private GridView gv;
    private CoupersApp app;

    public DealGridFragment(boolean nearby){
        mShowNearby = nearby;
    }

    public DealGridFragment(){

    }

    public boolean NearbyDeal(){
        return mShowNearby;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        gv = new GridView(getActivity());
        gv.setId(R.id.ultimate_grid);

		return gv;
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.app=(CoupersApp)getActivity().getApplication();

        if (savedInstanceState!=null)
            mShowNearby=savedInstanceState.getBoolean("mShowNearby");

        //GridView gv = (GridView) mContainer.findViewById(R.id.locations_grid);
        final DealAdapter adapter = new DealAdapter(this.getActivity());
        for (CoupersLocation location : app.locations) {
            if (location.Nearby==this.mShowNearby && location.show)
                adapter.addLocation(location);
        }

        if (gv!=null)
        {
            gv.setAdapter(adapter);

            gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    if (getActivity() == null)
                        return;

                    MainActivity activity = (MainActivity) getActivity();
                    app.selected_location=app.findLocation(adapter.getLocation(position).location_id);
                    activity.onLocationPressed();

                }
            });
        }
    }

    @Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        outState.putBoolean("mShowNearby",mShowNearby);

	}

}
