package makazas.imint.hr.meteorremote.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import makazas.imint.hr.meteorremote.util.Constants;

public class NetworkUtil {

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    public static boolean isValidLocalIpAddress(String ipAddress){
        if(ipAddress == null || ipAddress.isEmpty()) return false;

        String[] parts = ipAddress.split("\\.");

        //if the IP address doesn't have 4 parts, its not a valid IPv4 address.
        if(parts.length != 4) return false;

        if(!(parts[0].equals("192") && parts[1].equals("168"))){
            return false;
        }

        for(int i = 2; i < 4; i++){
            int num;
            try{
                //if a part is not a number, its not a valid IPv4 address.
                num = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e){
                return false;
            }

            //each part has to be in range [0, 255]
            if(num < 0 || num > 255){
                return false;
            }
        }

        return true;
    }

    public static boolean isValidPort(String port){
        if(port == null || port.isEmpty()) return false;

        int portNum;
        try{
            portNum = Integer.parseInt(port);
        } catch(NumberFormatException e){
            return false;
        }

        //port numbers in range [0, 1023] are reserved.
        return portNum >= 1024 && portNum <= 65535;
    }

    public static void logIfClosed(Socket socket, BufferedReader reader, BufferedWriter writer){
        boolean writerClosed = false;
        boolean readerClosed = false;

        try{
            writer.flush();
        }catch(IOException e){
            writerClosed = true;
        }

        try{
            readerClosed = !reader.ready();
        }catch(IOException e){
            readerClosed = true;
        }

        Log.d(Constants.LOG_TAG,
                "clientSocket is closed: " + socket.isClosed() +
                        ", writer is closed: " + writerClosed +
                        ", reader is closed: " + readerClosed
        );
    }

    public static boolean isDeviceOnline(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isDeviceConnectedToWifi(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return isDeviceOnline(context) && (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }
}
