package com.coupers.coupers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.coupers.entities.CoupersDeal;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.Contents;
import com.coupers.utils.QRCodeEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates a "card-flip" animation using custom fragment transactions ({@link
 * android.app.FragmentTransaction#setCustomAnimations(int, int)}).
 *
 * <p>This sample shows an "info" action bar button that shows the back of a "card", rotating the
 * front of the card out and the back of the card in. The reverse animation is played when the user
 * presses the system Back button or the "photo" action bar button.</p>
 */
public class CardFlipActivity extends Activity
        implements FragmentManager.OnBackStackChangedListener {
    /**
     * A handler object, used for deferring UI operations.
     */
    private Handler mHandler = new Handler();
    public Activity a;
    private CoupersLocation data;

    /**
     * Whether or not we're showing the back of the card (otherwise showing the front).
     */
    private boolean mShowingBack = false;

    public static Intent newInstance(Activity activity, String pos) {
        Intent intent = new Intent(activity, CardFlipActivity.class);
        intent.putExtra("dealID", pos);
        return intent;
    }

    public static Intent newInstance(Activity activity, CoupersLocation obj){
        Intent intent = new Intent(activity, CardFlipActivity.class );
        intent.putExtra("data",obj);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_flip);

        if (getIntent().getExtras() != null) {
            data = (CoupersLocation) getIntent().getExtras().getSerializable("data");
        }


        if (savedInstanceState == null) {
            // If there is no saved instance state, add a fragment representing the
            // front of the card to this activity. If there is saved instance state,
            // this fragment will have already been added to the activity.
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new LocationFrontFragment(data))
                    .commit();
        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }

        // Monitor back stack changes to ensure the action bar shows the appropriate
        // button (either "photo" or "info").
        getFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Add either a "photo" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.add(Menu.NONE, R.id.action_flip, Menu.NONE,
                mShowingBack
                        ? R.string.action_photo
                        : R.string.action_info);
        item.setIcon(mShowingBack
                ? R.drawable.ic_action_photo
                : R.drawable.ic_action_info);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                //NavUtils.navigateUpTo(this, this.getParentActivityIntent());
                //NavUtils.navigateUpFromSameTask(this);
                if (mShowingBack) flipCard();
                else finish();
                return true;

            case R.id.action_flip:
                flipCard();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void flipCard() {
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.

        mShowingBack = true;

        // Create and commit a new fragment transaction that adds the fragment for the back of
        // the card, uses custom animations, and is part of the fragment manager's back stack.
        int pos = 0;

        if (getIntent().getExtras() != null) {
            pos = getIntent().getExtras().getInt("pos");
        }
        String[] birdNames = getResources().getStringArray(R.array.birds);
        String birdName = birdNames[pos];
        String[] birdDescriptions = getResources().getStringArray(R.array.birds_desc);
        String birdDesc = birdDescriptions[pos];

        getFragmentManager()
                .beginTransaction()

                        // Replace the default fragment animations with animator resources representing
                        // rotations when switching to the back of the card, as well as animator
                        // resources representing rotations when flipping back to the front (e.g. when
                        // the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                        // Replace any fragments currently in the container view with a fragment
                        // representing the next page (indicated by the just-incremented currentPage
                        // variable).
                .replace(R.id.container, new CardBackFragment(birdName,birdDesc))

                        // Add this transaction to the back stack, allowing users to press Back
                        // to get to the front of the card.
                .addToBackStack(null)

                        // Commit the transaction.
                .commit();

        // Defer an invalidation of the options menu (on modern devices, the action bar). This
        // can't be done immediately because the transaction may not yet be committed. Commits
        // are asynchronous in that they are posted to the main thread's message loop.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);

        // When the back stack changes, invalidate the options menu (action bar).
        invalidateOptionsMenu();
    }

    public class LocationFrontFragment extends DialogFragment {

        private CoupersLocation data;
        private AQuery aq;

        public LocationFrontFragment(CoupersLocation data) {
            this.data = data;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View fragmentView = inflater.inflate(R.layout.location_card_front,null);
            aq = new AQuery(fragmentView);
            aq.id(R.id.location_logo).image(data.location_logo,true,true);
            aq.id(R.id.location_thumbnail).image(data.location_thumbnail,true,true);
            ViewPager vp = (ViewPager) fragmentView.findViewById(R.id.deal_pager);
            DealPagerAdapter dealPager=new DealPagerAdapter(getLayoutInflater());
            for (int i = 1; i <= data.location_deals.size(); i++) {
                dealPager.addDeal(data.location_deals.get(i));
            }
            vp.setAdapter(dealPager);

            return fragmentView;
        }
    }

    //------------------
    private class DealPagerAdapter extends PagerAdapter {
        private LayoutInflater mInflater;

        private ArrayList<CoupersDeal> aDeal = new ArrayList<CoupersDeal>();

        public DealPagerAdapter(LayoutInflater inflater){
            mInflater=inflater;
        }

        public void addDeal( CoupersDeal deal){
            aDeal.add(deal);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //super.instantiateItem(container, position);
            View layout = mInflater.inflate(R.layout.deal_pager_view, null);
            TextView level_deal_legend = (TextView) layout.findViewById(R.id.level_deal_legend);
            TextView level_deal_description = (TextView) layout.findViewById(R.id.level_deal_description);
            level_deal_legend.setText(aDeal.get(position).deal_levels.get(1).level_deal_legend);
            level_deal_description.setText(aDeal.get(position).deal_levels.get(1).level_deal_description);
            ((ViewPager) container).addView(layout);
            return layout;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view==o;
        }

        @Override
        public int getCount() {
            return aDeal.size();
        }
    }

    //-----------




    /**
     * A fragment representing the front of the card.
     */
    public  class CardFrontFragment extends DialogFragment {
        private int resImage= 0;
        //private final Context context = this.getActivity().getBaseContext();

        public CardFrontFragment(int res) {
            resImage = res;


        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(R.layout.location_card_front, null); //container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            /*ImageView myImage = (ImageView) findViewById(R.id.frontImage);
            if(myImage !=null)
                myImage.setImageResource(resImage);*/
            /*Button shareButton = (Button) findViewById(R.id.share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();

                    //Find screen size
                    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    Display display = manager.getDefaultDisplay();
                    Point point = new Point();
                    display.getSize(point);
                    int width = point.x;
                    int height = point.y;
                    int smallerDimension = width < height ? width : height;
                    smallerDimension = smallerDimension * 3/4;

                    View sharedealview = inflater.inflate(R.layout.share_deal,null);
                    QRCodeEncoder qrcodeDeal = new QRCodeEncoder("USER:112764576837312|DEAL_ID:817628397A213|DATE:20130706|TIME:1653|LOCATION_CITY:MEXICALI|LOCATION_STATE:BC|LOCATION_COUNTRY:MX",null, Contents.Type.TEXT, null,smallerDimension);

                    builder.setView(sharedealview);
                    ImageView dealqrcode = (ImageView) sharedealview.findViewById(R.id.deal_qrcode);
                    Bitmap qrcode = null;
                    try
                    {
                        qrcode = qrcodeDeal.encodeAsBitmap();
                    }
                    catch(Exception e)
                    {
                        //TODO if error change dialog to say there was an error
                    }
                    if(dealqrcode != null || qrcode == null ) dealqrcode.setImageBitmap(qrcode);
                    builder.setNeutralButton(R.string.share_deal_dialog_button,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.setTitle(R.string.share_deal_dialog_title);
                    builder.show();
                }
            });*/
        }
    }

    /**
     * A fragment representing the back of the card.
     */
    public class CardBackFragment extends DialogFragment {
        private String resName = "";
        private String resDescription = "";

        public CardBackFragment( String name, String desc) {
            resName = name;
            resDescription  = desc;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.location_card_back, container, false);
        }
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);


        }
    }
}