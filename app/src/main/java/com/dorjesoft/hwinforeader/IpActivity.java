package com.dorjesoft.hwinforeader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jdorje.  Released under the GNU General Public Licence.
 */
public class IpActivity extends AppCompatActivity implements Hwinfo.Callback {

    private static final String PREFS = "HwinfoReaders";
    private static final String PREFS_IP = "reader_ip_";
    private static final String PREFS_PORT = "reader_port_";
    private static final String PREFS_NAME = "reader_name_";
    private static final String PREFS_NUM_READERS = "num_readers";

    protected int mMaxId = 0;
    protected final List<StandardReader> mReaders = new LinkedList<>();

    protected TableLayout mTable;

    private final String PREFS_SHOW_MIN = "show_min";
    private final String PREFS_SHOW_MAX = "show_max";
    private final String PREFS_SHOW_AVG = "show_avg";
    private boolean mShowMin, mShowMax, mShowAvg;

    private static final int COLUMN_MIN = 2;
    private static final int COLUMN_MAX = 3;
    private static final int COLUMN_AVG = 4;
    private static final int NUM_COLUMNS = 5;

    private FragmentManager fm = getSupportFragmentManager();

    protected void loadReaders() {
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        int numReaders = prefs.getInt(PREFS_NUM_READERS, 0);
        // int numReaders = 0;
        for (int i = 0; i < numReaders; i++) {
            String ip = prefs.getString(PREFS_IP + i, null).trim();
            int port = prefs.getInt(PREFS_PORT + i, -1);
            String name = prefs.getString(PREFS_NAME + i, null).trim();

            boolean dup = false;
            for (StandardReader r : mReaders) {
                if (r.getPort() == port && r.getIp().equals(ip)) {
                    dup = true;
                    break;
                }
            }

            if (!dup) {
                mReaders.add(new StandardReader(++mMaxId, this, name, ip, port));
            }
        }

        mShowMin = prefs.getBoolean(PREFS_SHOW_MIN, false);
        mShowMax = prefs.getBoolean(PREFS_SHOW_MAX, false);
        mShowAvg = prefs.getBoolean(PREFS_SHOW_AVG, false);

        mTable.post(new Runnable() {
            @Override
            public void run() {
                mTable.setColumnCollapsed(COLUMN_MIN, !mShowMin);
                mTable.setColumnCollapsed(COLUMN_MAX, !mShowMax);
                mTable.setColumnCollapsed(COLUMN_AVG, !mShowAvg);
            }
        });
    }

    protected void saveReaders() {
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(PREFS_NUM_READERS, mReaders.size());
        for (int i = 0; i < mReaders.size(); i++) {
            StandardReader r = mReaders.get(i);
            editor.putString(PREFS_IP + i, r.getIp());
            editor.putInt(PREFS_PORT + i, r.getPort());
            editor.putString(PREFS_NAME + i, r.getName());
        }

        editor.putBoolean(PREFS_SHOW_MIN, mShowMin);
        editor.putBoolean(PREFS_SHOW_MAX, mShowMax);
        editor.putBoolean(PREFS_SHOW_AVG, mShowAvg);

        editor.apply();
    }

