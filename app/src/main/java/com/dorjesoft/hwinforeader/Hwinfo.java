package com.dorjesoft.hwinforeader;

/**
 * Created by jdorje on 2/6/2016.
 */
public class Hwinfo {


    public interface Callback {
        public void setHwinfo(Hwinfo hwinfo);
    }

    final long version, revision;
    final int sensorCount;
    final Sensor[] sensors;

    final int readingCount;
    final Reading[] readings;

    final StandardReader mReader;

    // creates a hwinfo from a standard reader binary data
    public Hwinfo(StandardReader reader, byte[] b) {
        mReader = reader;

        String sig = NetUtils.scanDwordString(b, 0);
        version = NetUtils.scanDwordInt(b, 4);
        revision = NetUtils.scanDwordInt(b, 8);
        if (!sig.equals("SiWH") || version != 1 || revision != 0) {
            System.out.println("Sig: " + sig + "; version: " + version + "; revision: " + revision);
            throw new RuntimeException();
        }
        long timeStamp = NetUtils.scanLong(b, 12);
        System.out.println("Timestamp: " + timeStamp);
        int sensorOffset = (int) NetUtils.scanDwordInt(b, 20);
        int sensorSize = (int) NetUtils.scanDwordInt(b, 24);
        sensorCount = (int) NetUtils.scanDwordInt(b, 28);
        System.out.println(
                "Sensor section: " + sensorOffset + " offset, " + sensorSize + " size, " + sensorCount + " count.");

        sensors = new Sensor[sensorCount];
        for (int i = 0; i < sensorCount; i++) {
            sensors[i] = new Sensor(b, sensorOffset + i * sensorSize);
            System.out.println(sensors[i].toString());
        }

        int readingOffset = (int) NetUtils.scanDwordInt(b, 32);
        int readingSize = (int) NetUtils.scanDwordInt(b, 36);
        readingCount = (int) NetUtils.scanDwordInt(b, 40);
        System.out.println("Reading section: " + readingOffset + " offset, " + readingSize + " size, "
                + readingCount + " count.");
        readings = new Reading[readingCount];
        for (int i = 0; i < readingCount; i++) {
            readings[i] = new Reading(b, readingOffset + i * readingSize);
            System.out.println(readings[i].toString());
        }
    }

}
