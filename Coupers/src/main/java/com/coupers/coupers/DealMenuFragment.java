package com.coupers.coupers;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.coupers.utils.XMLParser;

import org.w3c.dom.NodeList;


//TODO add icons to menu options
//TODO add settings or my deals option to menu
public class DealMenuFragment extends Fragment {

    private String mFilter = "food"; //TODO change hardcode to setting or last category used?
    private NodeList mNL = null;
    private XMLParser mParser = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View convertView = getActivity().getLayoutInflater().inflate(R.layout.grid_item, null);
        GridView lv = (GridView) inflater.inflate(R.layout.list_grid_menu, null);

        // Getting adapter by passing xml data ArrayList
        MenuAdapter adapter=new MenuAdapter(this.getActivity(), getResources().obtainTypedArray(R.array.deals_menu));
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position,
                                    long id) {
                if (getActivity() == null)
                    return;

                TextView option_selected = (TextView) view.findViewById(R.id.item_menu_text);
                TypedArray dealsmenu = getResources().obtainTypedArray(R.array.deals_menu_id);
                String filter = dealsmenu.getString(position);

                Fragment newContent = new DealGridFragment(filter, mNL, mParser);
                if (newContent != null)
                    switchFragment(newContent);


            }
        });
		return lv;
	}

    public DealMenuFragment(String filter, NodeList nl, XMLParser parser){

        mFilter=filter;
        mNL= nl;
        mParser = parser;

    }

    public DealMenuFragment(){
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);


/*		String[] deals_menu = getResources().getStringArray(R.array.deals_menu);
		ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_activated_1, android.R.id.text1, deals_menu);
		setListAdapter(colorAdapter);*/
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("filter", mFilter);
        //outState.putSerializable("lk",mNL);

    }
/*	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
        TypedArray dealsmenu = getResources().obtainTypedArray(R.array.deals_menu_id);
        String filter = dealsmenu.getString(position);
		Fragment newContent = new DealGridFragment(filter, mNL,mParser);
		if (newContent != null)
			switchFragment(newContent);
	}*/
	
	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null)
			return;

		if (getActivity() instanceof ResponsiveUIActivity) {
			ResponsiveUIActivity ra = (ResponsiveUIActivity) getActivity();
			ra.switchContent(fragment);
		}
	}


}