    public void addServer() {
        ServerDialog dFragment = new ServerDialog();
        dFragment.setData(IpActivity.this, null);
        // Show DialogFragment
        dFragment.show(fm, "Dialog Fragment");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("hwinfo", "Hwinfo create.");

        mTable = (TableLayout) findViewById(R.id.table);

        loadReaders();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("hwinfo", "Hwinfo resume.");

        for (StandardReader r : mReaders) {
            r.resume();
            // todo: resume chronometer
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        Log.d("hwinfo", "Hwinfo pause.");

        for (StandardReader r : mReaders) {
            r.pause();
            // todo: pause chronometer
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
        if (id == R.id.action_add_listener) {
            addServer();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getServerTag(Hwinfo hwinfo) {
        return "hwinfo_" + hwinfo.getReader().getId() + "_";
    }

    public final static String CHRONOMETER_TAG = "tag_chronometer";

    private void changeLayoutParams(View v, int weight) {
        TableRow.LayoutParams lp = (TableRow.LayoutParams) v.getLayoutParams();
        lp.width = 0;
        lp.weight = weight;
        lp.leftMargin = Math.max(lp.leftMargin, 8);
        lp.rightMargin = Math.max(lp.rightMargin, 8);
        v.setLayoutParams(lp);
    }

    private void tryCreateHwinfo(Hwinfo hwinfo) {
        String tag = getServerTag(hwinfo);
        final StandardReader reader = hwinfo.getReader();

        if (mTable.findViewWithTag(tag + CHRONOMETER_TAG) != null) {
            return;
        }


        {
            TableRow tr = new TableRow(this);

            {
                Button tv = new Button(this);
                tv.setText(hwinfo.getName());
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ServerDialog dFragment = new ServerDialog();
                        dFragment.setData(IpActivity.this, reader);
                        // Show DialogFragment
                        dFragment.show(fm, "Dialog Fragment");
                    }
                });
                tv.setSingleLine(true);
                tr.addView(tv);
                changeLayoutParams(tv, 2);
            }

            {
                Chronometer c = new Chronometer(this);
                c.setTag(tag + CHRONOMETER_TAG);
                c.start();
                c.setSingleLine(true);
                tr.addView(c);
                changeLayoutParams(c, 1);
            }

            for (int i = 0; i < 3; i++) {
                final Chronometer chrono = new Chronometer(this);
                chrono.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chrono.setBase(SystemClock.elapsedRealtime());
                        chrono.start();
                    }
                });
                chrono.setText("Tap");
                chrono.setSingleLine(true);
                tr.addView(chrono);
                changeLayoutParams(chrono, 1);
            }

            // TableRow.LayoutParams params = new TableRow.LayoutParams();
            //params.width = ViewGroup.LayoutParams.FILL_PARENT;
            //params.span = 4;
            //params.weight = 1;
            //tv.setLayoutParams(params);
            mTable.addView(tr);
        }

        for (Reading r : hwinfo.getReadings()) {
            TableRow tr = new TableRow(this);


            {
                HorizontalScrollView sv = new HorizontalScrollView(this);
                TextView tv = new TextView(this);
                tv.setText(r.getLabelUser());
                tv.setSingleLine(true);
                tv.setHorizontallyScrolling(true);
                sv.addView(tv);
                tr.addView(sv);
                changeLayoutParams(sv, 2);
            }

            {
                TextView tv = new TextView(this);
                tv.setTag(tag + r.getLabelOrig());
                tv.setText(r.format());
                tr.addView(tv);
                tv.setSingleLine(true);
                changeLayoutParams(tv, 1);
            }

            {
                TextView tv = new TextView(this);
                tv.setTag(tag + r.getLabelOrig() + " min");
                tv.setText(r.formatMin());
                tr.addView(tv);
                tv.setSingleLine(true);
                changeLayoutParams(tv, 1);
            }
            {
                TextView tv = new TextView(this);
                tv.setTag(tag + r.getLabelOrig() + " max");
                tv.setText(r.formatMax());
                tr.addView(tv);
                tv.setSingleLine(true);
                changeLayoutParams(tv, 1);
            }
            {
                TextView tv = new TextView(this);
                tv.setTag(tag + r.getLabelOrig() + " avg");
                tv.setText(r.formatAvg());
                tr.addView(tv);
                tv.setSingleLine(true);
                changeLayoutParams(tv, 1);
            }
            mTable.addView(tr);
        }

    }

    protected void mySetHwinfo(Hwinfo hwinfo) {

        tryCreateHwinfo(hwinfo);

        String tag = getServerTag(hwinfo);
        Chronometer c = (Chronometer) mTable.findViewWithTag(tag + CHRONOMETER_TAG);
        c.setBase(SystemClock.elapsedRealtime());

        for (Reading r : hwinfo.getReadings()) {
            TextView tv = (TextView) mTable.findViewWithTag(tag + r.getLabelOrig());

            if (tv == null) {
                // TODO: this happens if monitoring is enabled after connection
                continue;
            }

            tv.setText(r.format());

            tv = (TextView) mTable.findViewWithTag(tag + r.getLabelOrig() + " min");
            tv.setText(r.formatMin());

            tv = (TextView) mTable.findViewWithTag(tag + r.getLabelOrig() + " max");
            tv.setText(r.formatMax());

            tv = (TextView) mTable.findViewWithTag(tag + r.getLabelOrig() + " avg");
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
