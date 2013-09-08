package com.coupers.coupers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.coupers.entities.CoupersData;
import com.coupers.entities.CoupersDeal;
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
    private final Activity a;
    private  int bg=R.drawable.list_selector_eat;

    private ArrayList<CoupersDeal> aDeal = new ArrayList<CoupersDeal>();

    public DealPagerAdapter(LayoutInflater inflater, Activity a,int bg){
        mInflater=inflater;
        this.bg = bg;
        this.a = a;

    }

    public void addDeal( CoupersDeal deal,String URL){
        deal.deal_URL = URL;
        aDeal.add(deal);
    }

    public void addDeal( CoupersDeal deal){
        aDeal.add(deal);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);

    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        //super.instantiateItem(container, position);
        final int pos = position;
        View layout = mInflater.inflate(R.layout.deal_pager_view, null);
        TextView level_deal_legend = (TextView) layout.findViewById(R.id.level_deal_legend);
        TextView level_deal_description = (TextView) layout.findViewById(R.id.level_deal_description);
        level_deal_legend.setBackgroundResource(bg);
        level_deal_legend.setText(aDeal.get(position).deal_levels.get(0).level_deal_legend);
        level_deal_description.setBackgroundResource(bg);
        level_deal_description.setText(aDeal.get(position).deal_levels.get(0).level_deal_description);

        //---- Create redeem code
        ImageView redeem_code = (ImageView) layout.findViewById(R.id.level_redeem_code);
        TextView redee_code_text = (TextView) layout.findViewById(R.id.level_redeem_code_text);

        WindowManager manager = (WindowManager) container.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        //smallerDimension = smallerDimension * 3/4;


        QRCodeEncoder qrcodeDeal = new QRCodeEncoder(aDeal.get(position).deal_levels.get(0).level_redeem_code,null, Contents.Type.TEXT, BarcodeFormat.UPC_A.toString(),500); //smallerDimension); //QRCodeEncoder(,null, Contents.Type.TEXT, null,smallerDimension);

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
        if (redee_code_text != null) redee_code_text.setText(aDeal.get(position).deal_levels.get(0).level_redeem_code);

        //--- Create save button

        final ImageButton saveDeal = (ImageButton) layout.findViewById(R.id.save_deal);
        if (saveDeal!= null)
        {
            saveDeal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CoupersObject obj = new CoupersObject(CoupersData.Methods.SAVE_DEAL);
                    obj.addParameter(CoupersData.Parameters.USER_ID,((CoupersApp)a.getApplication()).getUser_id());
                    obj.addParameter(CoupersData.Parameters.DEAL_ID,String.valueOf(aDeal.get(position).deal_id));
                    String _tag[]={
                        CoupersData.Fields.COLUMN1};
                    obj.setTag(_tag);

                    CoupersServer server = new CoupersServer(obj,new CoupersServer.ResultCallback() {
                        @Override
                        public void Update(ArrayList<HashMap<String, String>> result, String method_name, Exception e) {
                            //TODO add code to check if all is OK
                            saveDeal.setImageResource(R.drawable.save_deal_sel);
                        }
                    });

                    server.execute("dummy string");
                }
            });

        }


        //--- Create facebook share button
        ImageButton shareFacebook = (ImageButton) layout.findViewById(R.id.facebook_share);
        if (shareFacebook!=null)
        {
            shareFacebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (a instanceof CardFlipActivity)
                        ((CardFlipActivity)a).postFacebook(aDeal.get(position));
                }
            });
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
                    QRCodeEncoder qrcodeDeal = new QRCodeEncoder(aDeal.get(pos).deal_levels.get(0).level_share_code,null, Contents.Type.TEXT, null,smallerDimension);

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

