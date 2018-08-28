package makazas.imint.hr.meteorremote;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;

public class QueuedSongListeningThread extends Thread {

    private BufferedReader bufferedReader;

    private boolean isRunning;

    public QueuedSongListeningThread(){
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
                    // TODO: 28-Aug-18 actually display playing song and position in queue(use listener?)
                    Log.d(Constants.LOG_TAG, bufferedReader.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
