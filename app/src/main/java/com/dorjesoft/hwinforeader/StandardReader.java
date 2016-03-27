package com.dorjesoft.hwinforeader;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by jdorje on 2/6/2016. Released under the GNU General Public Licence.
 */
public class StandardReader {
    private final Hwinfo.Callback mCallback;
    private String mIp;
    private int mPort;
    private Thread mThread;
    private boolean mRunning = false;
    private final int mId;
    private String mName;

    public String getIp() {
        return mIp;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public int getPort() {
        return mPort;
    }

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

        switch (packet) {
            case "HWRR":
                for (int i = 0; i < 30; i++) {
                    NetUtils.readDwordInt(is);
                }
                break;
            case "HWRP":
                byte[] b = new byte[(int) len];

                NetUtils.forceRead(is, b);

                Log.d("hwinfo", "Read full hwrp of length " + len);

                Hwinfo hwinfo = new Hwinfo(this, b);

                mCallback.setHwinfo(hwinfo);

                return true;
            default:
                Log.d("hwinfo", "Unknown packet " + packet);
                throw new RuntimeException();
        }

        return false;
    }

    public void resume() {
        mRunning = true;
        mThread = new Thread() {
            @Override
            public void run() {
                /*
                 * The loop re-uses the connection but if the connection is broken it stays
                 * within the outer loop.
                 */
                while (isRunning()) {
                    Log.d("hwinfo", "Hwinfo connecting.");
                    try (Socket s = new Socket(mIp, mPort)) {
                        OutputStream os = s.getOutputStream();
                        InputStream is = s.getInputStream();
                        Log.d("hwinfo", "Hwinfo connected.");

                        while (isRunning()) {
                            Log.d("hwinfo", "Hwinfo requesting packet.");
                            writeHwrc(os);
                            while (!readPacket(is)) {

                            }
                            try {
                                Thread.sleep(10 * 1000);
                            } catch (InterruptedException e) {
                                // nothing/expected but probably breaks
                            }
                            Log.d("hwinfo", "Hwinfo continuance.");

                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                Log.d("hwinfo", "Hwinfo thread done.");
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

    public void setName(String name) {
        mName = name;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public void setIp(String ip) {
        mIp = ip;
    }

    public StandardReader(int id, Hwinfo.Callback cb, String name, String ip, int port) {
        mName = name;
        mId = id;
        mCallback = cb;
        mIp = ip;
        mPort = port;
        mRunning = true;
    }
}
