package com.toneloc.olympicsapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by t on 7/1/16.
 */

//code from here: https://guides.codepath.com/android/Populating-a-ListView-with-a-CursorAdapter

public class ResultsListCursorAdapter extends CursorAdapter {

    public ArrayList<Country> mArrayListOfAllCountryObjects;

    public ResultsListCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_country, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.txt_country_name);
        TextView tvPrice = (TextView) view.findViewById(R.id.txt_price);
        TextView tvPredictedGolds = (TextView) view.findViewById(R.id.txt_predicted_golds);
        TextView tvPredictedSilvers = (TextView) view.findViewById(R.id.txt_predicted_silvers);
        TextView tvPredictedBronzes = (TextView) view.findViewById(R.id.txt_predicted_bronzes);
        ImageView ivFlag = (ImageView) view.findViewById(R.id.img_flag);

        // Extract properties from cursor
        String countryName = cursor.getString(cursor.getColumnIndexOrThrow("country"));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow("price"));

        //get id --- image has same id as country id
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        String alpha = "r" + Integer.toString(id);
        int idForImage = context.getResources().getIdentifier("com.toneloc.olympicsapp:drawable/" + alpha, null, null);

        //format number to local currency change locale to get locale . . . .
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedPrice = n.format(price);
        formattedPrice = formattedPrice.replaceAll("\\.00", "");

        //populate fields with extracted properties
        tvName.setText(countryName);
        tvPrice.setText(String.valueOf(formattedPrice));
        tvPredictedGolds.setText("0");
        tvPredictedSilvers.setText("0");
        tvPredictedBronzes.setText("0");
        ivFlag.setImageResource(idForImage);

    }

    public ArrayList<Country> generateArrayListOfAllCountryObjects(Cursor cursor) {

        mArrayListOfAllCountryObjects = new ArrayList<>();

        cursor.moveToFirst();
        //we can also create country objects here ... ?
        for (int i = 0; i < cursor.getCount(); i++) {
            //add dummy to handle
            // Extract properties from cursor
            String countryName = cursor.getString(cursor.getColumnIndexOrThrow("country"));
            int price = cursor.getInt(cursor.getColumnIndexOrThrow("price"));
            int id = cursor.getInt(cursor.getColumnIndex("_id"));

            Country countryToAdd = new Country(countryName,price,id,0,0,0,0);
            mArrayListOfAllCountryObjects.add(i,countryToAdd);
            cursor.moveToNext();
        }

        return mArrayListOfAllCountryObjects;
    }

}
