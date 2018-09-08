package makazas.imint.hr.meteorremote.multithreading;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;

import makazas.imint.hr.meteorremote.model.ServerCode;
import makazas.imint.hr.meteorremote.model.ServerResponse;
import makazas.imint.hr.meteorremote.util.Constants;

/**
 * Observable thread that actively listens for a {@link ServerResponse}.
 * If no {@link BufferedReader} is attached, no server responses can be read.
 * Only after attaching the reader via {@link ServerResponseListenerThread#setBufferedReader(BufferedReader)}
 * can the thread actually read server responses.
 *
 * When the thread receives a server response, it first connects all lines from the {@link BufferedReader}
 * and creates a new {@link ServerResponse}. It then notifies all attached{@link ServerResponseChangedObserver}s
 * of the received server response.
 */
public class ServerResponseListenerThread extends ObservableThread {

    private BufferedReader bufferedReader;

    private boolean isRunning;

    public ServerResponseListenerThread(){
        setRunning(true);
    }

    private void setRunning(boolean running) {
        isRunning = running;
    }

    public void setBufferedReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    @Override
    public void run(){
        while(isRunning){
            if(bufferedReader != null) {
                try {
                    String serverResponse = connectAllLinesFromBufferedReader();
                    notifyObserversOfChange(
                            new ServerResponse(serverResponse)
                    );
                    Log.d(Constants.LOG_TAG, serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @return      connected lines from {@link ServerResponseListenerThread#bufferedReader} separated by newline
     *              up until {@link ServerCode#SERVER_BROADCAST_ENDED} is read. There is no trailing newline.
     *
     * @throws IOException
     */
    private String connectAllLinesFromBufferedReader() throws IOException {
        StringBuilder everything = new StringBuilder();
        String line;

        while(!(line = bufferedReader.readLine()).equals(ServerCode.SERVER_BROADCAST_ENDED.toString())){
            everything.append(line).append("\n");
        }
        String allLines = everything.toString();

        //removes trailing \n
        return allLines.substring(0, allLines.length() - 1);
    }
}
