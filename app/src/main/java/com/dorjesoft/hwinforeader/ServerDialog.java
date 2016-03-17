package com.dorjesoft.hwinforeader;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by jdorj on 3/17/2016.
 */
public class ServerDialog extends DialogFragment {

    private List<StandardReader> mReaders;

    public void setReaders(List<StandardReader> readers) {
        mReaders = readers;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.server_fragment, container,
                false);
        getDialog().setTitle("Connect...");
        // Do something else
        return rootView;
    }


}
