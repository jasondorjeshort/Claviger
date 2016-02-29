package com.dorjesoft.hwinforeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


/**
 * Created by jdorje on 2/6/2016.
 */
public class StandardReader {


    static boolean mRead = false;
    static Object mLock = new Object();


    public  void writeHwrc(OutputStream os) throws IOException {
        NetUtils.writeDword(os, "HWRC");
        NetUtils.writeDword(os, 2);
        for (int i = 0; i < 30; i++) {
            NetUtils.writeDword(os, "JDS ");
        }
        os.flush();
    }


    public  void readPacket(InputStream is) throws IOException {
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

            mCallback.setHwinfo(hwinfo);
        } else

        {
            System.out.println("Unknown packet " + packet);
            throw new RuntimeException();
        }

    }

    final Hwinfo.Callback mCallback;
    final String mIp;
    final int mPort;

    public StandardReader(Hwinfo.Callback cb, String ip, int port) {
        mCallback = cb;
        mIp = ip;
        mPort = port;

        new Thread() {
            @Override
            public void run() {
                try (Socket s = new Socket(mIp, mPort)) {

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
        };
    }


}
