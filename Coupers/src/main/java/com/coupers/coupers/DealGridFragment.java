package com.coupers.coupers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.coupers.utils.XMLParser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

public class DealGridFragment extends Fragment {

	private int mPos = -1;
    private String mFilter = "food";
    private int catFilter   =0;
    private NodeList mNL = null;
    private XMLParser mParser = null;
	private int mImgRes;
    ArrayList<HashMap<String, String>> DealsList = new ArrayList<HashMap<String, String>>();
	
	public DealGridFragment() { }
	public DealGridFragment(int pos) {
		mPos = pos;
	}

    public DealGridFragment(String filter, NodeList nl, XMLParser parser){

        mFilter=filter;
        mNL= nl;
        mParser = parser;

    }

    public DealGridFragment(int filter, ArrayList<HashMap<String, String>> deals){
        catFilter = filter;
        mParser = new XMLParser();
        DealsList = deals;
    }

    public String DealType(){
        return mFilter;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ArrayList<HashMap<String, String>> FilteredDealsList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();
        GridView gv = (GridView) inflater.inflate(R.layout.list_grid, null);
        DealAdapter adapter;


        if(mNL != null)
        {
            // looping through all deal nodes
            for (int i = 0; i <  mNL.getLength(); i++) {
                // creating new HashMap

                Element e = (Element) mNL.item(i);
                // adding each child node to HashMap key => value
                if (mParser.getValue(e, ResponsiveUIActivity.KEY_TYPE).toLowerCase().equals(mFilter)){
                    map.put(ResponsiveUIActivity.KEY_ID , mParser.getValue(e, ResponsiveUIActivity.KEY_ID));
                    map.put(ResponsiveUIActivity.KEY_TYPE, mParser.getValue(e, ResponsiveUIActivity.KEY_TYPE));
                    map.put(ResponsiveUIActivity.KEY_DEAL_DESC, mParser.getValue(e, ResponsiveUIActivity.KEY_DEAL_DESC));
                    map.put(ResponsiveUIActivity.KEY_LOCATION_ID, mParser.getValue(e, ResponsiveUIActivity.KEY_LOCATION_ID));
                    map.put(ResponsiveUIActivity.KEY_LOCATION_LOGO, mParser.getValue(e, ResponsiveUIActivity.KEY_LOCATION_LOGO));
                    map.put(ResponsiveUIActivity.KEY_DEAL_TIP, mParser.getValue(e, ResponsiveUIActivity.KEY_DEAL_TIP));
                    map.put(ResponsiveUIActivity.KEY_THUMB_URL, mParser.getValue(e, ResponsiveUIActivity.KEY_THUMB_URL));

                    // adding HashList to ArrayList
                    DealsList.add(map);
                }
            }
            adapter = new DealAdapter(this.getActivity(), DealsList);
        }
        else
        {
            for (int j =0;j< DealsList.size();j++)
            {

                if (Integer.parseInt( DealsList.get(j).get(ResponsiveUIActivity.KEY_TYPE)) == catFilter)
                {
                    map.put(ResponsiveUIActivity.KEY_ID, DealsList.get(j).get(ResponsiveUIActivity.KEY_ID).toString());
                    map.put(ResponsiveUIActivity.KEY_TYPE,DealsList.get(j).get(ResponsiveUIActivity.KEY_TYPE).toString());
                    map.put(ResponsiveUIActivity.KEY_DEAL_DESC,DealsList.get(j).get(ResponsiveUIActivity.KEY_DEAL_DESC).toString());
                    map.put(ResponsiveUIActivity.KEY_LOCATION_ID,DealsList.get(j).get(ResponsiveUIActivity.KEY_LOCATION_ID).toString());
                    map.put(ResponsiveUIActivity.KEY_LOCATION_LOGO,DealsList.get(j).get(ResponsiveUIActivity.KEY_LOCATION_LOGO).toString());
                    map.put(ResponsiveUIActivity.KEY_DEAL_TIP,DealsList.get(j).get(ResponsiveUIActivity.KEY_DEAL_TIP).toString());
                    map.put(ResponsiveUIActivity.KEY_THUMB_URL,DealsList.get(j).get(ResponsiveUIActivity.KEY_THUMB_URL).toString());

                    FilteredDealsList.add(map);

                }

            }
            adapter = new DealAdapter(this.getActivity(), FilteredDealsList);
        }


        gv.setAdapter(adapter);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if (getActivity() == null)
                    return;
                TextView dealID = (TextView) view.findViewById(R.id.deal_id);
                ResponsiveUIActivity activity = (ResponsiveUIActivity) getActivity();

                activity.onDealPressed(dealID.getText().toString());

            }
        });


        /*if (mPos == -1 && savedInstanceState != null)
			mPos = savedInstanceState.getInt("mPos");
		TypedArray imgs = getResources().obtainTypedArray(R.array.birds_img);
		mImgRes = imgs.getResourceId(mPos, -1);
		
		GridView gv = (GridView) inflater.inflate(R.layout.list_grid, null);
		gv.setBackgroundResource(android.R.color.black);
		gv.setAdapter(new GridAdapter());
		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				if (getActivity() == null)
					return;
				ResponsiveUIActivity activity = (ResponsiveUIActivity) getActivity();
				activity.onDealPressed(mPos);
			}			
		});*/
		return gv;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mPos", mPos);

	}

 /*   @Override
    public Object onRetainCustomNonConfigurationInstance(){
        super.onRetainCustomNonConfigurationInstance();

        //TODO figure out how to save mNL
        return "1";
    }*/


}
