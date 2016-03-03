package com.dorjesoft.hwinforeader;

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
    String IP = "192.168.1.100";
    int PORT = 27007;

    public final List<StandardReader> readers = new LinkedList<StandardReader>();

    private TableLayout table;

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

        readers.add(new StandardReader(readers.size(), this, IP, PORT));

        table = (TableLayout) findViewById(R.id.table);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("hwinfo", "Hwinfo resume.");

        for (StandardReader r : readers) {
            r.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("hwinfo", "Hwinfo pause.");

        for (StandardReader r : readers) {
            r.pause();
        }
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

        for (Reading r : hwinfo.readings) {
            TableRow tr = new TableRow(this);

            TextView tv = new TextView(this);
            tv.setText(r.getLabel());
            tr.addView(tv);

            tv = new TextView(this);
            tv.setTag(r.mLabelOrig);
            tv.setText(r.format());
            tr.addView(tv);

            table.addView(tr);
        }

    }

    protected void mySetHwinfo(Hwinfo hwinfo) {
        for (Reading r : hwinfo.readings) {
            TextView tv = (TextView) table.findViewWithTag(r.mLabelOrig);

            if (tv == null) {
                createHwinfo(hwinfo);
                tv = (TextView) table.findViewWithTag(r.mLabelOrig);
            }

            tv.setText(r.format());
        }
    }

    @Override
    public void setHwinfo(final Hwinfo hwinfo) {
        table.post(new Runnable() {
            @Override
            public void run() {
                mySetHwinfo(hwinfo);
            }
        });
    }
}
