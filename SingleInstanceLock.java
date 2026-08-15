package service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class SingleInstanceLock {
    private static final int PORT = 49152;
    private static ServerSocket lockSocket;

    public static boolean acquireLock() {
        try {
            lockSocket = new ServerSocket(PORT, 10, InetAddress.getByName("127.0.0.1"));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void releaseLock() {
        if (lockSocket != null && !lockSocket.isClosed()) {
            try {
                lockSocket.close();
            } catch (IOException ignored) {}
        }
    }
}
