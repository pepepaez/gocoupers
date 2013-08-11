package com.coupers.coupers;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.coupers.entities.CoupersDeal;
import com.coupers.entities.CoupersLocation;
import com.coupers.entities.WebServiceDataFields;
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

import java.util.Arrays;
import java.util.List;

/**
 * Created by pepe on 8/10/13.
 */
public class LocationFrontFragment extends DialogFragment {

   /* // Activity code to flag an incoming activity result is due
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

    private boolean pendingAnnounce;*/

/*    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };*/


    private CoupersLocation data;
    private AQuery aq;
    private CoupersDeal deal;

    public LocationFrontFragment(CoupersLocation data) {
        this.data = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.location_card_front,null);
        aq = new AQuery(fragmentView);
        int bgResource = R.drawable.list_selector_eat;

        if (savedInstanceState != null) {
            //pendingAnnounce = savedInstanceState.getBoolean(FB_PENDING_ANNOUNCE_KEY, false);
        }

        if (data !=null)
        {
            switch(data.category_id)
            {
                case WebServiceDataFields.CATEGORY_ID_EAT:
                    bgResource = R.drawable.list_selector_eat;
                    break;
                case WebServiceDataFields.CATEGORY_ID_FEEL_GOOD:
                    bgResource= R.drawable.list_selector_feel_good;
                    break;
                case WebServiceDataFields.CATEGORY_ID_HAVE_FUN:
                    bgResource = R.drawable.list_selector_have_fun;
                    break;
                case WebServiceDataFields.CATEGORY_ID_LOOK_GOOD:
                    bgResource= R.drawable.list_selector_look_good;
                    break;
                case WebServiceDataFields.CATEGORY_ID_RELAX:
                    bgResource=R.drawable.list_selector_relax;
                    break;
            }
        }
        ImageView location_logo = (ImageView) fragmentView.findViewById(R.id.location_logo);
        location_logo.setBackgroundResource(bgResource);

        aq.id(R.id.location_logo).image(data.location_logo,true,true);
        aq.id(R.id.location_thumbnail).image(data.location_thumbnail,true,true);
        ViewPager vp = (ViewPager) fragmentView.findViewById(R.id.deal_pager);
        DealPagerAdapter dealPager=new DealPagerAdapter(inflater,getActivity(), bgResource);

        for (CoupersDeal deal : data.location_deals.values())
            dealPager.addDeal(deal,data.location_thumbnail);

        vp.setAdapter(dealPager);

        //uiHelper = new UiLifecycleHelper(getActivity(), callback);
        //uiHelper.onCreate(savedInstanceState);

        return fragmentView;
    }

/*
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

    public void postFacebook(CoupersDeal deal){
        this.deal = deal;
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
        progressDialog = ProgressDialog.show(getActivity(), "",
                getActivity().getResources().getString(R.string.progress_dialog_text), true);

        // Run this in a background thread since some of the populate methods may take
        // a non-trivial amount of time.
        AsyncTask<Void, Void, Response> task = new AsyncTask<Void, Void, Response>() {

            @Override
            protected Response doInBackground(Void... voids) {
                GetAction getAction = GraphObject.Factory.create(GetAction.class);
                //Set Place
                //getAction.setPlace();

                //Set Deal
                DealGraphObject og_deal =
                        GraphObject.Factory.create(DealGraphObject.class);
                String dealURL;
                dealURL = "http://marvinduran.com/pepe/data/og_objects/repeater.php?"
                        + "fb:app_id=" + getResources().getString(R.string.fb_app_id)
                        + "&og:type=gocoupers:deal"
                        + "&og:title=" + deal.deal_levels.get(1).level_deal_legend
                        + "&og:description=" + deal.deal_levels.get(1).level_deal_legend
                        + "&og:image=" + deal.deal_URL
                        + "&body=" + deal.deal_levels.get(1).level_deal_legend;
                og_deal.setUrl(dealURL);
                getAction.setDeal(og_deal);


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
        if (getActivity() == null) {
            // if the user removes the app from the website, then a request will
            // have caused the session to close (since the token is no longer valid),
            // which means the splash fragment will be shown rather than this one,
            // causing activity to be null. If the activity is null, then we cannot
            // show any dialogs, so we return.
            return;
        }

        PostResponse postResponse = response.getGraphObjectAs(PostResponse.class);

        if (postResponse != null && postResponse.getId() != null) {
            String dialogBody = String.format(getString(R.string.result_dialog_text), postResponse.getId());
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(R.string.result_dialog_button_text, null)
                    .setTitle(R.string.result_dialog_title)
                    .setMessage(dialogBody)
                    .show();
            //init(null);
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
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.error_dialog_button_text, listener)
                .setTitle(R.string.error_dialog_title)
                .setMessage(dialogBody)
                .show();
    }

    private void requestPublishPermissions(Session session) {
        if (session != null) {
            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this.getActivity(), FB_PERMISSIONS)
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
    }*/
}

