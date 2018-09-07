package makazas.imint.hr.meteorremote.presentation;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import makazas.imint.hr.meteorremote.ServerResponseChangedObserver;
import makazas.imint.hr.meteorremote.model.ClientCode;
import makazas.imint.hr.meteorremote.model.ServerCode;
import makazas.imint.hr.meteorremote.model.ServerResponse;
import makazas.imint.hr.meteorremote.ServerResponseListenerThread;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListActivity;
import makazas.imint.hr.meteorremote.util.Constants;
import makazas.imint.hr.meteorremote.util.NetworkUtil;

public class SongsListPresenter implements SongsListContract.Presenter, ServerResponseChangedObserver {

    private SongsListContract.View view;

    private Socket clientSocket;

    private BufferedWriter clientSocketWriter;

    private BufferedReader clientSocketReader;

    private ServerResponseListenerThread listenerThread;

    private int clientSongIndex;

    private String clientQueuedSong;

    private LinkedList<String> allQueuedSongs;

    public SongsListPresenter(SongsListContract.View view){
        this.view = view;
        this.allQueuedSongs = new LinkedList<>();
        this.clientSongIndex = -1;
    }

    private void addOrReplaceClientSong(String songName){
        if(clientQueuedSong == null) {
            allQueuedSongs.addLast(songName);
            clientQueuedSong = songName;
            clientSongIndex = allQueuedSongs.indexOf(clientQueuedSong);
        } else {
            clientQueuedSong = songName;
            allQueuedSongs.set(clientSongIndex, songName);
        }
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public void sendSongToServer(String song) {
        addOrReplaceClientSong(song);
        //here we use asynctask because the action is short.
        new AsyncTask<String, String, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    clientSocketWriter.write(ClientCode.CLIENT_QUEUE.toString());
                    clientSocketWriter.newLine();
                    clientSocketWriter.write(NetworkUtil.getMacAddress());
                    clientSocketWriter.newLine();
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
    /**
     * Connects to a server, reads song names and updates {@link RecyclerView RecyclerView}.<br>
     * Data from server is read as {@code Strings} until {@link ServerCode#SERVER_BROADCAST_ENDED} is read. Each read
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
    public void initSongsFromServer(String ipAddress, String portNumber) {
        //here we use thread because it is a long action.
        Thread listenForSongsThread = new Thread(() -> {
            try {
                clientSocket = new Socket(InetAddress.getByName(ipAddress), Integer.parseInt(portNumber));
                clientSocketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientSocketWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                String serverLine;
                List<String> receivedSongs = new ArrayList<>();
                while (!(serverLine = clientSocketReader.readLine()).equals(ServerCode.SERVER_BROADCAST_ENDED.toString())) {
                    receivedSongs.add(serverLine);
                }

                sendMacAddressToServer();
                view.updateListWithSongs(receivedSongs);
                startListeningForServerResponse();
            } catch(IOException e){
                e.printStackTrace();
            }
        });
        listenForSongsThread.setDaemon(true);
        listenForSongsThread.start();
    }

    private void sendMacAddressToServer(){
        try {
            clientSocketWriter.write(ClientCode.CLIENT_MAC_ADDRESS.toString());
            clientSocketWriter.newLine();
            clientSocketWriter.write(NetworkUtil.getMacAddress());
            clientSocketWriter.newLine();
            clientSocketWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListeningForServerResponse() {
        listenerThread = new ServerResponseListenerThread();
        listenerThread.setBufferedReader(clientSocketReader);
        listenerThread.setDaemon(true);
        listenerThread.attach(this);
        listenerThread.start();
    }

    @Override
    public void update(ServerResponse serverResponse) {
        switch(serverResponse.getServerCode()){
            //runs on first start when we receive the initial queue list.
            case SERVER_QUEUE_LIST:
                allQueuedSongs.addAll(serverResponse.getQueuedSongs());
                break;

            //runs when app restarts and the user still had a queued song in the list.
            case SERVER_MY_QUEUED_SONG:
                clientQueuedSong = serverResponse.getQueuedSongName();
                clientSongIndex = serverResponse.getPositionInQueue();

                view.setNowPlayingSong(serverResponse.getNowPlayingSong());
                view.setQueuedSong(clientQueuedSong);
                view.setQueuedSongPosition(clientSongIndex);
                break;

            //runs when anyone queues a song.
            case SERVER_ENQUEUED:
                if(serverResponse.getPositionInQueue() == clientSongIndex){
                    //when client swaps his song
                    view.setQueuedSong(clientQueuedSong);
                    view.setQueuedSongPosition(clientSongIndex);
                } else if(serverResponse.getPositionInQueue() == allQueuedSongs.size()) {
                    //when a new song is added to queue by another client.
                    allQueuedSongs.addLast(serverResponse.getQueuedSongName());
                } else {
                    //when another user swaps his song.
                    allQueuedSongs.set(serverResponse.getPositionInQueue(), serverResponse.getQueuedSongName());
                }
                break;
            case SERVER_MOVE_UP:
                if(clientSongIndex <= -1){
                    //user hasn't queued anything yet.
                    break;
                }

                clientSongIndex--;
                if(clientSongIndex <= -1){
                    view.setNowPlayingSong(clientQueuedSong);
                    view.clearQueuedSongView();

                    allQueuedSongs.removeFirst();
                    clientQueuedSong = null;
                    clientSongIndex = -1;
                } else {
                    view.setQueuedSongPosition(clientSongIndex);
                }
                break;
        }
    }
}
