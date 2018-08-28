package makazas.imint.hr.meteorremote.presentation;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import makazas.imint.hr.meteorremote.Constants;
import makazas.imint.hr.meteorremote.QueuedSongListeningThread;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListActivity;

public class SongsListPresenter implements SongsListContract.Presenter {

    private SongsListContract.View view;

    private Socket clientSocket;

    private BufferedWriter clientSocketWriter;

    private BufferedReader clientSocketReader;

    private QueuedSongListeningThread listeningThread;

    public SongsListPresenter(SongsListContract.View view){
        this.view = view;
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public void sendSongToServer(String song) {
        new AsyncTask<String, String, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    clientSocketWriter.write(Constants.CLIENT_TOKEN);
                    clientSocketWriter.newLine();
                    clientSocketWriter.flush();
                    clientSocketWriter.write(strings[0]);
                    clientSocketWriter.newLine();
                    clientSocketWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(song);
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public void initSongsFromServer(String ipAddress, String portNumber) {
        /*clientSocket, reader and writer must be initialised in
        async task because a/NetworkOnMainThreadException is thrown otherwise.*/
        new AsyncTask<String, String, Void>() {
            /**
             * Connects to a server, reads song names and updates {@link RecyclerView RecyclerView}.<br>
             * Data from server is read as {@code Strings} until {@link Constants#BROADCAST_END} is read. Each read
             * {@code String} is added to {@link SongsListActivity#songsAdapter} of this {@link SongsListActivity <code>SongsListActivity</code>}.<br><br>
             * <p>
             * This method will throw an exception if any of following occurs:
             * <ul>
             * <li>Invalid IPv4/IPv6 address or invalid port number</li>
             * <li>I/O error occurs while creating a socket or while creating {@link InputStream <code>InputStream</code>} of a socket</li>
             * <li>Socket is closed or not connected</li>
             * </ul>
             *
             * @param strings An array with two elements:
             *                <ol>
             *                <li>IPv4 address</li>
             *                <li>Port number</li>
             *                </ol>
             * @return <code>null</code>
             * Since progress of a task is not tracked, null is returned
             * @throws UnknownHostException if provided IPv4/IPv6 address or port number in <code>strings</code> aren't not valid
             * @throws IOException          if an I/O error occurs while creating a socket or while creating socket's input stream or while reading data.
             */
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    clientSocket = new Socket(InetAddress.getByName(strings[0]), Integer.parseInt(strings[1]));
                    clientSocketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    //can listen for queued song only after socket reader is initialised
                    listenForQueuedSong();

                    clientSocketWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    String str;
                    List<String> receivedSongs = new ArrayList<>();
                    while (!(str = clientSocketReader.readLine()).equals(Constants.BROADCAST_END)) {
                        receivedSongs.add(str);
                    }
                    view.updateListWithSongs(receivedSongs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(ipAddress, portNumber);
    }

    private void listenForQueuedSong() {
        listeningThread = new QueuedSongListeningThread();
        listeningThread.setBufferedReader(clientSocketReader);
        listeningThread.start();
    }
}
