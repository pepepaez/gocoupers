package com.coupers.coupers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coupers.entities.CoupersDeal;
import com.coupers.entities.CoupersLocation;
import com.coupers.entities.CoupersData;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.model.OpenGraphAction;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
     * FACEBOOK OPEN GRAPH VARIABLES
     */
    // Activity code to flag an incoming activity result is due
    // to a new permissions request
    private static final int FB_REAUTH_ACTIVITY_CODE = 100;
    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

    // Indicates an on-going reauthorization request
    private ProgressDialog progressDialog;

    // Key used in storing the pendingAnnounce flag
    private static final String FB_PENDING_ANNOUNCE_KEY = "pendingAnnounce";

    private static final String POST_ACTION_PATH = "me/gocoupers:get";

    /// List of additional write permissions being requested
    private static final List<String> FB_PERMISSIONS = Arrays.asList("publish_actions");

    private boolean pendingAnnounce;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    /**
     * A handler object, used for deferring UI operations.
     */
    private Handler mHandler = new Handler();
    public Activity a;
    //public boolean isFavorite = false;
    //private CoupersLocation data;
    private CoupersDeal deal;
    private CoupersApp app;
    private CoupersData.Interfaces.CallBack coupers_call_back=null;
    private Menu menu;

    /**
     * Whether or not we're showing the back of the card (otherwise showing the front).
     */
    private boolean mShowingBack = false;

    public static Intent newInstance(Activity activity){
        Intent intent = new Intent(activity, CardFlipActivity.class );
//        intent.putExtra("data",obj);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_flip);
        app = (CoupersApp) getApplication();


        if (savedInstanceState == null) {
            // If there is no saved instance state, add a fragment representing the
            // front of the card to this activity. If there is saved instance state,
            // this fragment will have already been added to the activity.
            LocationFrontFragment loc = new LocationFrontFragment();
            getFragmentManager().beginTransaction().add(R.id.container, loc).commit();

        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
            pendingAnnounce = savedInstanceState.getBoolean(FB_PENDING_ANNOUNCE_KEY, false);
        }

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        // Monitor back stack changes to ensure the action bar shows the appropriate
        // button (either "photo" or "info").
        getFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FB_PENDING_ANNOUNCE_KEY, pendingAnnounce);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==FB_REAUTH_ACTIVITY_CODE)
            uiHelper.onActivityResult(requestCode,resultCode,data);
    }

    public void postFacebook(CoupersDeal deal, CoupersData.Interfaces.CallBack listener){
        this.deal = deal;
        this.coupers_call_back=listener;
        handlePost();
    }

    public void handlePost(){
        pendingAnnounce = false;
        Session session = Session.getActiveSession();

        if (session == null || !session.isOpened()) {
            return;
        }

        List<String> permissions = session.getPermissions();
        if (!permissions.containsAll(FB_PERMISSIONS)) {
            pendingAnnounce = true;
            requestPublishPermissions(session);
            return;
        }

        // Show a progress dialog because sometimes the requests can take a while.
        progressDialog = ProgressDialog.show(this, "",
                getResources().getString(R.string.progress_dialog_text), true);

        // Run this in a background thread since some of the populate methods may take
        // a non-trivial amount of time.
        AsyncTask<Void, Void, Response> task = new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(Void... voids) {
                GetAction getAction = GraphObject.Factory.create(GetAction.class);

                //Set Deal
                DealGraphObject og_deal =
                        GraphObject.Factory.create(DealGraphObject.class);
                String dealURL;
                dealURL = "http://marvinduran.com/pepe/data/og_objects/repeater.php?"
                        + "fb:app_id=" + getResources().getString(R.string.fb_app_id)
                        + "&og:type=gocoupers:deal"
                        + "&og:title=" + deal.deal_levels.get(0).level_deal_legend
                        + "&og:description=" + deal.deal_levels.get(0).level_deal_description
                        + "&og:image=" + app.selected_location.location_thumbnail
                        + "&body=" + deal.deal_levels.get(0).level_deal_legend;
                og_deal.setUrl(dealURL);
                getAction.setDeal(og_deal);
                //getAction.setPlace();


                Request request = new Request(Session.getActiveSession(),
                        POST_ACTION_PATH, null, HttpMethod.POST);
                request.setGraphObject(getAction);
                return request.executeAndWait();
            }

            @Override
            protected void onPostExecute(Response response) {
                onPostActionResponse(response);
            }
        };

        task.execute();
    }

    private void onPostActionResponse(Response response) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (this == null) {
            // if the user removes the app from the website, then a request will
            // have caused the session to close (since the token is no longer valid),
            // which means the splash fragment will be shown rather than this one,
            // causing activity to be null. If the activity is null, then we cannot
            // show any dialogs, so we return.
            return;
        }

        PostResponse postResponse = response.getGraphObjectAs(PostResponse.class);

        if (postResponse != null && postResponse.getId() != null) {
            CoupersObject obj = new CoupersObject(CoupersData.Methods.SHARE_DEAL_FACEBOOK);
            obj.addParameter(CoupersData.Parameters.USER_ID,((CoupersApp)getApplication()).getUser_id());
            obj.addParameter(CoupersData.Parameters.DEAL_ID,String.valueOf(this.deal.deal_id));
            obj.addParameter(CoupersData.Parameters.FACEBOOK_POST_ID,postResponse.getId());
            String _tag[]={
                    CoupersData.Fields.COLUMN1};
            obj.setTag(_tag);

            CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
                @Override
                public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
                    //TODO add code to check if all is OK
                    if (progressDialog!=null)
                    {
                        progressDialog.dismiss();
                        progressDialog=null;
                    }
                    coupers_call_back.update("ok");
                }
            });

            server.execute();
        } else {
            handleError(response.getError());
        }
    }

    private void handleError(FacebookRequestError error) {
        DialogInterface.OnClickListener listener = null;
        String dialogBody = null;

        if (error == null) {
            // There was no response from the server.
            dialogBody = getString(R.string.error_dialog_default_text);
        } else {
            switch (error.getCategory()) {
                case AUTHENTICATION_RETRY:
                    // Tell the user what happened by getting the
                    // message id, and retry the operation later.
                    String userAction = (error.shouldNotifyUser()) ? "" :
                            getString(error.getUserActionMessageId());
                    dialogBody = getString(R.string.error_authentication_retry,
                            userAction);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                            int i) {
                            // Take the user to the mobile site.
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    M_FACEBOOK_URL);
                            startActivity(intent);
                        }
                    };
                    break;

                case AUTHENTICATION_REOPEN_SESSION:
                    // Close the session and reopen it.
                    dialogBody =
                            getString(R.string.error_authentication_reopen);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                            int i) {
                            Session session = Session.getActiveSession();
                            if (session != null && !session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        }
                    };
                    break;

                case PERMISSION:
                    // A permissions-related error
                    dialogBody = getString(R.string.error_permission);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                            int i) {
                            pendingAnnounce = true;
                            // Request publish permission
                            requestPublishPermissions(Session.getActiveSession());
                        }
                    };
                    break;

                case SERVER:
                case THROTTLING:
                    // This is usually temporary, don't clear the fields, and
                    // ask the user to try again.
                    dialogBody = getString(R.string.error_server);
                    break;

                case BAD_REQUEST:
                    // This is likely a coding error, ask the user to file a bug.
                    dialogBody = getString(R.string.error_bad_request,
                            error.getErrorMessage());
                    break;

                case OTHER:
                case CLIENT:
                default:
                    // An unknown issue occurred, this could be a code error, or
                    // a server side issue, log the issue, and either ask the
                    // user to retry, or file a bug.
                    dialogBody = getString(R.string.error_unknown,
                            error.getErrorMessage());
                    break;
            }
        }

        // Show the error and pass in the listener so action
        // can be taken, if necessary.
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.error_dialog_button_text, listener)
                .setTitle(R.string.error_dialog_title)
                .setMessage(dialogBody)
                .show();
    }

    private void requestPublishPermissions(Session session) {
        if (session != null) {
            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, FB_PERMISSIONS)
                    // demonstrate how to set an audience for the publish permissions,
                    // if none are set, this defaults to FRIENDS
                    .setDefaultAudience(SessionDefaultAudience.FRIENDS)
                    .setRequestCode(FB_REAUTH_ACTIVITY_CODE);
            session.requestNewPublishPermissions(newPermissionsRequest);
        }
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                tokenUpdated();
            } else {
                makeMeRequest(session);
            }
        }
    }

    private void tokenUpdated() {
        if (pendingAnnounce) {
            handlePost();
        }
    }

    private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        //profilePictureView.setProfileId(user.getId());
                        //userNameView.setText(user.getName());
                    }
                }
                if (response.getError() != null) {
                    //handleError(response.getError());
                }
            }
        });
        request.executeAsync();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Add either a "photo" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item_favorite = menu.add(Menu.NONE, R.id.add_location_favorite, Menu.NONE,R.string.add_location_favorite);
        item_favorite.setIcon(app.selected_location.location_isfavorite
                ? R.drawable.coupers_location_favorite
                : R.drawable.coupers_location_not_favorite);
        item_favorite.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem item_info = menu.add(Menu.NONE, R.id.action_flip, Menu.NONE,
                mShowingBack
                        ? R.string.action_photo
                        : R.string.action_info);
        item_info.setIcon(mShowingBack
                ? R.drawable.ic_action_photo
                : R.drawable.ic_action_info);
        item_info.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        this.menu = menu;
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
            case R.id.add_location_favorite:
                if (!app.selected_location.location_isfavorite){
                    progressDialog = ProgressDialog.show(this, "",getResources().getString(R.string.progress_adding_favorite), true);
                    AddLocationFavorite();
                }else
                {
                    progressDialog = ProgressDialog.show(this, "",getResources().getString(R.string.progress_removing_favorite), true);
                    RemoveLocationFavorite();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void AddLocationFavorite(){
        CoupersObject obj = new CoupersObject(CoupersData.Methods.ADD_LOCATION_FAVORITE);
        obj.addParameter(CoupersData.Parameters.LOCATION_ID,String.valueOf(app.selected_location.location_id));
        obj.addParameter(CoupersData.Parameters.USER_ID,((CoupersApp)getApplication()).getUser_id());
        String _tag[]={
                CoupersData.Fields.RESULT_CODE};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
                toggleFavorite(result,method_name);
            }
        });

        server.execute();
    }

    public void RemoveLocationFavorite(){
        CoupersObject obj = new CoupersObject(CoupersData.Methods.REMOVE_LOCATION_FAVORITE);
        obj.addParameter(CoupersData.Parameters.LOCATION_ID,String.valueOf(app.selected_location.location_id));
        obj.addParameter(CoupersData.Parameters.USER_ID,((CoupersApp)getApplication()).getUser_id());
        String _tag[]={
                CoupersData.Fields.RESULT_CODE};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
                toggleFavorite(result,method_name);
            }
        });

        server.execute();
    }

    public void toggleFavorite(ArrayList<HashMap<String, String>> aResult, String WebServiceExecuted)
    {

        if (WebServiceExecuted==CoupersData.Methods.ADD_LOCATION_FAVORITE || WebServiceExecuted==CoupersData.Methods.REMOVE_LOCATION_FAVORITE) {
            //((CoupersApp) getApplication()).RefreshFavorites();

            if (!app.selected_location.location_isfavorite)
            {
                app.setFavorite(app.selected_location);
                //((CoupersApp) getApplication()).addFavorite(data);
                this.menu.getItem(0).setIcon(R.drawable.coupers_location_favorite);
            }
            else
            {
                app.unsetFavorite(app.selected_location);
                //((CoupersApp) getApplication()).removeFavorite(data.location_id);
                this.menu.getItem(0).setIcon(R.drawable.coupers_location_not_favorite);
            }
            app.selected_location.location_isfavorite=!app.selected_location.location_isfavorite;


            if (progressDialog!=null){
                progressDialog.dismiss();
                progressDialog=null;
            }
        }

    }


    private void flipCard() {
        if (mShowingBack) {
            Fragment map = getFragmentManager().findFragmentById(R.id.map);
            if (map!=null)
                getFragmentManager().beginTransaction().remove(map).commit();
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
                .replace(R.id.container, new LocationBackFragment())

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

    private interface DealGraphObject extends GraphObject {
        // A URL
        public String getUrl();
        public void setUrl(String url);

        // An ID
        public String getId();
        public void setId(String id);
    }

    private interface GetAction extends OpenGraphAction {
        // The deal object
        public DealGraphObject getDeal();
        public void setDeal(DealGraphObject deal);
    }

    private interface PostResponse extends GraphObject {
        String getId();
    }

}