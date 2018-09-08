package makazas.imint.hr.meteorremote.multithreading;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;

import makazas.imint.hr.meteorremote.model.ServerCode;
import makazas.imint.hr.meteorremote.model.ServerResponse;
import makazas.imint.hr.meteorremote.util.Constants;

public class ServerResponseListenerThread extends ObservableServerResponseListenerThread {

    private BufferedReader bufferedReader;

    private boolean isRunning;

    public ServerResponseListenerThread(){
        setRunning(true);
    }

    public void setRunning(boolean running) {
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
