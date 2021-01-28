package org.pagemeeting.App2011;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Date;

public class AboutActivity extends Activity {
    private static final String TAG = AboutActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/about/AndroidAbout.html");
        webView.setWebViewClient(new WebViewClient() {
             @Override
             public void onPageFinished(WebView view, String url) {
                 super.onPageFinished(view, url);

                 SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.last_modified_database), Context.MODE_PRIVATE);
                 if (sharedPreferences != null) {
                     Long lastModified = Long.parseLong(sharedPreferences.getString(getString(R.string.last_modified_database), ""));
                     Date date = new Date(lastModified);

                     String databaseDate = "Database updated: " + date.toString();
                     view.loadUrl("javascript:appendText('" + databaseDate + "')");
                 }
             }
         });


            //TextView textView = (TextView) findViewById(R.id.textView);
            //textView.setText("Database date: " + date.toString());
    }
}
