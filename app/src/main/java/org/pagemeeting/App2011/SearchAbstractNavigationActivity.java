package org.pagemeeting.App2011;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class SearchAbstractNavigationActivity extends ListActivity {
    private static final String TAG = SearchAbstractNavigationActivity.class.getSimpleName();
    private ArrayList<AbstractAdapter.AbstractItem> items;

    public SearchAbstractNavigationActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_search_abstractnavigation);

        EditText searchText = (EditText) findViewById(R.id.searchText);
        searchText.requestFocus();

        searchText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SearchAbstracts(s.toString());
            }
        });

        ListView listView = (ListView)findViewById(android.R.id.list);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        SearchAbstracts("");
    }

    SearchTask currentSearchTask;
    public void SearchAbstracts(String search) {

        setListAdapter(new AbstractAdapter(getApplicationContext(), R.layout.abstract_item, new ArrayList<AbstractAdapter.AbstractItem>()));

        if (currentSearchTask != null)
            currentSearchTask.cancel(true);

        if (!search.isEmpty()) {
            String[] searches = search.split(" ");
            String[] finalSearches = new String[searches.length + 1];
            finalSearches[0] = search;
            for (int i = 0; i < searches.length; i++)
                finalSearches[i + 1] = searches[i];

            currentSearchTask = new SearchTask();
            currentSearchTask.execute(finalSearches);
        }else{
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_abstractnavigation, container, false);
        return view;
    }*/

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Log.d(TAG, "Clicked list item: " + position);
        Intent i = new Intent(this, ViewAbstractActivity.class);
        i.putExtra(ViewAbstractActivity.ABSTRACT_ID, items.get(position).Id);
        startActivity(i);
    }

    // usually, subclasses of AsyncTask are declared inside the activity class.
    // that way, you can easily modify the UI thread from here
    private class SearchTask extends AsyncTask<String, Integer, AbstractAdapter> {
        private final String TAG = SearchTask.class.getSimpleName();

        ArrayList<AbstractAdapter.AbstractItem> localItems;
        public SearchTask() {

        }

        @Override
        protected AbstractAdapter doInBackground(String... searches) {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.database_updating), Context.MODE_PRIVATE);
            if (sharedPreferences.getBoolean(getString(R.string.database_updating), false))
                return null;

            DatabaseOpenHelper databaseOpenHelper = new DatabaseOpenHelper(getApplicationContext());
            SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();

            localItems = new ArrayList<AbstractAdapter.AbstractItem>();

            searchesLoop:
            for (String s: searches) {

                String[] searchComponents = {
                        "firstname LIKE '" + s + "%'",
                        "lastname LIKE '" + s + "%'",
                        "number LIKE '" + s + "%'",
                        "author LIKE '%" + s + "%'",
                        "affiliation LIKE '%" + s + "%'",
                        "title LIKE '%" + s + "%'",
                        "content LIKE '%" + s + "%'"
                };

                for (String sc: searchComponents) {
                    String query = "SELECT _id, Number, title, firstname, lastname, ETime, content FROM Abstracts WHERE " + sc;
                    Cursor cursor = database.rawQuery(query, null);


                    if (cursor.getCount()>0) {
                        cursor.moveToFirst();

                        queryLoop:
                        do {
                            for (AbstractAdapter.AbstractItem ai : localItems)
                                if (ai.Id.equals(cursor.getString(0)))
                                    continue queryLoop;

                            localItems.add(
                                    new AbstractAdapter.AbstractItem(
                                            cursor.getString(0),
                                            cursor.getString(1),
                                            cursor.getString(2),
                                            cursor.getString(3) == null ? "" : cursor.getString(3) + " " + cursor.getString(4),
                                            cursor.getString(5),
                                            !cursor.isNull(6)
                                    )
                            );
                        } while (cursor.moveToNext());
                    }

                    cursor.close();
                }
            }

            database.close();
            return new AbstractAdapter(getApplicationContext(), R.layout.abstract_item, localItems);
        }

        @Override
        protected void onPreExecute() {
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected void onPostExecute(AbstractAdapter result) {
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);

            if (result != null) {
                items = localItems;
                setListAdapter(result);
            }
        }
    }
}
