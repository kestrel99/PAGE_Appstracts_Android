package org.pagemeeting.App2011;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.sqlite.*;

import java.util.ArrayList;

import static org.pagemeeting.App2011.GenericTextNavigationFragment.*;
import static org.pagemeeting.App2011.AbstractNavigationFragment.*;


public class MainNavigation extends Fragment
    implements OnNavigationSelectedListener, OnAbstractSelectedListener {

    private static final String TAG = ListFragment.class.getSimpleName();
    private static final String NAVIGATION_TYPE_MAIN = "main";

    private static final String NAVIGATION_PROGRAMME_ITEM = "programme-item";
    private static final String NAVIGATION_PROGRAMME_ITEM_CHOSEN = "programme-item-chosen";

    private static final String NAVIGATION_ABSTRACT_CATEGORY = "abstract-category";
    private static final String NAVIGATION_ABSTRACT_CATEGORY_CHOSEN = "abstract-category-chosen";

    private static final String NAVIGATION_PRESENTER = "presenter";
    private static final String NAVIGATION_PRESENTER_CHOSEN = "presenter-chosen";

    public MainNavigation() {
        // Required empty public constructor
    }

    public static MainNavigation newInstance() {
        MainNavigation fragment = new MainNavigation();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getChildFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        Log.d(TAG, "backstack changed!");
                        ((MainActivity) getActivity()).SetPageTitle();
                    }
                });

        if (savedInstanceState == null) {
            Fragment fragment = GenericTextNavigationFragment.newInstance(
                    NAVIGATION_TYPE_MAIN,
                    new NavigationItem[]{
                            new NavigationItem(NAVIGATION_PROGRAMME_ITEM, "Programme Item"),
                            new NavigationItem(NAVIGATION_ABSTRACT_CATEGORY, "Abstract Category"),
                            new NavigationItem(NAVIGATION_PRESENTER, "Presenter"),
                    }
            );
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.navigation_container, fragment).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_navigation, container, false);
        return view;
    }

    public void onAbstractSelected(String id) {
        Intent i = new Intent(getActivity(), ViewAbstractActivity.class);
        i.putExtra(ViewAbstractActivity.ABSTRACT_ID, id);
        startActivity(i);
    }

    public void onNavigationSelected(String navigationType, String id) {

        SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(getString(R.string.database_updating), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.database_updating), false))
            return;

        Log.d(TAG, "Navigation selected, type: " + navigationType + ", id: " + id);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        DatabaseOpenHelper databaseOpenHelper = new DatabaseOpenHelper(getActivity());
        SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();
        String title = new String();

        switch (navigationType) {
            case NAVIGATION_TYPE_MAIN: {
                String query = new String();
                String newNavigationType = new String();

                switch (id) {
                    case NAVIGATION_PROGRAMME_ITEM:
                        query = "SELECT distinct Grp FROM Abstracts ORDER BY Grp COLLATE NOCASE";
                        newNavigationType = NAVIGATION_PROGRAMME_ITEM_CHOSEN;
                        title = "Programme Item";
                        break;
                    case NAVIGATION_ABSTRACT_CATEGORY:
                        query = "SELECT distinct type FROM Abstracts WHERE type <> ' ' ORDER BY type COLLATE NOCASE";
                        newNavigationType = NAVIGATION_ABSTRACT_CATEGORY_CHOSEN;
                        title = "Abstract Category";
                        break;
                    case NAVIGATION_PRESENTER:
                        query = "SELECT fullname FROM Abstracts WHERE fullname <> ',' ORDER BY fullname COLLATE NOCASE";
                        newNavigationType = NAVIGATION_PRESENTER_CHOSEN;
                        title = "Presenter";
                        break;
                }

                Cursor cursor = database.rawQuery(query, null);

                NavigationItem items[] = new NavigationItem[cursor.getCount()];
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    items[i] = new NavigationItem(
                            cursor.getString(0),
                            cursor.getString(0));
                    cursor.moveToNext();
                }
                cursor.close();
                database.close();

                Fragment fragment = GenericTextNavigationFragment.newInstance(newNavigationType, items);
                transaction.replace(R.id.navigation_container, fragment);
                break;
            }
            case NAVIGATION_PROGRAMME_ITEM_CHOSEN:
            case NAVIGATION_ABSTRACT_CATEGORY_CHOSEN:
            case NAVIGATION_PRESENTER_CHOSEN: {

                String query = new String();
                switch (navigationType) {
                    case NAVIGATION_PROGRAMME_ITEM_CHOSEN:
                        query = "SELECT _id, Number, title, firstname, lastname, ETime, content FROM Abstracts WHERE Grp = '" + id + "' ORDER BY Number COLLATE NOCASE";
                        title = id;
                        break;
                    case NAVIGATION_ABSTRACT_CATEGORY_CHOSEN:
                        query = "SELECT _id, Number, title, firstname, lastname, ETime, content FROM Abstracts WHERE type = '" + id + "' ORDER BY Number COLLATE NOCASE";
                        title = id;
                        break;
                    case NAVIGATION_PRESENTER_CHOSEN:
                        query = "SELECT _id, Number, title, firstname, lastname, ETime, content FROM Abstracts WHERE fullname = '" + id + "' ORDER BY Number COLLATE NOCASE";
                        title = id;
                        break;
                }
                Cursor cursor = database.rawQuery(query, null);

                ArrayList<AbstractAdapter.AbstractItem> items = new ArrayList<>();
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    items.add(
                            new AbstractAdapter.AbstractItem(
                                    cursor.getString(0),
                                    cursor.getString(1),
                                    cursor.getString(2),
                                    cursor.getString(3) == null ? "" : cursor.getString(3) + " " + cursor.getString(4),
                                    cursor.getString(5),
                                    !cursor.isNull(6)
                            )
                    );
                    cursor.moveToNext();
                }
                cursor.close();
                database.close();

                Fragment fragment = AbstractNavigationFragment.newInstance(items);
                transaction.replace(R.id.navigation_container, fragment);
                break;
            }
        }

        transaction.addToBackStack(title);
        transaction.commit();
    }
}
