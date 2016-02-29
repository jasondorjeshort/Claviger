package com.dorjesoft.hwinforeader;

/**
 * Created by jdorje on 2/6/2016.
 */
public class Reading {
    final Type type;

    ;
    final int sensorIndex;
    final int readingId;
    final String labelOrig;
    final String labelUser;
    final String units;
    final double value, vMin, vMax, vAvg;

    // creates a reading from a standard reader binary data
    Reading(byte[] b, int offset) {
        type = Type.fromInt(NetUtils.scanDwordInt(b, offset + 0));
        sensorIndex = (int) NetUtils.scanDwordInt(b, offset + 4);
        readingId = (int) NetUtils.scanDwordInt(b, offset + 8);
        labelOrig = NetUtils.scanString(b, offset + 12, 128);
        labelUser = NetUtils.scanString(b, offset + 140, 128);
        units = NetUtils.scanString(b, offset + 268, 16);
        value = NetUtils.scanDouble(b, offset + 284);
        vMin = NetUtils.scanDouble(b, offset + 292);
        vMax = NetUtils.scanDouble(b, offset + 300);
        vAvg = NetUtils.scanDouble(b, offset + 308);
    }

    @Override
    public String toString() {
        return "Reading: " + type.toString() + "|" + sensorIndex + "|" + readingId + "|" + labelOrig + "|"
                + labelUser + "|" + units + "|" + value + "|" + vMin + "|" + vMax + "|" + vAvg;
    }

    static enum Type {
        SENSOR_TYPE_NONE(0), SENSOR_TYPE_TEMP(1), SENSOR_TYPE_VOLT(2), SENSOR_TYPE_FAN(3), SENSOR_TYPE_CURRENT(
                4), SENSOR_TYPE_POWER(5), SENSOR_TYPE_CLOCK(6), SENSOR_TYPE_USAGE(7), SENSOR_TYPE_OTHER(8);

        final int c;

        Type(int constant) {
            c = constant;
        }

        static Type fromInt(long constant) {
            for (Type t : Type.values()) {
                if (t.c == constant) {
                    return t;
                }
            }
            return SENSOR_TYPE_OTHER;
        }
    }
}
