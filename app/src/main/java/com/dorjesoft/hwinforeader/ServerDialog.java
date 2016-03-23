package com.dorjesoft.hwinforeader;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jdorje on 3/17/2016. Released under the GNU General Public Licence.
 */
public class ServerDialog extends DialogFragment {
    private TextView mName, mIp, mPort;
    private IpActivity mActivity;
    private StandardReader mReader;

    public void setData(IpActivity activity, StandardReader reader) {
        mActivity = activity;
        mReader = reader;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.server_fragment, container,
                false);

        Button mAddButton = (Button) rootView.findViewById(R.id.reader_button_add);
        Button mCancelButton = (Button) rootView.findViewById(R.id.reader_button_cancel);
        mName = (TextView) rootView.findViewById(R.id.reader_name);
        mIp = (TextView) rootView.findViewById(R.id.reader_ip);
        mPort = (TextView) rootView.findViewById(R.id.reader_port);

        if (mReader != null) {
            mName.setText(mReader.getName());
            mIp.setText(mReader.getIp());
            mPort.setText(mReader.getPort());
        }

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        getDialog().setTitle("Connect...");

        // Do something else
        return rootView;
    }

    protected void add() {
        Log.d("hwinfo", "IP: " + mIp.getText() + "; port: " + mPort.getText());

        String ip = mIp.getText().toString().trim();
        int port = Integer.valueOf(mPort.getText().toString().trim());
        String name = mName.getText().toString().trim();


        boolean dup = false;
        for (StandardReader r : mActivity.mReaders) {
            if (r.getIp().equals(ip) || r.getPort() == port) {
                dup = true;
                break;
            }
        }

        if (dup) {
            Snackbar.make(mActivity.findViewById(android.R.id.content), "Could not add duplicate listener.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            StandardReader r = new StandardReader(++mActivity.mMaxId, mActivity, name, ip, port);
            Snackbar.make(mActivity.findViewById(android.R.id.content), "Added server listener.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            mActivity.mReaders.add(r);
            r.resume();
        }
        dismiss();
    }

    protected void cancel() {
        dismiss();
    }


}
