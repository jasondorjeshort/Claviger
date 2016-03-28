package com.dorjesoft.claviger;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jdorje on 2/6/2016.
 */
public class NetUtils {

    public static char hex1(int b) {
        if (b < 0 || b >= 16) {
            return '-';
        }
        char table[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        // Log.d("hwinfo", "Hex of " + b + " is " + table[b]);
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

        Log.d("hwinfo", "> " + wordo + " > " + hex(b));
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

        Log.d("hwinfo", "> " + word + " > " + hex(b));
        os.write(b);
    }

    public static String scanDwordString(byte[] b, int offset) {

        String word = "";
        for (int i = 0; i < 4; i++) {
            word += ((char) b[offset + 3 - i]);
        }

        // Log.d("hwinfo", "< " + word + " < " + hex(b));

        return word;
    }

    public static long scanDwordInt(byte[] b, int offset) {
        long word = 0;
        for (int i = 0; i < 4; i++) {
            int k = b[offset + 3 - i];
            word = word * 256 + (k & 0xFF);
        }

        // Log.d("hwinfo", "< " + word + " < " + hex(b));

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

        Log.d("hwinfo", "Long: " + l1 + "_" + l2 + " = ");

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
		 * String hex = hex(b); Log.d("hwinfo", "> " + "." + " > " + str +
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
}
