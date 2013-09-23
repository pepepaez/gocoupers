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
import com.coupers.entities.CoupersData;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.coupers.utils.IntentIntegrator;
import com.coupers.utils.IntentResult;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends SlidingFragmentActivity {

    private CoupersLocation selected_location = null;
    public ProgressDialog progressDialog;
    private CoupersApp app = null;
    ViewPager vp;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.main_hub_ui);
        setContentView(R.layout.responsive_content_frames);
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


		// set the Behind View Fragment
        //TODO Change DealMenuFragment to accept mData to then pass it onto selected categories and locations
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new DealMenuFragment())
		.commit();


        List<Fragment> fragments = new ArrayList<Fragment>();
        if (app.gps_available && app.nearby_locations) fragments.add(new DealGridFragment(true));
        fragments.add(new DealGridFragment(false));
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
                return true;
            case R.id.open_settings:
                return true;

		}
		return super.onOptionsItemSelected(item);
	}

    //region Activity Control
    @Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem scan_option = menu.add(Menu.NONE, R.id.deal_scan, Menu.NONE,R.string.action_scan);
        scan_option.setIcon(R.drawable.coupers_iconos_scan);
        scan_option.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem settings_option = menu.add(Menu.NONE, R.id.open_settings, Menu.NONE,R.string.open_settings);
        settings_option.setIcon(R.drawable.coupers_iconos_settings);
        settings_option.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }
    //endregion

	public void switchContent() {

        CustomPagerAdapter pageAdapter = (CustomPagerAdapter) vp.getAdapter();
        pageAdapter.clearALL();
        vp.setAdapter(null);

        List<Fragment> fragments = new ArrayList<Fragment>();
        if (app.gps_available && app.nearby_locations) fragments.add(new DealGridFragment(true));
        fragments.add(new DealGridFragment(false));
        CustomPagerAdapter newPA = new CustomPagerAdapter(getSupportFragmentManager(),fragments);

        vp.setAdapter(newPA);

		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
	}


    public void onLocationPressed() {

        progressDialog = ProgressDialog.show(this, "",
                getResources().getString(R.string.progress_loading_deals), true);

        if (app.selected_location.location_deals.size()==0)
            loadDealsWS(app.selected_location);
        else
            showLocationDeals();


    }

    public void loadDealsWS(final CoupersLocation location){
        CoupersObject obj = new CoupersObject(CoupersData.Methods.GET_LOCATION_DEALS);
        obj.addParameter(CoupersData.Parameters.LOCATION_ID,String.valueOf(location.location_id));
        String _tag[]={
                CoupersData.Fields.DEAL_ID,
                CoupersData.Fields.LOCATION_ID,
                CoupersData.Fields.DEAL_START_DATE,
                CoupersData.Fields.DEAL_END_DATE,
                CoupersData.Fields.DEAL_DAY_SPECIAL,
                CoupersData.Fields.LEVEL_ID,
                CoupersData.Fields.LEVEL_START_AT,
                CoupersData.Fields.LEVEL_SHARE_CODE,
                CoupersData.Fields.LEVEL_REDEEM_CODE,
                CoupersData.Fields.LEVEL_DEAL_LEGEND,
                CoupersData.Fields.LEVEL_DEAL_DESCRIPTION
        };
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
                location.location_deals=parseDeals(result);
                showLocationDeals();
            }
        });

        server.execute();
    }

    public ArrayList<CoupersDeal> parseDeals(ArrayList<HashMap<String, String>> aResult) {

        ArrayList<CoupersDeal> deals = new ArrayList<CoupersDeal>();

        for (HashMap<String, String> map : aResult) {
            CoupersDeal deal = new CoupersDeal(Integer.valueOf(map.get(CoupersData.Fields.LOCATION_ID)), Integer.valueOf(map.get(CoupersData.Fields.DEAL_ID)), map.get(CoupersData.Fields.DEAL_START_DATE), map.get(CoupersData.Fields.DEAL_END_DATE));
            CoupersDealLevel level = new CoupersDealLevel(
                    Integer.valueOf(map.get(CoupersData.Fields.DEAL_ID)),
                    Integer.valueOf(map.get(CoupersData.Fields.LEVEL_ID)),
                    Integer.valueOf(map.get(CoupersData.Fields.LEVEL_START_AT)),
                    map.get(CoupersData.Fields.LEVEL_SHARE_CODE),
                    map.get(CoupersData.Fields.LEVEL_REDEEM_CODE),
                    map.get(CoupersData.Fields.LEVEL_DEAL_LEGEND),
                    map.get(CoupersData.Fields.LEVEL_DEAL_DESCRIPTION));
            deal.deal_levels.add(level);
            if (!app.db.exists(deal))
                app.db.addDeal(deal);
            deals.add(deal);
        }

        return deals;
    }

    public void showLocationDeals(){
        Intent intent = CardFlipActivity.newInstance(this);

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