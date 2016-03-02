package com.dorjesoft.hwinforeader;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by jdorje on 2/6/2016.
 */
public class StandardReader {
    public void writeHwrc(OutputStream os) throws IOException {
        NetUtils.writeDword(os, "HWRC");
        NetUtils.writeDword(os, 2);
        for (int i = 0; i < 30; i++) {
            NetUtils.writeDword(os, "JDS ");
        }
        os.flush();
    }

    public boolean readPacket(InputStream is) throws IOException {
        Log.d("hwinfo", "Reading packet.");
        String packet = NetUtils.readDwordString(is);
        long cmd = NetUtils.readDwordInt(is);
        long len = NetUtils.readDwordInt(is);

        Log.d("hwinfo", packet + " : " + cmd + " + " + len);

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

            return true;
        } else

        {
            System.out.println("Unknown packet " + packet);
            throw new RuntimeException();
        }

        return false;
    }

    private final Hwinfo.Callback mCallback;
    private final String mIp;
    private final int mPort;
    private Thread mThread;
    private boolean mRunning = false;

    public void resume() {
        mRunning = true;
        mThread = new Thread() {
            @Override
            public void run() {
                while (isRunning()) {
                    Log.d("hwinfo", "Hwinfo connecting.");
                    try (Socket s = new Socket(mIp, mPort)) {

                        Log.d("hwinfo", "Hwinfo connected.");

                        writeHwrc(s.getOutputStream());
                        InputStream is = s.getInputStream();
                        while (!readPacket(is)) {

                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Log.d("hwinfo", "Hwinfo closed.");

                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        // nothing/expected but probably breaks
                    }

                    Log.d("hwinfo", "Hwinfo continuance.");
                }
            }
        };

        mThread.start();
    }

    public boolean isRunning() {
        synchronized (this) {
            return mRunning;
        }
    }

    public void pause() {
        synchronized (this) {
            mRunning = false;
        }
        mThread.interrupt();
    }

    public StandardReader(Hwinfo.Callback cb, String ip, int port) {
        mCallback = cb;
        mIp = ip;
        mPort = port;
        mRunning = true;
    }
}
