package org.pagemeeting.App2011;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    SetPageTitle();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            if (mSectionsPagerAdapter != null) {
                mViewPager.setAdapter(mSectionsPagerAdapter);
            }

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            if (tabLayout != null)
                tabLayout.setupWithViewPager(mViewPager);
        }

        mViewPager.setVisibility(View.INVISIBLE);

        DownloadTask task = new DownloadTask(this);
        task.execute(getApplicationContext().getSharedPreferences(getString(R.string.last_modified_database), Context.MODE_PRIVATE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void EnableViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);

        FavouriteNavigation favouriteNavigation = (FavouriteNavigation) getTabbedFragment(mViewPager, 1);
        favouriteNavigation.LoadFavourites();

        mViewPager.setVisibility(View.VISIBLE);
        SetPageTitle();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_search) {
            Intent i = new Intent(this, SearchAbstractNavigationActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        // If the fragment exists and has some back-stack entry
        Fragment activeFragment = getActiveFragment(mViewPager);
        if (activeFragment != null && activeFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
            // Get the fragment fragment manager - and pop the backstack
            activeFragment.getChildFragmentManager().popBackStack();
        }
        // Else, nothing in the direct fragment back stack
        else {
            // Let super handle the back press
            super.onBackPressed();
        }
    }

    public void SetPageTitle() {
        String title = "PAGE Abstracts";

        Fragment activeFragment = getActiveFragment(mViewPager);
        /*if (activeFragment != null) {
            for (int i=0; i<activeFragment.getChildFragmentManager().getBackStackEntryCount(); i++)
                title += " > " + activeFragment.getChildFragmentManager().getBackStackEntryAt(i).getName();
        }*/
        int backStackCount = activeFragment.getChildFragmentManager().getBackStackEntryCount();
        if (activeFragment != null && backStackCount > 0)
            title = activeFragment.getChildFragmentManager().getBackStackEntryAt(backStackCount - 1).getName();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
    }

    public Fragment getTabbedFragment(ViewPager container, int index) {
        Log.d(TAG, "Current tab: " + mViewPager.getCurrentItem());
        String name = makeFragmentName(container.getId(), index);
        return getSupportFragmentManager().findFragmentByTag(name);
    }

    public Fragment getActiveFragment(ViewPager container) {
        return  getTabbedFragment(container, container.getCurrentItem());
    }

    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final FragmentManager fragmentManager;
        private final Activity activity;

        public SectionsPagerAdapter(FragmentManager fm, Activity activity) {
            super(fm);
            fragmentManager = fm;
            this.activity = activity;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MainNavigation.newInstance();
                case 1:
                    return FavouriteNavigation.newInstance();
                //case 2:
                //   return SearchAbstractNavigationFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Abstracts";
                case 1:
                    return "Favourites";
                //case 2:
                //    return "Search";
            }
            return null;
        }
    }


    // usually, subclasses of AsyncTask are declared inside the activity class.
    // that way, you can easily modify the UI thread from here
    private class DownloadTask extends AsyncTask<SharedPreferences, Integer, String> {
        private final String TAG = DownloadTask.class.getSimpleName();

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private SharedPreferences sharedPreferences;
        private String lastModified;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(SharedPreferences... sharedPrefs) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            sharedPreferences = sharedPrefs[0];

            try {
                URL url = new URL("https://www.page-meeting.org/page/App/pageabstracts.sqlite");
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                String savedLastModified = sharedPreferences.getString(getString(R.string.last_modified_database), "");
                Log.d(TAG, "saved last modified: " + savedLastModified);

                lastModified = String.valueOf(connection.getLastModified());

                if (!lastModified.equals(savedLastModified)) {

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // download the file
                    input = connection.getInputStream();
                    String outputPath = new File(context.getExternalFilesDir(null).getAbsolutePath(), DatabaseOpenHelper.DATABASE_NAME).toString();
                    Log.d(TAG, outputPath);
                    output = new FileOutputStream(outputPath);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        /*if (isCancelled()) {
                            input.close();
                            return null;
                        }*/
                        total += count;

                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));

                        output.write(data, 0, count);
                    }
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }

            publishProgress(100);
            return null;
        }

        @Override
        protected void onPreExecute() {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.database_updating), Context.MODE_PRIVATE);
            sharedPreferences.edit().clear().putBoolean(getString(R.string.database_updating), true).apply();

            Fragment mainNavigation = getTabbedFragment(mViewPager, 0);
            if (mainNavigation != null) {
                int backstackCount = mainNavigation.getChildFragmentManager().getBackStackEntryCount();
                for (int i = 0; i < backstackCount; i++)
                    mainNavigation.getChildFragmentManager().popBackStack();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress[0]);
            }
        }

        @Override
        protected void onPostExecute(String result) {

            if (result == null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString(getString(R.string.last_modified_database), lastModified);
                editor.commit();
            }else {
                Log.d(TAG, "Error: " + result);
            }

            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.database_updating), Context.MODE_PRIVATE);
            sharedPreferences.edit().clear().putBoolean(getString(R.string.database_updating), false).apply();

            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            if (progressBar != null)
                progressBar.setVisibility(View.INVISIBLE);

            EnableViews();
        }
    }
}