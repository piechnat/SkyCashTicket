package pl.piechnat.skycashticket;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    SkyCashData data;
    Switch swOnlineMode;
    SeekBar sbFakeInterval;
    EditText[] etList;
    EditText etUserId, etUserPass, etUserPin, etTicketId, etSupplierId, etDeviceId;
    TextView tvAppStatus, tvFakeInterval;
    Button btnBalance, btnLogout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, null)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    onBackPressed();
                    return true;
                }
            } );
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setElevation(2);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setIcon(R.mipmap.skycash);
        ab.setTitle("  " + ab.getTitle());
        setContentView(R.layout.activity_main);
        data = SkyCashData.getInstance(this);
        swOnlineMode = (Switch) findViewById(R.id.swOnlineMode);
        // Online Mode Change // status
        swOnlineMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                data.onlineMode = isChecked;
                updateLayout();
                MainActivity.showNotification(getApplicationContext());
            }
        });
        sbFakeInterval = (SeekBar) findViewById(R.id.sbFakeInterval);
        tvFakeInterval = (TextView) findViewById(R.id.tvFakeInterval);
        // Fake Interval Change
        sbFakeInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvFakeInterval.setText(progress > 0 ? "-" + Integer.toString(progress * 5) : "brak");
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        etList = new EditText[] {
            etUserId = (EditText) findViewById(R.id.etUserId),
            etUserPass = (EditText) findViewById(R.id.etUserPass),
            etUserPin = (EditText) findViewById(R.id.etUserPin),
            etTicketId = (EditText) findViewById(R.id.etTicketId),
            etSupplierId = (EditText) findViewById(R.id.etSupplierId),
            etDeviceId = (EditText) findViewById(R.id.etDeviceId)
        };
        tvAppStatus = (TextView) findViewById(R.id.tvAppStatus);
        btnBalance = (Button) findViewById(R.id.btnBalance);
        btnLogout = (Button) findViewById(R.id.btnLogout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        swOnlineMode.setChecked(data.onlineMode);
        etUserId.setText(data.userId);
        etUserPass.setText(data.userPass);
        etUserPin.setText(data.userPin);
        etTicketId.setText(data.ticketId);
        etSupplierId.setText(data.supplierId);
        etDeviceId.setText(data.deviceId);
        sbFakeInterval.setProgress((int) data.fakeInterval / 5000);
        updateLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateSettings();
        data.saveSettings();
        data.saveData();
    }

    void updateSettings() {
        data.onlineMode = swOnlineMode.isChecked();
        data.userId = etUserId.getText().toString();
        data.userPass = etUserPass.getText().toString();
        data.userPin = etUserPin.getText().toString();
        data.ticketId = etTicketId.getText().toString();
        data.supplierId = etSupplierId.getText().toString();
        data.deviceId = etDeviceId.getText().toString();
        data.fakeInterval = sbFakeInterval.getProgress() * 5000;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_enter, R.anim.right_exit);
    }

    public void btnBalance_onClick(View view) {
        new GetBalance().execute();
    }

    public void btnLogout_onClick(View v) {
        data.setSessionId(null);
        updateLayout();
    }

    public void lastError_onClick(View v) {
        if (data.getLastError() == null) return;
        showDialog(this, null, "Usunąć ostatni błąd aplikacji?", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                data.setLastError(null);
                dialog.dismiss();
                updateLayout();
            }
        } );
    }

    void updateLayout() {
        swOnlineMode.setText(getString(R.string.swOnlineMode) + (data.onlineMode ? " ▷" : ""));
        swOnlineMode.setBackgroundResource(data.onlineMode ? R.drawable.border_hover : R.drawable.border);
        tvAppStatus.setVisibility(View.GONE);
        tvAppStatus.setText(null);
        String errorMsg = data.getLastError();
        if (errorMsg != null) tvAppStatus.append("Ostatni błąd aplikacji: " + errorMsg + " ");
        if (data.isValidTicket()) tvAppStatus.append("Zakupiony bilet ważny do " + data.getRealExpiration());
        if (tvAppStatus.getText().length() == 0 && data.onlineMode) tvAppStatus.setText(getString(R.string.tvAppStatusDefText));
        if (tvAppStatus.getText().length() > 0) tvAppStatus.setVisibility(View.VISIBLE);
        btnBalance.setText(data.lastBalance > -1 ? String.format(getString(R.string.btnBalanceText), (float) data.lastBalance / 10000) : getString(R.string.btnBalanceDefText));
        if (data.getSessionId() != null) {
            btnBalance.setEnabled(false);
            btnLogout.setEnabled(true);
            for (EditText et : etList) et.setEnabled(false);
        } else {
            btnBalance.setEnabled(data.onlineMode);
            btnLogout.setEnabled(false);
            if (data.onlineMode) btnBalance.setText(getString(R.string.btnBalanceDefText));
            for (EditText et : etList) et.setEnabled(data.onlineMode);
        }
    }

    class GetBalance extends AsyncTask<Void, Void, Void> {
        SkyCashClient client;
        @Override
        protected void onPreExecute() {
            updateSettings();
            if (data.onlineMode) btnBalance.setEnabled(false); else this.cancel(true);
        }
        @Override
        protected Void doInBackground(Void... params) {
            client = new SkyCashClient(data);
            try {
                if (client.login() == SkyCashClient.ALREADY_LOGGED_IN) {
                    data.lastBalance = client.sendData(client.getInfoReq).getJSONObject("accountInfo").getInt("availableBalanceInTicks");
                }
            } catch (Exception e) {
                data.setLastError(e.getMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            updateLayout();
        }
    }

    static void showNotification(Context cntx) {
        ((NotificationManager)cntx.getSystemService(Context.NOTIFICATION_SERVICE)).notify(1,
                new NotificationCompat.Builder(cntx)
                        .setContentTitle("SkyCash bilety")
                        .setContentText("Szczegóły aktywnych biletów...")
                        .setContentInfo("1 bilet" + (SkyCashData.getInstance(cntx).onlineMode ? " ▷" : ""))
                        .setWhen(System.currentTimeMillis())
                        .setPriority(Notification.PRIORITY_MAX)
                        .setSmallIcon(R.drawable.skycash_noti)
                        .setLargeIcon(BitmapFactory.decodeResource(cntx.getResources(), R.mipmap.skycash))
                        .setContentIntent(
                                TaskStackBuilder.create(cntx)
                                        .addParentStack(TicketActivity.class)
                                        .addNextIntent(new Intent(cntx, TicketActivity.class))
                                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                        ).build()
        );
    }

    static void showToast(String msg, Context cntx) {
        Toast.makeText(cntx, msg, Toast.LENGTH_SHORT).show();
    }

    static void showDialog(Context cntx, String title, String msg, DialogInterface.OnClickListener okListener) {
        DialogInterface.OnClickListener closeListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
        };
        AlertDialog dlg = new AlertDialog.Builder(cntx).create();
        if (title != null) dlg.setTitle(title);
        if (msg != null) dlg.setMessage(msg);
        if (okListener != null) {
            dlg.setButton(AlertDialog.BUTTON_POSITIVE, "OK", okListener);
            dlg.setButton(AlertDialog.BUTTON_NEUTRAL, "Anuluj", closeListener);
        } else {
            dlg.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", closeListener);
        }
        dlg.show();
    }
}
