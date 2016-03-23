package util.su;

import java.io.Closeable;
import java.net.DatagramSocket;
import java.net.Socket;

import util.U;

public class Closer {
    public static void closeSilently(Object... xs) {
        // Note: on Android API levels prior to 19 Socket does not implement Closeable
        for (Object x : xs) {
            if (x != null) {
                try {
                    //CSService.d("closing: " + x);
                    if (x instanceof Closeable) {
                        ((Closeable)x).close();
                    } else if (x instanceof Socket) {
                        ((Socket)x).close();
                    } else if (x instanceof DatagramSocket) {
                        ((DatagramSocket)x).close();
                    } else {
                        U.d("cannot close: " + x);
                        throw new RuntimeException("cannot close "+x);
                    }
                } catch (Throwable e) {
                    U.e(e);
                }
            }
        }
    }
}