package makazas.imint.hr.meteorremote.multithreading;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;

import makazas.imint.hr.meteorremote.model.ServerCode;
import makazas.imint.hr.meteorremote.model.ServerResponse;
import makazas.imint.hr.meteorremote.util.Constants;

/**
 * Observable thread that actively listens for a {@link ServerResponse}.<br>
 * If no {@link BufferedReader} is attached, no server responses can be read.<br>
 * Only after attaching the reader via {@link ServerResponseListenerThread#setBufferedReader(BufferedReader)}
 * can the thread actually read server responses.<br>
 * <p>
 * <br>When the thread receives a server response, it first connects all lines from the {@link BufferedReader}
 * and creates a new {@link ServerResponse}. It then notifies all attached {@link ServerResponseChangedObserver}s
 * of the received server response.
 */
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
                    notifyObserversOfChange(new ServerResponse(serverResponse));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(Constants.LOG_TAG, "listening thread stopped");
    }

    /**
     * @return connected lines from {@link ServerResponseListenerThread#bufferedReader} separated by newline
     * up until {@link ServerCode#SERVER_BROADCAST_ENDED} is read. There is no trailing newline.
     * @throws IOException  if BufferedReader throws an IOException
     * @throws NullPointerException if the reader reads a null line, typically meaning the connection between
     * server and client has been severed(lost internet connection and such)
     */
    private String connectAllLinesFromBufferedReader() throws IOException, NullPointerException {
        StringBuilder everything = new StringBuilder();
        String line;

        while (!(line = bufferedReader.readLine()).equals(ServerCode.SERVER_BROADCAST_ENDED.toString())) {
            everything.append(line).append("\n");
        }

        String allLines = everything.toString();

        //removes trailing \n
        return allLines.substring(0, allLines.length() - 1);
    }
}
