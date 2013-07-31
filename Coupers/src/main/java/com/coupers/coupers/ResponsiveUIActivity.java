package com.coupers.coupers;

import android.app.AlertDialog;
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
import com.coupers.utils.IntentIntegrator;
import com.coupers.utils.IntentResult;
import com.coupers.utils.XMLParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import android.view.Menu;

/**
 * This activity is an example of a responsive Android UI.
 * On phones, the SlidingMenu will be enabled only in portrait mode.
 * In landscape mode, it will present itself as a dual pane layout.
 * On tablets, it will will do the same general thing. In portrait
 * mode, it will enable the SlidingMenu, and in landscape mode, it
 * will be a dual pane layout.
 * 
 * @author jeremy
 *
 */
public class ResponsiveUIActivity extends SlidingFragmentActivity {

	private Fragment mContent;
    ArrayList<CoupersLocation> mData = new ArrayList<CoupersLocation>();
    private  boolean gps_available=false;
    ViewPager vp;

    //TODO review if all these node keys will be sufficient, can we (should we) use a class instead?
    static final String KEY_DEAL = "deal"; // parent node
    static final String KEY_ID = "id";
    static final String KEY_TYPE = "type";
    static final String KEY_DEAL_DESC = "desc";
    static final String KEY_LOCATION_NAME = "location";
    static final String KEY_LOCATION_LOGO = "logo";
    static final String KEY_DEAL_TIP = "tip";
    static final String KEY_THUMB_URL = "thumbnail";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.main_hub_ui);
        setContentView(R.layout.responsive_content_frame);

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

            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
            mData = (ArrayList<CoupersLocation>) savedInstanceState.getSerializable("data");
            gps_available = savedInstanceState.getBoolean("gps");
        }else {
            mData = (ArrayList<CoupersLocation>) getIntent().getSerializableExtra("data");
            gps_available = getIntent().getBooleanExtra("gps", false);

        }

		if (mContent == null)
            if (gps_available) mContent = new DealGridFragment(mData,true); //Create fragment with retrieved data

		// set the Behind View Fragment
        //TODO Change DealMenuFragment to accept mData to then pass it onto selected categories and locations
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new DealMenuFragment(mData))
		.commit();


        List<Fragment> fragments = new ArrayList<Fragment>();
        if (mContent != null) fragments.add(mContent);
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
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
        outState.putSerializable("data",mData);
        outState.putBoolean("gps",gps_available);

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


	public void switchContent(final Fragment fragment) {
		mContent = fragment;


        CustomPagerAdapter pageAdapter = (CustomPagerAdapter) vp.getAdapter();
        pageAdapter.clearALL();
        vp.setAdapter(null);

        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(mContent);
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

    public void onDealPressed(String dealID) {

        CoupersLocation obj = new CoupersLocation(
                1,
                1,
                "Muelle 240",
                "Camaron factory para comer rico con tus amigos y familiares",
                "www.muelle240.com",
                "http://www.marvinduran.com/pepe/images/logos/muelle.png",
                "http://www.marvinduran.com/pepe/images/lanegrita.png",
                "lazaro cardenas #1234",
                "mexicali",
                "423434234",
                "234134234",
                1123123,
                123123);
        CoupersDeal deal = new CoupersDeal(
                1,
                "20130712",
                "20130801");
        CoupersDealLevel deal_level = new CoupersDealLevel(
                1,
                0,
                "share me",
                "redeem me",
                "2x1",
                "Camarones para ti y tus cuates");
        CoupersDeal deal2 = new CoupersDeal(
                2,
                "20130712",
                "20130801");
        CoupersDealLevel deal_level2 = new CoupersDealLevel(
                1,
                10,
                "share me",
                "redeem me too",
                "3x1",
                "Camarones para ti y tus cuates y otros mas");

        deal.deal_levels.put(deal_level.level_id,deal_level);
        deal2.deal_levels.put(deal_level2.level_id,deal_level2);
        obj.location_deals.put(deal.deal_id, deal);
        obj.location_deals.put(deal2.deal_id, deal2);
        Intent intent = CardFlipActivity.newInstance(this,obj);

        startActivity(intent);
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
