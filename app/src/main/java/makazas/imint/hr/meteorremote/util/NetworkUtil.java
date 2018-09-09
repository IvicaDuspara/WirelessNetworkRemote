package makazas.imint.hr.meteorremote.util;

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

    public static boolean isValidIpAddress(String ipAddress){
        if(ipAddress == null || ipAddress.isEmpty()) return false;

        String[] parts = ipAddress.split("\\.");

        //if the IP address doesn't have 4 parts, its not a valid IPv4 address.
        if(parts.length != 4) return false;

        for(String part: parts){
            int num;
            try{
                //if a part is not a number, its not a valid IPv4 address.
                num = Integer.parseInt(part);
            } catch(NumberFormatException e){
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
}
