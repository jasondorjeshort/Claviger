package com.dorjesoft.hwinforeader;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
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
    private final String PREFS_NUM_READERS = "num_readers";

    private int mMaxId = 0;
    private final List<StandardReader> mReaders = new LinkedList<>();

    private TableLayout mTable;

    private final String PREFS_SHOW_MIN = "show_min";
    private final String PREFS_SHOW_MAX = "show_max";
    private final String PREFS_SHOW_AVG = "show_avg";
    private boolean mShowMin, mShowMax, mShowAvg;

    private final int COLUMN_MIN = 1;
    private final int COLUMN_MAX = 2;
    private final int COLUMN_AVG = 3;

    private FragmentManager fm = getSupportFragmentManager();

    protected void loadReaders() {
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        //  int numReaders = prefs.getInt(PREFS_NUM_READERS, 0);
        int numReaders = 0;
        for (int i = 0; i < numReaders; i++) {
            String ip = prefs.getString(PREFS_IP + i, null);
            int port = prefs.getInt(PREFS_PORT + i, -1);

            if (ip == null || port == -1) {
                break;
            }

            mReaders.add(new StandardReader(++mMaxId, this, ip, port));
        }

        if (mReaders.size() == 0 && false) {
            String DEFAULT_IP = "192.168.1.100";
            int DEFAULT_PORT = 27007;

            mReaders.add(new StandardReader(++mMaxId, this, DEFAULT_IP, DEFAULT_PORT));
        }

        mShowMin = prefs.getBoolean(PREFS_SHOW_MIN, false);
        mShowMax = prefs.getBoolean(PREFS_SHOW_MAX, false);
        mShowAvg = prefs.getBoolean(PREFS_SHOW_AVG, false);

        mTable.setColumnCollapsed(COLUMN_MIN, !mShowMin);
        mTable.setColumnCollapsed(COLUMN_MAX, !mShowMax);
        mTable.setColumnCollapsed(COLUMN_AVG, !mShowAvg);
    }

    protected void saveReaders() {
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(PREFS_NUM_READERS, mReaders.size());
        for (int i = 0; i < mReaders.size(); i++) {
            StandardReader r = mReaders.get(i);
            editor.putString(PREFS_IP + i, r.getIp());
            editor.putInt(PREFS_PORT + i, r.getPort());
        }

        editor.putBoolean(PREFS_SHOW_MIN, mShowMin);
        editor.putBoolean(PREFS_SHOW_MAX, mShowMax);
        editor.putBoolean(PREFS_SHOW_AVG, mShowAvg);

        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("hwinfo", "Hwinfo create.");

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerDialog dFragment = new ServerDialog();
                dFragment.setReaders(IpActivity.this, mReaders);
                // Show DialogFragment
                dFragment.show(fm, "Dialog Fragment");
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

        menu.findItem(R.id.action_show_min).setChecked(mShowMin);
        menu.findItem(R.id.action_show_max).setChecked(mShowMax);
        menu.findItem(R.id.action_show_avg).setChecked(mShowAvg);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_show_min) {
            mShowMin = !mShowMin;
            item.setChecked(mShowMin);
            mTable.setColumnCollapsed(COLUMN_MIN, !mShowMin);
            return true;
        }
        if (id == R.id.action_show_max) {
            mShowMax = !mShowMax;
            item.setChecked(mShowMax);
            mTable.setColumnCollapsed(COLUMN_MAX, !mShowMax);
            return true;
        }
        if (id == R.id.action_show_avg) {
            mShowAvg = !mShowAvg;
            item.setChecked(mShowAvg);
            mTable.setColumnCollapsed(COLUMN_AVG, !mShowAvg);
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

            tv = new TextView(this);
            tv.setTag(r.getLabelOrig() + " min");
            tv.setText(r.formatMin());
            tr.addView(tv);

            tv = new TextView(this);
            tv.setTag(r.getLabelOrig() + " max");
            tv.setText(r.formatMax());
            tr.addView(tv);

            tv = new TextView(this);
            tv.setTag(r.getLabelOrig() + " avg");
            tv.setText(r.formatAvg());
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

            tv = (TextView) mTable.findViewWithTag(r.getLabelOrig() + " min");
            tv.setText(r.formatMin());

            tv = (TextView) mTable.findViewWithTag(r.getLabelOrig() + " max");
            tv.setText(r.formatMax());

            tv = (TextView) mTable.findViewWithTag(r.getLabelOrig() + " avg");
            tv.setText(r.formatAvg());
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
