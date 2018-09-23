package pl.piechnat.skycashticket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

public class WelcomeActivity extends AppCompatActivity {

    Rect btnLocation = new Rect();
    ImageView ivMain;
    TextView tvBalance;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, null);
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        ImageButton img = new ImageButton(this);
        img.setBackgroundResource(0);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels - (int)(179 * metrics.density) - getSupportActionBar().getHeight();
        img.setLayoutParams(new LinearLayout.LayoutParams(width, getSupportActionBar().getHeight()));
        item.setActionView(img);
        item.getActionView().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                connectionError();
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
        ab.setDisplayShowTitleEnabled(false);
        ab.setHomeAsUpIndicator(R.drawable.menu);
        ab.setIcon(R.drawable.fake_logo);
        setContentView(R.layout.activity_welcome);
        ivMain = (ImageView) findViewById(R.id.ivMain);
        tvBalance = (TextView) findViewById(R.id.tvBalance);
        ivMain.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int width = ivMain.getMeasuredWidth();
                int height = ivMain.getMeasuredHeight();
                if (btnLocation.isEmpty()) btnLocation.set(
                    (int)(width * 0.3402), // Left
                    (int)(height * 0.6664),// Top 0.6357
                    (int)(width * 0.6597), // Right
                    (int)(height * 0.8321) // Bottom 0.8121
                );
                tvBalance.setPadding((int)(width * 0.1), (int)(height * 0.048), 0, 0);
                tvBalance.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(width * 0.069));
            }
        });
        ivMain.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.playSoundEffect(SoundEffectConstants.CLICK);
                    final boolean btnClick = btnLocation.contains((int)event.getX(), (int)event.getY());
                    v.postDelayed(new Runnable() {
                        public void run() {
                            if (!btnClick) connectionError(); else
                                startActivity(new Intent(getApplicationContext(), ListActivity.class));
                        }
                    }, 100);
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.showNotification(this);
        SkyCashData data = SkyCashData.getInstance(this);
        tvBalance.setText(data.lastBalance > -1 ? String.format("%5.2f PLN", (float) data.lastBalance / 10000) : "0,00 PLN");
    }

    public void connectionError() {
        AlertDialog dlg = new AlertDialog.Builder(this, R.style.MyAlertDialog).create();
        dlg.setTitle("Wystąpił błąd");
        dlg.setMessage("Wystąpił błąd podczas komunikacji z serwerem. Sprawdź dostępność połączenia internetowego w telefonie.\n");
        dlg.setButton(AlertDialog.BUTTON_NEUTRAL, "Bilety offline", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getApplicationContext(), ListActivity.class));
            }
        });
        dlg.setButton(AlertDialog.BUTTON_POSITIVE, "Tak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
        });
        dlg.show();
    }
}
