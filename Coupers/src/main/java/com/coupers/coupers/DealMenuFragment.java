package com.coupers.coupers;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.coupers.utils.XMLParser;

import org.w3c.dom.NodeList;


//TODO add icons to menu options
//TODO add settings or my deals option to menu
public class DealMenuFragment extends Fragment {

    private String mFilter = "food"; //TODO change hardcode to setting or last category used?
    private NodeList mNL = null;
    private ImageView last_selected = null;
    private XMLParser mParser = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        GridView lv = (GridView) container.findViewById(R.id.gridView);
        ImageButton settings = (ImageButton) container.findViewById(R.id.settings);
        settings.setImageResource(android.R.drawable.ic_menu_manage);
        container.removeView(container.findViewById(R.id.gridView));

        // Getting adapter by passing xml data ArrayList
        TypedArray deals_menu = getResources().obtainTypedArray(R.array.deals_menu);
        MenuAdapter adapter=new MenuAdapter(this.getActivity());
        for (int i=0; i<deals_menu.length();i++)
        {
            if(deals_menu.getString(i).toUpperCase().equals("MENU") || deals_menu.getString(i).toUpperCase().equals("FAVORITOS"))
                adapter.addHeader(deals_menu.getString(i));
            else
                adapter.addItem(deals_menu.getString(i));
        }

        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position,
                                    long id) {
                if (getActivity() == null)
                    return;

                ImageView option_selected = (ImageView) view.findViewById(R.id.selected_indicator);
                if (option_selected!=null){
                    if (last_selected != null) last_selected.setBackgroundColor(0);
                    option_selected.setBackgroundResource(R.drawable.list_selector_gym);
                    last_selected = option_selected;
                    TypedArray dealsmenu = getResources().obtainTypedArray(R.array.deals_menu_id);
                    String filter = dealsmenu.getString(position);

                    Fragment newContent = new DealGridFragment(filter, mNL, mParser);
                    if (newContent != null)
                        switchFragment(newContent);
                }

            }
        });

		return lv;
	}

    public DealMenuFragment(String filter, NodeList nl, XMLParser parser){

        mFilter=filter;
        mNL= nl;
        mParser = parser;

    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("filter", mFilter);
        //outState.putSerializable("lk",mNL);

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
