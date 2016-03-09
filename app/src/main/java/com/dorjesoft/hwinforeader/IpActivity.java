package com.dorjesoft.hwinforeader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class IpActivity extends AppCompatActivity implements Hwinfo.Callback {

    private final String PREFS = "HwinfoReaders";
    private final String PREFS_IP = "reader_ip_";
    private final String PREFS_PORT = "reader_port_";

    private int mMaxId = 0;
    private final List<StandardReader> mReaders = new LinkedList<>();

    private TableLayout mTable;

    protected void loadReaders() {
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        for (int i = 0; ; i++) {
            String ip = prefs.getString(PREFS_IP + i, null);
            int port = prefs.getInt(PREFS_PORT + i, -1);

            if (ip == null || port == -1) {
                break;
            }

            mReaders.add(new StandardReader(++mMaxId, this, ip, port));
        }

        if (mReaders.size() == 0) {
            String DEFAULT_IP = "192.168.1.100";
            int DEFAULT_PORT = 27007;

            mReaders.add(new StandardReader(++mMaxId, this, DEFAULT_IP, DEFAULT_PORT));
        }
    }

    protected void saveReaders() {
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < mReaders.size(); i++) {
            StandardReader r = mReaders.get(i);
            editor.putString(PREFS_IP + i, r.getIp());
            editor.putInt(PREFS_PORT + i, r.getPort());
        }
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("hwinfo", "Hwinfo create.");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mTable = (TableLayout) findViewById(R.id.table);

        loadReaders();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("hwinfo", "Hwinfo resume.");

        for (StandardReader r : mReaders) {
            r.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("hwinfo", "Hwinfo pause.");

        for (StandardReader r : mReaders) {
            r.pause();
        }

        saveReaders();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createHwinfo(Hwinfo hwinfo) {
        for (Reading r : hwinfo.getReadings()) {
            TableRow tr = new TableRow(this);

            TextView tv = new TextView(this);
            tv.setText(r.getLabelUser());
            tr.addView(tv);

            tv = new TextView(this);
            tv.setTag(r.getLabelOrig());
            tv.setText(r.format());
            tr.addView(tv);

            mTable.addView(tr);
        }

    }

    protected void mySetHwinfo(Hwinfo hwinfo) {
        for (Reading r : hwinfo.getReadings()) {
            TextView tv = (TextView) mTable.findViewWithTag(r.getLabelOrig());

            if (tv == null) {
                createHwinfo(hwinfo);
                tv = (TextView) mTable.findViewWithTag(r.getLabelOrig());
            }

            tv.setText(r.format());
        }
    }

    @Override
    public void setHwinfo(final Hwinfo hwinfo) {
        mTable.post(new Runnable() {
            @Override
            public void run() {
                mySetHwinfo(hwinfo);
            }
        });
    }
}
