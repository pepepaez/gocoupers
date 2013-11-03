package com.coupers.coupers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersDeal;
import com.coupers.entities.CoupersDealLevel;
import com.coupers.entities.CoupersLocation;
import com.coupers.utils.Contents;
import com.coupers.utils.CoupersObject;
import com.coupers.utils.CoupersServer;
import com.coupers.utils.QRCodeEncoder;
import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pepe on 8/10/13.
 */
//------------------
public class DealPagerAdapter extends PagerAdapter {

    private LayoutInflater mInflater;
    private ProgressDialog progressDialog=null;
    private final Activity a;
    private ImageButton saved_deal = null;
    private ImageButton shared_deal = null;
    private CoupersDeal deal_to_save = null;
    private CoupersApp app;
    private  int bg=R.drawable.list_selector_eat;

    private ArrayList<CoupersDeal> deals = new ArrayList<CoupersDeal>();

    public DealPagerAdapter(LayoutInflater inflater, Activity a,int bg){
        mInflater=inflater;
        this.bg = bg;
        this.a = a;
        this.app=(CoupersApp)a.getApplication();
        deals=app.getSelectedLocation().location_deals;

    }

    public void addDeal( CoupersDeal deal,String URL){
        deal.deal_URL = URL;
        deals.add(deal);
    }

    public void addDeal( CoupersDeal deal){
        deals.add(deal);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);

    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        //super.instantiateItem(container, position);
        final int pos = position;
        final CoupersDeal deal = deals.get(position);
        View layout = mInflater.inflate(R.layout.deal_pager_view, null);
        TextView level_deal_legend = (TextView) layout.findViewById(R.id.level_deal_legend);
        TextView level_deal_description = (TextView) layout.findViewById(R.id.level_deal_description);
        level_deal_legend.setBackgroundResource(bg);
        level_deal_description.setBackgroundResource(bg);
        if (deal.deal_levels.size()>0)
        {
            if (!deal.saved_deal)
            {
                level_deal_legend.setText(deal.deal_levels.get(0).level_deal_legend);
                level_deal_description.setText(deal.deal_levels.get(0).level_deal_description);
            }
            else
            {
                level_deal_legend.setText(deal.deal_levels.get(deal.current_level_id).level_deal_legend);
                level_deal_description.setText(deal.deal_levels.get(deal.current_level_id).level_deal_description);
            }
        }

        //---- Create redeem code
        ImageView redeem_code = (ImageView) layout.findViewById(R.id.level_redeem_code);
        TextView redeem_code_text = (TextView) layout.findViewById(R.id.level_redeem_code_text);

        WindowManager manager = (WindowManager) container.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        //smallerDimension = smallerDimension * 3/4;


        //QRCodeEncoder qrcodeDeal = new QRCodeEncoder(deals.get(position).deal_levels.get(0).level_redeem_code,null, Contents.Type.TEXT, BarcodeFormat.UPC_A.toString(),500); //smallerDimension); //QRCodeEncoder(,null, Contents.Type.TEXT, null,smallerDimension);
        QRCodeEncoder qrcodeDeal = new QRCodeEncoder("123456789012",null, Contents.Type.TEXT, BarcodeFormat.UPC_A.toString(),500); //smallerDimension); //QRCodeEncoder(,null, Contents.Type.TEXT, null,smallerDimension);
        Bitmap qrcode = null;
        try
        {
            qrcode = qrcodeDeal.encodeAsBitmap();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //TODO if error change dialog to say there was an error
        }
        if(redeem_code != null || qrcode == null ) redeem_code.setImageBitmap(qrcode);
        if (redeem_code_text != null) redeem_code_text.setText("1234567890");
        //if (redeem_code_text != null) redeem_code_text.setText(deals.get(position).deal_levels.get(0).level_redeem_code);

        //--- Get all deal levels view

