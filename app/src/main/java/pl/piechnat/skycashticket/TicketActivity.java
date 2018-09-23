package pl.piechnat.skycashticket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class TicketActivity extends AppCompatActivity {

    SkyCashData data;
    ConstraintLayout loadingLayer;
    WebView webView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, null);
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        ImageButton img = new ImageButton(this);
        img.setBackgroundResource(0);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels - (int)(124 * metrics.density) - getSupportActionBar().getHeight();
        img.setLayoutParams(new LinearLayout.LayoutParams(width, getSupportActionBar().getHeight()));
        item.setActionView(img);
        item.getActionView().setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                new UpdateTicket(true).execute();
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setElevation(8);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setIcon(R.drawable.logo);
        ab.setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_ticket);
        data = SkyCashData.getInstance(this);
        loadingLayer = (ConstraintLayout) findViewById(R.id.loadingLayer);
        webView = (WebView) findViewById(R.id.webView);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) { return true; }
        });
        webView.setLongClickable(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("updated")) loadingLayer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new UpdateTicket(false).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        data.saveData();
    }

    class UpdateTicket extends AsyncTask<Void, Void, Void> {
        boolean forceUpdate = false;
        UpdateTicket(boolean update) {
            forceUpdate = update;
        }
        @Override
        protected void onPreExecute() {
            loadingLayer.setVisibility(View.VISIBLE);
            webView.loadDataWithBaseURL(Long.toString(System.currentTimeMillis()) + ":static",
                    data.getTicket(forceUpdate), "text/html", "UTF-8", null);
            if (data.isValidTicket() || !data.onlineMode) this.cancel(true);
        }
        @Override
        protected Void doInBackground(Void... params) {
            SkyCashClient client = new SkyCashClient(data);
            try {
                client.login();
                client.buyTicket();
                ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
            } catch (Exception e) {
                data.setLastError(e.getMessage());
                this.cancel(true);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            webView.loadDataWithBaseURL(Long.toString(System.currentTimeMillis()) + ":updated",
                    data.getTicket(), "text/html", "UTF-8", null);
            invalidateOptionsMenu();
        }
        @Override
        protected void onCancelled() {
            loadingLayer.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }
    }
}



