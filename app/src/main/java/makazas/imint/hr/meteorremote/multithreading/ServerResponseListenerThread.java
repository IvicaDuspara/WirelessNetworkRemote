package makazas.imint.hr.meteorremote.multithreading;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        Log.d(Constants.LOG_TAG, "listener thread in run, reader null:" + (bufferedReader == null));

        setRunning(true);
        while (isRunning) {
            if (bufferedReader != null) {
                Log.d(Constants.LOG_TAG, "made it past check");
                try {
                    List<String> serverResponse;
                    try {
                        serverResponse = responseToList();
                        Log.d(Constants.LOG_TAG, serverResponse.toString());
                    } catch (NullPointerException e) {
                        //when internet connection is lost, but reader isn't closed yet
                        //and reads null, just continue until the thread gets shut down
                        //(which is very soon because as soon as the client disconnects, this thread
                        //is set to stop)
                        Log.d(Constants.LOG_TAG, "connectAllLines threw null");

                        continue;
                    }

                    notifyObserversOfChange(serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(Constants.LOG_TAG, "listening thread stopped");
    }

    private List<String> responseToList() throws IOException, NullPointerException {
        List<String> response = new ArrayList<>();
        String line;

        while (!(line = bufferedReader.readLine()).equals(Constants.SERVER_BROADCAST_ENDED_CODE)){
            response.add(line);
        }

        return response;
    }
}
