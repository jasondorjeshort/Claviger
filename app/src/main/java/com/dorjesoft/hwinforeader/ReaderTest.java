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

            Hwinfo hwinfo = new Hwinfo(b);

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

        try (Socket s = new Socket("192.168.1.100", 27007)) {

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
