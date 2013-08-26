package com.coupers.coupers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.coupers.entities.CoupersDeal;
import com.coupers.entities.CoupersDealLevel;
import com.coupers.entities.CoupersLocation;
import com.coupers.entities.WebServiceDataFields;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.coupers.utils.IntentIntegrator;
import com.coupers.utils.IntentResult;
import com.coupers.utils.XMLParser;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends SlidingFragmentActivity {

    ArrayList<CoupersLocation> mData = new ArrayList<CoupersLocation>();
    private CoupersLocation selected_location = null;
    private  boolean gps_available=false;
    private boolean nearby_locations=false;
    public ProgressDialog progressDialog;
    private CoupersApp app = null;
    ViewPager vp;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.main_hub_ui);
        setContentView(R.layout.responsive_content_frame);
        app = (CoupersApp) getApplication();

        //TODO Need to make use of saved instance!!


        // check if the content frame contains the menu frame
		if (findViewById(R.id.menu_frame) == null) {
			setBehindContentView(R.layout.menu_frame_new);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			// show home as up so we can toggle
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		} else {
			// add a dummy view
			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);
        setSlidingActionBarEnabled(true);

		// set the Above View Fragment & get saved instance data
		if (savedInstanceState != null){
            mData = (ArrayList<CoupersLocation>) savedInstanceState.getSerializable("data");
            gps_available = savedInstanceState.getBoolean("gps");
            nearby_locations=savedInstanceState.getBoolean("nearby");
        }else {
            mData = (ArrayList<CoupersLocation>) getIntent().getSerializableExtra("data");
            gps_available = getIntent().getBooleanExtra("gps", false);
            nearby_locations = getIntent().getBooleanExtra("nearby",false);

        }


		// set the Behind View Fragment
        //TODO Change DealMenuFragment to accept mData to then pass it onto selected categories and locations
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new DealMenuFragment(app))
		.commit();


        List<Fragment> fragments = new ArrayList<Fragment>();
        if (gps_available && nearby_locations) fragments.add(new DealGridFragment(mData,true));
        fragments.add(new DealGridFragment(mData,false));
        CustomPagerAdapter pageAdapter = new CustomPagerAdapter(getSupportFragmentManager(),fragments);
        vp = (ViewPager) findViewById(R.id.pager);
        vp.setAdapter(pageAdapter);
	}



    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
            case R.id.deal_scan:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();

		}
		return super.onOptionsItemSelected(item);
	}

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null && scanResult.getContents() != null) {
            AlertDialog.Builder downloadDialog = new AlertDialog.Builder(this);
            downloadDialog.setTitle("Oh, see what you found!");
            downloadDialog.setMessage(scanResult.getContents().toString());
            downloadDialog.setNeutralButton("Got it!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            downloadDialog.show();
            //Toast.makeText(this.getBaseContext(),scanResult.toString(),Toast.LENGTH_LONG).show();
        }
        // else continue with any other code you need in the method
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//getSupportFragmentManager().putFragment(outState, "mContent", mContent);
        outState.putSerializable("data",mData);
        outState.putBoolean("gps",gps_available);
        outState.putBoolean("nearby",nearby_locations);

	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Add either a "photo" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.add(Menu.NONE, R.id.deal_scan, Menu.NONE,R.string.action_scan);
        item.setIcon(R.drawable.ic_action_barcode);
        item.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }


	public void switchContent(ArrayList<CoupersLocation> mData, boolean gps_available, boolean nearby_locations) {

        CustomPagerAdapter pageAdapter = (CustomPagerAdapter) vp.getAdapter();
        pageAdapter.clearALL();
        vp.setAdapter(null);

        List<Fragment> fragments = new ArrayList<Fragment>();
        if (gps_available && nearby_locations) fragments.add(new DealGridFragment(mData,true));
        fragments.add(new DealGridFragment(mData,false));
        CustomPagerAdapter newPA = new CustomPagerAdapter(getSupportFragmentManager(),fragments);

        vp.setAdapter(newPA);

		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
	}

    public void onDealPressed(int location_id) {

        progressDialog = ProgressDialog.show(this, "",
                getResources().getString(R.string.progress_loading_deals), true);

        CoupersObject obj = new CoupersObject("http://tempuri.org/GetLocationDeals",
                "http://coupers.elasticbeanstalk.com/CoupersWS/Coupers.asmx",
                "GetLocationDeals");
        obj.addParameter("location_id",String.valueOf(location_id));
        String _tag[]={
                WebServiceDataFields.DEAL_ID,
                WebServiceDataFields.LOCATION_ID,
                WebServiceDataFields.DEAL_START_DATE,
                WebServiceDataFields.DEAL_END_DATE,
                WebServiceDataFields.DEAL_DAY_SPECIAL,
                WebServiceDataFields.LEVEL_ID,
                WebServiceDataFields.LEVEL_START_AT,
                WebServiceDataFields.LEVEL_SHARE_CODE,
                WebServiceDataFields.LEVEL_REDEEM_CODE,
                WebServiceDataFields.LEVEL_DEAL_LEGEND,
                WebServiceDataFields.LEVEL_DEAL_DESCRIPTION
        };
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,this);

        for (CoupersLocation location:mData)
        if (location.location_id == location_id) selected_location = location;

        server.execute("dummy string");

    }

    public void Update(ArrayList<HashMap<String, String>> aResult, String WebServiceExecuted){

        for (HashMap<String,String> map: aResult) {
            CoupersDeal deal = new CoupersDeal(Integer.valueOf(map.get(WebServiceDataFields.DEAL_ID)), map.get(WebServiceDataFields.DEAL_START_DATE), map.get(WebServiceDataFields.DEAL_END_DATE));
            CoupersDealLevel level = new CoupersDealLevel(
                    Integer.valueOf(map.get(WebServiceDataFields.LEVEL_ID)),
                    Integer.valueOf(map.get(WebServiceDataFields.LEVEL_START_AT)),
                    map.get(WebServiceDataFields.LEVEL_SHARE_CODE),
                    map.get(WebServiceDataFields.LEVEL_REDEEM_CODE),
                    map.get(WebServiceDataFields.LEVEL_DEAL_LEGEND),
                    map.get(WebServiceDataFields.LEVEL_DEAL_DESCRIPTION));
            deal.deal_levels.put(level.level_id,level);
            selected_location.location_deals.put(deal.deal_id, deal);
        }

        Intent intent = CardFlipActivity.newInstance(this,selected_location);

        startActivity(intent);
        if (progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    // Adapter used to display
    private class CustomPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments;
        private FragmentManager fm;

        public CustomPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fm = fm;
            this.fragments = fragments;
        }
        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            DealGridFragment fragment = (DealGridFragment) this.fragments.get(position);

            if (fragment==null)
                return "ALL DEALS";
            else
                return fragment.NearbyDeal() ? "NEARBY DEALS" : "ALL DEALS";
        }

        public void clearALL()
        {
            for (Fragment frag : fragments)
                fm.beginTransaction().remove(frag).commit();
            fragments.clear();
        }
    }
}