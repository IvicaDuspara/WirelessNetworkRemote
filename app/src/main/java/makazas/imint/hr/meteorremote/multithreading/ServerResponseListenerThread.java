package makazas.imint.hr.meteorremote.multithreading;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;

import makazas.imint.hr.meteorremote.util.Constants;

public class ServerResponseListenerThread extends ObservableThread {

    private BufferedReader bufferedReader;

    private volatile boolean isRunning;

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void setBufferedReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    @Override
    public void run() {
        setRunning(true);
        while (isRunning) {
            if (bufferedReader != null) {
                try {
                    String serverResponse;
                    try {
                        serverResponse = connectAllLinesFromBufferedReader();
                    } catch (NullPointerException e) {
                        //when internet connection is lost, but reader isn't closed yet
                        //and reads null, just continue until the thread gets shut down
                        //(which is very soon because as soon as the client disconnects, this thread
                        //is set to stop)
                        Log.d(Constants.LOG_TAG, "connectAllLines threw null");

                        continue;
                    }

                    Log.d(Constants.LOG_TAG, serverResponse);
                    notifyObserversOfChange(serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(Constants.LOG_TAG, "listening thread stopped");
    }

    private String connectAllLinesFromBufferedReader() throws IOException, NullPointerException {
        // TODO: 21-Jun-19 just fuckin put it in a string array to begin with you dumbo 

        StringBuilder everything = new StringBuilder();
        String line;

        while (!(line = bufferedReader.readLine()).equals(Constants.SERVER_BROADCAST_ENDED_CODE)) {
            everything.append(line).append(Constants.SERVER_RESPONSE_SEPARATOR);
        }

        String allLines = everything.toString();

        //removes trailing \n
        return allLines.substring(0, allLines.length() - 1);
    }
}
