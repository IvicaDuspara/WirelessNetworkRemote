package makazas.imint.hr.meteorremote.networking;

import android.os.NetworkOnMainThreadException;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import makazas.imint.hr.meteorremote.util.Constants;

/**
 * A thread-safe singleton that provides a unique instance of a {@link Socket}.<br>
 */
public class SocketSingleton {

    private Socket socket;
    private static SocketSingleton instance;

    private SocketSingleton(){}

    public static synchronized SocketSingleton getInstance(){
        if(instance == null){
            instance = new SocketSingleton();
        }
        return instance;
    }

    /**
     * @return Underlying socket if it was initialized.
     * @throws IllegalStateException    if the socket was not yet initialized.<br>
     *                                  Use {@link SocketSingleton#initializeSocket(String, String)} beforehand.
     */
    public synchronized Socket getSocket() throws IllegalStateException {
        if(socket == null){
            throw new IllegalStateException("Socket wasn't initialized.");
        }
        return socket;
    }

    /**
     * Tries to initialize the underlying socket.
     *
     * @param ipAddress string representation of IP address the socket should connect to.
     * @param port      string representation of port number the socket should connect to.
     * @return          <code>true</code> if the socket was initialized successfully or if it was previously initialized.<br>
     *                  <code>false</code> if the socket was not initialized successfully, indicating the IP address
     *                  or port number are invalid.
     */
    public synchronized boolean initializeSocket(String ipAddress, String port){
        if(socket == null) {
            try {
                socket = new Socket(InetAddress.getByName(ipAddress), Integer.parseInt(port));
            } catch (Exception e) {
                Log.d(Constants.LOG_TAG, "Socket wasn't initialized");
                return false;
            }
        }

        Log.d(Constants.LOG_TAG, "Socket is null: " + (socket == null));

        //if no exceptions were thrown during construction, the socket is initialized successfully.
        return true;
    }

    /**
     * Closes underlying socket and sets its reference to <code>null</code> so it can be initialized again.
     */
    public synchronized void closeSocket(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
    }
}