        final ImageButton view_levels = (ImageButton) layout.findViewById(R.id.deal_level_indicator);
        int deal_resid = R.drawable.deal_level_1;
        if (deal.saved_deal)
        {
            switch (deal.current_level_id)
            {
                case 1:
                    deal_resid = R.drawable.deal_level_1;
                    break;
                case 2:
                    deal_resid = R.drawable.deal_level_2;
                    break;
                case 3:
                    deal_resid = R.drawable.deal_level_3;
                    break;
                case 4:
                    deal_resid = R.drawable.deal_level_4;
                    break;
                case 5:
                    deal_resid = R.drawable.deal_level_5;
                    break;
            }
        }
        view_levels.setImageResource(deal_resid);

        view_levels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getDealLevels(a,deal);
            }
        });

        //--- Create save button

        final ImageButton saveDeal = (ImageButton) layout.findViewById(R.id.save_deal);
        if (saveDeal!= null)
        {
            if (deals.get(position).saved_deal)
                saveDeal.setImageResource(R.drawable.deal_added);
            saveDeal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saved_deal = saveDeal;
                    deal_to_save = deals.get(position);
                    if (!deal_to_save.saved_deal)
                        saveLocationDeal(saveDeal.getContext(),deals.get(position));
                    else
                        unsaveLocationDeal(saveDeal.getContext(),deals.get(position));
                }
            });

        }


        //--- Create facebook share button
        final ImageButton shareFacebook = (ImageButton) layout.findViewById(R.id.facebook_share);
        if (shareFacebook!=null)
        {
            if (deals.get(position).fb_post_id!=null)
            {
                if (!deals.get(position).fb_post_id.isEmpty() || deals.get(position).fb_post_id.equals("0"))
                {
                    shareFacebook.setImageResource(R.drawable.share_facebook_sel);
                    shareFacebook.setClickable(false);
                }
            }
            if (shareFacebook.isClickable())
            {
                shareFacebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (a instanceof CardFlipActivity)
                            deal_to_save = deals.get(position);
                            saved_deal=saveDeal;
                            shared_deal=shareFacebook;
                            ((CardFlipActivity)a).postFacebook(deals.get(position),new CoupersData.Interfaces.CallBack() {
                                @Override
                                public void update(String result) {
                                    shared_deal.setImageResource(R.drawable.share_facebook_sel);
                                    saveLocationDeal(saved_deal.getContext(),deal_to_save);
                                }

                                @Override
                                public void update(int location_id) {

                                }

                                @Override
                                public void update(CoupersLocation location) {

                                }
                            });
                    }
                });
            }
            else
            {
                shareFacebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(a.getBaseContext(),a.getString(R.string.fb_deal_already_shared), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        //--- Create share code
        ImageButton shareButton = (ImageButton) layout.findViewById(R.id.qrcode_share);
        if (shareButton!=null)
        {
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    LayoutInflater inflater = mInflater;

                    //Find screen size
                    WindowManager manager = (WindowManager)  container.getContext().getSystemService(Context.WINDOW_SERVICE);
                    Display display = manager.getDefaultDisplay();
                    Point point = new Point();
                    display.getSize(point);
                    int width = point.x;
                    int height = point.y;
                    int smallerDimension = width < height ? width : height;
                    smallerDimension = smallerDimension * 3/4;

                    View sharedealview = inflater.inflate(R.layout.share_deal,null);
                    QRCodeEncoder qrcodeDeal = new QRCodeEncoder(deals.get(pos).deal_levels.get(0).level_share_code,null, Contents.Type.TEXT, null,smallerDimension);

                    //

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
            });
        }

        //-- Add layout to viewpager
        container.addView(layout);
        return layout;
    }

    public void saveLocationDeal(Context context,final CoupersDeal deal){

        if (progressDialog !=null)
            progressDialog = ProgressDialog.show(context, "", a.getResources().getString(R.string.progress_saving_deal), true);
        CoupersObject obj = new CoupersObject(CoupersData.Methods.SAVE_DEAL);
        obj.addParameter(CoupersData.Parameters.USER_ID,app.getUser_id());
        obj.addParameter(CoupersData.Parameters.DEAL_ID,String.valueOf(deal.deal_id));
        String _tag[]={
                CoupersData.Fields.RESULT_CODE};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {

                if (e==null)
                {
                    deal.saved_deal=true;
                    // TODO Set Saved Deal on SQLite
                    //TODO Set Remove Deal on SQLite and App
                    app.setSavedDeal(deal);

                    saved_deal.setImageResource(R.drawable.deal_added);
                }

                if (progressDialog!=null)
                {
                    progressDialog.dismiss();
                    progressDialog=null;
                }
            }
        });

        server.execute();

    }

    public void getDealLevels(Context context,final CoupersDeal deal){

        if (progressDialog ==null)
            progressDialog = ProgressDialog.show(context, "", a.getResources().getString(R.string.loading_deal_levels), true);
        CoupersObject obj = new CoupersObject(CoupersData.Methods.GET_DEAL_LEVELS);
        obj.addParameter(CoupersData.Parameters.DEAL_ID,String.valueOf(deal.deal_id));
        String _tag[]={
                CoupersData.Fields.LEVEL_ID,
                CoupersData.Fields.LEVEL_START_AT,
                CoupersData.Fields.LEVEL_SHARE_CODE,
                CoupersData.Fields.LEVEL_REDEEM_CODE,
                CoupersData.Fields.LEVEL_DEAL_LEGEND,
                CoupersData.Fields.LEVEL_DEAL_DESCRIPTION,
                CoupersData.Fields.LEVEL_AWARD_LIMIT};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {

                if (e==null)
                {
                    if (result.size()>0)
                    {
                        ArrayList<CoupersDealLevel> levels = new ArrayList<CoupersDealLevel>();
                        for (HashMap<String,String> map: result)
                        {
                            CoupersDealLevel level = new CoupersDealLevel(deal.deal_id,
                                    Integer.valueOf(map.get(CoupersData.Fields.LEVEL_ID)),
                                    Integer.valueOf(map.get(CoupersData.Fields.LEVEL_START_AT)),
                                    map.get(CoupersData.Fields.LEVEL_SHARE_CODE),
                                    map.get(CoupersData.Fields.LEVEL_REDEEM_CODE),
                                    map.get(CoupersData.Fields.LEVEL_DEAL_LEGEND),
                                    map.get(CoupersData.Fields.LEVEL_DEAL_DESCRIPTION));
                            levels.add(level);
                        }
                        //populate adapter and display dialog

                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                        View city_view =  a.getLayoutInflater().inflate(R.layout.deal_levels, null, false);
                        ListView lv = (ListView) city_view.findViewById(R.id.levels);
                        DealLevelAdapter adapter = new DealLevelAdapter(a,levels);

                        lv.setAdapter(adapter);
                        builder.setView(city_view);
                        builder.setNeutralButton("OK",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //load all locations from web server

                            }
                        });
                        builder.setTitle("Go Coupers!");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }

                if (progressDialog!=null)
                {
                    progressDialog.dismiss();
                    progressDialog=null;
                }
            }
        });

        server.execute();

    }



    public void unsaveLocationDeal(Context context,final CoupersDeal deal){

        progressDialog = ProgressDialog.show(context, "", a.getResources().getString(R.string.progress_saving_deal), true);
        CoupersObject obj = new CoupersObject(CoupersData.Methods.REMOVE_SAVED_DEAL);
        obj.addParameter(CoupersData.Parameters.USER_ID,app.getUser_id());
        obj.addParameter(CoupersData.Parameters.DEAL_ID,String.valueOf(deal.deal_id));
        String _tag[]={
                CoupersData.Fields.RESULT_CODE};
        obj.setTag(_tag);

        CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
            @Override
            public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {

                if (e==null)
                {
                    deal.saved_deal=false;

                    app.unsetSavedDeal(deal);

                    saved_deal.setImageResource(R.drawable.deal_not_added);
                }
                else
                {

                    //TODO notify user deal could not be removed from saved list

                }

                if (progressDialog!=null)
                {
                    progressDialog.dismiss();
                    progressDialog=null;
                }

            }
        });

        server.execute();

    }
    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view==o;
    }

    @Override
    public int getCount() {
        return deals.size();
    }
}

//-----------

