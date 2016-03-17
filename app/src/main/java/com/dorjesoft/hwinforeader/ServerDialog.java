package com.dorjesoft.hwinforeader;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jdorj on 3/17/2016.
 */
public class ServerDialog extends DialogFragment {

    private Button mAddButton, mCancelButton;
    private TextView mName, mIp, mPort;

    private List<StandardReader> mReaders;
    private IpActivity mActivity;

    public void setReaders(IpActivity activity, List<StandardReader> readers) {
        mActivity = activity;
        mReaders = readers;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.server_fragment, container,
                false);

        mAddButton = (Button) rootView.findViewById(R.id.reader_button_add);
        mCancelButton = (Button) rootView.findViewById(R.id.reader_button_cancel);
        mName = (TextView) rootView.findViewById(R.id.reader_name);
        mIp = (TextView) rootView.findViewById(R.id.reader_ip);
        mPort = (TextView) rootView.findViewById(R.id.reader_port);

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

        String ip = mIp.getText().toString();
        int port = Integer.valueOf(mPort.getText().toString()).intValue();

        StandardReader r = new StandardReader(1000, mActivity, ip, port);
        mReaders.add(r);
        r.resume();
        dismiss();
    }

    protected void cancel() {
        dismiss();
    }


}
