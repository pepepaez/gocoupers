package com.coupers.coupers;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.coupers.utils.XMLParser;

import org.w3c.dom.NodeList;


//TODO add icons to menu options
//TODO add settings or my deals option to menu
public class DealMenuFragment extends ListFragment {

    private String mFilter = "food"; //TODO change hardcode to setting or last category used?
    private NodeList mNL = null;
    private XMLParser mParser = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

    public DealMenuFragment(String filter, NodeList nl, XMLParser parser){

        mFilter=filter;
        mNL= nl;
        mParser = parser;

    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] deals_menu = getResources().getStringArray(R.array.deals_menu);
		ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(getActivity(), 
				android.R.layout.simple_list_item_1, android.R.id.text1, deals_menu);
		setListAdapter(colorAdapter);
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
        TypedArray dealsmenu = getResources().obtainTypedArray(R.array.deals_menu_id);
        String filter = dealsmenu.getString(position);
		Fragment newContent = new DealGridFragment(filter, mNL,mParser);
		if (newContent != null)
			switchFragment(newContent);
	}
	
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
