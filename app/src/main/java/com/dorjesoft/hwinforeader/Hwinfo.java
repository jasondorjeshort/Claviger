package com.dorjesoft.hwinforeader;

import android.util.Log;

/**
 * Created by jdorje on 2/6/2016.
 */
public class Hwinfo {
    public interface Callback {
        public void setHwinfo(Hwinfo hwinfo);
    }

    private final long mVersion, mRevision;
    private final int mSensorCount;
    private final Sensor[] mSensors;

    private final int mReadingCount;
    private final Reading[] mReadings;

    private final StandardReader mReader;

    // creates a hwinfo from a standard reader binary data
    public Hwinfo(StandardReader reader, byte[] b) {
        mReader = reader;

        String sig = NetUtils.scanDwordString(b, 0);
        mVersion = NetUtils.scanDwordInt(b, 4);
        mRevision = NetUtils.scanDwordInt(b, 8);
        if (!sig.equals("SiWH") || mVersion != 1 || mRevision != 0) {
            Log.d("hwinfo", "Sig: " + sig + "; version: " + mVersion + "; revision: " + mRevision);
            throw new RuntimeException();
        }
        long timeStamp = NetUtils.scanLong(b, 12);
        Log.d("hwinfo", "Timestamp: " + timeStamp);
        int sensorOffset = (int) NetUtils.scanDwordInt(b, 20);
        int sensorSize = (int) NetUtils.scanDwordInt(b, 24);
        mSensorCount = (int) NetUtils.scanDwordInt(b, 28);
        Log.d("hwinfo", "Sensor section: " + sensorOffset + " offset, "
                + sensorSize + " size, " + mSensorCount + " count.");

        mSensors = new Sensor[mSensorCount];
        for (int i = 0; i < mSensorCount; i++) {
            mSensors[i] = new Sensor(b, sensorOffset + i * sensorSize);
            Log.d("hwinfo", mSensors[i].toString());
        }

        int readingOffset = (int) NetUtils.scanDwordInt(b, 32);
        int readingSize = (int) NetUtils.scanDwordInt(b, 36);
        mReadingCount = (int) NetUtils.scanDwordInt(b, 40);
        Log.d("hwinfo", "Reading section: " + readingOffset + " offset, "
                + readingSize + " size, " + mReadingCount + " count.");
        mReadings = new Reading[mReadingCount];
        for (int i = 0; i < mReadingCount; i++) {
            mReadings[i] = new Reading(b, readingOffset + i * readingSize);
            Log.d("hwinfo", mReadings[i].toString());
        }
    }

    public Reading[] getReadings() {
        return mReadings;
    }

    public String getName() {
        return mReader.getName();
    }

}
