// Copyright (C) 2011-2013 ITOMIG GmbH
//
//   This file is part of iTopEnterprise.
//
//   iTopEnterprise is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 3 of the License, or
//   (at your option) any later version.
//
//   iTopEnterprise is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with iTopEnterprise. If not, see <http://www.gnu.org/licenses/>

package de.itomig.itopenterprise;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import static de.itomig.itopenterprise.ItopConfig.TAG;
import static de.itomig.itopenterprise.ItopConfig.debug;
import static de.itomig.itopenterprise.ItopConfig.itopmobileactivity;


public class SearchActivity extends Activity {

    private Button btnGo;
    private WebView webview;
    private EditText searchExpr;

    // the following string array should match the arrays in "arrays.xml"
    private String[] catArray = {"FunctionalCI", "Person"};
    // these are the corresponding attributes for the WHERE clause in the OQL statement
    private String[] whereArray = {"name", "friendlyname"};

    private static String createSearchUrl(String cat, String searchExpression, String where) {
        // including credentialas
        String creds = ItopConfig.getItopCredentials();

        String url1 = "/webservices/export.php?" + creds +
                "&expression=SELECT%20" + cat +
                "%20+WHERE+" + where + "+LIKE+'%25" + searchExpression + "%25'";

        // http://192.168.178.31/itop/webservices/export.php?auth_user=mary&auth_pwd=jane&expression=SELECT%20UserRequest%20WHERE%20status!=%22closed%22%20AND%20status!=%22resolved%22%20&format=csv&fields=priority,title,caller_id_friendlyname,description
        return ItopConfig.getItopUrl() + url1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // switch does not work for library project.
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, Preferences.class));
            return (true);
        } else if (item.getItemId() == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return (true);
        } else if (item.getItemId() == R.id.menu_tickets) {
            startActivity(new Intent(this, itopmobileactivity));
            return (true);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // needs to be called before anything else
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.search);

        if (debug) Log.i(TAG, this.getLocalClassName() + " onCreate");

        btnGo = (Button) findViewById(R.id.go);
        webview = (WebView) findViewById(R.id.search_result);
        searchExpr = (EditText) findViewById(R.id.search_string);

        final Spinner s = (Spinner) findViewById(R.id.sel_category);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.search_category, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);

        // expression=SELECT+FunctionalCI+WHERE+name+LIKE+'%25SERVER%25'

        btnGo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String cat = catArray[s.getSelectedItemPosition()];
                String where = whereArray[s.getSelectedItemPosition()];
                String expr = searchExpr.getText().toString();
                displaySearch(cat, expr, where);
            }
        });

        searchExpr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //				String cat = catArray[s.getSelectedItemPosition()];
                //				String where = whereArray[s.getSelectedItemPosition()];
                //				String expr = searchExpr.getText().toString();
                //				displaySearch(cat,expr,where);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (debug) Log.i(TAG, this.getLocalClassName() + " - onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (debug) Log.i(TAG, this.getLocalClassName() + " - onPause");

    }


    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void displaySearch(String cat, String searchExpression, String where) {
        setProgressBarIndeterminateVisibility(true);
        setProgressBarVisibility(true);

        if (debug) Log.d(TAG, this.getLocalClassName() + " - searching for " + searchExpression);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().supportZoom();
        webview.getSettings().setUseWideViewPort(true);
        webview.setInitialScale(100);
        webview.setWebViewClient(new MyWebViewClient());

        webview.loadUrl(createSearchUrl(cat, searchExpression, where));

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            setProgressBarIndeterminateVisibility(true);
            setProgressBarVisibility(true);
            String url1 = url + ItopConfig.getItopCredentials();
            Log.d(TAG, "Search: trying to load from webview: " + url1);
            view.getSettings().setJavaScriptEnabled(true);
            view.getSettings().setAppCacheEnabled(true);
            view.getSettings().setBuiltInZoomControls(true);
            view.getSettings().setDomStorageEnabled(true);
            view.getSettings().setLoadWithOverviewMode(true);
            view.getSettings().supportZoom();
            view.getSettings().setUseWideViewPort(true);
            webview.setInitialScale(100);
            view.loadUrl(url1);
            return true;
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // Toast.makeText(activity, "ERROR! ", Toast.LENGTH_SHORT).show();
            String loading = "<html><body>ERROR:<br />" + description + "</body></html>";
            webview.loadData(loading, "text/html", null);
            if (debug) Log.d(TAG, "Search:  Error: " + description);
        }


        public void onPageFinished(WebView view, String url) {
            if (debug) Log.d(TAG, "Search: page finished.");
            setProgressBarIndeterminateVisibility(false);
            setProgressBarVisibility(false);

        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                                              HttpAuthHandler handler, String host, String realm) {
            // will only work (i.e. will be called if auth needed) if iTop "form"-authentication is switched off in config-itop.php
            //     !!!
            if (debug) Log.d(TAG, "Search: Received AuthRequest");
            //handler.proceed(login,password);

        }


    }
}