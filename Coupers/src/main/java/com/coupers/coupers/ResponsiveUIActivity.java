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
import com.coupers.utils.IntentIntegrator;
import com.coupers.utils.IntentResult;
import com.coupers.utils.XMLParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
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
    NodeList nl;
    String xmlDeals;

    ViewPager vp;

    // All static variables
    // XML node keys
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
        // get deals
        Object mInstance = getLastCustomNonConfigurationInstance();

        if (mInstance !=null)
            xmlDeals=mInstance.toString();
        else
        {
            Bundle extras = getIntent().getExtras();
            xmlDeals = extras != null ? extras.getString("deals") : null;
        }

        XMLParser parser = new XMLParser();

        Document doc = parser.getDomElement(xmlDeals); // getting DOM element

        nl = doc.getElementsByTagName(KEY_DEAL);

		// check if the content frame contains the menu frame
		if (findViewById(R.id.menu_frame) == null) {
			setBehindContentView(R.layout.menu_frame);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
			// show home as up so we can toggle
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		} else {
			// add a dummy view
			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		// set the Above View Fragment
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		if (mContent == null)
			mContent = new DealGridFragment("food", nl, parser); //TODO Replace food to use last category used by user
		/*getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();*/

		// set the Behind View Fragment
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new DealMenuFragment("food", nl, parser))
		.commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);
        setSlidingActionBarEnabled(false);

        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(mContent);
        fragments.add(new DealGridFragment("cafe",nl,parser));
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
                //Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();

		}
		return super.onOptionsItemSelected(item);
	}

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
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
	}
    @Override
    public Object onRetainCustomNonConfigurationInstance(){
        super.onRetainCustomNonConfigurationInstance();
        return xmlDeals;
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

        //List<Fragment> fragments = new ArrayList<Fragment>();
        //fragments.add(fragment);
        //fragments.add(new DealGridFragment("cafe",nl,new XMLParser()));


        CustomPagerAdapter pageAdapter = (CustomPagerAdapter) vp.getAdapter();
        //vp.removeAllViews();
        //PagerTabStrip pagerTS = new PagerTabStrip(this);
        //vp.addView(pagerTS);
        pageAdapter.clearALL();
        vp.setAdapter(null);

        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(mContent);
        fragments.add(new DealGridFragment("cafe",nl,new XMLParser()));
        CustomPagerAdapter newPA = new CustomPagerAdapter(getSupportFragmentManager(),fragments);

        //pageAdapter.getItem(0).getFragmentManager().beginTransaction().replace(R.id.content_frame,fragment).commit();
        //pageAdapter.getItem(1).getFragmentManager().beginTransaction().replace(R.id.content_frame,new DealGridFragment("cafe",nl,new XMLParser())).commit();
        vp.setAdapter(newPA);
        //vp.getAdapter().notifyDataSetChanged();

        //CustomPagerAdapter paageAdapter = new CustomPagerAdapter(getSupportFragmentManager(),fragments);

        //vp.setAdapter(pageAdapter);
		/*getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, fragment)
		.commit();*/
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
	}

    public void onDealPressed(String dealID) {

        Intent intent = CardFlipActivity.newInstance(this, dealID);
        //this.getIntent().putExtra("deals",xmlDeals);
        startActivity(intent);
    }

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

            return "Viewing " + fragment.DealType() + " deals";
        }

        public void clearALL()
        {
            for (Fragment frag : fragments)
                fm.beginTransaction().remove(frag).commit();
            fragments.clear();
        }
    }




}
