package pl.piechnat.skycashticket;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListActivity extends AppCompatActivity {

    ImageView ivList;
    TextView tvTicketName, tvTicketCity;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, null);
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        ImageButton img = new ImageButton(this);
        img.setBackgroundResource(0);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels - (int)(214 * metrics.density) - getSupportActionBar().getHeight();
        img.setLayoutParams(new LinearLayout.LayoutParams(width, 1));
        item.setActionView(img);
        item = menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, null);
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setIcon(R.drawable.ic_action_refresh);
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
        setContentView(R.layout.activity_list);
        ivList = (ImageView) findViewById(R.id.ivList);
        tvTicketName = (TextView) findViewById(R.id.tvTicketName);
        tvTicketCity = (TextView) findViewById(R.id.tvTicketCity);
        ivList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), TicketActivity.class));
                    }
                }, 100);
            }
        });
        ivList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int width = ivList.getMeasuredWidth();
                int height = ivList.getMeasuredHeight();
                tvTicketName.setPadding((int)(width * 0.023), (int)(height * 0.05), 0, 0);
                tvTicketName.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(width * 0.052));
                tvTicketCity.setPadding((int)(width * 0.186), (int)(height * 0.416), 0, 0);
                tvTicketCity.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(width * 0.04));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SkyCashData d = SkyCashData.getInstance(this);
        tvTicketName.setText(d.ticketName);
        tvTicketCity.setText(d.ticketCity);
    }
}
