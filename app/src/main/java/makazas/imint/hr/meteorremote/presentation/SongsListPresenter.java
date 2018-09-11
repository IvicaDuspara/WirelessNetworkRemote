package makazas.imint.hr.meteorremote.presentation;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import makazas.imint.hr.meteorremote.networking.SocketSingleton;
import makazas.imint.hr.meteorremote.multithreading.ServerResponseChangedObserver;
import makazas.imint.hr.meteorremote.model.ClientCode;
import makazas.imint.hr.meteorremote.model.ServerResponse;
import makazas.imint.hr.meteorremote.multithreading.ServerResponseListenerThread;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;
import makazas.imint.hr.meteorremote.util.Constants;
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
    private List<String> allSongs;

    public SongsListPresenter(SongsListContract.View view) {
        this.view = view;
        this.allQueuedSongs = new LinkedList<>();
        this.clientSongIndex = -1;
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public void connectToServer() {
        new AsyncTask<String, String, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    clientSocket = SocketSingleton.getInstance().getSocket();
                    clientSocketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    clientSocketWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    sendMacAddressToServer();
                    startListeningForServerResponses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
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
        setQueuedSongIfNotExists(song);
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

    private void setQueuedSongIfNotExists(String songName) {
        if (clientQueuedSong == null) {
            allQueuedSongs.addLast(songName);
            clientQueuedSong = songName;
            clientSongIndex = allQueuedSongs.indexOf(clientQueuedSong);
        }
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public void disconnectFromServer() {
        new AsyncTask<String, String, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                if (clientSocketWriter != null) {
                    try {
                        clientSocketWriter.write(ClientCode.CLIENT_DISCONNECT.toString());
                        clientSocketWriter.newLine();
                        clientSocketWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(clientSocket != null){
                    try {
                        SocketSingleton.getInstance().closeSocket();
                        clientSocketReader.close();
                        clientSocketWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                listenerThread.setRunning(false);

                NetworkUtil.logIfClosed(clientSocket, clientSocketReader, clientSocketWriter);

                return null;
            }
        }.execute();
    }

    @Override
    public void displaySongsThatMatchQuery(String query) {
        Pattern searchPattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);

        List<String> matchingSongs = new ArrayList<>();

        for(String song: allSongs){
            if(searchPattern.matcher(song).find()){
                matchingSongs.add(song);
            }
        }

        view.updateListWithSongs(matchingSongs);
        // TODO: 10-Sep-18 upon clicking the song in the view, tell the presenter to display all songs again.
    }

    @Override
    public void displayAllSongs() {
        view.updateListWithSongs(allSongs);
    }

    @Override
    public void update(ServerResponse response) {
        switch (response.getServerCode()) {
            case SERVER_SONG_LIST:
                handleSongListResponse(response);
                break;

            case SERVER_QUEUE_LIST:
                handleQueueListResponse(response);
                break;

            case SERVER_NOW_PLAYING:
                handleNowPlayingResponse(response);
                break;

            case SERVER_MY_QUEUED_SONG:
                handleMyQueuedSongResponse(response);
                break;

            case SERVER_ENQUEUED:
                handleEnqueuedResponse(response);
                break;

            case SERVER_MOVE_UP:
                handleMoveUpResponse(response);
                break;
        }
    }

    private void handleSongListResponse(ServerResponse response) {
        allSongs = response.getAllSongs();
        view.updateListWithSongs(allSongs);
    }

    private void handleQueueListResponse(ServerResponse response) {
        allQueuedSongs.addAll(response.getQueuedSongs());
    }

    private void handleNowPlayingResponse(ServerResponse response) {
        view.setNowPlayingSong(response.getNowPlayingSong());
    }

    private void handleMyQueuedSongResponse(ServerResponse response) {
        clientQueuedSong = response.getQueuedSong();
        clientSongIndex = response.getPositionInQueue();

        view.setQueuedSong(clientQueuedSong);
        view.setQueuedSongPosition(clientSongIndex);
    }

    private void handleEnqueuedResponse(ServerResponse response) {
        if (response.getPositionInQueue() == clientSongIndex) {
            //if the currently enqueued song's position equals clients queued song index,
            //this means he swapped his song.

            clientQueuedSong = response.getQueuedSong();
            allQueuedSongs.set(clientSongIndex, response.getQueuedSong());

            view.setQueuedSong(clientQueuedSong);
            view.setQueuedSongPosition(clientSongIndex);
            view.showSuccessfulQueuedSongToast(clientQueuedSong);

        } else if (response.getPositionInQueue() == allQueuedSongs.size()) {
            //if the currently enqueued song's position is greater than the size of all queued songs
            //this means ANOTHER client enqueued a song. therefore we update our list of queued songs.

            allQueuedSongs.addLast(response.getQueuedSong());

        } else {
            //if the currently enqueued song's position isn't greater than the size of all queued songs
            //and its index isn't equal to this client's song position, another user swapped his song.

            allQueuedSongs.set(response.getPositionInQueue(), response.getQueuedSong());
        }
    }

    private void handleMoveUpResponse(ServerResponse response) {
        if (clientSongIndex == -1) {
            //if the client hasn't queued anything yet, his queued song index is -1,
            //denoting the song doesn't exist in the list of queued songs.

            return;
        }

        //if it's not -1, this means he has a queued song.
        //when the queue list moves up, his queued song index reduces by one.
        clientSongIndex--;

        if (clientSongIndex == -1) {
            //if his song index is now -1, this means the song has reached the top of the
            //queue and is now playing.

            view.setNowPlayingSong(clientQueuedSong);
            view.clearQueuedSongView();

            allQueuedSongs.removeFirst();
            clientQueuedSong = null;
            clientSongIndex = -1;

        } else {
            //if his song index isn't -1, we just update the song's position.

            view.setQueuedSongPosition(clientSongIndex);
        }
    }
}

