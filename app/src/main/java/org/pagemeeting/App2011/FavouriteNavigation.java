package org.pagemeeting.App2011;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;

public class FavouriteNavigation extends ListFragment {
    private static final String TAG = AbstractNavigationFragment.class.getSimpleName();
    private ArrayList<AbstractAdapter.AbstractItem> items;

    public FavouriteNavigation() {
        // Required empty public constructor
    }

    public static FavouriteNavigation newInstance() {
        FavouriteNavigation fragment = new FavouriteNavigation();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void LoadFavourites() {
        SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(getString(R.string.database_updating), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.database_updating), false))
            return;

        DatabaseOpenHelper databaseOpenHelper = new DatabaseOpenHelper(getActivity());
        SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();

        String whereClause = "";
        sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(getString(R.string.favourites), Context.MODE_PRIVATE);
        HashSet<String> favourites = (HashSet<String>) sharedPreferences.getStringSet(getString(R.string.favourites), new HashSet<String>());
        items = new ArrayList<AbstractAdapter.AbstractItem>();

        if (!favourites.isEmpty()) {
            for (String s : favourites) {
                if (whereClause != "")
                    whereClause += " OR ";
                whereClause += "_id='" + s + "'";
            }

            String query = "SELECT _id, Number, title, firstname, lastname, ETime FROM Abstracts WHERE " + whereClause + " ORDER BY Number COLLATE NOCASE";
            Cursor cursor = database.rawQuery(query, null);

            favourites.clear();

            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                favourites.add(cursor.getString(0));
                items.add(
                        new AbstractAdapter.AbstractItem(
                                cursor.getString(0),
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3) == null ? "" : cursor.getString(3) + " " + cursor.getString(4),
                                cursor.getString(5),
                                true
                        )
                );
                cursor.moveToNext();
            }
            cursor.close();
            database.close();

            sharedPreferences.edit().clear().putStringSet(getString(R.string.favourites), favourites).apply();
        }

        AbstractAdapter adapter = new AbstractAdapter(getActivity().getApplicationContext(), R.layout.abstract_item, items);
        getListView().setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_abstractnavigation, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        LoadFavourites();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            LoadFavourites();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.d(TAG, "Clicked list item: " + position);
        Intent i = new Intent(getActivity(), ViewAbstractActivity.class);
        i.putExtra(ViewAbstractActivity.ABSTRACT_ID, items.get(position).Id);
        startActivity(i);
    }
}
