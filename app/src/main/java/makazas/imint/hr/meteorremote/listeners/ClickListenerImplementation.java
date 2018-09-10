package makazas.imint.hr.meteorremote.listeners;

import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import makazas.imint.hr.meteorremote.Codes;

/**
 *
 *
 * @author  Ivica Duspara
 * @version 1.0
 */
public class ClickListenerImplementation implements  ClickListener {
    /**
     * Decorated {@link java.net.Socket Socket's} writer.
     */
    private BufferedWriter clientSocketWriter;

    /**
     * Decorated {@link java.net.Socket Socket's} reader.
     */
    private BufferedReader clientSocketReader;

    /**
     * Songs which can be clicked
     */
    private List<String> songs;

    /**
     * MACAddress of a client
     */
    private String MACAddress;

    /**
     * Constructs new {@code {@link ClickListenerImplementation}}.
     *
     * @param songs List of available songs for user to play/queue
     *
     * @param MACAddress of a user
     *

     */
    public ClickListenerImplementation(List<String> songs, String MACAddress) {
        this.songs = songs;
        this.MACAddress = MACAddress;
    }

    /**
     * Sets {@code clientSocketWriter}
     *
     * @param clientSocketWriter which is set
     */
    public void setClientSocketWriter(BufferedWriter clientSocketWriter) {
        this.clientSocketWriter = clientSocketWriter;
    }

    /**
     * Sets {@code ClientSocketReader}
     *
     * @param clientSocketReader which is set
     */
    public void setClientSocketReader(BufferedReader clientSocketReader) {
        this.clientSocketReader = clientSocketReader;
    }


    @Override
    public void onClick(View view, int position) {
        String selectedSong = songs.get(position);
        try {
            clientSocketWriter.write(Codes.SU_MakaZas_PLAY.toString());
            clientSocketWriter.newLine();
            clientSocketWriter.write(selectedSong);
            clientSocketWriter.newLine();
            clientSocketWriter.flush();
        }catch (IOException ignorable) {
            ignorable.printStackTrace();
        }
    }

    @Override
    public void onLongClick(View view, int position) {
        String selectedSong = songs.get(position);
        try{
            clientSocketWriter.write(Codes.CLIENT_QUEUE.toString());
            clientSocketWriter.newLine();
            clientSocketWriter.write(MACAddress);
            clientSocketWriter.newLine();
            clientSocketWriter.write(selectedSong);
            clientSocketWriter.newLine();
            clientSocketWriter.flush();
        }catch(IOException ignorable) {
            ignorable.printStackTrace();
        }
    }
}
