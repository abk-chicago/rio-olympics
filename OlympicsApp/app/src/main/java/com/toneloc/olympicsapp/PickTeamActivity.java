package com.toneloc.olympicsapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class PickTeamActivity extends AppCompatActivity {

    SQLiteDatabase db;
    DatabaseHelper myDbHelper;
    GridView mGridView;
    ArrayAdapter<String> mGridAdapter;
    ListView mListViewCountries;
    CountryListCursorAdapter mCountryListCursorAdapter;
    ArrayList<Country> mSelectedCountriesArrayList;
    ArrayList<String> mSelectedCountryNamesForGridDisplay;
    TextView mRemainingMoney;
    AlertDialog.Builder mAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRemainingMoney = (TextView) findViewById(R.id.budget);

        myDbHelper = new DatabaseHelper(this);

        try {
            myDbHelper.createDatabase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            myDbHelper.openDatabase();

        } catch (SQLException sqle) {
            throw sqle;
        }

        populateListView();
        setCountryListOnClick();
//        setOnLongClickForDetailView(); --- took this out as I want to have all info in listView
        buildAlertAndFab();
        mSelectedCountriesArrayList = new ArrayList<>();
        mSelectedCountryNamesForGridDisplay = new ArrayList<>();
    }

    public void populateListView() {
        // Get access to the underlying writeable database
        db = myDbHelper.getReadableDatabase();
        // Query for items from the database and get a cursor back
        Cursor countryListCursor = db.query("countries", // a. table
                null, // b. column names
                null, // c. selections
                null, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
        // Setup cursor adapter
        mCountryListCursorAdapter = new CountryListCursorAdapter(this, countryListCursor, 0);
        // Find the ListView to populate
        mListViewCountries = (ListView) findViewById(R.id.lvItems);

        //add a header row
        View header = getLayoutInflater().inflate(R.layout.header, null);
        mListViewCountries.addHeaderView(header, null, false);

        // Attach cursor adapter to the ListView
        //*****************************************************************
        mListViewCountries.setAdapter(mCountryListCursorAdapter);
        mCountryListCursorAdapter.generateArrayListOfAllCountryObjects(countryListCursor);
    }

    public void setCountryListOnClick() {
        AdapterView.OnItemClickListener listViewOnItemClick = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                int mSelectedItem = position;

                //adjust pointer by one because of header rwo
                mSelectedItem -= mListViewCountries.getHeaderViewsCount();

                mCountryListCursorAdapter.notifyDataSetChanged();
                addSelectedCountryToArray(mCountryListCursorAdapter.mArrayListOfAllCountryObjects.get(mSelectedItem));
            }
        };

        mListViewCountries.setOnItemClickListener(listViewOnItemClick);
    }


//    public void setOnLongClickForDetailView() {
//        AdapterView.OnItemLongClickListener listViewOnItemLongClick = new AdapterView.OnItemLongClickListener() {
//
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
//                int mSelectedItem = position;
//                //is this necessary?
//                mCountryListCursorAdapter.notifyDataSetChanged();
//
//                Country selectedCountry = mCountryListCursorAdapter.mArrayListOfAllCountryObjects.get(mSelectedItem);
//
//                //goToNewActivity
//                Intent mIntentToBeLame = new Intent(MainActivity.this, DetailActivity.class);
//                String message = "hey main, plz learn 2 be chill and not call the RA next time";
//                mIntentToBeLame.putExtra("MSG", message);
//
//                startActivity(mIntentToBeLame);
//                return true;
//            }
//
//        };
//
//        mListViewCountries.setOnItemLongClickListener(listViewOnItemLongClick);
//
//    }

    public ArrayList<Country> addSelectedCountryToArray(Country selectedCountry) {

        //check if adding country is still under 8 countries and under budget
        if (mSelectedCountriesArrayList.size() < 8) {
            if (isUnderSalaryCap(selectedCountry)) {
                if (!isADuplicateSelection(selectedCountry)) {
                    mSelectedCountriesArrayList.add(selectedCountry);
                    setGridView();
                    calculateRemainingMoney();

                } else {
                    Toast.makeText(PickTeamActivity.this, "You have already chosen this country.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PickTeamActivity.this, "You will exceed your salary cap.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PickTeamActivity.this, "You may only select up to 8 countries.", Toast.LENGTH_SHORT).show();
        }
        return mSelectedCountriesArrayList;
    }

    public void setGridView() {
        mGridView = (GridView) findViewById(R.id.grid_selected_countries);

        //The country to add to the grid view.
        int noToAdd = mSelectedCountriesArrayList.size() - 1;
        String name = mSelectedCountriesArrayList.get(noToAdd).getmName();
        mSelectedCountryNamesForGridDisplay.add(name);

        mGridAdapter = new ArrayAdapter<>(this, R.layout.grid_value, mSelectedCountryNamesForGridDisplay);
        mGridView.setAdapter(mGridAdapter);

        //add this method - press to delete
        setupListViewListenerToDelete();

    }

    public boolean isUnderSalaryCap(Country selectedCountry) {

        int spentMoney = (int) selectedCountry.getmPrice();
        for (int i = 0; i < mSelectedCountriesArrayList.size(); i++) {
            int thisCountryPrice = (int) mSelectedCountriesArrayList.get(i).getmPrice();
            spentMoney = thisCountryPrice + spentMoney;
        }

        if (spentMoney > 15000) {
            return false;
        } else {
            return true;
        }
    }

    public float calculateRemainingMoney() {
        int spentMoney = 0;

        for (int i = 0; i < mSelectedCountriesArrayList.size(); i++) {
            int thisCountryPrice = (int) mSelectedCountriesArrayList.get(i).getmPrice();
            spentMoney = thisCountryPrice + spentMoney;
        }
        int remainingMoney = 15000 - spentMoney;
        mRemainingMoney.setText("$" + Integer.toString(remainingMoney));
        return remainingMoney;
    }

    public boolean isADuplicateSelection(Country selectedCountry) {
        boolean isADuplicateSelection = false;
        for (int i = 0; i < mSelectedCountriesArrayList.size(); i++) {
            if (selectedCountry == mSelectedCountriesArrayList.get(i)) {
                isADuplicateSelection = true;
                break;
            } else {
                isADuplicateSelection = false;
            }
        }
        return isADuplicateSelection;
    }

    // Attaches a click listener to the listview to allow deletion with click.
    private void setupListViewListenerToDelete() {
        mGridView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter,
                                            View item, int pos, long id) {
                        //remove this from the arraylist of lists to display
                        mSelectedCountryNamesForGridDisplay.remove(pos);
                        //aaaannd remove it from the object too
                        mSelectedCountriesArrayList.remove(pos);
                        // Refresh the adapter
                        mGridAdapter.notifyDataSetChanged();
                        calculateRemainingMoney();
                    }
                }
        );
    }

    private void buildAlertAndFab() {
        mAlert = new AlertDialog.Builder(this);     // new alert dialog w/access to context
        mAlert.setView(R.layout.dialog_submit_team);    // set dialog view to xml file

        // Set a positive button. :)
        // And override the OnClickListener.
        mAlert.setPositiveButton(R.string.dialog_submit_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dialog popup = (Dialog) dialog;
            }
        });

        mAlert.setNegativeButton(R.string.dialog_submit_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.show(); // show our dialog

            }
        });

    }

}

//add a filter
//send a bundle of the array list of selected countries
//add flags in the list view
//create a new PickCountry.java class
//change Main activity to have a carousel view