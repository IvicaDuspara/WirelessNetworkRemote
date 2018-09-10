package makazas.imint.hr.meteorremote.makazas.imint.hr.meteorremote.jobs;

import android.util.Log;
import android.widget.TextView;

import makazas.imint.hr.meteorremote.R;


import java.io.BufferedReader;

import java.io.IOException;
import java.util.List;

import makazas.imint.hr.meteorremote.Codes;
import makazas.imint.hr.meteorremote.ListedSongsActivity;

/**
 * @author  Ivica Duspara
 * @version 1.0
 */
public class ListeningJob implements Runnable{

    /**
     * Client's reader
     */
    private BufferedReader clientSocketReader;

    /**
     * Queue of songs
     */
    private List<String> queue;

    private TextView nowPlayingText;

    private TextView myQueuedSongText;


    /**
     * Indicates whether job is running
     */
    private boolean isRunning;


    /**
     * Listed songs activity
     */
    private ListedSongsActivity lsa;

    /**
     * Sets {@code queue}
     *
     * @param queue which is set.
     */
    public ListeningJob(List<String> queue, ListedSongsActivity lsa) {
        this.queue = queue;
        this.lsa = lsa;
        nowPlayingText = lsa.findViewById(R.id.nowPlayingText);
        myQueuedSongText = lsa.findViewById(R.id.nextInQueueText);
        setRunning(true);
    }



    /**
     * Sets {@code clientSocketReader}
     *
     * @param clientSocketReader which is set
     */
    public void setClientSocketReader(BufferedReader clientSocketReader) {
        this.clientSocketReader = clientSocketReader;
    }


    /**
     * Sets {@code isRunning}
     *
     * @param isRunning which is set
     */
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }


    @Override
    public void run() {
        while(isRunning) {
            if(clientSocketReader != null) {
                try {

                    handle(clientSocketReader.readLine());
                }catch(IOException ignorable) {
                    ignorable.printStackTrace();
                }
            }
        }
    }

    private void handle(String code) throws  IOException{
        if(code.equals(Codes.SERVER_MOVE_UP.toString())) {
            String[] splits = myQueuedSongText.getText().toString().split("\\s+",2);
            splits[0] = splits[0].substring(1);
            int position = Integer.valueOf(splits[0]);
            position--;
            String song = splits[1];
            String result;
            if(position == 0) {
                lsa.runOnUiThread(() -> myQueuedSongText.setText(""));
            }
            else {
                result = "#" + position + "  " + song;
                lsa.runOnUiThread(() -> myQueuedSongText.setText(result));
            }

        }
        else if(code.equals(Codes.SERVER_ENQUEUED.toString())) {
            String song = clientSocketReader.readLine();
            String position = clientSocketReader.readLine();
            int index = Integer.valueOf(position);
            index++;
            clientSocketReader.readLine();
            String result = "#" + index + "  " + song;
            lsa.runOnUiThread(() -> myQueuedSongText.setText(result));
        }
        else if(code.equals(Codes.SERVER_NOW_PLAYING.toString())) {
           String song = clientSocketReader.readLine();
           clientSocketReader.readLine();
           lsa.runOnUiThread(() -> nowPlayingText.setText(song));
        }
        else if(code.equals(Codes.SERVER_MY_QUEUED_SONG.toString())) {
            String song = clientSocketReader.readLine();
            String position = clientSocketReader.readLine();
            int index = Integer.valueOf(position);
            index++;
            clientSocketReader.readLine();
            String result = "#" + index + "  " +  song;
            lsa.runOnUiThread(() -> myQueuedSongText.setText(result));
        }
     }
}
