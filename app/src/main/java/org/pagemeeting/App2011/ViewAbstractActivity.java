package org.pagemeeting.App2011;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.shamanland.fab.FloatingActionButton;
import android.view.View;

import java.util.HashSet;

public class ViewAbstractActivity extends Activity {
    private static final String TAG = ViewAbstractActivity.class.getSimpleName();

    public static final String ABSTRACT_ID = "abstract-id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_abstract);

        ObservableWebView webView = (ObservableWebView) findViewById(R.id.webView);
        webView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback(){

            public void onScrollChanged(int l, int t, int oldl, int oldt){
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                int tdelta = oldt - t;
                if (tdelta > 0)
                    fab.show();
                else if (tdelta < 0)
                    fab.hide();

            }
        });

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.database_updating), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.database_updating), false))
            return;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String abstractId = extras.getString(ABSTRACT_ID);

            DatabaseOpenHelper databaseOpenHelper = new DatabaseOpenHelper(this);
            SQLiteDatabase database = databaseOpenHelper.getReadableDatabase();
            String query = "SELECT type, Number, firstname, lastname, title, author, affiliation, content FROM Abstracts WHERE _id='" + abstractId + "'";
            Cursor cursor = database.rawQuery(query, null);
            cursor.moveToFirst();

            String abstractHtml = "<html><body><p align=right>"
                    +cursor.getString(0)+"<BR><HR></p><H3><center>"
                    +cursor.getString(1)+" <I>"
                    +cursor.getString(2)+" "
                    +cursor.getString(3)+" </I> <b>"
                    +cursor.getString(4)+"</b></center></H3><p><center>"
                    +cursor.getString(5)+"<br><I>"
                    +cursor.getString(6)+"</I></center></p><p>"
                    +cursor.getString(7)+"<p> </body></html>";

            cursor.close();
            database.close();

            webView.loadDataWithBaseURL("", abstractHtml, "text/html", "UTF-8", "");

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {
                        String abstractId = extras.getString(ABSTRACT_ID);

                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.favourites), Context.MODE_PRIVATE);
                        HashSet<String> favourites = (HashSet<String>) sharedPreferences.getStringSet(getString(R.string.favourites), new HashSet<String>());


                        if (favourites.contains(abstractId))
                            favourites.remove(abstractId);
                        else
                            favourites.add(abstractId);


                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.putStringSet(getString(R.string.favourites), favourites);
                        editor.commit();

                        SetFabIcon();
                    }
                }
            });

            SetFabIcon();
        }
    }

    void SetFabIcon() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String abstractId = extras.getString(ABSTRACT_ID);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.favourites), Context.MODE_PRIVATE);
            HashSet<String> favourites = (HashSet<String>) sharedPreferences.getStringSet(getString(R.string.favourites), new HashSet<String>());
            if (favourites.contains(abstractId))
                fab.setImageResource(android.R.drawable.btn_star_big_on);
            else
                fab.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

}
