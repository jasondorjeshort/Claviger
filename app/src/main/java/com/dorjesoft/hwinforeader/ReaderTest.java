package com.dorjesoft.hwinforeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by jdorj on 2/6/2016.
 */
public class ReaderTest {


        public static char hex1(int b) {
            if (b < 0 || b >= 16) {
                return '-';
            }
            char table[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
            // System.out.println("Hex of " + b + " is " + table[b]);
            return table[b];
        }

        public static String hex(byte[] buf, int offset, int len) {
            String s = "";

            for (int i = offset; i < offset + len; i++) {
                int k = (int) buf[i] & 0xFF;
                s += hex1(k / 16);
                s += hex1(k % 16);
                s += ' ';
            }

            return s;
        }

        public static String hex(byte[] buf) {
            return hex(buf, 0, buf.length);
        }

        static boolean mRead = false;
        static Object mLock = new Object();

        public static void forceRead(InputStream is, byte[] b) throws IOException {
            int read = 0;
            while (read < b.length) {
                int r = is.read(b, read, b.length - read);
                if (r < 0)
                    throw new IOException();

                read += r;
            }
        }

        public static void writeDword(OutputStream os, long wordo) throws IOException {
            if (wordo < 0) {
                throw new RuntimeException();
            }
            byte[] b = new byte[4];
            long w = wordo;
            for (int i = 0; i < 4; i++) {
                b[i] = (byte) (w % 256);
                w /= 256;
            }

            if (w != 0) {
                throw new RuntimeException();
            }

            System.out.println("> " + wordo + " > " + hex(b));
            os.write(b);
        }

        public static void writeDword(OutputStream os, String word) throws IOException {
            if (word.length() != 4) {
                throw new RuntimeException();
            }
            byte[] b = new byte[4];
            for (int i = 0; i < 4; i++) {
                b[3 - i] = (byte) word.charAt(i);

            }

            System.out.println("> " + word + " > " + hex(b));
            os.write(b);
        }

        public static void writeHwrc(OutputStream os) throws IOException {
            writeDword(os, "HWRC");
            writeDword(os, 2);
            for (int i = 0; i < 30; i++) {
                writeDword(os, "JDS ");
            }
            os.flush();
        }

        public static String scanDwordString(byte[] b, int offset) {

            String word = "";
            for (int i = 0; i < 4; i++) {
                word += ((char) b[offset + 3 - i]);
            }

            // System.out.println("< " + word + " < " + hex(b));

            return word;
        }

        public static long scanDwordInt(byte[] b, int offset) {
            long word = 0;
            for (int i = 0; i < 4; i++) {
                int k = b[offset + 3 - i];
                word = word * 256 + (k & 0xFF);
            }

            // System.out.println("< " + word + " < " + hex(b));

            return word;
        }

        public static String toBinary(long l) {
            String binary = "";

            for (int i = 0; i < 64; i++) {
                long b = (l >> i) & 1;
                binary += ((b > 0) ? '1' : '0');
            }

            return binary;
        }

        public static long scanLong(byte[] b, int offset) {
            long l1 = scanDwordInt(b, offset);
            long l2 = scanDwordInt(b, offset + 4);

            System.out.println("Long: " + l1 + "_" + l2 + " = ");

            return (l1 << 32) + l2;
        }

        public static double scanDouble(byte[] b, int offset) {
		/*
		 * int[] binary = new int[64]; String str = "";
		 *
		 * byte[] b = new byte[8];
		 *
		 * for(int i = 0; i < 8; i++) { b[i] = bOrig[i + off]; }
		 *
		 *
		 *
		 * for (int i = 0; i < 64; i++) { int b_ = b[i / 8] & 0xFF;
		 *
		 * b_ &= (1 << (7 - (i % 8))); str += (b_ == 0) ? "0" : "1"; binary[i] =
		 * b_;
		 *
		 * if (i % 8 == 7) { str += " "; } }
		 *
		 * String hex = hex(b); System.out.println("> " + "." + " > " + str +
		 * " > " + hex);
		 */

            long l1 = scanDwordInt(b, offset);
            long l2 = scanDwordInt(b, offset + 4);
            return Double.longBitsToDouble((l2 << 32) + l1);
        }

        public static String scanString(byte[] b, int offset, int maxLen) {
            String s = "";
            for (int i = 0; i < maxLen; i++) {
                if (b[offset + i] == 0) {
                    break;
                }
                s += (char) b[offset + i];
            }
            return s;
        }

        public static String readDwordString(InputStream is) throws IOException {
            byte[] b = new byte[4];
            forceRead(is, b);
            return scanDwordString(b, 0);
        }

        public static long readDwordInt(InputStream is) throws IOException {
            byte[] b = new byte[4];
            forceRead(is, b);
            return scanDwordInt(b, 0);
        }

        public static class Sensor {
            final long id;
            final int instance;
            final String nameOrig;
            final String nameUser;

            Sensor(byte[] b, int offset) {
                id = scanDwordInt(b, offset + 0);
                instance = (int) scanDwordInt(b, offset + 4);
                nameOrig = scanString(b, offset + 8, 128);
                nameUser = scanString(b, offset + 136, 128);
            }

            @Override
            public String toString() {
                return "Sensor: " + id + "|" + instance + "|" + nameOrig + "|" + nameUser;
            }
        }

        public static class Reading {

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
            };

            final Type type;
            final int sensorIndex;
            final int readingId;
            final String labelOrig;
            final String labelUser;
            final String units;
            final double value, vMin, vMax, vAvg;

            Reading(byte[] b, int offset) {
                type = Type.fromInt(scanDwordInt(b, offset + 0));
                sensorIndex = (int) scanDwordInt(b, offset + 4);
                readingId = (int) scanDwordInt(b, offset + 8);
                labelOrig = scanString(b, offset + 12, 128);
                labelUser = scanString(b, offset + 140, 128);
                units = scanString(b, offset + 268, 16);
                value = scanDouble(b, offset + 284);
                vMin = scanDouble(b, offset + 292);
                vMax = scanDouble(b, offset + 300);
                vAvg = scanDouble(b, offset + 308);
            }

            @Override
            public String toString() {
                return "Reading: " + type.toString() + "|" + sensorIndex + "|" + readingId + "|" + labelOrig + "|"
                        + labelUser + "|" + units + "|" + value + "|" + vMin + "|" + vMax + "|" + vAvg;
            }
        }

