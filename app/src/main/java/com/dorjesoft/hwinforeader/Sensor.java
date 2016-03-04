package com.dorjesoft.hwinforeader;

/**
 * Created by jdorje on 2/6/2016.
 */
public class Sensor {

    private final long id;
    private final int instance;
    private final String nameOrig;
    private final String nameUser;

    // creates a sensor from a standard reader binary data
    Sensor(byte[] b, int offset) {
        id = NetUtils.scanDwordInt(b, offset + 0);
        instance = (int) NetUtils.scanDwordInt(b, offset + 4);
        nameOrig = NetUtils.scanString(b, offset + 8, 128);
        nameUser = NetUtils.scanString(b, offset + 136, 128);
    }

    @Override
    public String toString() {
        return "Sensor: " + id + "|" + instance + "|" + nameOrig + "|" + nameUser;
    }
}
