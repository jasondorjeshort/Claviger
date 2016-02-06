package com.dorjesoft.hwinforeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


/**
 * Created by jdorje on 2/6/2016.
 */
public class ReaderTest {


    static boolean mRead = false;
    static Object mLock = new Object();


    public static void writeHwrc(OutputStream os) throws IOException {
        NetUtils.writeDword(os, "HWRC");
        NetUtils.writeDword(os, 2);
        for (int i = 0; i < 30; i++) {
            NetUtils.writeDword(os, "JDS ");
        }
        os.flush();
    }


    public static void readPacket(InputStream is) throws IOException {
        String packet = NetUtils.readDwordString(is);
        long cmd = NetUtils.readDwordInt(is);
        long len = NetUtils.readDwordInt(is);

        System.out.println(packet + " : " + cmd + " + " + len);

        if (packet.equals("HWRR")) {
            for (int i = 0; i < 30; i++) {
                NetUtils.readDwordInt(is);
            }
        } else if (packet.equals("HWRP")) {

            byte[] b = new byte[(int) len];

            NetUtils.forceRead(is, b);

            System.out.println("Read full hwrp of length " + len);

            String sig = NetUtils.scanDwordString(b, 0);
            long version = NetUtils.scanDwordInt(b, 4);
            long revision = NetUtils.scanDwordInt(b, 8);
            if (!sig.equals("SiWH") || version != 1 || revision != 0) {
                System.out.println("Sig: " + sig + "; version: " + version + "; revision: " + revision);
                throw new RuntimeException();
            }
            long timeStamp = NetUtils.scanLong(b, 12);
            System.out.println("Timestamp: " + timeStamp);
            int sensorOffset = (int) NetUtils.scanDwordInt(b, 20);
            int sensorSize = (int) NetUtils.scanDwordInt(b, 24);
            int sensorCount = (int) NetUtils.scanDwordInt(b, 28);
            System.out.println(
                    "Sensor section: " + sensorOffset + " offset, " + sensorSize + " size, " + sensorCount + " count.");

            Sensor[] sensors = new Sensor[sensorCount];
            for (int i = 0; i < sensorCount; i++) {
                sensors[i] = new Sensor(b, sensorOffset + i * sensorSize);
                System.out.println(sensors[i].toString());
            }

            int readingOffset = (int) NetUtils.scanDwordInt(b, 32);
            int readingSize = (int) NetUtils.scanDwordInt(b, 36);
            int readingCount = (int) NetUtils.scanDwordInt(b, 40);
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