        public static void readPacket(InputStream is) throws IOException {
            String packet = readDwordString(is);
            long cmd = readDwordInt(is);
            long len = readDwordInt(is);

            System.out.println(packet + " : " + cmd + " + " + len);

            if (packet.equals("HWRR")) {
                for (int i = 0; i < 30; i++) {
                    readDwordInt(is);
                }
            } else if (packet.equals("HWRP")) {

                byte[] b = new byte[(int) len];

                forceRead(is, b);

                System.out.println("Read full hwrp of length " + len);

                String sig = scanDwordString(b, 0);
                long version = scanDwordInt(b, 4);
                long revision = scanDwordInt(b, 8);
                if (!sig.equals("SiWH") || version != 1 || revision != 0) {
                    System.out.println("Sig: " + sig + "; version: " + version + "; revision: " + revision);
                    throw new RuntimeException();
                }
                long timeStamp = scanLong(b, 12);
                System.out.println("Timestamp: " + timeStamp);
                int sensorOffset = (int) scanDwordInt(b, 20);
                int sensorSize = (int) scanDwordInt(b, 24);
                int sensorCount = (int) scanDwordInt(b, 28);
                System.out.println(
                        "Sensor section: " + sensorOffset + " offset, " + sensorSize + " size, " + sensorCount + " count.");

                Sensor[] sensors = new Sensor[sensorCount];
                for (int i = 0; i < sensorCount; i++) {
                    sensors[i] = new Sensor(b, sensorOffset + i * sensorSize);
                    System.out.println(sensors[i].toString());
                }

                int readingOffset = (int) scanDwordInt(b, 32);
                int readingSize = (int) scanDwordInt(b, 36);
                int readingCount = (int) scanDwordInt(b, 40);
                System.out.println("Reading section: " + readingOffset + " offset, " + readingSize + " size, "
                        + readingCount + " count.");
                Reading[] readings = new Reading[readingCount];
                for (int i = 0; i < readingCount; i++) {
                    readings[i] = new Reading(b, readingOffset + i * readingSize);
                    System.out.println(readings[i].toString());
                }

                System.out.println("Reading one more...");
                is.read(b, 0, 1);
                System.out.println("Read one more...");
                System.exit(0);
            } else

            {
                System.out.println("Unknown packet " + packet);
                throw new RuntimeException();
            }

        }

        public static void read() {

            new Thread() {
                public void run() {
                    while (true) {
                        try {
                            sleep(10 * 1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        boolean r;
                        synchronized (mLock) {
                            r = mRead;
                            mRead = false;
                        }

                        System.out.println("Exiting due to time.");
                        System.exit(0);
                    }
                }
            }.start();

            try (Socket s = new Socket("192.168.1.101", 27007)) {

                System.out.println("connected?");

                writeHwrc(s.getOutputStream());
                while (true) {
                    readPacket(s.getInputStream());
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("closed");

        }



}
