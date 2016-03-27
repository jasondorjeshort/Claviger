package com.dorjesoft.hwinforeader;

import android.util.Log;

/**
 * Created by jdorje on 2/6/2016.
 */
public class Hwinfo {
    public interface Callback {
        void setHwinfo(Hwinfo hwinfo);
    }

    private final Reading[] mReadings;
    private final StandardReader mReader;

    // creates a hwinfo from a standard reader binary data
    public Hwinfo(StandardReader reader, byte[] b) {
        mReader = reader;

        String sig = NetUtils.scanDwordString(b, 0);
        long version = NetUtils.scanDwordInt(b, 4);
        long revision = NetUtils.scanDwordInt(b, 8);
        if (!sig.equals("SiWH") || version != 1 || revision != 0) {
            Log.d("hwinfo", "Sig: " + sig + "; version: " + version + "; revision: " + revision);
            throw new RuntimeException();
        }
        long timeStamp = NetUtils.scanLong(b, 12);
        Log.d("hwinfo", "Timestamp: " + timeStamp);
        int sensorOffset = (int) NetUtils.scanDwordInt(b, 20);
        int sensorSize = (int) NetUtils.scanDwordInt(b, 24);
        int sensorCount = (int) NetUtils.scanDwordInt(b, 28);
        Log.d("hwinfo", "Sensor section: " + sensorOffset + " offset, "
                + sensorSize + " size, " + sensorCount + " count.");

        Sensor[] sensors = new Sensor[sensorCount];
        for (int i = 0; i < sensorCount; i++) {
            sensors[i] = new Sensor(b, sensorOffset + i * sensorSize);
            Log.d("hwinfo", sensors[i].toString());
        }

        int readingOffset = (int) NetUtils.scanDwordInt(b, 32);
        int readingSize = (int) NetUtils.scanDwordInt(b, 36);
        int readingCount = (int) NetUtils.scanDwordInt(b, 40);
        Log.d("hwinfo", "Reading section: " + readingOffset + " offset, "
                + readingSize + " size, " + readingCount + " count.");
        mReadings = new Reading[readingCount];
        for (int i = 0; i < readingCount; i++) {
            mReadings[i] = new Reading(b, readingOffset + i * readingSize);
            Log.d("hwinfo", mReadings[i].toString());
        }
    }

    public StandardReader getReader() {
        return mReader;
    }

    public Reading[] getReadings() {
        return mReadings;
    }

    public String getName() {
        return mReader.getName();
    }

}
