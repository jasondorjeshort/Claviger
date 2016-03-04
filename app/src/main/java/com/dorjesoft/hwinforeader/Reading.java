package com.dorjesoft.hwinforeader;

/**
 * Created by jdorje on 2/6/2016.
 */
public class Reading {
    private final Type mType;
    private final int mSensorIndex;
    private final int mReadingId;
    private final String mLabelOrig;
    private final String mLabelUser;
    private final String mUnits;
    private final double mValue, mMin, mMax, mAvg;

    // creates a reading from a standard reader binary data
    public Reading(byte[] b, int offset) {
        mType = Type.fromInt(NetUtils.scanDwordInt(b, offset + 0));
        mSensorIndex = (int) NetUtils.scanDwordInt(b, offset + 4);
        mReadingId = (int) NetUtils.scanDwordInt(b, offset + 8);
        mLabelOrig = NetUtils.scanString(b, offset + 12, 128);
        mLabelUser = NetUtils.scanString(b, offset + 140, 128);
        String units = NetUtils.scanString(b, offset + 268, 16);
        units = units.replace('ﾰ', '°');
        mUnits = units;
        mValue = NetUtils.scanDouble(b, offset + 284);
        mMin = NetUtils.scanDouble(b, offset + 292);
        mMax = NetUtils.scanDouble(b, offset + 300);
        mAvg = NetUtils.scanDouble(b, offset + 308);
    }

    @Override
    public String toString() {
        return "Reading: " + mType.toString() + "|" + mSensorIndex + "|" + mReadingId + "|" + mLabelOrig + "|"
                + mLabelUser + "|" + mUnits + "|" + mValue + "|" + mMin + "|" + mMax + "|" + mAvg;
    }

    private static enum Type {
        SENSOR_TYPE_NONE(0), SENSOR_TYPE_TEMP(1), SENSOR_TYPE_VOLT(2), SENSOR_TYPE_FAN(3),
        SENSOR_TYPE_CURRENT(4), SENSOR_TYPE_POWER(5), SENSOR_TYPE_CLOCK(6),
        SENSOR_TYPE_USAGE(7), SENSOR_TYPE_OTHER(8);

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

    public String format3(double value) {
        return String.valueOf(Math.round(value * 1000) / 1000f);
    }

    public String format(double value) {
        switch (mType) {
            case SENSOR_TYPE_TEMP:
                return String.format("%3.1f%s", Math.round(value * 10) / 10f, mUnits);
            case SENSOR_TYPE_VOLT:
                return String.format("%3.3f%s", Math.round(value * 1000) / 1000f, mUnits);
            case SENSOR_TYPE_FAN:
                return String.format("%d%s", Math.round(value), mUnits);
            case SENSOR_TYPE_CURRENT:
                return String.format("%3.3f%s", Math.round(value * 1000) / 1000f, mUnits);
            case SENSOR_TYPE_POWER:
                return String.format("%3.3f%s", Math.round(value * 1000) / 1000f, mUnits);
            case SENSOR_TYPE_CLOCK:
                return String.format("%3.1f%s", Math.round(value * 10) / 10f, mUnits);
            case SENSOR_TYPE_USAGE:
                return String.format("%3.3f%s", Math.round(value * 1000) / 1000f, mUnits);
            case SENSOR_TYPE_OTHER:
                return String.format("%3.1f%s", Math.round(value * 10) / 10f, mUnits);
            case SENSOR_TYPE_NONE:
            default:
                break;
        }

        return "-";
    }

    public String format() {
        return format(mValue);
    }

    public String formatMin() {
        return format(mMin);
    }

    public String formatMax() {
        return format(mMax);
    }

    public String formatAvg() {
        return format(mAvg);
    }

    public String getLabelUser() {
        return mLabelUser;
    }

    public String getLabelOrig() {
        return mLabelOrig;
    }
}
