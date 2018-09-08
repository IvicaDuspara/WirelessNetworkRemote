package makazas.imint.hr.meteorremote.presentation;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

import makazas.imint.hr.meteorremote.multithreading.ServerResponseChangedObserver;
import makazas.imint.hr.meteorremote.model.ClientCode;
import makazas.imint.hr.meteorremote.model.ServerResponse;
import makazas.imint.hr.meteorremote.multithreading.ServerResponseListenerThread;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;
import makazas.imint.hr.meteorremote.util.NetworkUtil;

// TODO: 08-Sep-18 documentation

public class SongsListPresenter implements SongsListContract.Presenter, ServerResponseChangedObserver {

    private SongsListContract.View view;

    private Socket clientSocket;
    private BufferedWriter clientSocketWriter;
    private BufferedReader clientSocketReader;

    private ServerResponseListenerThread listenerThread;

    private int clientSongIndex;
    private String clientQueuedSong;
    private LinkedList<String> allQueuedSongs;

    public SongsListPresenter(SongsListContract.View view) {
        this.view = view;
        this.allQueuedSongs = new LinkedList<>();
        this.clientSongIndex = -1;
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public void connectToServer(String ipAddress, String portNumber) {
        new AsyncTask<String, String, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    clientSocket = new Socket(InetAddress.getByName(ipAddress), Integer.parseInt(portNumber));
                    clientSocketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    clientSocketWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    sendMacAddressToServer();
                    startListeningForServerResponses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(ipAddress, portNumber);
    }

    private void sendMacAddressToServer() throws IOException {
        clientSocketWriter.write(ClientCode.CLIENT_MAC_ADDRESS.toString());
        clientSocketWriter.newLine();
        clientSocketWriter.write(NetworkUtil.getMacAddress());
        clientSocketWriter.newLine();
        clientSocketWriter.flush();
    }

    private void startListeningForServerResponses() {
        listenerThread = new ServerResponseListenerThread();
        listenerThread.setBufferedReader(clientSocketReader);
        listenerThread.setDaemon(true);
        listenerThread.attach(this);
        listenerThread.start();
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public void sendSongToServer(String song) {
        addOrReplaceClientSong(song);
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

    private void addOrReplaceClientSong(String songName) {
        if (clientQueuedSong == null) {
            allQueuedSongs.addLast(songName);
            clientQueuedSong = songName;
            clientSongIndex = allQueuedSongs.indexOf(clientQueuedSong);
        } else {
            clientQueuedSong = songName;
            allQueuedSongs.set(clientSongIndex, songName);
        }
    }

    @Override
    public void update(ServerResponse response) {
        switch (response.getServerCode()) {
            //when server sends all available songs
            case SERVER_SONG_LIST:
                view.updateListWithSongs(response.getAllSongs());
                break;

            //runs on first start when we receive the initial queue list.
            case SERVER_QUEUE_LIST:
                allQueuedSongs.addAll(response.getQueuedSongs());
                break;

            //runs when app restarts and the user still had a queued song in the list.
            case SERVER_MY_QUEUED_SONG:
                clientQueuedSong = response.getQueuedSong();
                clientSongIndex = response.getPositionInQueue();

                view.setNowPlayingSong(response.getNowPlayingSong());
                view.setQueuedSong(clientQueuedSong);
                view.setQueuedSongPosition(clientSongIndex);
                break;

            //runs when anyone queues a song.
            case SERVER_ENQUEUED:
                if (response.getPositionInQueue() == clientSongIndex) {
                    //when client swaps his song
                    view.setQueuedSong(clientQueuedSong);
                    view.setQueuedSongPosition(clientSongIndex);
                } else if (response.getPositionInQueue() == allQueuedSongs.size()) {
                    //when a new song is added to queue by another client.
                    allQueuedSongs.addLast(response.getQueuedSong());
                } else {
                    //when another client swaps his song.
                    allQueuedSongs.set(response.getPositionInQueue(), response.getQueuedSong());
                }
                break;

            //when the queue moves up
            case SERVER_MOVE_UP:
                if (clientSongIndex <= -1) {
                    //user hasn't queued anything yet.
                    break;
                }

                clientSongIndex--;
                if (clientSongIndex <= -1) {
                    view.setNowPlayingSong(clientQueuedSong);
                    view.clearQueuedSongView();

                    allQueuedSongs.removeFirst();
                    clientQueuedSong = null;
                    clientSongIndex = -1;
                } else {
                    view.setQueuedSongPosition(clientSongIndex);
                }
                break;

            //when a client manually plays a song on the server
            case SERVER_NOW_PLAYING:
                view.setNowPlayingSong(response.getNowPlayingSong());
                break;
        }
    }
}
